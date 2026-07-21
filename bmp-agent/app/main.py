import asyncio
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
from app.core.ratelimit import InMemoryRateLimiter
from app.core.replay import InMemoryReplayGuard, RedisReplayGuard
from app.core.security import build_context_verifier
from app.memory.session import InMemorySessionStore, Session, SessionStore
from app.observability.logging import configure_logging
from app.observability.tracing import TraceIdMiddleware, get_trace_id
from app.tools.client import JavaToolClient
from app.tools.court import CourtTool
from app.tools.venue import VenueTool

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


def _build_agent_runtime(
    settings: AppSettings,
    tool_client: JavaToolClient | None = None,
) -> tuple[MockAgent, SessionStore, Any | None, JavaToolClient | None]:
    """Build the agent and its session/checkpoint stores as one runtime unit."""

    engine = None
    session_factory = None
    checkpoint_store = None
    if settings.agent_storage_backend == "postgres":
        from app.memory.postgres import (
            PostgresCheckpointStore,
            create_engine,
            create_session_factory,
        )

        engine = create_engine(
            str(settings.database_url),
            pool_size=settings.db_pool_size,
            max_overflow=settings.db_max_overflow,
        )
        session_factory = create_session_factory(engine)
        checkpoint_store = PostgresCheckpointStore(session_factory)

    runtime_tool_client = tool_client
    if runtime_tool_client is None and settings.java_service_token:
        runtime_tool_client = JavaToolClient(settings)
    mock_agent = MockAgent(
        checkpoint_store=checkpoint_store,
        venue_tool=VenueTool(runtime_tool_client) if runtime_tool_client is not None else None,
        court_tool=CourtTool(runtime_tool_client) if runtime_tool_client is not None else None,
    )

    async def on_expire(session: Session) -> None:
        # D-4: 基于注册表路由不同 agent 类型的清除逻辑，为真实 Agent 预留扩展点。
        agent_registry = {
            "booking": mock_agent,
            "analytics": mock_agent,
            "support": mock_agent,
        }
        target_agent = agent_registry.get(session.agent_type)
        if target_agent is not None:
            await target_agent.delete_thread(session.thread_id)

    if settings.agent_storage_backend == "postgres":
        from app.memory.postgres import PostgresSessionStore

        if session_factory is None:
            raise RuntimeError("postgres session factory was not initialized")
        store: SessionStore = PostgresSessionStore(
            session_factory,
            ttl_seconds=settings.session_ttl_seconds,
            on_expire=on_expire,
        )
        return mock_agent, store, engine, runtime_tool_client

    store = InMemorySessionStore(
        ttl_seconds=settings.session_ttl_seconds,
        on_expire=on_expire,
    )
    return mock_agent, store, None, runtime_tool_client


def create_app(
    settings: AppSettings | None = None,
    *,
    tool_client: JavaToolClient | None = None,
) -> FastAPI:
    resolved_settings = settings or get_settings()

    @asynccontextmanager
    async def lifespan(app_inst: FastAPI) -> AsyncIterator[None]:
        configure_logging(resolved_settings.log_level, resolved_settings.log_format)
        logger.info("application started", extra={"event": "app_started"})

        # D-2: 注册后台定时清理过期会话的任务
        purge_task = None
        store = app_inst.state.session_store
        if hasattr(store, "purge_expired_sessions"):
            async def purge_loop() -> None:
                try:
                    while True:
                        await asyncio.sleep(60)
                        await store.purge_expired_sessions()
                except asyncio.CancelledError:
                    pass
                except Exception as e:
                    logger.error("session purge background task failed", exc_info=e)

            purge_task = asyncio.create_task(purge_loop())

        yield

        if purge_task is not None:
            purge_task.cancel()
            try:
                await purge_task
            except asyncio.CancelledError:
                pass

        # D-1: 优雅关闭 PostgreSQL 引擎连接池
        if hasattr(app_inst.state, "db_engine") and app_inst.state.db_engine is not None:
            logger.info("disposing database engine")
            await app_inst.state.db_engine.dispose()

        # D-3: 优雅关闭共享 Redis 客户端
        if hasattr(app_inst.state, "redis_client") and app_inst.state.redis_client is not None:
            logger.info("closing shared redis client")
            await app_inst.state.redis_client.aclose()

        if app_inst.state.java_tool_client is not None:
            logger.info("closing shared Java Tool client")
            await app_inst.state.java_tool_client.aclose()

        logger.info("application stopped", extra={"event": "app_stopped"})

    app = FastAPI(title=resolved_settings.app_name, lifespan=lifespan)
    app.state.settings = resolved_settings
    app.state.context_verifier = build_context_verifier(resolved_settings)

    mock_agent, store, engine, runtime_tool_client = _build_agent_runtime(
        resolved_settings, tool_client
    )
    app.state.mock_agent = mock_agent
    app.state.session_store = store
    app.state.db_engine = engine
    app.state.java_tool_client = runtime_tool_client

    # D-3: 初始化共享 Redis 客户端
    redis_client = None
    if resolved_settings.redis_url:
        import redis.asyncio as redis_async
        redis_client = redis_async.from_url(
            resolved_settings.redis_url,
            socket_connect_timeout=resolved_settings.health_check_timeout_seconds,
            socket_timeout=resolved_settings.health_check_timeout_seconds,
        )
    app.state.redis_client = redis_client
    app.state.replay_guard = (
        RedisReplayGuard(redis_client) if redis_client is not None else InMemoryReplayGuard()
    )

    # D-3: 将共享连接池和 Redis 客户端注入健康检查服务
    app.state.health_service = DependencyHealthService(
        resolved_settings,
        db_engine=engine,
        redis_client=redis_client,
    )

    app.state.rate_limiter = InMemoryRateLimiter(
        max_requests=resolved_settings.agent_rate_limit_max_requests,
        window_seconds=resolved_settings.agent_rate_limit_window_seconds,
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
