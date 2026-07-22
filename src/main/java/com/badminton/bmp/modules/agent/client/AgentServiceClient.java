package com.badminton.bmp.modules.agent.client;

import com.badminton.bmp.modules.agent.enums.AgentType;
import com.badminton.bmp.modules.agent.security.AgentContext;
import com.badminton.bmp.modules.agent.vo.AgentResponseVO;

/**
 * FastAPI Agent 服务客户端。负责签名上下文透传与响应解析，屏蔽底层 HTTP 细节。
 */
public interface AgentServiceClient {

    /**
     * 调用 FastAPI 处理一条用户消息。
     *
     * @param conversationId 会话 ID
     * @param agentType      Agent 类型
     * @param content        用户消息内容
     * @param messageId      客户端消息 ID，可为 null
     * @param context        已签发的短期用户上下文（客户端负责签名透传）
     * @return Agent 处理结果视图
     */
    AgentResponseVO process(String conversationId, AgentType agentType, String content,
                            String messageId, AgentContext context);

    /**
     * 删除指定会话及关联的 Checkpoint 和消息历史。
     *
     * @param conversationId 会话 ID
     * @param context        已签发的短期用户上下文
     */
    void deleteConversation(String conversationId, AgentContext context);
}
