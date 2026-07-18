import asyncio
from collections.abc import Awaitable, Callable
from typing import Literal, Protocol

import asyncpg
import redis.asyncio as redis_async

from app.core.config import AppSettings
from app.llm.openai_adapter import OpenAICompatibleChatModel

type ServiceStatus = Literal["healthy", "unhealthy", "disabled"]


class HealthService(Protocol):
    async def check(self) -> dict[str, ServiceStatus]: ...


class DependencyHealthService:
    def __init__(self, settings: AppSettings) -> None:
        self._settings = settings

    async def check(self) -> dict[str, ServiceStatus]:
        statuses: dict[str, ServiceStatus] = {
            "app": "healthy",
            "llm": "disabled",
            "database": "disabled",
            "redis": "disabled",
        }
        if not self._settings.external_health_checks:
            return statuses

        configured_checks: list[tuple[str, Callable[[], Awaitable[bool]]]] = []
        if self._settings.openai_api_key:
            configured_checks.append(("llm", self._check_llm))
        if self._settings.database_url:
            configured_checks.append(("database", self._check_database))
        if self._settings.redis_url:
            configured_checks.append(("redis", self._check_redis))

        tasks = {
            name: asyncio.create_task(self._status(check)) for name, check in configured_checks
        }
        for name, task in tasks.items():
            statuses[name] = await task
        return statuses

    async def _status(self, check: Callable[[], Awaitable[bool]]) -> ServiceStatus:
        try:
            healthy = await asyncio.wait_for(
                check(),
                timeout=self._settings.health_check_timeout_seconds,
            )
        except Exception:
            return "unhealthy"
        return "healthy" if healthy else "unhealthy"

    async def _check_llm(self) -> bool:
        model = OpenAICompatibleChatModel(self._settings)
        return await model.health_check()

    async def _check_database(self) -> bool:
        database_url = self._settings.database_url
        if database_url is None:
            return False
        connection = await asyncpg.connect(
            database_url,
            timeout=self._settings.health_check_timeout_seconds,
        )
        try:
            return bool(await connection.fetchval("SELECT 1") == 1)
        finally:
            await connection.close()

    async def _check_redis(self) -> bool:
        redis_url = self._settings.redis_url
        if redis_url is None:
            return False
        client = redis_async.from_url(
            redis_url,
            socket_connect_timeout=self._settings.health_check_timeout_seconds,
            socket_timeout=self._settings.health_check_timeout_seconds,
        )
        try:
            return bool(await client.ping())
        finally:
            await client.aclose()
