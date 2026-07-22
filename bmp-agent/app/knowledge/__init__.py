"""知识库模块：知识存储、切片、RAG 检索与版本控制。"""

from app.knowledge.default_docs import DEFAULT_KNOWLEDGE_DOCS
from app.knowledge.retriever import KnowledgeRetriever, SearchResult
from app.knowledge.store import DocumentChunk, DocumentItem, KnowledgeStore
from app.knowledge.versioning import KnowledgeManager, KnowledgeVersionRecord

__all__ = [
    "DEFAULT_KNOWLEDGE_DOCS",
    "DocumentChunk",
    "DocumentItem",
    "KnowledgeManager",
    "KnowledgeRetriever",
    "KnowledgeStore",
    "KnowledgeVersionRecord",
    "SearchResult",
]
