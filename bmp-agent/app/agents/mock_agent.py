from typing import TypedDict

from langchain_core.runnables import RunnableConfig
from langgraph.graph import END, START, StateGraph

from app.agents.base import AgentMetadata, AgentResult
from app.memory.checkpoint import create_memory_checkpointer
from app.memory.session import Session


class MockState(TypedDict, total=False):
    message: str
    response: str
    turn: int


def _respond(state: MockState) -> MockState:
    turn = state.get("turn", 0) + 1
    return {
        "turn": turn,
        "response": f"Mock Agent received message {turn}.",
    }


class MockAgent:
    def __init__(self) -> None:
        builder = StateGraph(MockState)
        builder.add_node("respond", _respond)
        builder.add_edge(START, "respond")
        builder.add_edge("respond", END)
        self._graph = builder.compile(checkpointer=create_memory_checkpointer())

    async def process(self, session: Session, message: str) -> AgentResult:
        config = RunnableConfig(configurable={"thread_id": session.thread_id})
        input_state: MockState = {"message": message}
        state = await self._graph.ainvoke(input_state, config=config)
        turn = int(state["turn"])
        return AgentResult(
            response=str(state["response"]),
            actions=[],
            references=[],
            metadata=AgentMetadata(
                model="mock",
                tokens_used=0,
                duration_ms=0,
                tool_calls=[],
                turn=turn,
            ),
        )
