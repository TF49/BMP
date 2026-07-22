"""经营分析 Agent：处理经营总览、趋势图表、热力图、财务构成与场馆对比请求。

主要设计点：
- 严格遵循受控工具原则：模型与 Agent 绝不生成或执行任意 SQL。
- 身份与范围完全依赖 Java 服务端裁定：忽略前端/模型伪造的范围。
- 格式化标准输出：包含统计周期、数据范围、指标口径及受控 ECharts 图表数据。
"""

import logging
import re
import time
from datetime import date, datetime, timedelta
from typing import Any

from app.agents.base import AgentMetadata, AgentResult
from app.llm.base import ChatModel
from app.memory.checkpoint import AgentCheckpointStore
from app.memory.session import Session
from app.observability.tracing import get_trace_id
from app.tools.analytics import AnalyticsResult, AnalyticsTool
from app.tools.base import ToolPermissionError, ToolError

logger = logging.getLogger("bmp_agent.analytics")

_MODEL_LABEL = "analytics-agent"


class AnalyticsAgent:
    """经营分析 Agent。"""

    def __init__(
        self,
        analytics_tool: AnalyticsTool,
        model: ChatModel | None = None,
        checkpoint_store: AgentCheckpointStore | None = None,
    ) -> None:
        self._analytics_tool = analytics_tool
        self._model = model
        self._checkpoint_store = checkpoint_store

    async def process(
        self,
        session: Session,
        message: str,
        context_token: str | None = None,
    ) -> AgentResult:
        start_time = time.monotonic()
        trace_id = get_trace_id()
        ctx_token = context_token or ""

        # 读取轮次历史
        turn = 1
        if self._checkpoint_store is not None and session.thread_id:
            try:
                state = await self._checkpoint_store.get(session.thread_id)
                if state and isinstance(state, dict):
                    turn = int(state.get("turn", 0)) + 1
            except Exception as e:
                logger.warning("failed to fetch checkpoint state: %s", e)

        # 意图识别与参数抽取
        intent, start_date, end_date, venue_id = self._parse_intent_and_params(message)

        tool_calls: list[str] = []
        try:
            if intent == "booking_trend":
                tool_calls.append("get_booking_trend")
                res = await self._analytics_tool.get_booking_trend(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    start_date=start_date,
                    end_date=end_date,
                    venue_id=venue_id,
                )
            elif intent == "court_heatmap":
                tool_calls.append("get_court_heatmap")
                res = await self._analytics_tool.get_court_heatmap(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    start_date=start_date,
                    end_date=end_date,
                    venue_id=venue_id,
                )
            elif intent == "finance_trend":
                tool_calls.append("get_finance_trend")
                res = await self._analytics_tool.get_finance_trend(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    start_date=start_date,
                    end_date=end_date,
                    venue_id=venue_id,
                )
            elif intent == "revenue_breakdown":
                tool_calls.append("get_revenue_breakdown")
                res = await self._analytics_tool.get_revenue_breakdown(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    start_date=start_date,
                    end_date=end_date,
                    venue_id=venue_id,
                )
            elif intent == "venue_comparison":
                tool_calls.append("get_venue_comparison")
                res = await self._analytics_tool.get_venue_comparison(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    start_date=start_date,
                    end_date=end_date,
                )
            else:  # default overview
                tool_calls.append("get_overview")
                res = await self._analytics_tool.get_overview(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    start_date=start_date,
                    end_date=end_date,
                    venue_id=venue_id,
                )

            formatted_response, actions = self._format_agent_output(intent, res)

        except ToolPermissionError:
            formatted_response = (
                "❌ **访问拒绝**：权限不足。\n\n"
                "经营分析数据仅对平台会长（`PRESIDENT`）和场馆管理者（`VENUE_MANAGER`）开放。"
                "请检查您的当前登录账号角色。"
            )
            actions = []
        except ToolError as exc:
            formatted_response = f"⚠️ 数据查询失败：{exc.message}"
            actions = []
        except Exception as exc:
            logger.error("analytics agent process error", exc_info=exc)
            formatted_response = "⚠️ 系统在处理经营分析请求时遇到了异常，请稍后重试。"
            actions = []

        # 保存轮次
        if self._checkpoint_store is not None and session.thread_id:
            try:
                await self._checkpoint_store.put(session.thread_id, {"turn": turn})
            except Exception as e:
                logger.warning("failed to save checkpoint state: %s", e)

        duration_ms = int((time.monotonic() - start_time) * 1000)

        metadata = AgentMetadata(
            model=_MODEL_LABEL,
            tokens_used=0,
            duration_ms=duration_ms,
            tool_calls=tool_calls,
            turn=turn,
        )

        return AgentResult(
            response=formatted_response,
            type="analytics",
            requires_action=False,
            actions=actions,
            references=[],
            metadata=metadata,
        )

    async def delete_thread(self, thread_id: str) -> None:
        if self._checkpoint_store is not None and thread_id:
            try:
                await self._checkpoint_store.delete(thread_id)
            except Exception as e:
                logger.warning("failed to delete thread checkpoint: %s", e)

    def _parse_intent_and_params(
        self, message: str
    ) -> tuple[str, str | None, str | None, int | None]:
        msg = message.lower()

        # Intent detection
        if any(k in msg for k in ["对比", "比较", "横向", "各场馆"]):
            intent = "venue_comparison"
        elif any(k in msg for k in ["热力图", "利用率", "高峰", "繁忙时段", "时段"]):
            intent = "court_heatmap"
        elif any(k in msg for k in ["构成", "占比", "业务", "来源", "分类"]):
            intent = "revenue_breakdown"
        elif any(k in msg for k in ["财务", "收支", "支出", "净收益"]):
            intent = "finance_trend"
        elif any(k in msg for k in ["预约量", "预订量", "场次", "趋势"]):
            intent = "booking_trend"
        else:
            intent = "overview"

        # Date range extraction
        today = date.today()
        start_date_str = None
        end_date_str = None

        iso_dates = []
        for raw_d in re.findall(r"\d{4}-\d{2}-\d{2}", message):
            try:
                date.fromisoformat(raw_d)
                iso_dates.append(raw_d)
            except ValueError:
                pass

        if len(iso_dates) >= 2:
            start_date_str = iso_dates[0]
            end_date_str = iso_dates[1]
        elif len(iso_dates) == 1:
            start_date_str = iso_dates[0]
            end_date_str = iso_dates[0]
        elif "本周" in msg or "这周" in msg:
            start = today - timedelta(days=today.weekday())
            start_date_str = start.isoformat()
            end_date_str = today.isoformat()
        elif "本月" in msg:
            start = today.replace(day=1)
            start_date_str = start.isoformat()
            end_date_str = today.isoformat()
        elif "上周" in msg:
            end = today - timedelta(days=today.weekday() + 1)
            start = end - timedelta(days=6)
            start_date_str = start.isoformat()
            end_date_str = end.isoformat()
        elif "最近一周" in msg or "近7天" in msg or "7天" in msg:
            start = today - timedelta(days=6)
            start_date_str = start.isoformat()
            end_date_str = today.isoformat()

        # Venue ID extraction
        venue_id = None
        venue_match = re.search(r"(\d+)号(?:场馆|馆)", message)
        if venue_match:
            venue_id = int(venue_match.group(1))

        return intent, start_date_str, end_date_str, venue_id

    def _format_agent_output(
        self, intent: str, result: AnalyticsResult
    ) -> tuple[str, list[dict[str, Any]]]:
        lines: list[str] = []

        # 1. 标题与头部
        title_map = {
            "overview": "📊 经营总览分析报告",
            "booking_trend": "📈 场地预约趋势报告",
            "court_heatmap": "🔥 场地利用率与热力图分析",
            "finance_trend": "💰 财务收支趋势报告",
            "revenue_breakdown": "🍩 业务收入构成与占比分析",
            "venue_comparison": "🏢 多场馆经营横向对比报告",
        }
        lines.append(f"## {title_map.get(intent, '📊 经营分析报告')}\n")

        # 2. 统计周期与数据范围
        meta = result.meta
        if meta and meta.period and meta.scope:
            lines.append(
                f"- **统计周期**：`{meta.period.start_date or '-'}` 至 `{meta.period.end_date or '-'}` ({meta.period.granularity or 'DAY'})"
            )
            lines.append(
                f"- **数据范围**：`{meta.scope.venue_name or meta.scope.description or '全部场馆'}` (范围级别: `{meta.scope.level or 'ALL'}`)"
            )
            lines.append("")

        # 3. 核心 KPI 摘要
        if result.kpis:
            lines.append("### 💡 核心指标摘要")
            for kpi in result.kpis:
                lines.append(f"- **{kpi.name}**：`{kpi.value}` {kpi.unit or ''}")
            lines.append("")

        # 4. 图表表达说明
        actions: list[dict[str, Any]] = []
        if result.charts:
            for i, chart in enumerate(result.charts):
                if chart.empty:
                    lines.append(f"ℹ️ **图表提示**：{chart.empty_text or '当前暂无相关经营数据'}")
                else:
                    lines.append(f"### 📉 图表展示：{chart.title or '分析图表'}")
                    chart_dict = chart.model_dump(by_alias=True)
                    actions.append(
                        {
                            "type": "render_chart",
                            "chart_type": chart.chart_type,
                            "title": chart.title,
                            "chart_data": chart_dict,
                        }
                    )

        # 5. 固化指标口径说明
        if meta and meta.metrics:
            lines.append("\n### 📖 指标计算口径说明")
            for metric in meta.metrics:
                lines.append(
                    f"- **{metric.name}** (`{metric.key}`): {metric.definition} (单位: {metric.unit})"
                )

        return "\n".join(lines), actions
