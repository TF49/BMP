import asyncio
import logging
from datetime import date, time
from unittest.mock import AsyncMock

import httpx
import pytest

from app.core.config import AppSettings
from app.core.exceptions import AgentException
from app.core.health import DependencyHealthService
from app.core.security import (
    StaticAgentContextVerifier,
    build_context_verifier,
)
from app.observability.logging import JsonFormatter, configure_logging
from app.tools.base import (
    ToolNotFoundError,
    ToolUnavailableError,
)
from app.tools.circuit_breaker import CircuitBreaker
from app.tools.client import JavaToolClient
from app.tools.court import CourtTool
from app.tools.venue import VenueTool


def test_configure_logging_json_and_text() -> None:
    configure_logging("DEBUG", "json")
    logger = logging.getLogger("bmp_agent")
    assert logger.level == logging.DEBUG

    configure_logging("INFO", "text")
    assert logger.level == logging.INFO


def test_masking_json_formatter() -> None:
    formatter = JsonFormatter()
    record = logging.LogRecord(
        name="test",
        level=logging.INFO,
        pathname="",
        lineno=0,
        msg="User secret token is %s",
        args=("secret123",),
        exc_info=None,
    )
    formatted = formatter.format(record)
    assert "INFO" in formatted


def test_agent_context_verifier_static() -> None:
    settings = AppSettings(
        agent_context_mode="static",
        static_agent_context_token="static-token-123",
        static_agent_user_id="user-1",
        static_agent_user_role="ADMIN",
    )
    verifier = StaticAgentContextVerifier(settings)
    ctx = verifier.verify("static-token-123", "trace-1")
    assert ctx.user_id == "user-1"
    assert ctx.role == "ADMIN"

    with pytest.raises(AgentException) as exc_info:
        verifier.verify("invalid-token", "trace-1")
    assert exc_info.value.status_code == 401


def test_agent_context_verifier_signed_validation() -> None:
    settings = AppSettings(
        agent_context_mode="signed",
        agent_context_secret="secret-key-for-test",
    )
    verifier = build_context_verifier(settings)
    with pytest.raises(AgentException) as exc_info:
        verifier.verify(None, "trace-1")
    assert exc_info.value.status_code == 401

    with pytest.raises(AgentException) as exc_info:
        verifier.verify("invalid_format_token", "trace-1")
    assert exc_info.value.status_code == 401


@pytest.mark.asyncio
async def test_circuit_breaker_state_transitions() -> None:
    breaker = CircuitBreaker(failure_threshold=2, reset_timeout_seconds=0.1)
    assert breaker.allow_request()
    breaker.record_failure()
    assert breaker.allow_request()
    breaker.record_failure()
    assert not breaker.allow_request()

    await asyncio.sleep(0.15)
    assert breaker.allow_request()
    breaker.record_success()
    assert breaker.allow_request()


@pytest.mark.asyncio
async def test_java_tool_client_error_responses() -> None:
    # Test 404 -> ToolNotFoundError
    def handler_404(req: httpx.Request) -> httpx.Response:
        return httpx.Response(404, json={"code": 404, "message": "not found"})

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler_404), base_url="http://test"
    ) as mock_http:
        settings = AppSettings(java_service_token="token")
        client = JavaToolClient(settings, client=mock_http)
        with pytest.raises(ToolNotFoundError):
            await client.get("/api/agent-tools/courts/999", context_token="tok", trace_id="tr")

    # Test 500 -> ToolUnavailableError
    def handler_500(req: httpx.Request) -> httpx.Response:
        return httpx.Response(500, json={"code": 500, "message": "server error"})

    async with httpx.AsyncClient(
        transport=httpx.MockTransport(handler_500), base_url="http://test"
    ) as mock_http:
        settings = AppSettings(java_service_token="token")
        client = JavaToolClient(settings, client=mock_http)
        with pytest.raises(ToolUnavailableError):
            await client.get("/api/agent-tools/courts/1", context_token="tok", trace_id="tr")


@pytest.mark.asyncio
async def test_venue_and_court_tools() -> None:
    mock_client = AsyncMock()
    mock_client.get.return_value = [
        {"venueId": 1, "venueName": "Main Hall", "address": "123 Street", "status": 1}
    ]

    venue_tool = VenueTool(mock_client)
    venues = await venue_tool.list_venues(context_token="token", trace_id="trace")
    assert len(venues) == 1
    assert venues[0].venue_name == "Main Hall"

    court_tool = CourtTool(mock_client)
    mock_client.get.return_value = [
        {
            "courtId": 101,
            "venueId": 1,
            "courtName": "Court 1",
            "pricePerHour": 50.0,
            "available": True,
            "date": "2026-07-22",
            "startTime": "09:00",
            "endTime": "10:00",
        }
    ]
    courts = await court_tool.query_availability(
        venue_id=1,
        date=date(2026, 7, 22),
        start_time=time(9, 0),
        end_time=time(10, 0),
        context_token="token",
        trace_id="trace",
    )
    assert len(courts) == 1
    assert courts[0].court_name == "Court 1"


@pytest.mark.asyncio
async def test_dependency_health_service_disabled_and_errors() -> None:
    settings = AppSettings(
        external_health_checks=True,
        openai_api_key=None,
        database_url=None,
        redis_url=None,
    )
    health = DependencyHealthService(settings)
    statuses = await health.check()
    assert statuses["app"] == "healthy"
    assert statuses["llm"] == "disabled"
    assert statuses["database"] == "disabled"
    assert statuses["redis"] == "disabled"
