from typing import Any


class AgentException(Exception):
    def __init__(
        self,
        *,
        status_code: int,
        message: str,
        data: dict[str, Any] | None = None,
    ) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.message = message
        self.data = data
