"""场馆客服 Tool：封装 Java `/api/agent-tools/support/...` 接口。"""

import uuid
from typing import Any
from pydantic import BaseModel, ConfigDict, Field

from app.tools.client import JavaToolClient


class BusinessHoursInfo(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    open: str = "09:00"
    close: str = "22:00"


class SupportVenueStatus(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    venue_id: str = Field(alias="venue_id")
    venue_name: str = Field(alias="venue_name")
    status: str = "open"
    business_hours: BusinessHoursInfo = Field(default_factory=BusinessHoursInfo, alias="business_hours")
    current_time: str = Field(alias="current_time")
    available_courts: int = Field(alias="available_courts")
    total_courts: int = Field(alias="total_courts")


class SupportPriceItem(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    court_type: str = Field(alias="court_type")
    time_slot: str = Field(alias="time_slot")
    price: float = Field(alias="price")
    unit: str = Field(alias="unit", default="小时")


class SupportPriceList(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    venue_id: str = Field(alias="venue_id")
    price_list: list[SupportPriceItem] = Field(default_factory=list, alias="price_list")
    updated_at: str = Field(alias="updated_at")


class SupportHandoffResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    handoff_id: str = Field(alias="handoff_id")
    status: str = Field(alias="status", default="pending")
    estimated_wait_time: int = Field(alias="estimated_wait_time", default=5)
    created_at: str = Field(alias="created_at")


class SupportTool:
    """场馆客服专用的 Java 只读与人工转接 Tool 客户端。"""

    def __init__(self, client: JavaToolClient) -> None:
        self._client = client

    async def get_venue_realtime_status(
        self,
        *,
        context_token: str,
        trace_id: str,
        venue_id: int | str = 1,
    ) -> SupportVenueStatus:
        path = f"/api/agent-tools/support/venues/{venue_id}"
        data = await self._client.get(
            path,
            context_token=context_token,
            trace_id=trace_id,
        )
        return SupportVenueStatus.model_validate(data)

    async def get_service_prices(
        self,
        *,
        context_token: str,
        trace_id: str,
        venue_id: int | str = 1,
    ) -> SupportPriceList:
        path = f"/api/agent-tools/support/venues/{venue_id}/prices"
        data = await self._client.get(
            path,
            context_token=context_token,
            trace_id=trace_id,
        )
        return SupportPriceList.model_validate(data)

    async def create_human_handoff(
        self,
        *,
        context_token: str,
        trace_id: str,
        venue_id: int | str = 1,
        topic: str,
        description: str,
        conversation_id: str,
        idempotency_key: str | None = None,
    ) -> SupportHandoffResult:
        path = "/api/agent-tools/support/handoffs"
        key = idempotency_key or f"handoff-{uuid.uuid4()}"
        body = {
            "venue_id": str(venue_id),
            "topic": topic,
            "description": description,
            "conversation_id": conversation_id,
        }
        data = await self._client.post(
            path,
            context_token=context_token,
            trace_id=trace_id,
            idempotency_key=key,
            json_body=body,
        )
        return SupportHandoffResult.model_validate(data)
