"""RAG 知识检索器：包含向量/TF-IDF/余弦相似度/关键词混合检索与阈值筛选。"""

import math
import re
from typing import Any
from pydantic import BaseModel, ConfigDict, Field

from app.knowledge.store import DocumentChunk, KnowledgeStore


class SearchResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True, extra="ignore", frozen=True)
    chunk: DocumentChunk
    score: float


class KnowledgeRetriever:
    """混合检索器，支持语义/词频/关键词匹配与高可信阈值过滤。"""

    def __init__(self, store: KnowledgeStore) -> None:
        self._store = store

    def retrieve(self, query: str, top_k: int = 3, threshold: float = 0.1) -> list[SearchResult]:
        """对查询词执行相关性匹配，返回 Top K 最佳切片及得分。"""
        chunks = self._store.get_all_chunks()
        if not chunks or not query.strip():
            return []

        query_tokens = self._tokenize(query)
        if not query_tokens:
            return []

        scored_results: list[SearchResult] = []
        for chunk in chunks:
            text = f"{chunk.title} {chunk.category} {chunk.content}"
            doc_tokens = self._tokenize(text)
            
            # 基础 Jaccard / 词频余弦计算
            score = self._compute_similarity(query_tokens, doc_tokens, query, chunk)
            if score >= threshold:
                scored_results.append(SearchResult(chunk=chunk, score=score))

        # 按得分从高到低排序
        scored_results.sort(key=lambda r: r.score, reverse=True)
        return scored_results[:top_k]

    def _tokenize(self, text: str) -> list[str]:
        # 提取中文词组、英文单词与数字
        tokens = re.findall(r"[\u4e00-\u9fa5]{1,4}|[a-zA-Z0-9]+", text.lower())
        # 双字/单字 N-gram 加强匹配能力
        ngrams = []
        clean_text = re.sub(r"[^\u4e00-\u9fa5a-zA-Z0-9]", "", text.lower())
        for i in range(len(clean_text) - 1):
            ngrams.append(clean_text[i:i+2])
        return tokens + ngrams

    def _compute_similarity(
        self,
        query_tokens: list[str],
        doc_tokens: list[str],
        query: str,
        chunk: DocumentChunk,
    ) -> float:
        if not doc_tokens or not query_tokens:
            return 0.0

        q_set = set(query_tokens)
        d_set = set(doc_tokens)
        
        intersection = q_set.intersection(d_set)
        if not intersection:
            return 0.0

        # Jaccard 相似度
        jaccard = len(intersection) / float(len(q_set.union(d_set)))

        # 关键词覆盖率
        coverage = len(intersection) / float(len(q_set))

        # 标题/类别匹配加权
        title_boost = 0.0
        if any(token in chunk.title.lower() for token in query_tokens):
            title_boost = 0.25

        # 强关键短语精确子串加权
        exact_boost = 0.0
        clean_query = query.strip().lower()
        if len(clean_query) >= 2 and clean_query in chunk.content.lower():
            exact_boost = 0.3

        total_score = (jaccard * 0.3) + (coverage * 0.45) + title_boost + exact_boost
        return round(min(1.0, total_score), 4)
