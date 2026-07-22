package com.badminton.bmp.modules.agent.service;

import com.badminton.bmp.modules.agent.dto.AgentActionRequest;
import com.badminton.bmp.modules.agent.dto.AgentMessageRequest;
import com.badminton.bmp.modules.agent.enums.AgentType;
import com.badminton.bmp.modules.agent.vo.AgentConversationVO;
import com.badminton.bmp.modules.agent.vo.AgentResponseVO;

/**
 * Agent 网关服务。负责基于已认证身份签发短期上下文并转发到 FastAPI，
 * 所有用户身份一律取自 Spring Security，绝不信任请求体。
 */
public interface AgentGatewayService {

    /** 创建指定类型的会话。 */
    AgentConversationVO createConversation(AgentType agentType);

    /** 向会话发送一条用户消息。 */
    AgentResponseVO sendMessage(String conversationId, AgentMessageRequest request);

    /** 确认一次待执行动作。 */
    AgentResponseVO confirmAction(String conversationId, String actionId, AgentActionRequest request);

    /** 拒绝一次待执行动作。 */
    AgentResponseVO rejectAction(String conversationId, String actionId, AgentActionRequest request);

    /** 查询本人会话摘要。仅返回属于当前用户的会话信息。 */
    AgentConversationVO getConversation(String conversationId);

    /** 删除本人会话。仅允许删除属于当前用户的会话，包括关联的 Checkpoint 和消息历史。 */
    void deleteConversation(String conversationId);
}
