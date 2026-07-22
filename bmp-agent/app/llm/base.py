from collections.abc import AsyncIterator, Sequence
from typing import Protocol

from pydantic import BaseModel

from app.core.exceptions import AgentException
from app.llm.schemas import ChatMessage, ChatResult


class ModelConfigurationError(AgentException):
    def __init__(self, message: str) -> None:
        super().__init__(status_code=503, message=message)


class ModelTimeoutError(AgentException):
    def __init__(self, message: str = "model request timed out") -> None:
        super().__init__(status_code=504, message=message)


class ModelProviderError(AgentException):
    def __init__(self, message: str = "model provider request failed") -> None:
        super().__init__(status_code=502, message=message)


class ModelResponseError(AgentException):
    def __init__(self, message: str) -> None:
        super().__init__(status_code=502, message=message)


class ChatModel(Protocol):
    async def generate(self, messages: Sequence[ChatMessage]) -> ChatResult: ...

    async def generate_structured[T: BaseModel](
        self,
        messages: Sequence[ChatMessage],
        response_model: type[T],
    ) -> T: ...

    def generate_stream(self, messages: Sequence[ChatMessage]) -> AsyncIterator[str]: ...

    async def health_check(self) -> bool: ...
