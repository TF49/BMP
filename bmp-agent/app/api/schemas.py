from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict

type ServiceStatus = Literal["healthy", "unhealthy", "disabled"]


class StrictModel(BaseModel):
    model_config = ConfigDict(extra="forbid")


class ApiResponse[T](StrictModel):
    code: int
    message: str
    data: T | None
    trace_id: str


class HealthData(StrictModel):
    status: Literal["healthy", "degraded"]
    timestamp: datetime
    services: dict[str, ServiceStatus]
