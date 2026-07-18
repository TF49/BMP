from datetime import UTC, datetime

from fastapi import APIRouter, Request

from app.api.schemas import ApiResponse, HealthData, ServiceStatus
from app.observability.tracing import get_trace_id

router = APIRouter()


@router.get("/health", response_model=ApiResponse[HealthData])
async def health(_: Request) -> ApiResponse[HealthData]:
    services: dict[str, ServiceStatus] = {
        "app": "healthy",
        "llm": "disabled",
        "database": "disabled",
        "redis": "disabled",
    }
    data = HealthData(status="healthy", timestamp=datetime.now(UTC), services=services)
    return ApiResponse(code=200, message="success", data=data, trace_id=get_trace_id())
