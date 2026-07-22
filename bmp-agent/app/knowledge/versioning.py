"""知识版本控制与历史记录管理。"""

from typing import Any
from pydantic import BaseModel, ConfigDict

from app.knowledge.store import DocumentItem, KnowledgeStore


class KnowledgeVersionRecord(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    version_tag: str
    timestamp: str
    description: str
    snapshot: list[dict[str, Any]]


class KnowledgeManager:
    """提供文档添加、版本更新、历史记录审计与版本回滚服务。"""

    def __init__(self, store: KnowledgeStore) -> None:
        self._store = store
        self._version_history: list[KnowledgeVersionRecord] = []
        # 保存初始版本快照 (v1.0.0)
        self.commit_version("v1.0.0", "系统初始运营知识库版本")

    def commit_version(self, version_tag: str, description: str) -> KnowledgeVersionRecord:
        """提交当前知识库快照为一个新版本。"""
        import time
        from datetime import datetime

        docs = self._store.list_documents()
        snapshot = [doc.model_dump() for doc in docs]
        record = KnowledgeVersionRecord(
            version_tag=version_tag,
            timestamp=datetime.now().isoformat(),
            description=description,
            snapshot=snapshot,
        )
        self._version_history.append(record)
        return record

    def list_versions(self) -> list[KnowledgeVersionRecord]:
        return list(self._version_history)

    def rollback_to_version(self, version_tag: str) -> bool:
        """回滚知识库内容至历史指定版本。"""
        target = None
        for record in reversed(self._version_history):
            if record.version_tag == version_tag:
                target = record
                break
        if not target:
            return False

        # 清空当前 store 并根据快照恢复
        current_docs = self._store.list_documents()
        for doc in current_docs:
            self._store.delete_document(doc.doc_id)

        for doc_data in target.snapshot:
            self._store.add_document(doc_data)

        # 提交一个回滚操作标记
        self.commit_version(f"rollback-to-{version_tag}", f"回滚至历史版本 {version_tag}")
        return True
