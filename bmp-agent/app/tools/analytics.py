"""经营分析 Tool：封装 Java `/api/agent-tools/analytics/*` Endpoint。"""

from typing import Any

from pydantic import BaseModel, ConfigDict, Field

from app.tools.client import JavaToolClient

_OVERVIEW_PATH = "/api/agent-tools/analytics/overview"
_BOOKING_TREND_PATH = "/api/agent-tools/analytics/booking-trend"
_COURT_HEATMAP_PATH = "/api/agent-tools/analytics/court-heatmap"
_FINANCE_TREND_PATH = "/api/agent-tools/analytics/finance-trend"
_REVENUE_BREAKDOWN_PATH = "/api/agent-tools/analytics/revenue-breakdown"
_VENUE_COMPARISON_PATH = "/api/agent-tools/analytics/venue-comparison"


class AnalyticsPeriod(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    start_date: str | None = Field(default=None, alias="startDate")
    end_date: str | None = Field(default=None, alias="endDate")
    granularity: str | None = None
    description: str | None = None


class AnalyticsScope(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    level: str | None = None
    venue_id: int | None = Field(default=None, alias="venueId")
    venue_name: str | None = Field(default=None, alias="venueName")
    description: str | None = None


class AnalyticsMetric(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    key: str
    name: str
    unit: str
    definition: str


class AnalyticsMeta(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    period: AnalyticsPeriod | None = None
    scope: AnalyticsScope | None = None
    metrics: list[AnalyticsMetric] = Field(default_factory=list)


class AnalyticsChartSeries(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    name: str
    unit: str | None = None
    metric_key: str | None = Field(default=None, alias="metricKey")
    data: list[Any] = Field(default_factory=list)


class AnalyticsChart(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    chart_type: str = Field(alias="chartType")
    title: str | None = None
    categories: list[str] = Field(default_factory=list)
    y_categories: list[str] = Field(default_factory=list, alias="yCategories")
    series: list[AnalyticsChartSeries] = Field(default_factory=list)
    empty: bool = False
    empty_text: str | None = Field(default=None, alias="emptyText")


class AnalyticsKpi(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    key: str
    name: str
    value: Any
    unit: str | None = None


class AnalyticsResult(BaseModel):
    """通用经营分析结果信封。"""

    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    meta: AnalyticsMeta | None = None
    kpis: list[AnalyticsKpi] = Field(default_factory=list)
    charts: list[AnalyticsChart] = Field(default_factory=list)
    raw_data: Any = Field(default=None)


class AnalyticsTool:
    """经营分析工具封装。"""

    def __init__(self, client: JavaToolClient) -> None:
        self._client = client

    async def get_overview(
        self,
        *,
        context_token: str,
        trace_id: str,
        start_date: str | None = None,
        end_date: str | None = None,
        venue_id: int | None = None,
    ) -> AnalyticsResult:
        data = await self._client.get(
            _OVERVIEW_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={"startDate": start_date, "endDate": end_date, "venueId": venue_id},
        )
        return AnalyticsResult.model_validate(data or {})

    async def get_booking_trend(
        self,
        *,
        context_token: str,
        trace_id: str,
        start_date: str | None = None,
        end_date: str | None = None,
        granularity: str | None = None,
        venue_id: int | None = None,
    ) -> AnalyticsResult:
        data = await self._client.get(
            _BOOKING_TREND_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={
                "startDate": start_date,
                "endDate": end_date,
                "granularity": granularity,
                "venueId": venue_id,
            },
        )
        return AnalyticsResult.model_validate(data or {})

    async def get_court_heatmap(
        self,
        *,
        context_token: str,
        trace_id: str,
        start_date: str | None = None,
        end_date: str | None = None,
        venue_id: int | None = None,
    ) -> AnalyticsResult:
        data = await self._client.get(
            _COURT_HEATMAP_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={"startDate": start_date, "endDate": end_date, "venueId": venue_id},
        )
        return AnalyticsResult.model_validate(data or {})

    async def get_finance_trend(
        self,
        *,
        context_token: str,
        trace_id: str,
        start_date: str | None = None,
        end_date: str | None = None,
        venue_id: int | None = None,
    ) -> AnalyticsResult:
        data = await self._client.get(
            _FINANCE_TREND_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={"startDate": start_date, "endDate": end_date, "venueId": venue_id},
        )
        return AnalyticsResult.model_validate(data or {})

    async def get_revenue_breakdown(
        self,
        *,
        context_token: str,
        trace_id: str,
        start_date: str | None = None,
        end_date: str | None = None,
        venue_id: int | None = None,
    ) -> AnalyticsResult:
        data = await self._client.get(
            _REVENUE_BREAKDOWN_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={"startDate": start_date, "endDate": end_date, "venueId": venue_id},
        )
        return AnalyticsResult.model_validate(data or {})

    async def get_venue_comparison(
        self,
        *,
        context_token: str,
        trace_id: str,
        start_date: str | None = None,
        end_date: str | None = None,
    ) -> AnalyticsResult:
        data = await self._client.get(
            _VENUE_COMPARISON_PATH,
            context_token=context_token,
            trace_id=trace_id,
            params={"startDate": start_date, "endDate": end_date},
        )
        return AnalyticsResult.model_validate(data or {})
