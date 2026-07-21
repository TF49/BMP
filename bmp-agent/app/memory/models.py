"""Agent 服务 PostgreSQL 持久化的 SQLAlchemy 模型（P1-06）。

设计要点：
- 采用与 ``docs/数据库架构文档.md`` 一致的表结构，但列类型保持方言中立
  （``JSON`` 而非 ``JSONB``、Python 侧生成 UUID 与时间戳），以便离线用 SQLite 验证、
  集成环境用 PostgreSQL 运行同一套代码。
- ``user_id`` / ``venue_id`` 仅保存外部标识，不建立跨 MySQL 外键；会话归属与用户有效性
  由 Java 网关与 Tool 层校验。
"""

from datetime import UTC, datetime
from uuid import uuid4

from sqlalchemy import JSON, DateTime, ForeignKey, Index, String, Text
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


def _uuid() -> str:
    return uuid4().hex


def _now() -> datetime:
    return datetime.now(UTC)


class Base(DeclarativeBase):
    pass


class AgentConversation(Base):
    __tablename__ = "agent_conversations"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_uuid)
    conversation_id: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    user_id: Mapped[str] = mapped_column(String(64), nullable=False)
    agent_type: Mapped[str] = mapped_column(String(32), nullable=False)
    thread_id: Mapped[str] = mapped_column(String(64), nullable=False)
    venue_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    status: Mapped[str] = mapped_column(String(32), nullable=False, default="active")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=_now
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=_now, onupdate=_now
    )
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)

    __table_args__ = (
        Index("idx_agent_conversations_user_id", "user_id"),
        Index("idx_agent_conversations_thread_id", "thread_id"),
        Index("idx_agent_conversations_expires_at", "expires_at"),
    )


class AgentMessage(Base):
    __tablename__ = "agent_messages"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_uuid)
    conversation_id: Mapped[str] = mapped_column(
        String(64),
        ForeignKey("agent_conversations.conversation_id", ondelete="CASCADE"),
        nullable=False,
    )
    message_id: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    role: Mapped[str] = mapped_column(String(32), nullable=False)
    content: Mapped[str] = mapped_column(Text, nullable=False)
    message_type: Mapped[str] = mapped_column(String(32), nullable=False, default="text")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=_now
    )

    __table_args__ = (Index("idx_agent_messages_conversation_id", "conversation_id"),)


class AgentAction(Base):
    __tablename__ = "agent_actions"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_uuid)
    action_id: Mapped[str] = mapped_column(String(64), unique=True, nullable=False)
    conversation_id: Mapped[str] = mapped_column(
        String(64),
        ForeignKey("agent_conversations.conversation_id", ondelete="CASCADE"),
        nullable=False,
    )
    action_type: Mapped[str] = mapped_column(String(32), nullable=False)
    action_data: Mapped[dict] = mapped_column(JSON, nullable=False, default=dict)
    status: Mapped[str] = mapped_column(String(32), nullable=False, default="pending")
    idempotency_key: Mapped[str | None] = mapped_column(String(128), unique=True, nullable=True)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=_now
    )
    result: Mapped[dict | None] = mapped_column(JSON, nullable=True)

    __table_args__ = (
        Index("idx_agent_actions_conversation_id", "conversation_id"),
        Index("idx_agent_actions_status", "status"),
    )


class ToolCallLog(Base):
    __tablename__ = "tool_call_logs"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_uuid)
    trace_id: Mapped[str] = mapped_column(String(64), nullable=False)
    conversation_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    user_id: Mapped[str] = mapped_column(String(64), nullable=False)
    tool_name: Mapped[str] = mapped_column(String(128), nullable=False)
    tool_type: Mapped[str] = mapped_column(String(32), nullable=False)
    status: Mapped[str] = mapped_column(String(32), nullable=False)
    duration_ms: Mapped[int | None] = mapped_column(nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=_now
    )

    __table_args__ = (
        Index("idx_tool_call_logs_trace_id", "trace_id"),
        Index("idx_tool_call_logs_conversation_id", "conversation_id"),
    )


class Checkpoint(Base):
    """自建的 LangGraph Checkpoint 持久化表（本阶段自管，官方 saver 留到接线阶段）。"""

    __tablename__ = "checkpoints"

    thread_id: Mapped[str] = mapped_column(String(64), primary_key=True)
    checkpoint_ns: Mapped[str] = mapped_column(String(64), primary_key=True, default="")
    checkpoint_id: Mapped[str] = mapped_column(String(64), primary_key=True)
    parent_checkpoint_id: Mapped[str | None] = mapped_column(String(64), nullable=True)
    type: Mapped[str] = mapped_column(String(32), nullable=False, default="checkpoint")
    checkpoint: Mapped[dict] = mapped_column(JSON, nullable=False, default=dict)
    checkpoint_metadata: Mapped[dict] = mapped_column(
        "metadata", JSON, nullable=False, default=dict
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=_now
    )

    __table_args__ = (
        Index("idx_checkpoints_thread_id", "thread_id"),
        Index("idx_checkpoints_created_at", "created_at"),
    )
