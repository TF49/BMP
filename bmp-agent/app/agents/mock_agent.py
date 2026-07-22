from typing import TypedDict, cast

from langchain_core.runnables import RunnableConfig
from langgraph.graph import END, START, StateGraph

from app.agents.base import AgentMetadata, AgentResult
from app.memory.checkpoint import AgentCheckpointStore, create_memory_checkpointer
from app.memory.session import Session
from app.observability.tracing import get_trace_id
from app.tools.court import CourtTool
from app.tools.venue import VenueTool


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
    def __init__(
        self,
        *,
        checkpoint_store: AgentCheckpointStore | None = None,
        venue_tool: VenueTool | None = None,
        court_tool: CourtTool | None = None,
    ) -> None:
        self._persistent_checkpoint_store = checkpoint_store
        self._venue_tool = venue_tool
        self._court_tool = court_tool
        self._checkpointer = create_memory_checkpointer()
        builder = StateGraph(MockState)
        builder.add_node("respond", _respond)
        builder.add_edge(START, "respond")
        builder.add_edge("respond", END)
        self._graph = builder.compile(checkpointer=self._checkpointer)

    async def delete_thread(self, thread_id: str) -> None:
        await self._checkpointer.adelete_thread(thread_id)
        if self._persistent_checkpoint_store is not None:
            await self._persistent_checkpoint_store.delete_thread(thread_id)

    async def process(
        self, session: Session, message: str, context_token: str | None = None
    ) -> AgentResult:
        config = RunnableConfig(configurable={"thread_id": session.thread_id})
        input_state: MockState = {"message": message}
        if self._persistent_checkpoint_store is not None:
            persisted = await self._persistent_checkpoint_store.load_state(session.thread_id)
            if persisted is not None:
                input_state.update(cast(MockState, persisted))
                input_state["message"] = message
        state = await self._graph.ainvoke(input_state, config=config)
        references: list[dict[str, object]] = []
        tool_calls: list[str] = []
        response = str(state["response"])
        if message == "tool:list_venues" and self._venue_tool is not None:
            venues = await self._venue_tool.list_venues(
                context_token=context_token or "",
                trace_id=get_trace_id(),
                status=1,
            )
            references = [venue.model_dump(mode="json") for venue in venues]
            tool_calls = ["list_venues"]
            response = f"Found {len(venues)} available venues."
            state["response"] = response
        if self._persistent_checkpoint_store is not None:
            await self._persistent_checkpoint_store.save_state(session.thread_id, dict(state))
        turn = int(state["turn"])
        return AgentResult(
            response=response,
            actions=[],
            references=references,
            metadata=AgentMetadata(
                model="mock",
                tokens_used=0,
                duration_ms=0,
                tool_calls=tool_calls,
                turn=turn,
            ),
        )
