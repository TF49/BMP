"""场馆只读 Tool：封装 ``GET /api/agent-tools/venues``。"""

from typing import Any

from pydantic import BaseModel, ConfigDict, Field

from app.tools.client import JavaToolClient

_VENUES_PATH = "/api/agent-tools/venues"


class VenueSummary(BaseModel):
    """场馆摘要，字段与 Java ``AgentVenueDTO`` 对齐（camelCase 别名）。"""

    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)

    venue_id: int = Field(alias="venueId")
    venue_name: str = Field(alias="venueName")
    address: str | None = None
    business_hours: str | None = Field(default=None, alias="businessHours")
    status: int
    status_text: str | None = Field(default=None, alias="statusText")


class VenueTool:
    """面向预订/浏览 Agent 的场馆查询工具。"""

    def __init__(self, client: JavaToolClient) -> None:
        self._client = client

    async def list_venues(
        self,
        *,
        context_token: str,
        trace_id: str,
        status: int | None = None,
        limit: int = 20,
    ) -> list[VenueSummary]:
        data = await self._client.get(
            _VENUES_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={"status": status, "limit": limit},
        )
        return _parse_venues(data)


def _parse_venues(data: Any) -> list[VenueSummary]:
    if data is None:
        return []
    if not isinstance(data, list):
        return []
    return [VenueSummary.model_validate(item) for item in data]
