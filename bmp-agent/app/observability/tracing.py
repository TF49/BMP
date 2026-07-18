import re
from contextvars import ContextVar
from uuid import uuid4

from starlette.middleware.base import BaseHTTPMiddleware, RequestResponseEndpoint
from starlette.requests import Request
from starlette.responses import Response

TRACE_HEADER = "X-Agent-Trace-Id"
_TRACE_PATTERN = re.compile(r"^[A-Za-z0-9_-]{1,64}$")
_trace_id: ContextVar[str | None] = ContextVar("trace_id", default=None)


def normalize_trace_id(candidate: str | None) -> str:
    if candidate and _TRACE_PATTERN.fullmatch(candidate):
        return candidate
    return uuid4().hex


def get_trace_id() -> str:
    current = _trace_id.get()
    if current is None:
        current = uuid4().hex
        _trace_id.set(current)
    return current


def clear_trace_id() -> None:
    _trace_id.set(None)


class TraceIdMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next: RequestResponseEndpoint) -> Response:
        trace_id = normalize_trace_id(request.headers.get(TRACE_HEADER))
        token = _trace_id.set(trace_id)
        try:
            response = await call_next(request)
            response.headers[TRACE_HEADER] = trace_id
            return response
        finally:
            _trace_id.reset(token)
