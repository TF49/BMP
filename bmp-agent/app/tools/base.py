"""Agent Tool 领域异常与 Java 错误码映射。

将 Java Tool 返回的 HTTP/业务错误码转换为 Python 领域异常，避免向模型或调用方暴露
内部堆栈。所有异常继承 :class:`AgentException`，其 ``status_code`` 用于 FastAPI 层
统一响应。
"""

from app.core.exceptions import AgentException


class ToolError(AgentException):
    """Java Tool 调用失败的领域异常基类。"""

    def __init__(
        self,
        message: str = "agent tool call failed",
        *,
        status_code: int = 502,
    ) -> None:
        super().__init__(status_code=status_code, message=message)


class ToolConfigurationError(ToolError):
    """Tool 客户端配置缺失（如未配置服务凭证）。"""

    def __init__(self, message: str = "agent tool client is not configured") -> None:
        super().__init__(status_code=503, message=message)


class ToolValidationError(ToolError):
    """请求参数未通过 Java Tool 校验（Java 400）。"""

    def __init__(self, message: str = "agent tool rejected the request parameters") -> None:
        super().__init__(status_code=400, message=message)


class ToolAuthError(ToolError):
    """服务身份或上下文签名校验失败（Java 401）。"""

    def __init__(self, message: str = "agent tool authentication failed") -> None:
        super().__init__(status_code=502, message=message)


class ToolPermissionError(ToolError):
    """当前用户无权访问目标资源（Java 403）。"""

    def __init__(self, message: str = "agent tool access is forbidden") -> None:
        super().__init__(status_code=403, message=message)


class ToolNotFoundError(ToolError):
    """目标资源不存在（Java 404）。"""

    def __init__(self, message: str = "requested resource was not found") -> None:
        super().__init__(status_code=404, message=message)


class ToolRateLimitedError(ToolError):
    """触发限流（Java 429）。"""

    def __init__(self, message: str = "agent tool rate limit exceeded") -> None:
        super().__init__(status_code=429, message=message)


class ToolUnavailableError(ToolError):
    """Tool 暂时不可用：网络瞬时错误、超时或 Java 5xx。"""

    def __init__(self, message: str = "agent tool is temporarily unavailable") -> None:
        super().__init__(status_code=503, message=message)


def error_for_status(status_code: int) -> ToolError:
    """根据下游返回的状态码构造对应领域异常。

    409 Conflict 表示下游仍在正常响应但存在冲突（如幂等 key 重复），归为
    ToolUnavailableError 而非 ToolValidationError，以便上层采用可重试策略。
    未显式映射的其他 4xx 归为校验错误，5xx 归为不可用错误。
    """
    mapping: dict[int, type[ToolError]] = {
        400: ToolValidationError,
        401: ToolAuthError,
        403: ToolPermissionError,
        404: ToolNotFoundError,
        409: ToolUnavailableError,  # Conflict — 幂等 key 重复或资源版本冲突，视为暂时不可用
        429: ToolRateLimitedError,
        503: ToolUnavailableError,
    }
    exc_type = mapping.get(status_code)
    if exc_type is not None:
        return exc_type()
    if 500 <= status_code < 600:
        return ToolUnavailableError()
    return ToolValidationError()
