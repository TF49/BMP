from collections.abc import Sequence
from typing import Any, Protocol, cast

from openai import APITimeoutError, AsyncOpenAI
from pydantic import BaseModel, ValidationError

from app.core.config import AppSettings
from app.llm.base import (
    ModelConfigurationError,
    ModelProviderError,
    ModelResponseError,
    ModelTimeoutError,
)
from app.llm.schemas import ChatMessage, ChatResult


class _Responses(Protocol):
    async def create(self, **kwargs: Any) -> Any: ...


class OpenAIClient(Protocol):
    @property
    def responses(self) -> _Responses: ...


class OpenAICompatibleChatModel:
    def __init__(self, settings: AppSettings, client: OpenAIClient | None = None) -> None:
        if client is None and not settings.openai_api_key:
            raise ModelConfigurationError("model API key is not configured")
        self._settings = settings
        self._client = client or cast(
            OpenAIClient,
            AsyncOpenAI(
                api_key=settings.openai_api_key,
                base_url=settings.openai_base_url,
                timeout=settings.llm_timeout_seconds,
            ),
        )

    async def generate(self, messages: Sequence[ChatMessage]) -> ChatResult:
        return await self._complete(messages)

    async def generate_structured[T: BaseModel](
        self,
        messages: Sequence[ChatMessage],
        response_model: type[T],
    ) -> T:
        # Use json_schema (not json_object) so the model sees the exact
        # Pydantic schema and strict mode rejects unknown / missing fields.
        schema_format: dict[str, object] = {
            "type": "json_schema",
            "name": response_model.__name__,
            "schema": response_model.model_json_schema(),
            "strict": True,
        }
        result = await self._complete(messages, response_format=schema_format)
        try:
            return response_model.model_validate_json(result.content)
        except (ValidationError, ValueError) as exc:
            raise ModelResponseError("structured response validation failed") from exc

    async def health_check(self) -> bool:
        result = await self.generate([ChatMessage(role="user", content="Reply with OK.")])
        return bool(result.content)

    async def _complete(
        self,
        messages: Sequence[ChatMessage],
        *,
        response_format: dict[str, str] | None = None,
    ) -> ChatResult:
        request: dict[str, Any] = {
            "model": self._settings.openai_model,
            "input": [message.model_dump() for message in messages],
        }
        if response_format is not None:
            request["text"] = {"format": response_format}

        try:
            response = await self._client.responses.create(**request)
        except (APITimeoutError, TimeoutError) as exc:
            raise ModelTimeoutError() from exc
        except Exception as exc:
            raise ModelProviderError() from exc

        content = getattr(response, "output_text", None)
        if not isinstance(content, str) or not content.strip():
            raise ModelResponseError("model returned an empty response")

        usage = getattr(response, "usage", None)
        return ChatResult(
            content=content.strip(),
            model=str(getattr(response, "model", self._settings.openai_model)),
            input_tokens=int(getattr(usage, "input_tokens", 0) or 0),
            output_tokens=int(getattr(usage, "output_tokens", 0) or 0),
        )
