from enum import StrEnum
from typing import Any, Protocol

from pydantic import BaseModel, ConfigDict


class AgentType(StrEnum):
    BOOKING = "booking"
    ANALYTICS = "analytics"
    SUPPORT = "support"


class AgentMetadata(BaseModel):
    model_config = ConfigDict(extra="forbid", frozen=True)

    model: str
    tokens_used: int
    duration_ms: int
    tool_calls: list[str]
    turn: int


class AgentResult(BaseModel):
    model_config = ConfigDict(extra="forbid", frozen=True)

    response: str
    type: str = "text"
    requires_action: bool = False
    actions: list[dict[str, Any]]
    references: list[dict[str, Any]]
    metadata: AgentMetadata


class Agent(Protocol):
    """Interface for all business agents — use this type in routes and tests.

    Concrete implementations (MockAgent, BookingAgent …) must satisfy
    this protocol without inheriting from it.
    """

    async def process(
        self, session: "Any", message: str, context_token: str | None = None
    ) -> AgentResult: ...

    async def delete_thread(self, thread_id: str) -> None: ...
