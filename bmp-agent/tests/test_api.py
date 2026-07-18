import re

from fastapi import FastAPI
from httpx import ASGITransport, AsyncClient

from app.core.config import AppSettings
from app.core.exceptions import AgentException
from app.main import create_app


async def test_health_uses_envelope_and_reuses_valid_trace_id(
    test_settings: AppSettings,
) -> None:
    app = create_app(test_settings)

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/health", headers={"X-Agent-Trace-Id": "trace_valid-123"})

    assert response.status_code == 200
    assert response.headers["X-Agent-Trace-Id"] == "trace_valid-123"
    assert response.json() == {
        "code": 200,
        "message": "success",
        "data": {
            "status": "healthy",
            "timestamp": response.json()["data"]["timestamp"],
            "services": {
                "app": "healthy",
                "llm": "disabled",
                "database": "disabled",
                "redis": "disabled",
            },
        },
        "trace_id": "trace_valid-123",
    }


async def test_health_replaces_invalid_trace_id(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/health", headers={"X-Agent-Trace-Id": "invalid trace!"})

    generated = response.json()["trace_id"]
    assert response.status_code == 200
    assert generated != "invalid trace!"
    assert re.fullmatch(r"[0-9a-f]{32}", generated)
    assert response.headers["X-Agent-Trace-Id"] == generated


async def test_domain_exception_uses_sanitized_envelope(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    @app.get("/domain-error")
    async def domain_error() -> None:
        raise AgentException(status_code=503, message="dependency unavailable")

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/domain-error")

    assert response.status_code == 503
    assert response.json()["code"] == 503
    assert response.json()["message"] == "dependency unavailable"
    assert response.json()["data"] is None
    assert response.json()["trace_id"]


async def test_validation_error_is_mapped_to_400(test_settings: AppSettings) -> None:
    app = create_app(test_settings)

    @app.get("/validated")
    async def validated(limit: int) -> dict[str, int]:
        return {"limit": limit}

    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as client:
        response = await client.get("/validated", params={"limit": "not-a-number"})

    assert response.status_code == 400
    assert response.json()["code"] == 400
    assert response.json()["message"] == "request validation failed"
    assert response.json()["data"]["errors"][0]["field"] == "query.limit"


async def test_unexpected_exception_does_not_expose_details(test_settings: AppSettings) -> None:
    app: FastAPI = create_app(test_settings)

    @app.get("/unexpected-error")
    async def unexpected_error() -> None:
        raise RuntimeError("secret-token-value")

    async with AsyncClient(
        transport=ASGITransport(app=app, raise_app_exceptions=False),
        base_url="http://test",
    ) as client:
        response = await client.get("/unexpected-error")

    body = response.json()
    assert response.status_code == 500
    assert body["code"] == 500
    assert body["message"] == "internal server error"
    assert "secret-token-value" not in response.text
