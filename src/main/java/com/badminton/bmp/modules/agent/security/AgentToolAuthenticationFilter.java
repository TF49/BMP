package com.badminton.bmp.modules.agent.security;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.modules.agent.config.AgentProperties;
import com.badminton.bmp.modules.agent.support.AgentConversationRateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Agent Tool 认证过滤器。
 *
 * <p>仅拦截 {@code /api/agent-tools/**}。这些接口不由前端 JWT 访问，而是由 Python Agent 服务调用，
 * 因此需要同时校验：</p>
 * <ol>
 *   <li>服务身份：{@code X-Agent-Service-Token} 与配置的静态服务凭证一致（常量时间比较）。</li>
 *   <li>用户上下文：{@code X-Agent-Context-Token} 为 Java 网关签发、未过期未篡改的短期签名上下文。</li>
 * </ol>
 *
 * <p>校验通过后，基于签名上下文中的角色与场馆范围构建 {@link org.springframework.security.core.Authentication}，
 * 使下游 Tool 复用 {@code SecurityUtils} 完成角色与资源归属校验，无需再次查询用户表。</p>
 *
 * <p>安全日志仅记录失败类型与 TraceId，绝不记录原始服务凭证、签名或密钥。</p>
 */
@Component
public class AgentToolAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AgentToolAuthenticationFilter.class);

    /** Tool 接口路径前缀，仅此前缀下的请求由本过滤器处理。 */
    public static final String TOOL_PATH_PREFIX = "/api/agent-tools";
    public static final String SERVICE_TOKEN_HEADER = "X-Agent-Service-Token";
    public static final String CONTEXT_TOKEN_HEADER = "X-Agent-Context-Token";
    public static final String TRACE_HEADER = "X-Agent-Trace-Id";
    /** 校验通过后的上下文以此属性名暴露给 Tool 控制器。 */
    public static final String CONTEXT_ATTRIBUTE = "agentContext";

    private final AgentProperties properties;
    private final AgentContextSigner signer;
    private final AgentReplayGuard replayGuard;
    private final AgentConversationRateLimiter rateLimiter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentToolAuthenticationFilter(AgentProperties properties, AgentContextSigner signer,
                                         AgentReplayGuard replayGuard,
                                         AgentConversationRateLimiter rateLimiter) {
        this.properties = properties;
        this.signer = signer;
        this.replayGuard = replayGuard;
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith(TOOL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_HEADER);

        if (!serviceTokenMatches(request.getHeader(SERVICE_TOKEN_HEADER))) {
            log.warn("Agent Tool 调用被拒绝: 服务身份校验失败, traceId={}", safeTrace(traceId));
            writeError(response, Result.unauthorized("服务身份校验失败"));
            return;
        }

        AgentContext context;
        try {
            context = signer.verify(request.getHeader(CONTEXT_TOKEN_HEADER));
        } catch (AgentContextException e) {
            log.warn("Agent Tool 调用被拒绝: 上下文校验失败, reason={}, traceId={}",
                    e.getReason(), safeTrace(traceId));
            writeError(response, Result.unauthorized("上下文校验失败"));
            return;
        }

        AgentConversationRateLimiter.Decision rateDecision = checkRateLimits(context, request);
        if (!rateDecision.allowed()) {
            log.warn("Agent Tool 调用被拒绝: 触发分层限流, traceId={}", safeTrace(traceId));
            response.setHeader("Retry-After", String.valueOf(rateDecision.retryAfterSeconds()));
            writeError(response, new Result<>(429, "请求过于频繁",
                    Map.of("retryAfterSeconds", rateDecision.retryAfterSeconds(), "retryable", true)));
            return;
        }

        if (!replayGuard.tryClaim(context, request)) {
            log.warn("Agent Tool 调用被拒绝: 检测到上下文重放, traceId={}", safeTrace(traceId));
            writeError(response, Result.unauthorized("上下文已使用或已失效"));
            return;
        }

        try {
            SecurityContextHolder.getContext().setAuthentication(buildAuthentication(context));
            request.setAttribute(CONTEXT_ATTRIBUTE, context);
            filterChain.doFilter(request, response);
        } finally {
            // Tool 调用为无状态请求，处理完成后立即清理，避免线程复用导致身份泄漏。
            SecurityContextHolder.clearContext();
        }
    }

    private boolean serviceTokenMatches(String provided) {
        String expected = properties.getServiceToken();
        if (!StringUtils.hasText(expected)) {
            log.error("bmp.agent.service-token 未配置, 拒绝所有 Agent Tool 调用");
            return false;
        }
        if (!StringUtils.hasText(provided)) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                provided.getBytes(StandardCharsets.UTF_8));
    }

    private AgentConversationRateLimiter.Decision checkRateLimits(
            AgentContext context, HttpServletRequest request) {
        AgentConversationRateLimiter.Decision[] decisions = {
                rateLimiter.tryAcquire("tool-user", context.userId(), 100, 60),
                rateLimiter.tryAcquire("tool-ip",
                        request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr(), 100, 60),
                rateLimiter.tryAcquire("tool-endpoint",
                        request.getMethod() + ':' + request.getRequestURI(), 200, 60),
                rateLimiter.tryAcquire(isReadMethod(request.getMethod()) ? "tool-read" : "tool-write",
                        "all", isReadMethod(request.getMethod()) ? 50 : 5, 60)
        };
        for (AgentConversationRateLimiter.Decision decision : decisions) {
            if (!decision.allowed()) {
                return decision;
            }
        }
        return new AgentConversationRateLimiter.Decision(true, 0);
    }

    private boolean isReadMethod(String method) {
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method);
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(AgentContext context) {
        String role = context.role();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                StringUtils.hasText(role) ? "ROLE_" + role.toUpperCase() : "ROLE_USER");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                context.userId(), null, Collections.singletonList(authority));
        Map<String, Object> details = new HashMap<>();
        details.put("userId", context.userId());
        details.put("venueId", context.venueId());
        details.put("agentTool", Boolean.TRUE);
        authentication.setDetails(details);
        return authentication;
    }

    private void writeError(HttpServletResponse response, Result<?> body) throws IOException {
        response.setStatus(body.getCode());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private static String safeTrace(String traceId) {
        return StringUtils.hasText(traceId) ? traceId : "-";
    }
}
