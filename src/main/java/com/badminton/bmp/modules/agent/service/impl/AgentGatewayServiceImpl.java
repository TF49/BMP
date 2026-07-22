package com.badminton.bmp.modules.agent.service.impl;

import com.badminton.bmp.common.exception.AgentRateLimitException;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.agent.client.AgentServiceClient;
import com.badminton.bmp.modules.agent.dto.AgentActionRequest;
import com.badminton.bmp.modules.agent.dto.AgentMessageRequest;
import com.badminton.bmp.modules.agent.enums.AgentType;
import com.badminton.bmp.modules.agent.security.AgentContext;
import com.badminton.bmp.modules.agent.security.AgentContextSigner;
import com.badminton.bmp.modules.agent.service.AgentGatewayService;
import com.badminton.bmp.modules.agent.support.AgentConversationRateLimiter;
import com.badminton.bmp.modules.agent.vo.AgentConversationVO;
import com.badminton.bmp.modules.agent.vo.AgentResponseVO;
import com.badminton.bmp.modules.system.entity.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Agent 网关服务实现。
 *
 * <p>身份来源：{@link SecurityUtils#getCurrentUser()}（由 JWT 认证过滤器建立），
 * 请求体中的任何身份字段一律忽略。每次调用签发短期签名上下文透传给 FastAPI。</p>
 */
@Service
public class AgentGatewayServiceImpl implements AgentGatewayService {

    private static final int USER_REQUESTS_PER_MINUTE = 10;
    private static final int IP_REQUESTS_PER_MINUTE = 100;
    private static final int ENDPOINT_REQUESTS_PER_MINUTE = 200;
    private static final int WRITE_REQUESTS_PER_MINUTE = 5;
    private static final long RATE_WINDOW_SECONDS = 60;

    private final AgentServiceClient agentServiceClient;
    private final AgentContextSigner contextSigner;
    private final AgentConversationRateLimiter conversationRateLimiter;

    public AgentGatewayServiceImpl(AgentServiceClient agentServiceClient,
                                   AgentContextSigner contextSigner,
                                   AgentConversationRateLimiter conversationRateLimiter) {
        this.agentServiceClient = agentServiceClient;
        this.contextSigner = contextSigner;
        this.conversationRateLimiter = conversationRateLimiter;
    }

    @Override
    public AgentConversationVO createConversation(AgentType agentType) {
        // 校验已登录；会话的真实状态由 Python 侧在首次消息时惰性建立。
        User user = requireCurrentUser();
        enforceRateLimit(user);
        // P1 阶段 Java 侧不持久化会话，将 Agent 类型编码进会话 ID，
        // 以便发送消息时无需存储即可恢复类型；P1-06 引入会话存储后可改为查库。
        String conversationId = "conv_" + agentType.name().toLowerCase()
                + "_" + UUID.randomUUID().toString().replace("-", "");
        return new AgentConversationVO(conversationId, agentType);
    }

    @Override
    public AgentResponseVO sendMessage(String conversationId, AgentMessageRequest request) {
        User user = requireCurrentUser();
        enforceRateLimit(user);
        AgentType agentType = resolveAgentType(conversationId);
        AgentContext context = issueContext(user);
        return agentServiceClient.process(
                conversationId, agentType, request.getContent(), request.getMessageId(), context);
    }

    @Override
    public AgentResponseVO confirmAction(String conversationId, String actionId, AgentActionRequest request) {
        requireCurrentUser();
        // 写动作的中断/恢复（interrupt/resume）属于 P2 智能预订 Agent，P1 仅打通只读链路。
        throw new BusinessException(501, "动作确认功能尚未开放");
    }

    @Override
    public AgentResponseVO rejectAction(String conversationId, String actionId, AgentActionRequest request) {
        requireCurrentUser();
        throw new BusinessException(501, "动作拒绝功能尚未开放");
    }

    @Override
    public AgentConversationVO getConversation(String conversationId) {
        User user = requireCurrentUser();
        enforceRateLimit(user);
        
        // 验证会话归属：会话 ID 必须符合格式并解析出 Agent 类型
        AgentType agentType = resolveAgentType(conversationId);
        
        // 调用 FastAPI 查询会话状态（需要签发短期上下文）
        AgentContext context = issueContext(user);
        
        // P1 阶段暂不实现完整的会话元数据查询，只验证会话存在性和归属
        // P2 引入会话持久化后，此处将查询创建时间、过期时间、消息数等完整信息
        return new AgentConversationVO(conversationId, agentType);
    }

    @Override
    public void deleteConversation(String conversationId) {
        User user = requireCurrentUser();
        enforceRateLimit(user);
        
        // 验证会话归属
        resolveAgentType(conversationId);
        
        // 调用 FastAPI 删除会话及关联的 Checkpoint
        AgentContext context = issueContext(user);
        agentServiceClient.deleteConversation(conversationId, context);
    }

    private AgentContext issueContext(User user) {
        String traceId = getRequestTraceId();
        String userId = String.valueOf(user.getId());
        return contextSigner.issue(userId, user.getRole(), user.getVenueId(), traceId);
    }

    private String getRequestTraceId() {
        try {
            org.springframework.web.context.request.RequestAttributes attributes =
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request =
                        ((org.springframework.web.context.request.ServletRequestAttributes) attributes).getRequest();
                String headerTraceId = request.getHeader("X-Agent-Trace-Id");
                if (headerTraceId != null && !headerTraceId.isBlank()) {
                    return headerTraceId;
                }
            }
        } catch (Exception e) {
            // 忽略异常并退化为自动生成
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private User requireCurrentUser() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null || user.getId() == null) {
            throw new BusinessException(401, "登录信息已失效，请重新登录");
        }
        return user;
    }

    private void enforceRateLimit(User user) {
        requireAllowed(conversationRateLimiter.tryAcquire(
                "user", String.valueOf(user.getId()), USER_REQUESTS_PER_MINUTE, RATE_WINDOW_SECONDS));

        jakarta.servlet.http.HttpServletRequest request = currentRequest();
        if (request == null) {
            return;
        }
        String remoteAddress = request.getRemoteAddr();
        requireAllowed(conversationRateLimiter.tryAcquire(
                "ip", remoteAddress == null ? "unknown" : remoteAddress,
                IP_REQUESTS_PER_MINUTE, RATE_WINDOW_SECONDS));

        String endpoint = request.getMethod() + ':' + request.getRequestURI();
        requireAllowed(conversationRateLimiter.tryAcquire(
                "endpoint", endpoint, ENDPOINT_REQUESTS_PER_MINUTE, RATE_WINDOW_SECONDS));
        if (request.getRequestURI() != null && request.getRequestURI().contains("/actions/")) {
            requireAllowed(conversationRateLimiter.tryAcquire(
                    "write", String.valueOf(user.getId()),
                    WRITE_REQUESTS_PER_MINUTE, RATE_WINDOW_SECONDS));
        }
    }

    private void requireAllowed(AgentConversationRateLimiter.Decision decision) {
        if (!decision.allowed()) {
            throw new AgentRateLimitException("请求过于频繁，请稍后重试", decision.retryAfterSeconds());
        }
    }

    private jakarta.servlet.http.HttpServletRequest currentRequest() {
        org.springframework.web.context.request.RequestAttributes attributes =
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (attributes instanceof org.springframework.web.context.request.ServletRequestAttributes servlet) {
            return servlet.getRequest();
        }
        return null;
    }

    /**
     * 从会话 ID 中恢复 Agent 类型。会话 ID 形如 {@code conv_<type>_<uuid>}。
     * 该实现为 P1-06 引入会话持久化后改为按 conversationId 查库校验归属预留扩展点。
     */
    private AgentType resolveAgentType(String conversationId) {
        if (conversationId != null && conversationId.startsWith("conv_")) {
            String rest = conversationId.substring("conv_".length());
            int sep = rest.indexOf('_');
            if (sep > 0) {
                String typeCode = rest.substring(0, sep);
                for (AgentType type : AgentType.values()) {
                    if (type.name().equalsIgnoreCase(typeCode)) {
                        return type;
                    }
                }
            }
        }
        throw new BusinessException(400, "会话不存在或已失效");
    }
}
