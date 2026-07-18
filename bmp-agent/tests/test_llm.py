import os
from types import SimpleNamespace
from typing import Any

import httpx
import pytest
from openai import APITimeoutError
from pydantic import BaseModel

from app.core.config import AppSettings
from app.llm.base import (
    ModelConfigurationError,
    ModelProviderError,
    ModelResponseError,
    ModelTimeoutError,
)
from app.llm.openai_adapter import OpenAICompatibleChatModel
from app.llm.schemas import ChatMessage


class FakeCompletions:
    def __init__(self, result: Any = None, error: Exception | None = None) -> None:
        self.result = result
        self.error = error
        self.calls: list[dict[str, Any]] = []

    async def create(self, **kwargs: Any) -> Any:
        self.calls.append(kwargs)
        if self.error is not None:
            raise self.error
        return self.result


class FakeClient:
    def __init__(self, completions: FakeCompletions) -> None:
        self.chat = SimpleNamespace(completions=completions)


def completion(content: str | None, model: str = "test-model") -> Any:
    return SimpleNamespace(
        choices=[SimpleNamespace(message=SimpleNamespace(content=content))],
        model=model,
        usage=SimpleNamespace(prompt_tokens=4, completion_tokens=2),
    )


def model_settings() -> AppSettings:
    return AppSettings(
        _env_file=None,
        app_env="test",
        openai_api_key="test-key",
        openai_base_url="https://model.example/v1",
        openai_model="test-model",
        llm_timeout_seconds=12,
    )


async def test_generate_returns_normalized_chat_result() -> None:
    completions = FakeCompletions(completion("hello"))
    model = OpenAICompatibleChatModel(model_settings(), client=FakeClient(completions))

    result = await model.generate([ChatMessage(role="user", content="hi")])

    assert result.content == "hello"
    assert result.model == "test-model"
    assert result.input_tokens == 4
    assert result.output_tokens == 2
    assert completions.calls == [
        {
            "model": "test-model",
            "messages": [{"role": "user", "content": "hi"}],
        }
    ]


async def test_generate_structured_validates_pydantic_model() -> None:
    class Intent(BaseModel):
        venue: str
        hour: int

    completions = FakeCompletions(completion('{"venue":"north","hour":15}'))
    model = OpenAICompatibleChatModel(model_settings(), client=FakeClient(completions))

    result = await model.generate_structured(
        [ChatMessage(role="user", content="book north at 3pm")], Intent
    )

    assert result == Intent(venue="north", hour=15)
    assert completions.calls[0]["response_format"] == {"type": "json_object"}


@pytest.mark.parametrize("content", [None, "", "   "])
async def test_generate_rejects_empty_content(content: str | None) -> None:
    model = OpenAICompatibleChatModel(
        model_settings(), client=FakeClient(FakeCompletions(completion(content)))
    )

    with pytest.raises(ModelResponseError, match="empty response"):
        await model.generate([ChatMessage(role="user", content="hi")])


async def test_generate_structured_rejects_invalid_json() -> None:
    model = OpenAICompatibleChatModel(
        model_settings(), client=FakeClient(FakeCompletions(completion("not-json")))
    )

    with pytest.raises(ModelResponseError, match="structured response validation failed"):
        await model.generate_structured(
            [ChatMessage(role="user", content="return json")], SimpleSchema
        )


async def test_timeout_is_converted_without_provider_details() -> None:
    timeout = APITimeoutError(request=httpx.Request("POST", "https://model.example/v1"))
    model = OpenAICompatibleChatModel(
        model_settings(), client=FakeClient(FakeCompletions(error=timeout))
    )

    with pytest.raises(ModelTimeoutError, match="model request timed out"):
        await model.generate([ChatMessage(role="user", content="hi")])


async def test_provider_error_is_sanitized() -> None:
    model = OpenAICompatibleChatModel(
        model_settings(),
        client=FakeClient(FakeCompletions(error=RuntimeError("secret provider detail"))),
    )

    with pytest.raises(ModelProviderError) as exc_info:
        await model.generate([ChatMessage(role="user", content="hi")])

    assert str(exc_info.value) == "model provider request failed"
    assert "secret provider detail" not in str(exc_info.value)


def test_missing_api_key_fails_before_network_client_is_created() -> None:
    settings = AppSettings(_env_file=None, app_env="test", openai_api_key="")

    with pytest.raises(ModelConfigurationError, match="API key is not configured"):
        OpenAICompatibleChatModel(settings)


async def test_health_check_uses_small_text_request() -> None:
    completions = FakeCompletions(completion("OK"))
    model = OpenAICompatibleChatModel(model_settings(), client=FakeClient(completions))

    assert await model.health_check() is True
    assert completions.calls[0]["messages"] == [{"role": "user", "content": "Reply with OK."}]


class SimpleSchema(BaseModel):
    value: str


@pytest.mark.live_llm
async def test_live_model_connection() -> None:
    api_key = os.getenv("OPENAI_API_KEY")
    model_name = os.getenv("OPENAI_MODEL")
    if not api_key or not model_name:
        pytest.skip("OPENAI_API_KEY and OPENAI_MODEL are required")

    settings = AppSettings(
        _env_file=None,
        app_env="test",
        openai_api_key=api_key,
        openai_base_url=os.getenv("OPENAI_BASE_URL", "https://api.openai.com/v1"),
        openai_model=model_name,
    )
    model = OpenAICompatibleChatModel(settings)

    assert await model.health_check() is True
