import asyncio
import logging
from collections.abc import Awaitable, Callable
from contextlib import AbstractAsyncContextManager
from datetime import UTC, datetime, timedelta
from uuid import uuid4

from pydantic import BaseModel, ConfigDict

from app.agents.base import AgentType
from app.core.exceptions import AgentException
from app.core.security import VerifiedAgentContext

logger = logging.getLogger("bmp_agent")


class Session(BaseModel):
    model_config = ConfigDict(extra="forbid", frozen=True)

    conversation_id: str
    user_id: str
    agent_type: AgentType
    thread_id: str
    created_at: datetime
    expires_at: datetime


class SessionAccessError(AgentException):
    def __init__(self) -> None:
        super().__init__(status_code=403, message="session does not belong to current context")


class SessionExpiredError(AgentException):
    def __init__(self) -> None:
        super().__init__(status_code=401, message="session has expired")


class InMemorySessionStore:
    def __init__(
        self,
        *,
        ttl_seconds: int,
        clock: Callable[[], datetime] | None = None,
        on_expire: Callable[[Session], Awaitable[None]] | None = None,
    ) -> None:
        self._ttl = timedelta(seconds=ttl_seconds)
        self._clock = clock or (lambda: datetime.now(UTC))
        self._on_expire = on_expire
        self._sessions: dict[str, Session] = {}
        self._processing_locks: dict[str, asyncio.Lock] = {}
        self._creation_lock = asyncio.Lock()

    async def get_or_create(
        self,
        conversation_id: str,
        context: VerifiedAgentContext,
        agent_type: AgentType,
    ) -> Session:
        async with self._creation_lock:
            now = self._clock()
            session = self._sessions.get(conversation_id)
            requested_was_expired = session is not None and session.expires_at <= now
            expired_ids = [
                existing_id
                for existing_id, existing in self._sessions.items()
                if existing.expires_at <= now and not self._processing_locks[existing_id].locked()
            ]
            for expired_id in expired_ids:
                await self._remove_expired(expired_id)

            if requested_was_expired:
                raise SessionExpiredError()

            session = self._sessions.get(conversation_id)
            if session is None:
                session = Session(
                    conversation_id=conversation_id,
                    user_id=context.user_id,
                    agent_type=agent_type,
                    thread_id=f"thread_{uuid4().hex}",
                    created_at=now,
                    expires_at=now + self._ttl,
                )
                self._sessions[conversation_id] = session
                self._processing_locks[conversation_id] = asyncio.Lock()
                return session

            if session.user_id != context.user_id or session.agent_type is not agent_type:
                raise SessionAccessError()
            return session

    async def _remove_expired(self, conversation_id: str) -> None:
        session = self._sessions.pop(conversation_id)
        self._processing_locks.pop(conversation_id)
        if self._on_expire is None:
            return
        try:
            await self._on_expire(session)
        except Exception as exc:
            logger.warning(
                "expired session cleanup failed",
                extra={"exception_type": type(exc).__name__},
            )

    def processing_lock(self, session: Session) -> AbstractAsyncContextManager[None]:
        return self._processing_locks[session.conversation_id]
