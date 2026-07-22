"""场馆客服 Agent：处理知识库 FAQ、实时价格/状态查询、转人工及安全拒答。"""

import logging
import re
import time
from typing import Any

from app.agents.base import AgentMetadata, AgentResult
from app.agents.support.prompts import INJECTION_KEYWORDS, SYSTEM_PROMPT
from app.knowledge import KnowledgeRetriever, KnowledgeStore, SearchResult
from app.llm.base import ChatModel
from app.memory.checkpoint import AgentCheckpointStore
from app.memory.session import Session
from app.observability.tracing import get_trace_id
from app.tools.base import ToolError, ToolPermissionError
from app.tools.support import SupportTool

logger = logging.getLogger("bmp_agent.support")

_MODEL_LABEL = "support-agent"


class SupportAgent:
    """场馆客服 Agent。"""

    def __init__(
        self,
        support_tool: SupportTool,
        knowledge_retriever: KnowledgeRetriever,
        model: ChatModel | None = None,
        checkpoint_store: AgentCheckpointStore | None = None,
    ) -> None:
        self._support_tool = support_tool
        self._retriever = knowledge_retriever
        self._model = model
        self._checkpoint_store = checkpoint_store

    async def process(
        self,
        session: Session,
        message: str,
        context_token: str | None = None,
    ) -> AgentResult:
        start_time = time.monotonic()
        trace_id = get_trace_id()
        ctx_token = context_token or ""

        # 读取轮次
        turn = 1
        if self._checkpoint_store is not None and session.thread_id:
            try:
                state = await self._checkpoint_store.get(session.thread_id)
                if state and isinstance(state, dict):
                    turn = int(state.get("turn", 0)) + 1
            except Exception as e:
                logger.warning("failed to fetch checkpoint state: %s", e)

        tool_calls: list[str] = []
        references: list[dict[str, Any]] = []

        # 1. 安全防御：提示词注入与敏感信息检测
        if self._is_security_violation(message):
            tool_calls.append("security_guard")
            duration = int((time.monotonic() - start_time) * 1000)
            return AgentResult(
                response="抱歉，出于安全原则，我无法提供系统内部配置、密钥、数据库结构或私密架构信息。如需协助，请咨询正常的场馆预约与客服业务。",
                type="text",
                requires_action=False,
                actions=[],
                references=[],
                metadata=AgentMetadata(
                    model=_MODEL_LABEL,
                    tokens_used=0,
                    duration_ms=duration,
                    tool_calls=tool_calls,
                    turn=turn,
                ),
            )

        # 解析意图与提取场馆 ID
        venue_id = self._extract_venue_id(message)
        intent = self._detect_intent(message)

        response_text = ""
        actions: list[dict[str, Any]] = []

        try:
            if intent == "handoff":
                tool_calls.append("create_human_handoff")
                topic = "客户争议与人工咨询"
                if "退款" in message or "扣钱" in message:
                    topic = "资金退款争议"
                handoff_res = await self._support_tool.create_human_handoff(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    venue_id=venue_id,
                    topic=topic,
                    description=message,
                    conversation_id=session.conversation_id,

                )
                response_text = (
                    f"已为您成功创建人工客服转接单（单号：`{handoff_res.handoff_id}`）。\n"
                    f"当前等待状态：**{handoff_res.status}**，预计等待时间约 **{handoff_res.estimated_wait_time} 分钟**。"
                    f"客服专员将尽快为您跟进处理！"
                )
                actions.append({
                    "name": "human_handoff",
                    "status": handoff_res.status,
                    "handoff_id": handoff_res.handoff_id,
                })

            elif intent == "realtime_price":
                tool_calls.append("get_service_prices")
                prices = await self._support_tool.get_service_prices(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    venue_id=venue_id,
                )
                items_str = "\n".join(
                    f"- **{item.court_type}**（{item.time_slot}）：¥{item.price:.2f} / {item.unit}"
                    for item in prices.price_list
                )
                response_text = (
                    f"以下是 {prices.venue_id} 号场馆的**实时服务价格表**：\n\n"
                    f"{items_str}\n\n"
                    f"*(数据更新时间：{prices.updated_at[:19]})*"
                )
                references.append({
                    "title": f"场馆 {venue_id} 实时价格接口 (Java Tool)",
                    "doc_id": "tool_realtime_prices",
                    "version": "live",
                    "updated_at": prices.updated_at,
                })

            elif intent == "realtime_status":
                tool_calls.append("get_venue_realtime_status")
                status_res = await self._support_tool.get_venue_realtime_status(
                    context_token=ctx_token,
                    trace_id=trace_id,
                    venue_id=venue_id,
                )
                status_txt = "正常营业中" if status_res.status == "open" else "暂停服务/已闭馆"
                response_text = (
                    f"**{status_res.venue_name}** 当前实时营业状态：**{status_txt}**\n"
                    f"- 营业时间：{status_res.business_hours.open} - {status_res.business_hours.close}\n"
                    f"- 空闲场地：{status_res.available_courts} / {status_res.total_courts} 片\n"
                    f"- 当前时间：{status_res.current_time[:19]}"
                )
                references.append({
                    "title": f"{status_res.venue_name} 实时营业状态 (Java Tool)",
                    "doc_id": "tool_realtime_status",
                    "version": "live",
                    "updated_at": status_res.current_time,
                })

            else:
                # 执行知识库 RAG 检索
                tool_calls.append("rag_knowledge_search")
                search_results = self._retriever.retrieve(message, top_k=3, threshold=0.12)

                if not search_results:
                    response_text = (
                        "抱歉，在场馆官方知识库中暂未检索到关于该问题的明确记录。\n"
                        "为避免提供不准确的信息，建议您联系前台工作人员或转接人工客服获取详细解答。"
                    )
                else:
                    best = search_results[0].chunk
                    response_text = (
                        f"{best.content}\n\n"
                        f"---\n"
                        f"📌 **知识来源**：\n"
                        f"- 来源标题：{best.title}\n"
                        f"- 文档版本：{best.version}\n"
                        f"- 更新时间：{best.updated_at}"
                    )
                    for sr in search_results:
                        c = sr.chunk
                        references.append({
                            "title": c.title,
                            "doc_id": c.doc_id,
                            "version": c.version,
                            "updated_at": c.updated_at,
                        })

        except ToolPermissionError:
            response_text = "抱歉，您无权查询当前场馆的客服信息。"
        except ToolError as e:
            response_text = f"查询失败：{e.message}"
        except Exception as exc:
            logger.exception("unhandled support agent error: %s", exc)
            response_text = "系统客服服务暂时不可用，请稍后再试或联系场馆前台。"

        # 保存 checkpoint 轮次
        if self._checkpoint_store is not None and session.thread_id:
            try:
                await self._checkpoint_store.put(
                    session.thread_id,
                    {"turn": turn, "last_intent": intent},
                )
            except Exception as e:
                logger.warning("failed to update checkpoint state: %s", e)

        duration = int((time.monotonic() - start_time) * 1000)
        return AgentResult(
            response=response_text,
            type="text",
            requires_action=False,
            actions=actions,
            references=references,
            metadata=AgentMetadata(
                model=_MODEL_LABEL,
                tokens_used=0,
                duration_ms=duration,
                tool_calls=tool_calls,
                turn=turn,
            ),
        )

    async def delete_thread(self, thread_id: str) -> None:
        if self._checkpoint_store is not None:
            await self._checkpoint_store.delete(thread_id)

    def _is_security_violation(self, message: str) -> bool:
        lower_msg = message.lower()
        for kw in INJECTION_KEYWORDS:
            if kw.lower() in lower_msg:
                return True
        return False

    def _extract_venue_id(self, message: str) -> int:
        match = re.search(r"(\d+)号馆", message)
        if match:
            return int(match.group(1))
        cn_map = {"一": 1, "二": 2, "三": 3, "四": 4, "五": 5, "六": 6, "七": 7, "八": 8, "九": 9}
        cn_match = re.search(r"([一二三四五六七八九])号馆", message)
        if cn_match:
            return cn_map.get(cn_match.group(1), 1)
        return 1


    def _detect_intent(self, message: str) -> str:
        msg = message.lower()
        # 1. 资金争议/明确请求转人工
        if any(k in msg for k in ["转人工", "人工客服", "人工服务", "扣错钱", "重复扣款", "资金争议"]):
            return "handoff"
        # 2. 实时价格查询
        if any(k in msg for k in ["价格", "收费", "价目表", "多少钱"]) and any(k in msg for k in ["实时", "现在", "当前", "目前"]):
            return "realtime_price"
        # 3. 实时营业状态
        if any(k in msg for k in ["实时状态", "开门吗", "营业吗", "场地情况"]) or ("营业" in msg and "实时" in msg):
            return "realtime_status"
        # 默认 RAG 知识问答
        return "knowledge_qa"
