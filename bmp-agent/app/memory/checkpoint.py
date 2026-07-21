from typing import Any, Protocol

from langgraph.checkpoint.memory import InMemorySaver


class AgentCheckpointStore(Protocol):
    async def load_state(self, thread_id: str) -> dict[str, Any] | None: ...

    async def save_state(self, thread_id: str, state: dict[str, Any]) -> None: ...

    async def delete_thread(self, thread_id: str) -> None: ...


def create_memory_checkpointer() -> InMemorySaver:
    return InMemorySaver()
