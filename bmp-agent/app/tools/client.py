"""调用 Java 只读 Tool 接口的共享 HTTPX 客户端。

职责：
- 复用单个 :class:`httpx.AsyncClient`（连接池 + 统一超时）。
- 自动透传服务凭证、签名上下文、TraceId 与可选幂等键。
- 解析 Java ``{code, message, data}`` 响应信封，并把错误码映射为领域异常。
- 仅对明确的网络瞬时错误（连接/读取超时、连接失败）重试；写操作默认不重试。
"""

import logging
from typing import Any

import httpx

from app.core.config import AppSettings
from app.tools.base import (
    ToolConfigurationError,
    ToolError,
    ToolUnavailableError,
    error_for_status,
)
from app.tools.circuit_breaker import CircuitBreaker

logger = logging.getLogger("bmp_agent.tools")

SERVICE_TOKEN_HEADER = "X-Agent-Service-Token"
CONTEXT_TOKEN_HEADER = "X-Agent-Context-Token"
TRACE_HEADER = "X-Agent-Trace-Id"
IDEMPOTENCY_HEADER = "Idempotency-Key"

_SUCCESS_CODE = 200
# 仅这些传输层异常视为可重试的网络瞬时错误。
_TRANSIENT_ERRORS = (
    httpx.ConnectError,
    httpx.ConnectTimeout,
    httpx.ReadTimeout,
    httpx.PoolTimeout,
)


class JavaToolClient:
    """面向 Java Agent Tool 的异步客户端。"""

    def __init__(
        self,
        settings: AppSettings,
        client: httpx.AsyncClient | None = None,
        breaker: CircuitBreaker | None = None,
    ) -> None:
        self._service_token = settings.java_service_token
        self._max_retries = settings.tool_max_retries
        self._breaker = breaker or CircuitBreaker()
        self._client = client or httpx.AsyncClient(
            base_url=settings.java_tool_base_url,
            timeout=httpx.Timeout(
                connect=settings.tool_connect_timeout_seconds,
                read=settings.tool_read_timeout_seconds,
                # 目前 Java Tool 调用以 GET 读操作为主，写超时复用读超时配置；若未来引入耗时较长的写操作可单独拆分
                write=settings.tool_read_timeout_seconds,
                pool=settings.tool_connect_timeout_seconds,
            ),
            limits=httpx.Limits(max_connections=20, max_keepalive_connections=10),
        )

    async def aclose(self) -> None:
        await self._client.aclose()

    async def get(
        self,
        path: str,
        *,
        context_token: str,
        trace_id: str,
        params: dict[str, Any] | None = None,
        idempotency_key: str | None = None,
    ) -> Any:
        """执行只读 GET 请求，返回响应信封中的 ``data`` 字段。"""
        return await self._request(
            "GET",
            path,
            context_token=context_token,
            trace_id=trace_id,
            params=params,
            idempotency_key=idempotency_key,
            retry=True,
        )

    async def post(
        self,
        path: str,
        *,
        json: dict[str, Any],
        context_token: str,
        trace_id: str,
        idempotency_key: str | None = None,
    ) -> Any:
        """执行写 POST 请求，返回响应信封中的 ``data`` 字段。

        写操作默认不重试（``retry=False``），避免网络瞬时错误导致重复创建；
        创建预约的幂等由 ``Idempotency-Key`` 头在 Java 侧保证。
        """
        return await self._request(
            "POST",
            path,
            context_token=context_token,
            trace_id=trace_id,
            json_body=json,
            idempotency_key=idempotency_key,
            retry=False,
        )

    async def _request(
        self,
        method: str,
        path: str,
        *,
        context_token: str,
        trace_id: str,
        params: dict[str, Any] | None = None,
        json_body: dict[str, Any] | None = None,
        idempotency_key: str | None = None,
        retry: bool,
    ) -> Any:
        if not self._service_token:
            raise ToolConfigurationError()

        if not self._breaker.allow_request():
            logger.warning("agent tool circuit open", extra={"trace_id": trace_id})
            raise ToolUnavailableError()

        headers = self._build_headers(context_token, trace_id, idempotency_key)
        # 写操作不自动重试；读操作在网络瞬时错误时最多重试 _max_retries 次。
        attempts = self._max_retries + 1 if retry else 1
        last_error: Exception | None = None
        for attempt in range(attempts):
            try:
                response = await self._client.request(
                    method,
                    path,
                    params=self._clean_params(params),
                    json=json_body,
                    headers=headers,
                )
            except _TRANSIENT_ERRORS as exc:
                last_error = exc
                logger.warning(
                    "agent tool transient error",
                    extra={
                        "trace_id": trace_id,
                        "attempt": attempt + 1,
                        "exception_type": type(exc).__name__,
                    },
                )
                continue
            return self._handle_response(response, trace_id)

        # 重试耗尽视为一次下游失败，计入熔断器。
        self._breaker.record_failure()
        logger.warning(
            "agent tool exhausted retries",
            extra={"trace_id": trace_id, "attempts": attempts},
        )
        raise ToolUnavailableError() from last_error

    def _handle_response(self, response: httpx.Response, trace_id: str) -> Any:
        """解析响应并把结果计入熔断器：不可用错误记失败，其余（含 4xx 业务错误）记成功。"""
        try:
            data = self._parse_response(response, trace_id)
        except ToolUnavailableError:
            # 5xx / 无效响应视为下游不可用，计入失败。
            self._breaker.record_failure()
            raise
        except ToolError:
            # 4xx 等业务/客户端错误说明下游仍在正常响应，视为成功。
            self._breaker.record_success()
            raise
        self._breaker.record_success()
        return data

    def _build_headers(
        self,
        context_token: str,
        trace_id: str,
        idempotency_key: str | None,
    ) -> dict[str, str]:
        headers = {
            SERVICE_TOKEN_HEADER: self._service_token or "",
            CONTEXT_TOKEN_HEADER: context_token,
            TRACE_HEADER: trace_id,
        }
        if idempotency_key:
            headers[IDEMPOTENCY_HEADER] = idempotency_key
        return headers

    @staticmethod
    def _clean_params(params: dict[str, Any] | None) -> dict[str, Any] | None:
        if not params:
            return None
        return {key: value for key, value in params.items() if value is not None}

    def _parse_response(self, response: httpx.Response, trace_id: str) -> Any:
        if response.status_code >= 400:
            logger.warning(
                "agent tool returned error status",
                extra={"trace_id": trace_id, "status_code": response.status_code},
            )
            raise error_for_status(response.status_code)

        try:
            envelope = response.json()
        except ValueError as exc:
            logger.warning("agent tool returned invalid json", extra={"trace_id": trace_id})
            raise ToolUnavailableError("agent tool returned an invalid response") from exc

        if not isinstance(envelope, dict):
            raise ToolUnavailableError("agent tool returned an unexpected response")

        code = envelope.get("code")
        if code != _SUCCESS_CODE:
            logger.warning(
                "agent tool returned business error",
                extra={"trace_id": trace_id, "code": code},
            )
            raise error_for_status(int(code) if isinstance(code, int) else 502)

        return envelope.get("data")
