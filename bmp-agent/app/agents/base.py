from enum import StrEnum
from typing import Any

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
