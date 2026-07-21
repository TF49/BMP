from datetime import UTC, datetime
from typing import Annotated, Literal

from fastapi import APIRouter, Header, Request

from app.agents.base import Agent, AgentResult
from app.api.schemas import ApiResponse, HealthData, ProcessRequest
from app.core.exceptions import AgentException
from app.core.health import HealthService
from app.core.ratelimit import InMemoryRateLimiter
from app.core.replay import ReplayGuard
from app.core.security import AgentContextVerifier
from app.memory.session import SessionStore
from app.observability.tracing import get_trace_id

router = APIRouter()


@router.get("/health", response_model=ApiResponse[HealthData])
async def health(request: Request) -> ApiResponse[HealthData]:
    health_service: HealthService = request.app.state.health_service
    services = await health_service.check()
    status: Literal["healthy", "degraded"] = (
        "degraded" if "unhealthy" in services.values() else "healthy"
    )
    data = HealthData(status=status, timestamp=datetime.now(UTC), services=services)
    return ApiResponse(code=200, message="success", data=data, trace_id=get_trace_id())


@router.post("/api/v1/agent/process", response_model=ApiResponse[AgentResult])
async def process_message(
    payload: ProcessRequest,
    request: Request,
    context_token: Annotated[str | None, Header(alias="X-Agent-Context-Token")] = None,
) -> ApiResponse[AgentResult]:
    verifier: AgentContextVerifier = request.app.state.context_verifier
    store: SessionStore = request.app.state.session_store
    agent: Agent = request.app.state.mock_agent
    limiter: InMemoryRateLimiter = request.app.state.rate_limiter
    replay_guard: ReplayGuard = request.app.state.replay_guard
    trace_id = get_trace_id()
    context = verifier.verify(context_token, trace_id)
    claimed = await replay_guard.claim(
        nonce=context.nonce,
        expires_at_ms=context.expires_at,
        scope="process",
    )
    if not claimed:
        raise AgentException(status_code=401, message="agent context token has already been used")

    client_ip = request.client.host if request.client is not None else "unknown"
    rate_limit_keys = (
        f"ip:{client_ip}",
        f"user:{context.user_id}",
        f"route:{request.method}:{request.url.path}",
        "operation:agent-process",
    )
    for key in rate_limit_keys:
        decision = limiter.try_acquire(key)
        if not decision.allowed:
            raise AgentException(
                status_code=429,
                message="rate limit exceeded",
                data={"retry_after_seconds": decision.retry_after_seconds, "retryable": True},
            )
    session = await store.get_or_create(payload.conversation_id, context, payload.agent_type)
    async with store.processing_lock(session):
        result = await agent.process(session, payload.message.content, context_token=context_token)
    return ApiResponse(code=200, message="success", data=result, trace_id=trace_id)
