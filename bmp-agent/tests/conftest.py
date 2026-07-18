from collections.abc import Iterator
from typing import TYPE_CHECKING

import pytest

if TYPE_CHECKING:
    from app.core.config import AppSettings


@pytest.fixture
def test_settings() -> "AppSettings":
    from app.core.config import AppSettings

    return AppSettings(
        _env_file=None,
        app_env="test",
        app_debug=False,
        agent_context_mode="static",
        static_agent_context_token="test-context-token",
        static_agent_user_id="test-user",
        static_agent_user_role="MEMBER",
    )


@pytest.fixture(autouse=True)
def reset_trace_context() -> Iterator[None]:
    yield
    try:
        from app.observability.tracing import clear_trace_id

        clear_trace_id()
    except ImportError:
        pass
