"""预约报价与创建待支付 Tool：封装 ``POST /api/agent-tools/bookings/quote`` 与 ``POST /api/agent-tools/bookings``。

金额一律由 Java 计算，本模块仅透传报价与创建结果；创建通过 ``Idempotency-Key`` 保证幂等。
"""

from decimal import Decimal
from typing import Any

from pydantic import BaseModel, ConfigDict, Field

from app.tools.client import JavaToolClient

_QUOTE_PATH = "/api/agent-tools/bookings/quote"
_CREATE_PATH = "/api/agent-tools/bookings"


class BookingLineItem(BaseModel):
    """逐场地报价明细，字段与 Java ``AgentBookingLineItemDTO`` 对齐（camelCase 别名）。"""

    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)

    court_id: int = Field(alias="courtId")
    unit_price: Decimal | None = Field(default=None, alias="unitPrice")
    line_amount: Decimal | None = Field(default=None, alias="lineAmount")


class BookingQuote(BaseModel):
    """预约报价，字段与 Java ``AgentBookingQuoteResultDTO`` 对齐（camelCase 别名）。"""

    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)

    venue_id: int = Field(alias="venueId")
    court_ids: list[int] = Field(default_factory=list, alias="courtIds")
    date: str
    start_time: str = Field(alias="startTime")
    end_time: str = Field(alias="endTime")
    booking_mode: str | None = Field(default=None, alias="bookingMode")
    pricing_mode: str | None = Field(default=None, alias="pricingMode")
    duration: int | None = None
    total_amount: Decimal = Field(alias="totalAmount")
    line_items: list[BookingLineItem] = Field(default_factory=list, alias="lineItems")
    available: bool = True


class BookingCreateResult(BaseModel):
    """创建待支付预约结果，字段与 Java ``AgentBookingResultDTO`` 对齐（camelCase 别名）。"""

    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)

    booking_id: int = Field(alias="bookingId")
    booking_no: str | None = Field(default=None, alias="bookingNo")
    status: int
    status_text: str | None = Field(default=None, alias="statusText")
    payment_status: int = Field(alias="paymentStatus")
    venue_id: int = Field(alias="venueId")
    court_ids: list[int] = Field(default_factory=list, alias="courtIds")
    date: str
    start_time: str = Field(alias="startTime")
    end_time: str = Field(alias="endTime")
    booking_mode: str | None = Field(default=None, alias="bookingMode")
    pricing_mode: str | None = Field(default=None, alias="pricingMode")
    duration: int | None = None
    total_amount: Decimal = Field(alias="totalAmount")


class BookingTool:
    """面向预订 Agent 的报价 / 创建待支付工具。"""

    def __init__(self, client: JavaToolClient) -> None:
        self._client = client

    async def quote(
        self,
        *,
        venue_id: int,
        court_ids: list[int],
        date: str,
        start_time: str,
        end_time: str,
        context_token: str,
        trace_id: str,
        booking_mode: str | None = None,
        pricing_mode: str | None = None,
        member_id: int | None = None,
    ) -> BookingQuote:
        payload = _build_payload(
            venue_id=venue_id,
            court_ids=court_ids,
            date=date,
            start_time=start_time,
            end_time=end_time,
            booking_mode=booking_mode,
            pricing_mode=pricing_mode,
            member_id=member_id,
        )
        data = await self._client.post(
            _QUOTE_PATH,
            json=payload,
            context_token=context_token,
            trace_id=trace_id,
        )
        return BookingQuote.model_validate(data)

    async def create_pending(
        self,
        *,
        venue_id: int,
        court_ids: list[int],
        date: str,
        start_time: str,
        end_time: str,
        idempotency_key: str,
        context_token: str,
        trace_id: str,
        booking_mode: str | None = None,
        pricing_mode: str | None = None,
        member_id: int | None = None,
        remark: str | None = None,
    ) -> BookingCreateResult:
        payload = _build_payload(
            venue_id=venue_id,
            court_ids=court_ids,
            date=date,
            start_time=start_time,
            end_time=end_time,
            booking_mode=booking_mode,
            pricing_mode=pricing_mode,
            member_id=member_id,
        )
        if remark is not None:
            payload["remark"] = remark
        data = await self._client.post(
            _CREATE_PATH,
            json=payload,
            context_token=context_token,
            trace_id=trace_id,
            idempotency_key=idempotency_key,
        )
        return BookingCreateResult.model_validate(data)


def _build_payload(
    *,
    venue_id: int,
    court_ids: list[int],
    date: str,
    start_time: str,
    end_time: str,
    booking_mode: str | None,
    pricing_mode: str | None,
    member_id: int | None,
) -> dict[str, Any]:
    payload: dict[str, Any] = {
        "venueId": venue_id,
        "courtIds": list(court_ids),
        "date": date,
        "startTime": start_time,
        "endTime": end_time,
    }
    if booking_mode is not None:
        payload["bookingMode"] = booking_mode
    if pricing_mode is not None:
        payload["pricingMode"] = pricing_mode
    if member_id is not None:
        payload["memberId"] = member_id
    return payload
