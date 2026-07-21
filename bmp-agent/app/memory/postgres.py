"""PostgreSQL 持久化的会话存储与 Checkpoint 存储（P1-06）。

- :class:`PostgresSessionStore` 满足 :class:`app.memory.session.SessionStore` 协议，语义与
  :class:`app.memory.session.InMemorySessionStore` 保持一致（过期抛 401、越权抛 403）。
- :class:`PostgresCheckpointStore` 自建 Checkpoint 表读写，用于服务重启后按线程恢复状态。
- 引擎/会话工厂通过工厂函数创建，测试可注入 SQLite（``aiosqlite``）离线验证，集成环境
  切换 ``postgresql+asyncpg``。
"""

import asyncio
import logging
from collections.abc import Awaitable, Callable
from contextlib import AbstractAsyncContextManager
from datetime import UTC, datetime, timedelta
from typing import Any
from uuid import uuid4

from pydantic import BaseModel, ConfigDict
from sqlalchemy import delete, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.ext.asyncio import (
    AsyncEngine,
    AsyncSession,
    async_sessionmaker,
    create_async_engine,
)

from app.agents.base import AgentType
from app.core.security import VerifiedAgentContext
from app.memory.models import AgentConversation, Base, Checkpoint
from app.memory.session import (
    Session,
    SessionAccessError,
    SessionExpiredError,
)

logger = logging.getLogger("bmp_agent")


def _ensure_utc(value: datetime) -> datetime:
    """把可能为 naive（SQLite 读取）的时间统一为 UTC-aware，避免比较报错。"""
    if value.tzinfo is None:
        return value.replace(tzinfo=UTC)
    return value.astimezone(UTC)


def create_engine(url: str, **kwargs: Any) -> AsyncEngine:
    return create_async_engine(url, **kwargs)


def create_session_factory(engine: AsyncEngine) -> async_sessionmaker[AsyncSession]:
    return async_sessionmaker(engine, expire_on_commit=False)


async def create_all(engine: AsyncEngine) -> None:
    """按模型元数据建表；仅供测试或首次引导使用，生产环境走 Alembic。"""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


class PostgresSessionStore:
    """基于 PostgreSQL/SQLAlchemy 的会话存储。"""

    def __init__(
        self,
        session_factory: async_sessionmaker[AsyncSession],
        *,
        ttl_seconds: int,
        clock: Callable[[], datetime] | None = None,
        on_expire: Callable[[Session], Awaitable[None]] | None = None,
    ) -> None:
        self._session_factory = session_factory
        self._ttl = timedelta(seconds=ttl_seconds)
        self._clock = clock or (lambda: datetime.now(UTC))
        self._on_expire = on_expire
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
            async with self._session_factory() as db:
                row = await self._find(db, conversation_id)

                if row is not None and _ensure_utc(row.expires_at) <= now:
                    await self._expire_row(db, row)
                    await db.commit()
                    raise SessionExpiredError()

                if row is None:
                    session = self._new_session(conversation_id, context, agent_type, now)
                    db.add(
                        AgentConversation(
                            conversation_id=session.conversation_id,
                            user_id=session.user_id,
                            agent_type=str(session.agent_type),
                            thread_id=session.thread_id,
                            venue_id=context.venue_id,
                            created_at=session.created_at,
                            updated_at=session.created_at,
                            expires_at=session.expires_at,
                        )
                    )
                    try:
                        await db.commit()
                    except IntegrityError:
                        # 多实例部署时，另一个进程可能抢先插入了同一 conversation_id。
                        # 回滚后重新查询并复用已存在的行，避免将唯一约束冲突传播给用户。
                        await db.rollback()
                        row = await self._find(db, conversation_id)
                        if row is None:
                            raise  # 非预期的完整性错误，向上抛出
                        if row.user_id != context.user_id or row.agent_type != str(agent_type):
                            raise SessionAccessError() from None
                        self._processing_locks.setdefault(conversation_id, asyncio.Lock())
                        return self._to_session(row)
                    self._processing_locks.setdefault(conversation_id, asyncio.Lock())
                    return session

                # Use != (not `is not`) — StrEnum identity is not guaranteed.
                if row.user_id != context.user_id or row.agent_type != str(agent_type):
                    raise SessionAccessError()
                self._processing_locks.setdefault(conversation_id, asyncio.Lock())
                return self._to_session(row)

    async def _find(self, db: AsyncSession, conversation_id: str) -> AgentConversation | None:
        result = await db.execute(
            select(AgentConversation).where(
                AgentConversation.conversation_id == conversation_id
            )
        )
        return result.scalar_one_or_none()

    async def _expire_row(self, db: AsyncSession, row: AgentConversation) -> None:
        session = self._to_session(row)
        await db.delete(row)
        if self._on_expire is None:
            return
        try:
            await self._on_expire(session)
        except Exception as exc:
            logger.warning(
                "expired session cleanup failed",
                extra={"exception_type": type(exc).__name__},
            )

    def _new_session(
        self,
        conversation_id: str,
        context: VerifiedAgentContext,
        agent_type: AgentType,
        now: datetime,
    ) -> Session:
        return Session(
            conversation_id=conversation_id,
            user_id=context.user_id,
            agent_type=agent_type,
            thread_id=f"thread_{uuid4().hex}",
            created_at=now,
            expires_at=now + self._ttl,
        )

    @staticmethod
    def _to_session(row: AgentConversation) -> Session:
        return Session(
            conversation_id=row.conversation_id,
            user_id=row.user_id,
            agent_type=AgentType(row.agent_type),
            thread_id=row.thread_id,
            created_at=_ensure_utc(row.created_at),
            expires_at=_ensure_utc(row.expires_at),
        )

    def processing_lock(self, session: Session) -> AbstractAsyncContextManager[None]:
        return self._processing_locks.setdefault(session.conversation_id, asyncio.Lock())

    async def purge_expired_sessions(self) -> int:
        """主动删除所有已过期的会话行，供后台定时任务调用。

        Returns:
            删除的行数。
        """
        now = self._clock()
        async with self._session_factory() as db:
            # 先查询过期行以便触发 on_expire 回调
            result = await db.execute(
                select(AgentConversation).where(AgentConversation.expires_at <= now)
            )
            expired_rows = result.scalars().all()
            for row in expired_rows:
                await self._expire_row(db, row)
            if expired_rows:
                await db.commit()
            deleted = len(expired_rows)
            if deleted:
                logger.info(
                    "purged expired sessions",
                    extra={"count": deleted},
                )
            return deleted


class CheckpointRecord(BaseModel):
    model_config = ConfigDict(extra="forbid", frozen=True)

    thread_id: str
    checkpoint_ns: str
    checkpoint_id: str
    parent_checkpoint_id: str | None
    type: str
    checkpoint: dict[str, Any]
    metadata: dict[str, Any]
    created_at: datetime


class PostgresCheckpointStore:
    """自建 Checkpoint 表读写：写入、按线程读取最新/全部、删除线程。"""

    def __init__(self, session_factory: async_sessionmaker[AsyncSession]) -> None:
        self._session_factory = session_factory

    async def put(
        self,
        *,
        thread_id: str,
        checkpoint_id: str,
        checkpoint: dict[str, Any],
        metadata: dict[str, Any] | None = None,
        parent_checkpoint_id: str | None = None,
        checkpoint_ns: str = "",
        type_: str = "checkpoint",
        created_at: datetime | None = None,
    ) -> None:
        async with self._session_factory() as db:
            existing = await db.get(Checkpoint, (thread_id, checkpoint_ns, checkpoint_id))
            if existing is None:
                db.add(
                    Checkpoint(
                        thread_id=thread_id,
                        checkpoint_ns=checkpoint_ns,
                        checkpoint_id=checkpoint_id,
                        parent_checkpoint_id=parent_checkpoint_id,
                        type=type_,
                        checkpoint=checkpoint,
                        checkpoint_metadata=metadata or {},
                        created_at=created_at or datetime.now(UTC),
                    )
                )
            else:
                existing.parent_checkpoint_id = parent_checkpoint_id
                existing.type = type_
                existing.checkpoint = checkpoint
                existing.checkpoint_metadata = metadata or {}
            await db.commit()

    async def get_latest(
        self, thread_id: str, checkpoint_ns: str = ""
    ) -> CheckpointRecord | None:
        async with self._session_factory() as db:
            result = await db.execute(
                select(Checkpoint)
                .where(
                    Checkpoint.thread_id == thread_id,
                    Checkpoint.checkpoint_ns == checkpoint_ns,
                )
                .order_by(Checkpoint.created_at.desc())
                .limit(1)
            )
            row = result.scalar_one_or_none()
            return self._to_record(row) if row is not None else None

    async def load_state(self, thread_id: str) -> dict[str, Any] | None:
        latest = await self.get_latest(thread_id)
        return latest.checkpoint if latest is not None else None

    async def save_state(self, thread_id: str, state: dict[str, Any]) -> None:
        await self.put(
            thread_id=thread_id,
            checkpoint_id=uuid4().hex,
            checkpoint=state,
            metadata={"source": "agent"},
        )

    async def list_checkpoints(
        self, thread_id: str, checkpoint_ns: str = ""
    ) -> list[CheckpointRecord]:
        async with self._session_factory() as db:
            result = await db.execute(
                select(Checkpoint)
                .where(
                    Checkpoint.thread_id == thread_id,
                    Checkpoint.checkpoint_ns == checkpoint_ns,
                )
                .order_by(Checkpoint.created_at.desc())
            )
            return [self._to_record(row) for row in result.scalars().all()]

    async def delete_thread(self, thread_id: str) -> None:
        async with self._session_factory() as db:
            await db.execute(delete(Checkpoint).where(Checkpoint.thread_id == thread_id))
            await db.commit()

    @staticmethod
    def _to_record(row: Checkpoint) -> CheckpointRecord:
        return CheckpointRecord(
            thread_id=row.thread_id,
            checkpoint_ns=row.checkpoint_ns,
            checkpoint_id=row.checkpoint_id,
            parent_checkpoint_id=row.parent_checkpoint_id,
            type=row.type,
            checkpoint=row.checkpoint,
            metadata=row.checkpoint_metadata,
            created_at=_ensure_utc(row.created_at),
        )
