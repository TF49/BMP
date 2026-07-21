package com.badminton.bmp.modules.agent.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent 处理结果的前端响应视图，映射自 FastAPI 的 AgentResult。
 * 仅暴露前端所需的稳定字段，不透传模型内部 metadata、Prompt 或供应商原始错误。
 */
public class AgentResponseVO {

    private String conversationId;
    private String response;
    private String type = "text";
    private boolean requiresAction;
    private List<Map<String, Object>> actions = new ArrayList<>();
    private List<Map<String, Object>> references = new ArrayList<>();
    private String traceId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequiresAction() {
        return requiresAction;
    }

    public void setRequiresAction(boolean requiresAction) {
        this.requiresAction = requiresAction;
    }

    public List<Map<String, Object>> getActions() {
        return actions;
    }

    public void setActions(List<Map<String, Object>> actions) {
        this.actions = actions;
    }

    public List<Map<String, Object>> getReferences() {
        return references;
    }

    public void setReferences(List<Map<String, Object>> references) {
        this.references = references;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
