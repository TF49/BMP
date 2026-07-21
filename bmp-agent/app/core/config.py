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
    health_check_timeout_seconds: float = Field(default=3.0, gt=0, le=30)

    agent_context_mode: Literal["static", "signed"] = "static"
    static_agent_context_token: str | None = None
    static_agent_user_id: str = "local-user"
    static_agent_user_role: str = "MEMBER"
    static_agent_venue_id: str | None = None
    session_ttl_seconds: int = Field(default=3600, gt=0, le=2_592_000)

    # 会话/Checkpoint 存储后端：开发默认内存，集成环境切换 postgres 并需配置 database_url。
    agent_storage_backend: Literal["memory", "postgres"] = "memory"
    db_pool_size: int = Field(default=5, ge=1, le=100)
    db_max_overflow: int = Field(default=10, ge=0, le=100)

    # Java Agent Tool 集成：调用 Java 只读 Tool 接口的地址、服务凭证与超时。
    java_tool_base_url: str = "http://127.0.0.1:9090"
    java_service_token: str | None = None
    tool_connect_timeout_seconds: float = Field(default=2.0, gt=0, le=60)
    tool_read_timeout_seconds: float = Field(default=10.0, gt=0, le=120)
    tool_max_retries: int = Field(default=2, ge=0, le=5)

    # 进程内会话限流（第五防线）：每用户在固定窗口内的最大处理请求数。
    agent_rate_limit_max_requests: int = Field(default=20, ge=1, le=10_000)
    agent_rate_limit_window_seconds: int = Field(default=60, ge=1, le=3600)

    # HMAC-SHA256 签名密钥：Java 网关签发、Java Tool 与 Python 校验共用同一密钥。
    # signed 模式下必须通过环境变量注入，禁止使用占位值；static 模式下可为空。
    agent_context_secret: str | None = None

    @field_validator(
        "openai_api_key",
        "database_url",
        "redis_url",
        "static_agent_context_token",
        "static_agent_venue_id",
        "java_service_token",
        "agent_context_secret",
        mode="before",
    )
    @classmethod
    def empty_string_to_none(cls, value: object) -> object:
        if isinstance(value, str) and not value.strip():
            return None
        return value

    @model_validator(mode="after")
    def validate_production_safety(self) -> Self:
        if self.agent_storage_backend == "postgres" and not self.database_url:
            raise ValueError("database_url is required when agent_storage_backend is postgres")
        if self.agent_context_mode == "signed" and not self.agent_context_secret:
            raise ValueError("agent_context_secret is required when agent_context_mode is signed")
        if self.app_env != "production":
            return self
        if self.app_debug:
            raise ValueError("debug mode is forbidden in production")
        if self.agent_context_mode == "static":
            raise ValueError("static context is forbidden in production")
        if self.openai_api_key in {"your_api_key_here", "change-me", "changeme"}:
            raise ValueError("placeholder API keys are forbidden in production")
        if not self.java_service_token:
            raise ValueError("java_service_token is required in production")
        return self


@lru_cache(maxsize=1)
def get_settings() -> AppSettings:
    return AppSettings()
