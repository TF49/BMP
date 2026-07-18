from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.agents.base import AgentType
from app.core.health import ServiceStatus


class StrictModel(BaseModel):
    model_config = ConfigDict(extra="forbid", str_strip_whitespace=True)


class ApiResponse[T](StrictModel):
    code: int
    message: str
    data: T | None
    trace_id: str


class HealthData(StrictModel):
    status: Literal["healthy", "degraded"]
    timestamp: datetime
    services: dict[str, ServiceStatus]


class MessageInput(StrictModel):
    content: str = Field(min_length=1, max_length=2000)
    message_id: str | None = Field(default=None, min_length=1, max_length=64)


class ProcessRequest(StrictModel):
    conversation_id: str = Field(min_length=1, max_length=64)
    agent_type: AgentType
    message: MessageInput
