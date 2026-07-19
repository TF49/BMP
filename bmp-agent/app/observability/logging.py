import json
import logging
from datetime import UTC, datetime
from typing import Any

# Fields whose names contain any of these substrings will be redacted.
# This is a last-resort safeguard — callers must still avoid logging
# sensitive values in the message string itself.
_SENSITIVE_KEYWORDS: frozenset[str] = frozenset(
    {"key", "token", "secret", "password", "passwd", "signature", "sig", "jwt", "credential"}
)


def _is_sensitive(field_name: str) -> bool:
    lower = field_name.lower()
    return any(keyword in lower for keyword in _SENSITIVE_KEYWORDS)


class JsonFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        payload: dict[str, Any] = {
            "timestamp": datetime.now(UTC).isoformat(),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
        }
        for field in ("trace_id", "event", "exception_type"):
            value = getattr(record, field, None)
            if value is not None:
                payload[field] = "[REDACTED]" if _is_sensitive(field) else value
        return json.dumps(payload, ensure_ascii=False)


def configure_logging(level: str, log_format: str) -> None:
    logger = logging.getLogger("bmp_agent")
    logger.handlers.clear()
    handler = logging.StreamHandler()
    if log_format == "json":
        handler.setFormatter(JsonFormatter())
    else:
        handler.setFormatter(logging.Formatter("%(levelname)s %(name)s %(message)s"))
    logger.addHandler(handler)
    logger.setLevel(level.upper())
    logger.propagate = False
