import re
from collections.abc import Mapping

from fastapi import FastAPI
from httpx import ASGITransport, AsyncClient

from app.core.config import AppSettings
from app.core.exceptions import AgentException
from app.core.security import VerifiedAgentContext
from app.main import create_app


class MappingContextVerifier:
    def __init__(self, contexts: Mapping[str, VerifiedAgentContext]) -> None:
        self._contexts = contexts

    def verify(self, token: str | None, trace_id: str) -> VerifiedAgentContext:
        context = self._contexts.get(token or "")
        if context is None:
            raise AgentException(status_code=401, message="invalid agent context token")
        return context.model_copy(update={"trace_id": trace_id})


class FakeHealthService:
    async def check(self) -> dict[str, str]:
        return {
            "app": "healthy",
            "llm": "healthy",
            "database": "unhealthy",
            "redis": "healthy",
        }


def verified_context(user_id: str) -> VerifiedAgentContext:
    return VerifiedAgentContext(
        user_id=user_id,
        role="MEMBER",
        venue_id=None,
        trace_id="replaced-by-request",
    )


def process_payload(
    *, conversation_id: str = "conv-1", agent_type: str = "booking", content: str = "hello"
) -> dict[str, object]:
    return {
        "conversation_id": conversation_id,
        "agent_type": agent_type,
        "message": {"content": content, "message_id": "msg-1"},
    }


async def test_health_uses_envelope_and_reuses_valid_trace_id(
    test_settings: AppSettings,
) -> None:
    app = create_app(test_settings)

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/health", headers={"X-Agent-Trace-Id": "trace_valid-123"})

    assert response.status_code == 200
    assert response.headers["X-Agent-Trace-Id"] == "trace_valid-123"
    assert response.json() == {
        "code": 200,
        "message": "success",
        "data": {
            "status": "healthy",
            "timestamp": response.json()["data"]["timestamp"],
            "services": {
                "app": "healthy",
                "llm": "disabled",
                "database": "disabled",
                "redis": "disabled",
            },
        },
        "trace_id": "trace_valid-123",
    }


async def test_health_replaces_invalid_trace_id(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/health", headers={"X-Agent-Trace-Id": "invalid trace!"})

    generated = response.json()["trace_id"]
    assert response.status_code == 200
    assert generated != "invalid trace!"
    assert re.fullmatch(r"[0-9a-f]{32}", generated)
    assert response.headers["X-Agent-Trace-Id"] == generated


async def test_health_reports_dependency_results_and_degraded_status(
    test_settings: AppSettings,
) -> None:
    app = create_app(test_settings)
    app.state.health_service = FakeHealthService()

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/health")

    assert response.status_code == 200
    assert response.json()["data"]["status"] == "degraded"
    assert response.json()["data"]["services"] == {
        "app": "healthy",
        "llm": "healthy",
        "database": "unhealthy",
        "redis": "healthy",
    }


async def test_http_errors_use_standard_envelope(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        missing = await client.get("/missing")
        wrong_method = await client.get("/api/v1/agent/process")

    assert missing.status_code == 404
    assert missing.json()["code"] == 404
    assert missing.json()["message"] == "resource not found"
    assert missing.json()["data"] is None
    assert missing.json()["trace_id"]
    assert wrong_method.status_code == 405
    assert wrong_method.json()["code"] == 405
    assert wrong_method.json()["message"] == "method not allowed"
    assert wrong_method.headers["allow"] == "POST"


async def test_domain_exception_uses_sanitized_envelope(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    @app.get("/domain-error")
    async def domain_error() -> None:
        raise AgentException(status_code=503, message="dependency unavailable")

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/domain-error")

    assert response.status_code == 503
    assert response.json()["code"] == 503
    assert response.json()["message"] == "dependency unavailable"
    assert response.json()["data"] is None
    assert response.json()["trace_id"]


async def test_validation_error_is_mapped_to_400(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    @app.get("/validated")
    async def validated(limit: int) -> dict[str, int]:
        return {"limit": limit}

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/validated", params={"limit": "not-a-number"})

    assert response.status_code == 400
    assert response.json()["code"] == 400
    assert response.json()["message"] == "request validation failed"
    assert response.json()["data"]["errors"][0]["field"] == "query.limit"


async def test_unexpected_exception_does_not_expose_details(test_settings: AppSettings) -> None:
    app: FastAPI = create_app(test_settings)

    @app.get("/unexpected-error")
    async def unexpected_error() -> None:
        raise RuntimeError("secret-token-value")

    async with AsyncClient(
        transport=ASGITransport(app=app, raise_app_exceptions=False),
        base_url="http://test",
    ) as client:
        response = await client.get("/unexpected-error")

    body = response.json()
    assert response.status_code == 500
    assert body["code"] == 500
    assert body["message"] == "internal server error"
    assert "secret-token-value" not in response.text


async def test_process_runs_two_turn_mock_agent_flow(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        first = await client.post(
            "/api/v1/agent/process",
            headers={
                "X-Agent-Context-Token": "test-context-token",
                "X-Agent-Trace-Id": "trace-process",
            },
            json=process_payload(content="first"),
        )
        second = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "test-context-token"},
            json=process_payload(content="second"),
        )

    assert first.status_code == 200
    assert first.json()["trace_id"] == "trace-process"
    assert first.json()["data"]["metadata"]["turn"] == 1
    assert second.json()["data"]["metadata"]["turn"] == 2
    assert second.json()["data"]["actions"] == []
    assert second.json()["data"]["references"] == []


async def test_process_rejects_missing_and_invalid_token(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        missing = await client.post("/api/v1/agent/process", json=process_payload())
        invalid = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "wrong-token"},
            json=process_payload(),
        )
        non_ascii = await client.post(
            "/api/v1/agent/process",
            headers=[(b"X-Agent-Context-Token", b"\xe9")],
            json=process_payload(),
        )

    assert missing.status_code == 401
    assert invalid.status_code == 401
    assert non_ascii.status_code == 401
    assert invalid.json()["message"] == "invalid agent context token"
    assert non_ascii.json()["message"] == "invalid agent context token"


async def test_process_rejects_body_identity_and_invalid_fields(
    test_settings: AppSettings,
) -> None:
    app = create_app(test_settings)
    payload_with_identity = process_payload()
    payload_with_identity["user_context"] = {"user_id": "attacker"}

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        identity = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "test-context-token"},
            json=payload_with_identity,
        )
        unknown_agent = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "test-context-token"},
            json=process_payload(agent_type="unknown"),
        )
        blank = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "test-context-token"},
            json=process_payload(content="   "),
        )
        too_long = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "test-context-token"},
            json=process_payload(content="x" * 2001),
        )

    assert {
        identity.status_code,
        unknown_agent.status_code,
        blank.status_code,
        too_long.status_code,
    } == {400}


async def test_process_rejects_cross_user_and_agent_type(test_settings: AppSettings) -> None:
    app = create_app(test_settings)
    app.state.context_verifier = MappingContextVerifier(
        {"token-a": verified_context("user-a"), "token-b": verified_context("user-b")}
    )

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        created = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "token-a"},
            json=process_payload(),
        )
        cross_user = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "token-b"},
            json=process_payload(),
        )
        changed_agent = await client.post(
            "/api/v1/agent/process",
            headers={"X-Agent-Context-Token": "token-a"},
            json=process_payload(agent_type="analytics"),
        )

    assert created.status_code == 200
    assert cross_user.status_code == 403
    assert changed_agent.status_code == 403
    assert cross_user.json()["message"] == "session does not belong to current context"


async def test_process_rejects_expired_session(test_settings: AppSettings) -> None:
    """Expired sessions must return 401 through the route layer, not just in unit tests."""
    from datetime import UTC, datetime, timedelta

    from app.memory.session import InMemorySessionStore

    now = datetime(2026, 7, 18, tzinfo=UTC)
    current = now

    app = create_app(test_settings)
    # Inject a session store with a controllable clock
    app.state.session_store = InMemorySessionStore(
        ttl_seconds=60,
        clock=lambda: current,
        on_expire=lambda s: app.state.mock_agent.delete_thread(s.thread_id),
    )

    headers = {"X-Agent-Context-Token": "test-context-token"}

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        # First request creates the session while it is still valid
        first = await client.post(
            "/api/v1/agent/process",
            headers=headers,
            json=process_payload(conversation_id="conv-expire-test"),
        )
        assert first.status_code == 200

        # Advance the clock past the TTL
        current = now + timedelta(seconds=61)

        # Second request must be rejected with 401
        expired = await client.post(
            "/api/v1/agent/process",
            headers=headers,
            json=process_payload(conversation_id="conv-expire-test"),
        )

    assert expired.status_code == 401
    assert expired.json()["message"] == "session has expired"
    assert expired.json()["trace_id"]

