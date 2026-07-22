"""知识库文档与切片存储管理模块。"""

import re
from typing import Any
from pydantic import BaseModel, ConfigDict, Field

from app.knowledge.default_docs import DEFAULT_KNOWLEDGE_DOCS


class DocumentChunk(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    chunk_id: str
    doc_id: str
    title: str
    category: str
    version: str
    updated_at: str
    content: str


class DocumentItem(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    doc_id: str
    title: str
    category: str
    version: str
    updated_at: str
    content: str


class KnowledgeStore:
    """内存/PgVector 统一抽象的知识库文档与切片存储器。"""

    def __init__(self) -> None:
        self._documents: dict[str, DocumentItem] = {}
        self._chunks: list[DocumentChunk] = []
        self.load_defaults()

    def load_defaults(self) -> None:
        """加载初始运营确认的文档。"""
        self._documents.clear()
        self._chunks.clear()
        for doc in DEFAULT_KNOWLEDGE_DOCS:
            self.add_document(doc)

    def add_document(self, doc_dict: dict[str, Any]) -> DocumentItem:
        """添加或更新文档并自动执行切片与索引。"""
        doc = DocumentItem.model_validate(doc_dict)
        self._documents[doc.doc_id] = doc
        self._reindex_document_chunks(doc)
        return doc

    def get_document(self, doc_id: str) -> DocumentItem | None:
        return self._documents.get(doc_id)

    def list_documents(self) -> list[DocumentItem]:
        return list(self._documents.values())

    def delete_document(self, doc_id: str) -> bool:
        if doc_id in self._documents:
            del self._documents[doc_id]
            self._chunks = [c for c in self._chunks if c.doc_id != doc_id]
            return True
        return False

    def get_all_chunks(self) -> list[DocumentChunk]:
        return list(self._chunks)

    def _reindex_document_chunks(self, doc: DocumentItem) -> None:
        # 先清除旧 chunk
        self._chunks = [c for c in self._chunks if c.doc_id != doc.doc_id]
        
        # 简单清洗与分段切片
        paragraphs = [p.strip() for p in doc.content.split("\n") if p.strip()]
        chunk_idx = 1

        for para in paragraphs:
            # 如果段落很长，以句号/分号进一步切片
            sub_chunks = self._split_paragraph(para, max_length=250)
            for sub in sub_chunks:
                chunk = DocumentChunk(
                    chunk_id=f"{doc.doc_id}_c{chunk_idx}",
                    doc_id=doc.doc_id,
                    title=doc.title,
                    category=doc.category,
                    version=doc.version,
                    updated_at=doc.updated_at,
                    content=sub,
                )
                self._chunks.append(chunk)
                chunk_idx += 1

    def _split_paragraph(self, text: str, max_length: int = 250) -> list[str]:
        if len(text) <= max_length:
            return [text]
        
        sentences = re.split(r"([。；！？\n])", text)
        result: list[str] = []
        current = ""

        for i in range(0, len(sentences), 2):
            part = sentences[i]
            delim = sentences[i + 1] if i + 1 < len(sentences) else ""
            sentence = part + delim
            if not sentence.strip():
                continue
            if len(current) + len(sentence) <= max_length:
                current += sentence
            else:
                if current:
                    result.append(current.strip())
                current = sentence

        if current.strip():
            result.append(current.strip())

        return result if result else [text[:max_length]]
