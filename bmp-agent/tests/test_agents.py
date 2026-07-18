from datetime import UTC, datetime, timedelta

from app.agents.base import AgentType
from app.agents.mock_agent import MockAgent
from app.memory.session import Session


def session(conversation_id: str, user_id: str) -> Session:
    created_at = datetime.now(UTC)
    return Session(
        conversation_id=conversation_id,
        user_id=user_id,
        agent_type=AgentType.BOOKING,
        thread_id=f"thread_{conversation_id}_{user_id}",
        created_at=created_at,
        expires_at=created_at + timedelta(hours=1),
    )


async def test_mock_agent_persists_turns_in_langgraph_checkpoint() -> None:
    agent = MockAgent()
    current_session = session("conv-1", "user-a")

    first = await agent.process(current_session, "first")
    second = await agent.process(current_session, "second")

    assert first.metadata.turn == 1
    assert second.metadata.turn == 2
    assert second.response == "Mock Agent received message 2."
    assert second.metadata.model == "mock"
    assert second.metadata.tokens_used == 0
    assert second.metadata.tool_calls == []


async def test_mock_agent_isolates_checkpoints_by_thread() -> None:
    agent = MockAgent()

    first_user = await agent.process(session("conv-1", "user-a"), "hello")
    second_user = await agent.process(session("conv-2", "user-b"), "hello")

    assert first_user.metadata.turn == 1
    assert second_user.metadata.turn == 1
