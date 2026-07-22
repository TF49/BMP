"""BookingTool 端到端测试：用 httpx.MockTransport 模拟 Java 报价/创建响应，
验证 POST body、camelCase 对齐、Header 与幂等键透传，以及写操作不重试。"""

from decimal import Decimal

import httpx
import pytest

from app.core.config import AppSettings
from app.tools.base import ToolUnavailableError
from app.tools.booking import BookingCreateResult, BookingQuote, BookingTool
from app.tools.client import JavaToolClient

_QUOTE_ENVELOPE = {
    "code": 200,
    "message": "success",
    "data": {
        "venueId": 1,
        "courtIds": [10],
        "date": "2025-01-20",
        "startTime": "19:00",
        "endTime": "21:00",
        "bookingMode": "WHOLE",
        "pricingMode": "STANDARD",
        "duration": 2,
        "totalAmount": "120.00",
        "lineItems": [{"courtId": 10, "unitPrice": "60.00", "lineAmount": "120.00"}],
        "available": True,
    },
}

_CREATE_ENVELOPE = {
    "code": 200,
    "message": "success",
    "data": {
        "bookingId": 500,
        "bookingNo": "BK20250120001",
        "status": 1,
        "statusText": "待支付",
        "paymentStatus": 0,
        "venueId": 1,
        "courtIds": [10],
        "date": "2025-01-20",
        "startTime": "19:00",
        "endTime": "21:00",
        "duration": 2,
        "totalAmount": "120.00",
    },
}


def _settings() -> AppSettings:
    return AppSettings(_env_file=None, java_service_token="svc-token")


async def test_booking_tool_quote_sends_payload_and_parses_amount() -> None:
    captured: dict[str, object] = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["path"] = request.url.path
        captured["method"] = request.method
        captured["service_token"] = request.headers.get("X-Agent-Service-Token")
        captured["context_token"] = request.headers.get("X-Agent-Context-Token")
        captured["idempotency"] = request.headers.get("Idempotency-Key")
        captured["body"] = request.content
        return httpx.Response(200, json=_QUOTE_ENVELOPE)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as http:
        tool = BookingTool(JavaToolClient(_settings(), client=http))
        quote = await tool.quote(
            venue_id=1,
            court_ids=[10],
            date="2025-01-20",
            start_time="19:00",
            end_time="21:00",
            booking_mode="WHOLE",
            pricing_mode="STANDARD",
            context_token="signed.ctx",
            trace_id="trace-1",
        )

    assert isinstance(quote, BookingQuote)
    assert quote.total_amount == Decimal("120.00")
    assert quote.duration == 2
    assert quote.line_items[0].unit_price == Decimal("60.00")
    assert captured["path"] == "/api/agent-tools/bookings/quote"
    assert captured["method"] == "POST"
    assert captured["service_token"] == "svc-token"
    assert captured["context_token"] == "signed.ctx"
    # 报价不携带幂等键。
    assert captured["idempotency"] is None
    body = httpx.Request("POST", "http://test", content=captured["body"]).content.decode()
    assert '"venueId":1' in body.replace(" ", "")
    assert '"courtIds":[10]' in body.replace(" ", "")


async def test_booking_tool_create_passes_idempotency_key() -> None:
    captured: dict[str, object] = {}

    def handler(request: httpx.Request) -> httpx.Response:
        captured["path"] = request.url.path
        captured["idempotency"] = request.headers.get("Idempotency-Key")
        return httpx.Response(200, json=_CREATE_ENVELOPE)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as http:
        tool = BookingTool(JavaToolClient(_settings(), client=http))
        result = await tool.create_pending(
            venue_id=1,
            court_ids=[10],
            date="2025-01-20",
            start_time="19:00",
            end_time="21:00",
            idempotency_key="idem-key-1",
            context_token="signed.ctx",
            trace_id="trace-1",
            remark="agent 代订",
        )

    assert isinstance(result, BookingCreateResult)
    assert result.booking_id == 500
    assert result.status == 1
    assert result.payment_status == 0
    assert result.total_amount == Decimal("120.00")
    assert captured["path"] == "/api/agent-tools/bookings"
    assert captured["idempotency"] == "idem-key-1"


async def test_booking_tool_create_does_not_retry_on_transient_error() -> None:
    attempts = {"count": 0}

    def handler(request: httpx.Request) -> httpx.Response:
        attempts["count"] += 1
        raise httpx.ConnectError("boom", request=request)

    transport = httpx.MockTransport(handler)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as http:
        tool = BookingTool(JavaToolClient(_settings(), client=http))
        with pytest.raises(ToolUnavailableError):
            await tool.create_pending(
                venue_id=1,
                court_ids=[10],
                date="2025-01-20",
                start_time="19:00",
                end_time="21:00",
                idempotency_key="idem-key-1",
                context_token="signed.ctx",
                trace_id="trace-1",
            )

    # 写操作不重试：仅尝试一次。
    assert attempts["count"] == 1
