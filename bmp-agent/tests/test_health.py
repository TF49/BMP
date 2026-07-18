import asyncio

from app.core.config import AppSettings
from app.core.health import DependencyHealthService


class StubHealthService(DependencyHealthService):
    def __init__(self, settings: AppSettings) -> None:
        super().__init__(settings)
        self.calls: list[str] = []

    async def _check_llm(self) -> bool:
        self.calls.append("llm")
        return True

    async def _check_database(self) -> bool:
        self.calls.append("database")
        return False

    async def _check_redis(self) -> bool:
        self.calls.append("redis")
        return True


class TimeoutHealthService(StubHealthService):
    async def _check_llm(self) -> bool:
        await asyncio.sleep(1)
        return True


def health_settings(**overrides: object) -> AppSettings:
    defaults: dict[str, object] = {
        "_env_file": None,
        "app_env": "test",
        "external_health_checks": True,
        "health_check_timeout_seconds": 0.01,
        "openai_api_key": "test-key",
        "database_url": "postgresql://test",
        "redis_url": "redis://test",
    }
    defaults.update(overrides)
    return AppSettings(**defaults)


async def test_enabled_health_checks_report_each_configured_dependency() -> None:
    service = StubHealthService(health_settings())

    statuses = await service.check()

    assert statuses == {
        "app": "healthy",
        "llm": "healthy",
        "database": "unhealthy",
        "redis": "healthy",
    }
    assert sorted(service.calls) == ["database", "llm", "redis"]


async def test_unconfigured_or_disabled_health_checks_do_not_call_dependencies() -> None:
    unconfigured = StubHealthService(
        health_settings(openai_api_key="", database_url="", redis_url="")
    )
    disabled = StubHealthService(health_settings(external_health_checks=False))

    assert await unconfigured.check() == {
        "app": "healthy",
        "llm": "disabled",
        "database": "disabled",
        "redis": "disabled",
    }
    assert await disabled.check() == {
        "app": "healthy",
        "llm": "disabled",
        "database": "disabled",
        "redis": "disabled",
    }
    assert unconfigured.calls == []
    assert disabled.calls == []


async def test_health_check_timeout_is_reported_as_unhealthy() -> None:
    service = TimeoutHealthService(health_settings(database_url="", redis_url=""))

    statuses = await service.check()

    assert statuses["llm"] == "unhealthy"
