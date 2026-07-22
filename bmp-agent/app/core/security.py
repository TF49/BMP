"""Agent 上下文签名校验模块。

提供两种校验模式：
- ``static``：开发环境静态 Token 比较（hmac.compare_digest 常量时间）。
- ``signed``：生产环境 HMAC-SHA256 签名验证，令牌格式与 Java ``AgentContextSigner`` 完全一致：
  ``base64url(payloadJson) + "." + base64url(hmacSha256(secret, payloadJsonBytes))``。
"""

import base64
import hashlib
import hmac
import json
import time
from typing import Protocol

from pydantic import BaseModel, ConfigDict

from app.core.config import AppSettings
from app.core.exceptions import AgentException


class VerifiedAgentContext(BaseModel):
    """经过签名校验的 Agent 用户上下文。frozen=True 防止被意外修改。"""

    model_config = ConfigDict(extra="forbid", frozen=True)

    user_id: str
    role: str
    venue_id: str | None
    trace_id: str
    # C-1: 补充审计与防重放所需字段（HMAC 模式下由 payload 解析；static 模式下使用默认值）
    nonce: str = ""
    expires_at: int = 0  # epoch 毫秒；0 表示 static 模式下不适用


class AgentContextVerifier(Protocol):
    def verify(self, token: str | None, trace_id: str) -> VerifiedAgentContext: ...


# ---------------------------------------------------------------------------
# Static verifier（开发/测试环境）
# ---------------------------------------------------------------------------


class StaticAgentContextVerifier:
    """用于开发环境的静态 Token 比较校验器。"""

    def __init__(self, settings: AppSettings) -> None:
        self._token = settings.static_agent_context_token
        self._user_id = settings.static_agent_user_id
        self._role = settings.static_agent_user_role
        self._venue_id = settings.static_agent_venue_id

    def verify(self, token: str | None, trace_id: str) -> VerifiedAgentContext:
        if not self._token:
            raise AgentException(status_code=503, message="static agent context is not configured")
        if not token or not hmac.compare_digest(token.encode(), self._token.encode()):
            raise AgentException(status_code=401, message="invalid agent context token")
        return VerifiedAgentContext(
            user_id=self._user_id,
            role=self._role,
            venue_id=self._venue_id,
            trace_id=trace_id,
        )


# ---------------------------------------------------------------------------
# HMAC-SHA256 verifier（生产环境，S-1）
# ---------------------------------------------------------------------------


def _b64decode(data: str) -> bytes:
    """URL-safe base64 解码，自动补齐 padding。"""
    pad = 4 - len(data) % 4
    if pad != 4:
        data += "=" * pad
    return base64.urlsafe_b64decode(data)


class HmacAgentContextVerifier:
    """生产环境 HMAC-SHA256 上下文验签器。

    令牌格式（与 Java ``AgentContextSigner.sign`` 完全对齐）：
        ``base64url(payloadJson) + "." + base64url(hmacSha256(secret_bytes, payloadJsonBytes))``

    校验顺序：格式检查 → 签名比较（常量时间）→ 字段完整性 → 有效期。
    """

    def __init__(self, settings: AppSettings) -> None:
        secret = settings.agent_context_secret
        if not secret:
            raise ValueError("agent_context_secret is required for signed mode")
        self._secret = secret.encode("utf-8")

    def verify(self, token: str | None, trace_id: str) -> VerifiedAgentContext:
        if not token or not token.strip():
            raise AgentException(status_code=401, message="missing agent context token")

        dot = token.find(".")
        # 令牌必须恰好包含一个 "."，且两侧均非空
        if dot <= 0 or dot == len(token) - 1 or token.count(".") != 1:
            raise AgentException(status_code=401, message="malformed agent context token")

        try:
            payload_bytes = _b64decode(token[:dot])
            sig_bytes = _b64decode(token[dot + 1 :])
        except Exception:
            raise AgentException(status_code=401, message="malformed agent context token") from None

        # 常量时间比较，防止时序攻击
        expected = hmac.new(self._secret, payload_bytes, hashlib.sha256).digest()
        if not hmac.compare_digest(expected, sig_bytes):
            raise AgentException(status_code=401, message="invalid agent context token")

        # 解析并校验 payload
        try:
            payload: dict = json.loads(payload_bytes.decode("utf-8"))
        except Exception:
            raise AgentException(status_code=401, message="malformed agent context token") from None

        self._require_fields(
            payload, ("user_id", "role", "issued_at", "expires_at", "nonce", "trace_id")
        )

        expires_at_ms: int = int(payload["expires_at"])
        if expires_at_ms <= int(time.time() * 1000):
            raise AgentException(status_code=401, message="agent context token has expired")

        venue_raw = payload.get("venue_id")
        venue_id: str | None = str(venue_raw) if venue_raw is not None else None

        return VerifiedAgentContext(
            user_id=str(payload["user_id"]),
            role=str(payload["role"]),
            venue_id=venue_id,
            trace_id=str(payload["trace_id"]),
            nonce=str(payload["nonce"]),
            expires_at=expires_at_ms,
        )

    @staticmethod
    def _require_fields(payload: dict, fields: tuple[str, ...]) -> None:
        for field in fields:
            if payload.get(field) is None:
                raise AgentException(
                    status_code=401, message=f"missing field in agent context: {field}"
                )


# ---------------------------------------------------------------------------
# Factory
# ---------------------------------------------------------------------------


def build_context_verifier(settings: AppSettings) -> AgentContextVerifier:
    if settings.agent_context_mode == "static":
        return StaticAgentContextVerifier(settings)
    return HmacAgentContextVerifier(settings)
