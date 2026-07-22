"""预订 Agent：结构化提取 → Java 报价 → 用户确认（interrupt/resume）→ Java 创建待支付。

设计要点：
- 金额一律由 Java 计算，模型只做槽位提取与对话编排，绝不自行计算价格/优惠/余额。
- 采用 LangGraph ``interrupt``/``resume`` 实现确认闭环：报价后中断等待用户确认，
  收到明确确认后才携带幂等键调用创建接口；未确认绝不创建。
- 槽位提取以 LLM ``generate_structured`` 为主、确定性正则兜底；缺字段时每次只追问一个。
"""

import logging
import re
from datetime import date as date_cls, datetime, time as time_cls
from typing import Any, TypedDict, cast

from langchain_core.runnables import RunnableConfig
from langgraph.graph import END, START, StateGraph
from langgraph.types import Command, interrupt
from pydantic import BaseModel, ConfigDict, Field

from app.agents.base import AgentMetadata, AgentResult
from app.llm.base import ChatModel
from app.llm.schemas import ChatMessage
from app.memory.checkpoint import AgentCheckpointStore, create_memory_checkpointer
from app.memory.session import Session
from app.observability.tracing import get_trace_id
from app.tools.base import ToolError
from app.tools.booking import BookingTool
from app.tools.court import CourtAvailability, CourtTool

logger = logging.getLogger("bmp_agent")

_MODEL_LABEL = "booking-agent"

# 报价所需的必填槽位与追问顺序（每次只追问第一个缺失项）。
_REQUIRED_SLOTS = ("venue_id", "date", "start_time", "end_time", "court_ids")
_SLOT_QUESTIONS = {
    "venue_id": "请问您想预约哪个场馆？请提供场馆编号。",
    "date": "请问预约哪一天？（格式如 2025-01-20）",
    "start_time": "请问从几点开始？（格式 HH:MM，如 19:00）",
    "end_time": "请问到几点结束？（格式 HH:MM，如 21:00）",
    "court_ids": "请问预约哪几号场地？请提供场地编号。",
}

_CONFIRM_KEYWORDS = ("确认", "确定", "对的", "是的", "可以", "好的", "yes", "ok", "confirm")
_DECLINE_KEYWORDS = ("取消", "不用", "算了", "否", "no", "cancel")


def _get_extract_system_prompt() -> str:
    today_str = datetime.now().date().isoformat()
    return (
        "你是羽毛球场馆预约助手的槽位抽取器。请从用户消息中抽取预约要素，"
        "只输出 JSON，不要计算任何价格。未提及的字段填 null，场地编号用整数数组。"
        "日期格式 yyyy-MM-dd，时间格式 HH:mm。"
        f"当前系统日期为：{today_str}。若用户提及“明天”、“后天”、“这周六”等相对日期，"
        f"请基于系统日期 {today_str} 准确计算为具体的 yyyy-MM-dd 日期。"
    )


class BookingSlots(BaseModel):
    """预约槽位：供 LLM 结构化抽取，字段全部可空以便多轮增量合并。"""

    model_config = ConfigDict(extra="ignore")

    venue_id: int | None = Field(default=None)
    court_ids: list[int] = Field(default_factory=list)
    date: str | None = Field(default=None)
    start_time: str | None = Field(default=None)
    end_time: str | None = Field(default=None)
    booking_mode: str | None = Field(default=None)
    pricing_mode: str | None = Field(default=None)


class BookingState(TypedDict, total=False):
    message: str
    context_token: str
    trace_id: str
    slots: dict[str, Any]
    idempotency_key: str
    phase: str  # collecting | ready | created | cancelled
    response: str
    requires_action: bool
    actions: list[dict[str, Any]]
    references: list[dict[str, Any]]
    tool_calls: list[str]
    turn: int


def _is_confirmation(text: str | None) -> bool:
    if not text:
        return False
    lowered = text.strip().lower()
    if any(word in lowered for word in _DECLINE_KEYWORDS):
        return False
    return any(word in lowered for word in _CONFIRM_KEYWORDS)


def _regex_extract(message: str) -> dict[str, Any]:
    """确定性兜底抽取：仅在 LLM 漏抽时填补，绝不覆盖已知槽位。"""
    found: dict[str, Any] = {}
    date_match = re.search(r"(\d{4}-\d{1,2}-\d{1,2})", message)
    if date_match:
        found["date"] = date_match.group(1)
    times = re.findall(r"(\d{1,2}:\d{2})", message)
    if times:
        found["start_time"] = times[0]
        if len(times) >= 2:
            found["end_time"] = times[1]
    venue_match = re.search(r"(?:场馆|venue)\s*(\d+)", message, re.IGNORECASE)
    if venue_match:
        found["venue_id"] = int(venue_match.group(1))

    # 避开“场馆 / venue”等引发的“1号场馆”误匹配
    clean_msg = re.sub(r"(?:场馆|venue)\s*\d+", "", message, flags=re.IGNORECASE)
    court_ids = [int(n) for n in re.findall(r"(\d+)\s*号?\s*场(?:地)?(?!馆)", clean_msg)]
    if court_ids:
        found["court_ids"] = court_ids
    return found


def _missing_slot(slots: dict[str, Any]) -> str | None:
    for name in _REQUIRED_SLOTS:
        value = slots.get(name)
        if name == "court_ids":
            if not value:
                return name
        elif value in (None, ""):
            return name
    return None


class BookingAgent:
    """预订 Agent，满足 :class:`app.agents.base.Agent` 协议。"""

    def __init__(
        self,
        *,
        model: ChatModel,
        booking_tool: BookingTool,
        court_tool: CourtTool | None = None,
        checkpoint_store: AgentCheckpointStore | None = None,
    ) -> None:
        self._model = model
        self._booking_tool = booking_tool
        self._court_tool = court_tool
        self._persistent_checkpoint_store = checkpoint_store
        self._checkpointer = create_memory_checkpointer()
        builder = StateGraph(BookingState)
        builder.add_node("extract", self._extract_node)
        builder.add_node("quote_confirm", self._quote_confirm_node)
        builder.add_edge(START, "extract")
        builder.add_conditional_edges(
            "extract",
            self._route_after_extract,
            {"quote": "quote_confirm", "end": END},
        )
        builder.add_edge("quote_confirm", END)
        self._graph = builder.compile(checkpointer=self._checkpointer)

    async def delete_thread(self, thread_id: str) -> None:
        await self._checkpointer.adelete_thread(thread_id)
        if self._persistent_checkpoint_store is not None:
            await self._persistent_checkpoint_store.delete_thread(thread_id)

    async def process(
        self, session: Session, message: str, context_token: str | None = None
    ) -> AgentResult:
        config = RunnableConfig(configurable={"thread_id": session.thread_id})
        trace_id = get_trace_id()
        token = context_token or ""

        snapshot = await self._graph.aget_state(config)
        awaiting_confirmation = bool(snapshot.next)

        if awaiting_confirmation:
            result_state = await self._graph.ainvoke(
                Command(
                    resume=message,
                    update={"context_token": token, "trace_id": trace_id, "message": message},
                ),
                config=config,
            )
        else:
            result_state = await self._graph.ainvoke(
                {"message": message, "context_token": token, "trace_id": trace_id},
                config=config,
            )

        return await self._build_result(config, result_state)

    async def _build_result(
        self, config: RunnableConfig, result_state: dict[str, Any]
    ) -> AgentResult:
        snapshot = await self._graph.aget_state(config)
        turn = int(result_state.get("turn", 1) or 1)
        tool_calls = list(result_state.get("tool_calls", []) or [])

        if snapshot.next:
            # 已在 quote_confirm 处中断，等待用户确认。
            payload = _interrupt_payload(result_state)
            if not payload:
                payload = _interrupt_payload_from_snapshot(snapshot)
            response = str(payload.get("prompt", "请确认预约信息。"))
            quote = payload.get("quote")
            references = [quote] if isinstance(quote, dict) else []
            return AgentResult(
                response=response,
                type="confirmation",
                requires_action=True,
                actions=[payload],
                references=references,
                metadata=self._metadata(tool_calls, turn),
            )

        response = str(result_state.get("response", ""))
        return AgentResult(
            response=response,
            requires_action=bool(result_state.get("requires_action", False)),
            actions=list(result_state.get("actions", []) or []),
            references=list(result_state.get("references", []) or []),
            metadata=self._metadata(tool_calls, turn),
        )

    def _metadata(self, tool_calls: list[str], turn: int) -> AgentMetadata:
        return AgentMetadata(
            model=_MODEL_LABEL,
            tokens_used=0,
            duration_ms=0,
            tool_calls=tool_calls,
            turn=turn,
        )

    def _route_after_extract(self, state: BookingState) -> str:
        return "quote" if state.get("phase") == "ready" else "end"

    async def _extract_node(self, state: BookingState) -> BookingState:
        turn = int(state.get("turn", 0)) + 1
        prior = dict(state.get("slots", {}))

        # 预约已创建后，同一会话的后续消息直接给出结果摘要，不再重复报价。
        if state.get("phase") == "created":
            return {
                "turn": turn,
                "response": state.get("response", "您的预约已创建，请尽快前往支付。"),
                "requires_action": False,
            }

        message = str(state.get("message", ""))
        merged = await self._merge_slots(message, prior)
        token = str(state.get("context_token", ""))
        trace_id = str(state.get("trace_id", "")) or get_trace_id()

        # 若基础要素齐全但场地为空，尝试自动匹配空闲场地；无空场时搜索相邻替代方案。
        if (
            merged.get("venue_id")
            and merged.get("date")
            and merged.get("start_time")
            and merged.get("end_time")
            and not merged.get("court_ids")
        ):
            selected_courts, alt_msg = await self._auto_select_court_or_suggest_alternatives(
                merged, token, trace_id
            )
            if selected_courts:
                merged["court_ids"] = selected_courts
            elif alt_msg:
                return {
                    "turn": turn,
                    "slots": merged,
                    "phase": "collecting",
                    "response": alt_msg,
                    "requires_action": False,
                    "tool_calls": ["query_availability"],
                }

        missing = _missing_slot(merged)
        if missing is not None:
            return {
                "turn": turn,
                "slots": merged,
                "phase": "collecting",
                "response": _SLOT_QUESTIONS[missing],
                "requires_action": False,
            }
        return {
            "turn": turn,
            "slots": merged,
            "phase": "ready",
            "idempotency_key": state.get("idempotency_key") or _new_idempotency_key(),
        }

    async def _merge_slots(self, message: str, prior: dict[str, Any]) -> dict[str, Any]:
        extracted: dict[str, Any] = {}
        try:
            slots = await self._model.generate_structured(
                [
                    ChatMessage(role="system", content=_get_extract_system_prompt()),
                    ChatMessage(role="user", content=_extract_user_prompt(message, prior)),
                ],
                BookingSlots,
            )
            extracted = slots.model_dump(exclude_none=True)
            if not extracted.get("court_ids"):
                extracted.pop("court_ids", None)
        except Exception as exc:  # LLM 不可用/解析失败时退化为确定性抽取。
            logger.warning(
                "booking slot llm extraction failed, falling back to regex",
                extra={"exception_type": type(exc).__name__},
            )

        fallback = _regex_extract(message)
        for key, value in fallback.items():
            extracted.setdefault(key, value)

        merged = dict(prior)
        for key, value in extracted.items():
            if value in (None, "", []):
                continue
            merged[key] = value
        return merged

    async def _auto_select_court_or_suggest_alternatives(
        self, slots: dict[str, Any], token: str, trace_id: str
    ) -> tuple[list[int], str | None]:
        """在缺失场地或满场时自动匹配空场地，或搜索相邻替代时段。"""
        if self._court_tool is None:
            return slots.get("court_ids", []), None

        try:
            d_val = date_cls.fromisoformat(str(slots["date"]))
            st_parts = str(slots["start_time"]).split(":")
            et_parts = str(slots["end_time"]).split(":")
            st_val = time_cls(int(st_parts[0]), int(st_parts[1]))
            et_val = time_cls(int(et_parts[0]), int(et_parts[1]))
        except Exception:
            return slots.get("court_ids", []), None

        try:
            avail = await self._court_tool.query_availability(
                venue_id=int(slots["venue_id"]),
                date=d_val,
                start_time=st_val,
                end_time=et_val,
                context_token=token,
                trace_id=trace_id,
            )
            open_courts = [c.court_id for c in avail if c.available]
            if open_courts:
                return open_courts[:1], None
        except Exception as exc:
            logger.warning("court availability check failed: %s", exc)

        alternatives = await self._find_adjacent_alternatives(
            venue_id=int(slots["venue_id"]),
            target_date=d_val,
            target_start=st_val,
            target_end=et_val,
            token=token,
            trace_id=trace_id,
        )
        if alternatives:
            alt_str = "\n".join(
                f"- {alt['start_time']}-{alt['end_time']} ({alt['court_name'] or f'{alt['court_id']}号场地'})"
                for alt in alternatives
            )
            msg = (
                f"抱歉，{slots['date']} {slots['start_time']}-{slots['end_time']} 暂无可用场地。"
                f"为您找到以下相邻替代方案：\n{alt_str}\n请问是否调整预约时段？"
            )
            return [], msg

        return [], f"抱歉，{slots['date']} {slots['start_time']}-{slots['end_time']} 及相邻时段均无空余场地，请尝试更换日期或场馆。"

    async def _find_adjacent_alternatives(
        self,
        *,
        venue_id: int,
        target_date: date_cls,
        target_start: time_cls,
        target_end: time_cls,
        token: str,
        trace_id: str,
    ) -> list[dict[str, Any]]:
        if self._court_tool is None:
            return []

        duration_hours = max(1, target_end.hour - target_start.hour)
        offsets = [-2, -1, 1, 2]
        alts: list[dict[str, Any]] = []

        for offset in offsets:
            new_st_hour = target_start.hour + offset
            new_et_hour = new_st_hour + duration_hours
            if new_st_hour < 8 or new_et_hour > 22:
                continue
            try:
                st = time_cls(new_st_hour, target_start.minute)
                et = time_cls(new_et_hour, target_end.minute)
                avail = await self._court_tool.query_availability(
                    venue_id=venue_id,
                    date=target_date,
                    start_time=st,
                    end_time=et,
                    context_token=token,
                    trace_id=trace_id,
                )
                open_courts = [c for c in avail if c.available]
                if open_courts:
                    court = open_courts[0]
                    alts.append(
                        {
                            "start_time": st.strftime("%H:%M"),
                            "end_time": et.strftime("%H:%M"),
                            "court_id": court.court_id,
                            "court_name": court.court_name,
                        }
                    )
                    if len(alts) >= 3:
                        break
            except Exception:
                continue

        return alts

    async def _quote_confirm_node(self, state: BookingState) -> BookingState:
        slots = dict(state.get("slots", {}))
        token = str(state.get("context_token", ""))
        trace_id = str(state.get("trace_id", "")) or get_trace_id()

        try:
            quote = await self._booking_tool.quote(
                venue_id=int(slots["venue_id"]),
                court_ids=[int(c) for c in slots["court_ids"]],
                date=str(slots["date"]),
                start_time=str(slots["start_time"]),
                end_time=str(slots["end_time"]),
                booking_mode=slots.get("booking_mode"),
                pricing_mode=slots.get("pricing_mode"),
                context_token=token,
                trace_id=trace_id,
            )
        except ToolError as exc:
            _, alt_msg = await self._auto_select_court_or_suggest_alternatives(
                slots, token, trace_id
            )
            response_msg = (
                f"暂时无法完成当前预约（{exc}）。\n{alt_msg}"
                if alt_msg
                else f"暂时无法完成预约：{exc}。请尝试更改预约时间或更换场地。"
            )
            return {
                "phase": "collecting",
                "response": response_msg,
                "requires_action": False,
                "tool_calls": ["quote"],
            }

        quote_dump = quote.model_dump(mode="json", by_alias=True)
        key = str(state.get("idempotency_key", ""))
        payload: dict[str, Any] = {
            "type": "confirm_booking",
            "prompt": _confirmation_prompt(quote_dump),
            "quote": quote_dump,
            "idempotency_key": key,
        }

        decision = interrupt(payload)

        if not _is_confirmation(decision if isinstance(decision, str) else str(decision)):
            return {
                "phase": "cancelled",
                "response": "已为您取消本次预约，未产生任何订单。",
                "requires_action": False,
                "tool_calls": ["quote"],
            }

        try:
            result = await self._booking_tool.create_pending(
                venue_id=int(slots["venue_id"]),
                court_ids=[int(c) for c in slots["court_ids"]],
                date=str(slots["date"]),
                start_time=str(slots["start_time"]),
                end_time=str(slots["end_time"]),
                booking_mode=slots.get("booking_mode"),
                pricing_mode=slots.get("pricing_mode"),
                idempotency_key=key,
                context_token=token,
                trace_id=trace_id,
            )
        except ToolError as exc:
            return {
                "phase": "cancelled",
                "response": f"创建预约失败：{exc}",
                "requires_action": False,
                "tool_calls": ["quote", "create_pending"],
            }

        created = result.model_dump(mode="json", by_alias=True)
        return {
            "phase": "created",
            "response": _created_prompt(created),
            "requires_action": False,
            "actions": [{"type": "booking_created", "booking": created}],
            "references": [created],
            "tool_calls": ["quote", "create_pending"],
        }


def _new_idempotency_key() -> str:
    # 每个预约一个稳定幂等键（存入会话状态）：重复确认由 Java 侧回放首次结果，只创建一个预约。
    from uuid import uuid4

    return f"booking-{uuid4().hex}"


def _extract_user_prompt(message: str, prior: dict[str, Any]) -> str:
    if prior:
        return f"已知槽位：{prior}\n新消息：{message}"
    return f"新消息：{message}"


def _interrupt_payload(result_state: dict[str, Any]) -> dict[str, Any]:
    interrupts = result_state.get("__interrupt__")
    if not interrupts:
        return {}
    first = interrupts[0]
    value = getattr(first, "value", first)
    return cast(dict[str, Any], value) if isinstance(value, dict) else {}


def _interrupt_payload_from_snapshot(snapshot: Any) -> dict[str, Any]:
    for task in getattr(snapshot, "tasks", ()):  # StateSnapshot.tasks
        for intr in getattr(task, "interrupts", ()):
            value = getattr(intr, "value", None)
            if isinstance(value, dict):
                return cast(dict[str, Any], value)
    return {}


def _confirmation_prompt(quote: dict[str, Any]) -> str:
    return (
        f"报价确认：场馆 {quote.get('venueId')}、日期 {quote.get('date')}、"
        f"{quote.get('startTime')}-{quote.get('endTime')}、"
        f"场地 {quote.get('courtIds')}，时长 {quote.get('duration')} 小时，"
        f"合计 {quote.get('totalAmount')} 元。确认预约请回复“确认”，取消请回复“取消”。"
    )


def _created_prompt(created: dict[str, Any]) -> str:
    return (
        f"已为您创建待支付预约：单号 {created.get('bookingNo')}，"
        f"金额 {created.get('totalAmount')} 元，请尽快前往支付。"
    )
