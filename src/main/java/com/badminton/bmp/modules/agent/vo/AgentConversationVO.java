package com.badminton.bmp.modules.agent.vo;

import com.badminton.bmp.modules.agent.enums.AgentType;

/**
 * 创建会话响应。会话真正的状态由 Python 侧在首次处理消息时惰性建立，
 * Java 网关只负责生成会话标识并回显 Agent 类型。
 */
public class AgentConversationVO {

    private String conversationId;
    private AgentType agentType;

    public AgentConversationVO() {
    }

    public AgentConversationVO(String conversationId, AgentType agentType) {
        this.conversationId = conversationId;
        this.agentType = agentType;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public AgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(AgentType agentType) {
        this.agentType = agentType;
    }
}
