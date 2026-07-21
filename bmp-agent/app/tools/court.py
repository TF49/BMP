"""场地可用性只读 Tool：封装 ``GET /api/agent-tools/courts/availability``。"""

from datetime import date as date_type
from datetime import time as time_type
from decimal import Decimal
from typing import Any

from pydantic import BaseModel, ConfigDict, Field

from app.tools.client import JavaToolClient

_AVAILABILITY_PATH = "/api/agent-tools/courts/availability"


class CourtAvailability(BaseModel):
    """场地可用性，字段与 Java ``AgentCourtAvailabilityDTO`` 对齐（camelCase 别名）。"""

    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)

    court_id: int = Field(alias="courtId")
    court_code: str | None = Field(default=None, alias="courtCode")
    court_name: str | None = Field(default=None, alias="courtName")
    venue_id: int = Field(alias="venueId")
    billing_type: str | None = Field(default=None, alias="billingType")
    price_per_hour: Decimal | None = Field(default=None, alias="pricePerHour")
    available: bool
    date: str
    start_time: str = Field(alias="startTime")
    end_time: str = Field(alias="endTime")


class CourtTool:
    """面向预订 Agent 的场地可用性查询工具。"""

    def __init__(self, client: JavaToolClient) -> None:
        self._client = client

    async def query_availability(
        self,
        *,
        venue_id: int,
        date: date_type,
        start_time: time_type,
        end_time: time_type,
        context_token: str,
        trace_id: str,
        limit: int = 20,
    ) -> list[CourtAvailability]:
        data = await self._client.get(
            _AVAILABILITY_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={
                "venueId": venue_id,
                "date": date.isoformat(),
                "startTime": start_time.strftime("%H:%M"),
                "endTime": end_time.strftime("%H:%M"),
                "limit": limit,
            },
        )
        return _parse_courts(data)


def _parse_courts(data: Any) -> list[CourtAvailability]:
    if not isinstance(data, list):
        return []
    return [CourtAvailability.model_validate(item) for item in data]
