"""BookingAgent 离线测试：用 Fake ChatModel + Fake BookingTool 驱动确认闭环，
验证追问→报价→确认→创建路径，以及“未确认不创建”“重复确认只创建一次”“拒绝不创建”。
不消耗任何真实 Token / 网络。"""

from collections.abc import Sequence
from datetime import UTC, datetime, timedelta
from decimal import Decimal
from typing import Any

from app.agents.base import AgentType
from app.agents.booking_agent import BookingAgent, BookingSlots
from app.llm.schemas import ChatMessage
from app.memory.session import Session
from app.tools.booking import BookingCreateResult, BookingQuote

_COMPLETE = BookingSlots(
    venue_id=1,
    court_ids=[10],
    date="2025-01-20",
    start_time="19:00",
    end_time="21:00",
)
_EMPTY = BookingSlots()


class FakeModel:
    def __init__(self, slots: BookingSlots) -> None:
        self._slots = slots
        self.calls = 0

    async def generate_structured(
        self, messages: Sequence[ChatMessage], response_model: type[Any]
    ) -> Any:
        self.calls += 1
        return self._slots


class FakeBookingTool:
    def __init__(self) -> None:
        self.quote_calls = 0
        self.create_keys: list[str] = []

    async def quote(self, **kwargs: Any) -> BookingQuote:
        self.quote_calls += 1
        return BookingQuote(
            venue_id=1,
            court_ids=[10],
            date="2025-01-20",
            start_time="19:00",
            end_time="21:00",
            booking_mode="WHOLE",
            pricing_mode="STANDARD",
            duration=2,
            total_amount=Decimal("120.00"),
            line_items=[],
            available=True,
        )

    async def create_pending(self, **kwargs: Any) -> BookingCreateResult:
        self.create_keys.append(str(kwargs["idempotency_key"]))
        return BookingCreateResult(
            booking_id=500,
            booking_no="BK20250120001",
            status=1,
            payment_status=0,
            venue_id=1,
            court_ids=[10],
            date="2025-01-20",
            start_time="19:00",
            end_time="21:00",
            duration=2,
            total_amount=Decimal("120.00"),
        )


def _session(cid: str = "conv-b", uid: str = "user-a") -> Session:
    now = datetime.now(UTC)
    return Session(
        conversation_id=cid,
        user_id=uid,
        agent_type=AgentType.BOOKING,
        thread_id=f"thread_{cid}_{uid}",
        created_at=now,
        expires_at=now + timedelta(hours=1),
    )


from app.tools.court import CourtAvailability


class FakeCourtTool:
    def __init__(self, available_court_ids: list[int] | None = None) -> None:
        self.available_court_ids = available_court_ids if available_court_ids is not None else [10]

    async def query_availability(self, **kwargs: Any) -> list[CourtAvailability]:
        st = str(kwargs.get("start_time", ""))
        # 假设只有 19:00 或 20:00 时段有场地
        if self.available_court_ids and ("19:00" in st or "20:00" in st):
            return [
                CourtAvailability(
                    courtId=cid,
                    courtCode=f"C{cid}",
                    courtName=f"{cid}号场地",
                    venueId=1,
                    available=True,
                    date=str(kwargs.get("date")),
                    startTime="19:00",
                    endTime="21:00",
                )
                for cid in self.available_court_ids
            ]
        return [
            CourtAvailability(
                courtId=10,
                courtCode="C10",
                courtName="10号场地",
                venueId=1,
                available=False,
                date=str(kwargs.get("date")),
                startTime="19:00",
                endTime="21:00",
            )
        ]


async def test_asks_for_missing_slot_without_quoting() -> None:
    tool = FakeBookingTool()
    agent = BookingAgent(model=FakeModel(_EMPTY), booking_tool=tool)  # type: ignore[arg-type]

    result = await agent.process(_session(), "你好，我想订个场", context_token="ctx")

    assert result.requires_action is False
    assert "场馆" in result.response
    assert tool.quote_calls == 0
    assert tool.create_keys == []


async def test_quote_then_confirm_creates_pending_exactly_once() -> None:
    tool = FakeBookingTool()
    agent = BookingAgent(model=FakeModel(_COMPLETE), booking_tool=tool)  # type: ignore[arg-type]
    session = _session()

    first = await agent.process(
        session, "订1号场馆 2025-01-20 19:00-21:00 场地10", context_token="ctx"
    )
    assert first.requires_action is True
    assert first.type == "confirmation"
    assert tool.create_keys == []  # 未确认绝不创建
    assert any("120.00" in str(action) for action in first.actions)  # 报价金额透传

    second = await agent.process(session, "确认", context_token="ctx")
    assert second.requires_action is False
    assert len(tool.create_keys) == 1
    assert "BK20250120001" in second.response

    # 重复确认：已创建后不再重复创建。
    third = await agent.process(session, "确认", context_token="ctx")
    assert len(tool.create_keys) == 1
    assert third.requires_action is False


async def test_decline_does_not_create() -> None:
    tool = FakeBookingTool()
    agent = BookingAgent(model=FakeModel(_COMPLETE), booking_tool=tool)  # type: ignore[arg-type]
    session = _session()

    await agent.process(session, "订1号场馆 2025-01-20 19:00-21:00 场地10", context_token="ctx")
    result = await agent.process(session, "取消", context_token="ctx")

    assert result.requires_action is False
    assert tool.create_keys == []
    assert "取消" in result.response


async def test_auto_selects_court_when_court_ids_missing() -> None:
    no_court_slots = BookingSlots(
        venue_id=1,
        court_ids=[],
        date="2025-01-20",
        start_time="19:00",
        end_time="21:00",
    )
    tool = FakeBookingTool()
    court_tool = FakeCourtTool(available_court_ids=[10])
    agent = BookingAgent(
        model=FakeModel(no_court_slots),
        booking_tool=tool,  # type: ignore[arg-type]
        court_tool=court_tool,  # type: ignore[arg-type]
    )

    result = await agent.process(_session(), "订1号场馆 2025-01-20 19:00-21:00", context_token="ctx")
    assert result.requires_action is True
    assert result.type == "confirmation"
    assert tool.quote_calls == 1


async def test_suggests_adjacent_alternatives_when_full() -> None:
    full_slots = BookingSlots(
        venue_id=1,
        court_ids=[],
        date="2025-01-20",
        start_time="17:00",  # 17:00 无空场，但相邻时段有空
        end_time="19:00",
    )
    tool = FakeBookingTool()
    court_tool = FakeCourtTool(available_court_ids=[10])
    agent = BookingAgent(
        model=FakeModel(full_slots),
        booking_tool=tool,  # type: ignore[arg-type]
        court_tool=court_tool,  # type: ignore[arg-type]
    )

    result = await agent.process(_session(), "订1号场馆 2025-01-20 17:00-19:00", context_token="ctx")
    assert result.requires_action is False
    assert "替代方案" in result.response or "暂无可用场地" in result.response

