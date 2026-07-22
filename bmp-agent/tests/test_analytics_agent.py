import json
from datetime import UTC, datetime, timedelta
from pathlib import Path
from unittest.mock import AsyncMock

import pytest

from app.agents.analytics.analytics_agent import AnalyticsAgent
from app.agents.base import AgentType
from app.memory.session import Session
from app.tools.analytics import (
    AnalyticsChart,
    AnalyticsMeta,
    AnalyticsPeriod,
    AnalyticsResult,
    AnalyticsScope,
    AnalyticsTool,
)
from app.tools.base import ToolPermissionError, ToolError


@pytest.fixture
def session_fixture() -> Session:
    created_at = datetime.now(UTC)
    return Session(
        conversation_id="conv-analytics-1",
        user_id="user-123",
        agent_type=AgentType.ANALYTICS,
        thread_id="thread-analytics-123",
        created_at=created_at,
        expires_at=created_at + timedelta(hours=1),
    )


@pytest.fixture
def mock_analytics_tool() -> AnalyticsTool:
    tool = AsyncMock(spec=AnalyticsTool)
    fake_result = AnalyticsResult(
        meta=AnalyticsMeta(
            period=AnalyticsPeriod(
                startDate="2026-07-16",
                endDate="2026-07-22",
                granularity="DAY",
                description="2026-07-16 至 2026-07-22",
            ),
            scope=AnalyticsScope(
                level="ALL",
                venueId=None,
                venueName="全部场馆",
                description="全部场馆",
            ),
            metrics=[],
        ),
        kpis=[],
        charts=[
            AnalyticsChart(
                chartType="bar",
                title="测试图表",
                categories=["2026-07-16", "2026-07-17"],
                series=[],
                empty=False,
            )
        ],
    )
    tool.get_overview.return_value = fake_result
    tool.get_booking_trend.return_value = fake_result
    tool.get_court_heatmap.return_value = fake_result
    tool.get_finance_trend.return_value = fake_result
    tool.get_revenue_breakdown.return_value = fake_result
    tool.get_venue_comparison.return_value = fake_result
    return tool


@pytest.mark.asyncio
async def test_analytics_agent_overview_intent(
    mock_analytics_tool: AsyncMock, session_fixture: Session
) -> None:
    agent = AnalyticsAgent(analytics_tool=mock_analytics_tool)
    result = await agent.process(session_fixture, "查看全馆经营总览", context_token="ctx-token-1")

    assert result.type == "analytics"
    assert "经营总览" in result.response
    assert len(result.actions) == 1
    assert result.actions[0]["type"] == "render_chart"
    mock_analytics_tool.get_overview.assert_called_once()


@pytest.mark.asyncio
async def test_analytics_agent_intents(
    mock_analytics_tool: AsyncMock, session_fixture: Session
) -> None:
    agent = AnalyticsAgent(analytics_tool=mock_analytics_tool)

    # booking trend
    res_trend = await agent.process(session_fixture, "查看最近7天预约量趋势图", context_token="ctx")
    assert "预约趋势" in res_trend.response
    mock_analytics_tool.get_booking_trend.assert_called_once()

    # court heatmap
    res_heat = await agent.process(session_fixture, "查看场地利用率热力图", context_token="ctx")
    assert "热力图" in res_heat.response
    mock_analytics_tool.get_court_heatmap.assert_called_once()

    # finance trend
    res_fin = await agent.process(session_fixture, "查看财务收支走势", context_token="ctx")
    assert "财务" in res_fin.response
    mock_analytics_tool.get_finance_trend.assert_called_once()

    # revenue breakdown
    res_rev = await agent.process(session_fixture, "显示收入构成与业务占比", context_token="ctx")
    assert "收入构成" in res_rev.response
    mock_analytics_tool.get_revenue_breakdown.assert_called_once()

    # venue comparison
    res_comp = await agent.process(session_fixture, "横向对比各个场馆经营数据", context_token="ctx")
    assert "多场馆" in res_comp.response
    mock_analytics_tool.get_venue_comparison.assert_called_once()


@pytest.mark.asyncio
async def test_analytics_agent_permission_denied_handling(
    mock_analytics_tool: AsyncMock, session_fixture: Session
) -> None:
    mock_analytics_tool.get_overview.side_effect = ToolPermissionError("权限不足")
    agent = AnalyticsAgent(analytics_tool=mock_analytics_tool)

    result = await agent.process(session_fixture, "查看经营总览", context_token="ctx-user-token")

    assert "访问拒绝" in result.response
    assert "权限不足" in result.response
    assert result.actions == []


@pytest.mark.asyncio
async def test_analytics_agent_generic_tool_error(
    mock_analytics_tool: AsyncMock, session_fixture: Session
) -> None:
    mock_analytics_tool.get_overview.side_effect = ToolError("服务不可用")
    agent = AnalyticsAgent(analytics_tool=mock_analytics_tool)

    result = await agent.process(session_fixture, "查看经营总览", context_token="ctx-token")
    assert "查询失败" in result.response


def test_evals_jsonl_validity() -> None:
    cases_path = Path(__file__).parent.parent / "evals" / "analytics" / "analytics_cases.jsonl"
    assert cases_path.exists()

    with open(cases_path, encoding="utf-8") as f:
        lines = [line.strip() for line in f if line.strip()]

    assert len(lines) >= 30, f"Expected at least 30 evaluation cases, got {len(lines)}"

    for line in lines:
        case = json.loads(line)
        assert "id" in case
        assert "category" in case
        assert "input" in case
        assert "expected_tool" in case
        assert "user_role" in case
