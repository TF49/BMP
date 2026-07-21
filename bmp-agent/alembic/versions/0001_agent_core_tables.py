"""agent core tables

Revision ID: 0001
Revises:
Create Date: 2026-07-20

创建 Agent 服务核心表：会话、消息、待确认动作、Tool 调用日志与自建 Checkpoint。
``user_id`` / ``venue_id`` 仅存外部标识，不建立跨 MySQL 外键。
"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "0001"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "agent_conversations",
        sa.Column("id", sa.String(length=36), primary_key=True),
        sa.Column("conversation_id", sa.String(length=64), nullable=False, unique=True),
        sa.Column("user_id", sa.String(length=64), nullable=False),
        sa.Column("agent_type", sa.String(length=32), nullable=False),
        sa.Column("thread_id", sa.String(length=64), nullable=False),
        sa.Column("venue_id", sa.String(length=64), nullable=True),
        sa.Column("status", sa.String(length=32), nullable=False, server_default="active"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_index(
        "idx_agent_conversations_user_id", "agent_conversations", ["user_id"]
    )
    op.create_index(
        "idx_agent_conversations_thread_id", "agent_conversations", ["thread_id"]
    )
    op.create_index(
        "idx_agent_conversations_expires_at", "agent_conversations", ["expires_at"]
    )

    op.create_table(
        "agent_messages",
        sa.Column("id", sa.String(length=36), primary_key=True),
        sa.Column(
            "conversation_id",
            sa.String(length=64),
            sa.ForeignKey("agent_conversations.conversation_id", ondelete="CASCADE"),
            nullable=False,
        ),
        sa.Column("message_id", sa.String(length=64), nullable=False, unique=True),
        sa.Column("role", sa.String(length=32), nullable=False),
        sa.Column("content", sa.Text(), nullable=False),
        sa.Column("message_type", sa.String(length=32), nullable=False, server_default="text"),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_index(
        "idx_agent_messages_conversation_id", "agent_messages", ["conversation_id"]
    )

    op.create_table(
        "agent_actions",
        sa.Column("id", sa.String(length=36), primary_key=True),
        sa.Column("action_id", sa.String(length=64), nullable=False, unique=True),
        sa.Column(
            "conversation_id",
            sa.String(length=64),
            sa.ForeignKey("agent_conversations.conversation_id", ondelete="CASCADE"),
            nullable=False,
        ),
        sa.Column("action_type", sa.String(length=32), nullable=False),
        sa.Column("action_data", sa.JSON(), nullable=False),
        sa.Column("status", sa.String(length=32), nullable=False, server_default="pending"),
        sa.Column("idempotency_key", sa.String(length=128), nullable=True, unique=True),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("result", sa.JSON(), nullable=True),
    )
    op.create_index(
        "idx_agent_actions_conversation_id", "agent_actions", ["conversation_id"]
    )
    op.create_index("idx_agent_actions_status", "agent_actions", ["status"])

    op.create_table(
        "tool_call_logs",
        sa.Column("id", sa.String(length=36), primary_key=True),
        sa.Column("trace_id", sa.String(length=64), nullable=False),
        sa.Column("conversation_id", sa.String(length=64), nullable=True),
        sa.Column("user_id", sa.String(length=64), nullable=False),
        sa.Column("tool_name", sa.String(length=128), nullable=False),
        sa.Column("tool_type", sa.String(length=32), nullable=False),
        sa.Column("status", sa.String(length=32), nullable=False),
        sa.Column("duration_ms", sa.Integer(), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_index("idx_tool_call_logs_trace_id", "tool_call_logs", ["trace_id"])
    op.create_index(
        "idx_tool_call_logs_conversation_id", "tool_call_logs", ["conversation_id"]
    )

    op.create_table(
        "checkpoints",
        sa.Column("thread_id", sa.String(length=64), primary_key=True),
        sa.Column("checkpoint_ns", sa.String(length=64), primary_key=True, server_default=""),
        sa.Column("checkpoint_id", sa.String(length=64), primary_key=True),
        sa.Column("parent_checkpoint_id", sa.String(length=64), nullable=True),
        sa.Column("type", sa.String(length=32), nullable=False, server_default="checkpoint"),
        sa.Column("checkpoint", sa.JSON(), nullable=False),
        sa.Column("metadata", sa.JSON(), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), nullable=False),
    )
    op.create_index("idx_checkpoints_thread_id", "checkpoints", ["thread_id"])
    op.create_index("idx_checkpoints_created_at", "checkpoints", ["created_at"])


def downgrade() -> None:
    # 按外键依赖逆序删除：先删子表（agent_actions, agent_messages），再删父表（agent_conversations）
    op.drop_table("checkpoints")
    op.drop_table("tool_call_logs")
    op.drop_table("agent_actions")
    op.drop_table("agent_messages")
    op.drop_table("agent_conversations")
