import hmac
from typing import Protocol

from pydantic import BaseModel, ConfigDict

from app.core.config import AppSettings
from app.core.exceptions import AgentException


class VerifiedAgentContext(BaseModel):
    model_config = ConfigDict(extra="forbid", frozen=True)

    user_id: str
    role: str
    venue_id: str | None
    trace_id: str


class AgentContextVerifier(Protocol):
    def verify(self, token: str | None, trace_id: str) -> VerifiedAgentContext: ...


class StaticAgentContextVerifier:
    def __init__(self, settings: AppSettings) -> None:
        self._token = settings.static_agent_context_token
        self._user_id = settings.static_agent_user_id
        self._role = settings.static_agent_user_role
        self._venue_id = settings.static_agent_venue_id

    def verify(self, token: str | None, trace_id: str) -> VerifiedAgentContext:
        if not self._token:
            raise AgentException(status_code=503, message="static agent context is not configured")
        if not token or not hmac.compare_digest(token, self._token):
            raise AgentException(status_code=401, message="invalid agent context token")
        return VerifiedAgentContext(
            user_id=self._user_id,
            role=self._role,
            venue_id=self._venue_id,
            trace_id=trace_id,
        )


class UnavailableSignedContextVerifier:
    def verify(self, token: str | None, trace_id: str) -> VerifiedAgentContext:
        del token, trace_id
        raise AgentException(
            status_code=503,
            message="signed agent context verification is not available in P0",
        )


def build_context_verifier(settings: AppSettings) -> AgentContextVerifier:
    if settings.agent_context_mode == "static":
        return StaticAgentContextVerifier(settings)
    return UnavailableSignedContextVerifier()
