from functools import lru_cache
from typing import Literal, Self

from pydantic import Field, field_validator, model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class AppSettings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
        case_sensitive=False,
    )

    app_name: str = "bmp-agent"
    app_env: Literal["development", "test", "production"] = "development"
    app_port: int = Field(default=8000, ge=1, le=65535)
    app_debug: bool = False
    log_level: str = "INFO"
    log_format: Literal["json", "text"] = "json"

    openai_api_key: str | None = None
    openai_base_url: str = "https://api.openai.com/v1"
    openai_model: str = "your_model_name"
    llm_timeout_seconds: float = Field(default=30.0, gt=0, le=300)

    database_url: str | None = None
    redis_url: str | None = None
    external_health_checks: bool = False

    agent_context_mode: Literal["static", "signed"] = "static"
    static_agent_context_token: str | None = None
    static_agent_user_id: str = "local-user"
    static_agent_user_role: str = "MEMBER"
    static_agent_venue_id: str | None = None
    session_ttl_seconds: int = Field(default=3600, gt=0, le=2_592_000)

    @field_validator(
        "openai_api_key",
        "database_url",
        "redis_url",
        "static_agent_context_token",
        "static_agent_venue_id",
        mode="before",
    )
    @classmethod
    def empty_string_to_none(cls, value: object) -> object:
        if isinstance(value, str) and not value.strip():
            return None
        return value

    @model_validator(mode="after")
    def validate_production_safety(self) -> Self:
        if self.app_env != "production":
            return self
        if self.app_debug:
            raise ValueError("debug mode is forbidden in production")
        if self.agent_context_mode == "static":
            raise ValueError("static context is forbidden in production")
        if self.openai_api_key in {"your_api_key_here", "change-me", "changeme"}:
            raise ValueError("placeholder API keys are forbidden in production")
        return self


@lru_cache(maxsize=1)
def get_settings() -> AppSettings:
    return AppSettings()
