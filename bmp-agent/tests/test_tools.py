import httpx
import pytest

from app.core.config import AppSettings
from app.tools.base import ToolAuthError, ToolUnavailableError
from app.tools.circuit_breaker import CircuitBreaker
from app.tools.client import JavaToolClient


@pytest.mark.asyncio
async def test_java_tool_client_success() -> None:
    def handler(request: httpx.Request) -> httpx.Response:
        return httpx.Response(
            200,
            json={"code": 200, "message": "success", "data": [{"id": 1, "name": "Main Venue"}]},
        )

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as mock_http:
        settings = AppSettings(java_service_token="test-token")
        client = JavaToolClient(settings, client=mock_http)
        result = await client.get(
            "/api/agent-tools/venues",
            context_token="signed.token",
            trace_id="trace_1",
        )
        assert result == [{"id": 1, "name": "Main Venue"}]


@pytest.mark.asyncio
async def test_java_tool_client_error_mapping() -> None:
    def handler_401(request: httpx.Request) -> httpx.Response:
        return httpx.Response(401, json={"code": 401, "message": "unauthorized"})

    transport_401 = httpx.MockTransport(handler_401)
    async with httpx.AsyncClient(transport=transport_401, base_url="http://test") as mock_http:
        settings = AppSettings(java_service_token="test-token")
        client = JavaToolClient(settings, client=mock_http)
        with pytest.raises(ToolAuthError):
            await client.get("/api/agent-tools/venues", context_token="invalid", trace_id="trace_1")


@pytest.mark.asyncio
async def test_circuit_breaker_open() -> None:
    breaker = CircuitBreaker(failure_threshold=1)
    breaker.record_failure()
    assert not breaker.allow_request()

    settings = AppSettings(java_service_token="test-token")
    client = JavaToolClient(settings, breaker=breaker)
    with pytest.raises(ToolUnavailableError):
        await client.get(
            "/api/agent-tools/venues", context_token="signed.token", trace_id="trace_1"
        )
