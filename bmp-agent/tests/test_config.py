import pytest
from pydantic import ValidationError

from app.core.config import AppSettings


def test_development_allows_static_context_without_external_services() -> None:
    settings = AppSettings(
        _env_file=None,
        app_env="development",
        app_debug=True,
        agent_context_mode="static",
        static_agent_context_token="local-token",
        database_url="",
        redis_url="",
        openai_api_key="",
    )

    assert settings.app_env == "development"
    assert settings.external_health_checks is False
    assert settings.database_url is None
    assert settings.redis_url is None
    assert settings.openai_api_key is None


@pytest.mark.parametrize(
    ("overrides", "error_text"),
    [
        (
            {
                "app_debug": True,
                "agent_context_mode": "signed",
                "agent_context_secret": "valid-secret-1234567890",
            },
            "debug mode",
        ),
        ({"app_debug": False, "agent_context_mode": "static"}, "static context"),
        (
            {
                "app_debug": False,
                "agent_context_mode": "signed",
                "agent_context_secret": "valid-secret-1234567890",
                "openai_api_key": "your_api_key_here",
            },
            "placeholder",
        ),
        (
            {
                "app_debug": False,
                "agent_context_mode": "signed",
                "agent_context_secret": None,
            },
            "agent_context_secret",
        ),
        (
            {
                "app_debug": False,
                "agent_context_mode": "signed",
                "agent_context_secret": "valid-secret-1234567890",
                "java_service_token": None,
            },
            "java_service_token",
        ),
    ],
)
def test_production_rejects_unsafe_configuration(
    overrides: dict[str, object], error_text: str
) -> None:
    with pytest.raises(ValidationError, match=error_text):
        AppSettings(_env_file=None, app_env="production", **overrides)


def test_session_ttl_must_be_positive() -> None:
    with pytest.raises(ValidationError):
        AppSettings(_env_file=None, session_ttl_seconds=0)
