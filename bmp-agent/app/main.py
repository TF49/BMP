import logging
from collections.abc import AsyncIterator, Mapping
from contextlib import asynccontextmanager
from typing import Any

import uvicorn
from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.agents.mock_agent import MockAgent
from app.api.routes import router
from app.core.config import AppSettings, get_settings
from app.core.exceptions import AgentException
from app.core.health import DependencyHealthService
from app.core.security import build_context_verifier
from app.memory.session import InMemorySessionStore
from app.observability.logging import configure_logging
from app.observability.tracing import TraceIdMiddleware, get_trace_id

logger = logging.getLogger("bmp_agent")


def _error_response(
    status_code: int,
    message: str,
    data: Any = None,
    headers: Mapping[str, str] | None = None,
) -> JSONResponse:
    return JSONResponse(
        status_code=status_code,
        headers=headers,
        content={
            "code": status_code,
            "message": message,
            "data": data,
            "trace_id": get_trace_id(),
        },
    )


def _validation_errors(exc: RequestValidationError) -> list[dict[str, str]]:
    errors: list[dict[str, str]] = []
    for error in exc.errors():
        location = ".".join(str(part) for part in error["loc"])
        errors.append({"field": location, "message": str(error["msg"])})
    return errors


def create_app(settings: AppSettings | None = None) -> FastAPI:
    resolved_settings = settings or get_settings()

    @asynccontextmanager
    async def lifespan(_: FastAPI) -> AsyncIterator[None]:
        configure_logging(resolved_settings.log_level, resolved_settings.log_format)
        logger.info("application started", extra={"event": "app_started"})
        yield
        logger.info("application stopped", extra={"event": "app_stopped"})

    app = FastAPI(title=resolved_settings.app_name, lifespan=lifespan)
    app.state.settings = resolved_settings
    app.state.context_verifier = build_context_verifier(resolved_settings)
    app.state.health_service = DependencyHealthService(resolved_settings)
    mock_agent = MockAgent()
    app.state.mock_agent = mock_agent
    app.state.session_store = InMemorySessionStore(
        ttl_seconds=resolved_settings.session_ttl_seconds,
        on_expire=lambda session: mock_agent.delete_thread(session.thread_id),
    )
    app.add_middleware(TraceIdMiddleware)

    @app.exception_handler(AgentException)
    async def handle_agent_exception(_: Request, exc: AgentException) -> JSONResponse:
        return _error_response(exc.status_code, exc.message, exc.data)

    @app.exception_handler(RequestValidationError)
    async def handle_validation_error(_: Request, exc: RequestValidationError) -> JSONResponse:
        return _error_response(
            400,
            "request validation failed",
            {"errors": _validation_errors(exc)},
        )

    @app.exception_handler(StarletteHTTPException)
    async def handle_http_exception(_: Request, exc: StarletteHTTPException) -> JSONResponse:
        messages = {
            404: "resource not found",
            405: "method not allowed",
        }
        message = messages.get(exc.status_code, "request failed")
        return _error_response(exc.status_code, message, headers=exc.headers)

    @app.exception_handler(Exception)
    async def handle_unexpected_error(_: Request, exc: Exception) -> JSONResponse:
        logger.error(
            "unhandled request error",
            extra={"trace_id": get_trace_id(), "exception_type": type(exc).__name__},
        )
        return _error_response(500, "internal server error")

    app.include_router(router)
    return app


app = create_app()


if __name__ == "__main__":
    settings = get_settings()
    uvicorn.run("app.main:app", host="0.0.0.0", port=settings.app_port)
