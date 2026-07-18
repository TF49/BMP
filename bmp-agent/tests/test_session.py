import asyncio
from datetime import UTC, datetime, timedelta

import pytest

from app.agents.base import AgentType
from app.core.security import VerifiedAgentContext
from app.memory.session import (
    InMemorySessionStore,
    Session,
    SessionAccessError,
    SessionExpiredError,
)


def context(user_id: str = "user-a") -> VerifiedAgentContext:
    return VerifiedAgentContext(
        user_id=user_id,
        role="MEMBER",
        venue_id=None,
        trace_id="trace-test",
    )


async def test_first_request_creates_session_and_reuses_binding() -> None:
    store = InMemorySessionStore(ttl_seconds=3600)

    first = await store.get_or_create("conv-1", context(), AgentType.BOOKING)
    second = await store.get_or_create("conv-1", context(), AgentType.BOOKING)

    assert second is first
    assert first.user_id == "user-a"
    assert first.agent_type is AgentType.BOOKING
    assert first.thread_id.startswith("thread_")


async def test_concurrent_first_requests_create_one_session() -> None:
    store = InMemorySessionStore(ttl_seconds=3600)

    sessions = await asyncio.gather(
        *(store.get_or_create("conv-1", context(), AgentType.BOOKING) for _ in range(20))
    )

    assert len({session.thread_id for session in sessions}) == 1
    assert all(session is sessions[0] for session in sessions)


@pytest.mark.parametrize(
    ("request_context", "agent_type"),
    [
        (context("user-b"), AgentType.BOOKING),
        (context("user-a"), AgentType.ANALYTICS),
    ],
)
async def test_existing_session_rejects_context_mismatch(
    request_context: VerifiedAgentContext, agent_type: AgentType
) -> None:
    store = InMemorySessionStore(ttl_seconds=3600)
    await store.get_or_create("conv-1", context(), AgentType.BOOKING)

    with pytest.raises(SessionAccessError, match="session does not belong"):
        await store.get_or_create("conv-1", request_context, agent_type)


async def test_expired_session_is_rejected() -> None:
    now = datetime(2026, 7, 18, tzinfo=UTC)
    current = now
    store = InMemorySessionStore(ttl_seconds=60, clock=lambda: current)
    original = await store.get_or_create("conv-1", context(), AgentType.BOOKING)
    current = now + timedelta(seconds=61)

    with pytest.raises(SessionExpiredError, match="session has expired"):
        await store.get_or_create("conv-1", context(), AgentType.BOOKING)

    recreated = await store.get_or_create("conv-1", context(), AgentType.BOOKING)
    assert recreated.thread_id != original.thread_id


async def test_expired_session_runs_checkpoint_cleanup() -> None:
    now = datetime(2026, 7, 18, tzinfo=UTC)
    current = now
    cleaned_threads: list[str] = []

    async def cleanup(session: Session) -> None:
        cleaned_threads.append(session.thread_id)

    store = InMemorySessionStore(
        ttl_seconds=60,
        clock=lambda: current,
        on_expire=cleanup,
    )
    original = await store.get_or_create("conv-1", context(), AgentType.BOOKING)
    current = now + timedelta(seconds=61)

    with pytest.raises(SessionExpiredError):
        await store.get_or_create("conv-1", context(), AgentType.BOOKING)

    assert cleaned_threads == [original.thread_id]


async def test_new_request_purges_other_expired_sessions() -> None:
    now = datetime(2026, 7, 18, tzinfo=UTC)
    current = now
    cleaned_threads: list[str] = []

    async def cleanup(session: Session) -> None:
        cleaned_threads.append(session.thread_id)

    store = InMemorySessionStore(
        ttl_seconds=60,
        clock=lambda: current,
        on_expire=cleanup,
    )
    expired = await store.get_or_create("conv-old", context(), AgentType.BOOKING)
    current = now + timedelta(seconds=61)

    await store.get_or_create("conv-new", context(), AgentType.BOOKING)
    recreated = await store.get_or_create("conv-old", context(), AgentType.BOOKING)

    assert cleaned_threads == [expired.thread_id]
    assert recreated.thread_id != expired.thread_id


async def test_processing_lock_serializes_same_session() -> None:
    store = InMemorySessionStore(ttl_seconds=3600)
    session = await store.get_or_create("conv-1", context(), AgentType.BOOKING)
    active = 0
    peak = 0

    async def work() -> None:
        nonlocal active, peak
        async with store.processing_lock(session):
            active += 1
            peak = max(peak, active)
            await asyncio.sleep(0.01)
            active -= 1

    await asyncio.gather(*(work() for _ in range(5)))

    assert peak == 1
