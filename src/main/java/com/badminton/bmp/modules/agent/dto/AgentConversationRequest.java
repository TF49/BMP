package com.badminton.bmp.modules.agent.dto;

import com.badminton.bmp.modules.agent.enums.AgentType;

import jakarta.validation.constraints.NotNull;

/**
 * 创建 Agent 会话请求。仅接收 Agent 类型；用户身份一律来自 Spring Security 上下文，
 * 禁止信任请求体中的任何 userId、role 或 venueId。
 */
public class AgentConversationRequest {

    @NotNull(message = "agentType 不能为空")
    private AgentType agentType;

    public AgentType getAgentType() {
        return agentType;
    }

    public void setAgentType(AgentType agentType) {
        this.agentType = agentType;
    }
}
