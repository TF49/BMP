package com.badminton.bmp.modules.agent.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.agent.dto.AgentActionRequest;
import com.badminton.bmp.modules.agent.dto.AgentConversationRequest;
import com.badminton.bmp.modules.agent.dto.AgentMessageRequest;
import com.badminton.bmp.modules.agent.service.AgentGatewayService;
import com.badminton.bmp.modules.agent.vo.AgentConversationVO;
import com.badminton.bmp.modules.agent.vo.AgentResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 面向前端的 Agent 网关。用户身份由 JWT 认证过滤器建立，控制器不接收任何身份字段。
 */
@RestController
@RequestMapping("/api/agent")
@Validated
public class AgentController extends BaseController {

    private final AgentGatewayService agentGatewayService;

    public AgentController(AgentGatewayService agentGatewayService) {
        this.agentGatewayService = agentGatewayService;
    }

    /** 创建指定类型的会话。 */
    @PostMapping("/conversations")
    public Result<AgentConversationVO> createConversation(@Valid @RequestBody AgentConversationRequest request) {
        return success(agentGatewayService.createConversation(request.getAgentType()));
    }

    /** 发送一条用户消息。 */
    @PostMapping("/conversations/{id}/messages")
    public Result<AgentResponseVO> sendMessage(
            @PathVariable("id") @NotBlank String conversationId,
            @Valid @RequestBody AgentMessageRequest request) {
        return success(agentGatewayService.sendMessage(conversationId, request));
    }

    /** 确认一次待执行动作。 */
    @PostMapping("/conversations/{id}/actions/{actionId}/confirm")
    public Result<AgentResponseVO> confirmAction(
            @PathVariable("id") @NotBlank String conversationId,
            @PathVariable("actionId") @NotBlank String actionId,
            @Valid @RequestBody(required = false) AgentActionRequest request) {
        return success(agentGatewayService.confirmAction(conversationId, actionId, request));
    }

    /** 拒绝一次待执行动作。 */
    @PostMapping("/conversations/{id}/actions/{actionId}/reject")
    public Result<AgentResponseVO> rejectAction(
            @PathVariable("id") @NotBlank String conversationId,
            @PathVariable("actionId") @NotBlank String actionId,
            @Valid @RequestBody(required = false) AgentActionRequest request) {
        return success(agentGatewayService.rejectAction(conversationId, actionId, request));
    }
}
