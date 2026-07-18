from datetime import UTC, datetime
from typing import Annotated

from fastapi import APIRouter, Header, Request

from app.agents.base import AgentResult
from app.agents.mock_agent import MockAgent
from app.api.schemas import ApiResponse, HealthData, ProcessRequest, ServiceStatus
from app.core.security import AgentContextVerifier
from app.memory.session import InMemorySessionStore
from app.observability.tracing import get_trace_id

router = APIRouter()


@router.get("/health", response_model=ApiResponse[HealthData])
async def health(_: Request) -> ApiResponse[HealthData]:
    services: dict[str, ServiceStatus] = {
        "app": "healthy",
        "llm": "disabled",
        "database": "disabled",
        "redis": "disabled",
    }
    data = HealthData(status="healthy", timestamp=datetime.now(UTC), services=services)
    return ApiResponse(code=200, message="success", data=data, trace_id=get_trace_id())


@router.post("/api/v1/agent/process", response_model=ApiResponse[AgentResult])
async def process_message(
    payload: ProcessRequest,
    request: Request,
    context_token: Annotated[str | None, Header(alias="X-Agent-Context-Token")] = None,
) -> ApiResponse[AgentResult]:
    verifier: AgentContextVerifier = request.app.state.context_verifier
    store: InMemorySessionStore = request.app.state.session_store
    agent: MockAgent = request.app.state.mock_agent
    trace_id = get_trace_id()
    context = verifier.verify(context_token, trace_id)
    session = await store.get_or_create(payload.conversation_id, context, payload.agent_type)
    async with store.processing_lock(session):
        result = await agent.process(session, payload.message.content)
    return ApiResponse(code=200, message="success", data=result, trace_id=trace_id)
