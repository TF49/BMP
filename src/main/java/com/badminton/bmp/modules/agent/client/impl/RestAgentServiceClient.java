package com.badminton.bmp.modules.agent.client.impl;

import com.badminton.bmp.common.exception.ServiceUnavailableException;
import com.badminton.bmp.modules.agent.client.AgentServiceClient;
import com.badminton.bmp.modules.agent.config.AgentProperties;
import com.badminton.bmp.modules.agent.enums.AgentType;
import com.badminton.bmp.modules.agent.security.AgentContext;
import com.badminton.bmp.modules.agent.security.AgentContextSigner;
import com.badminton.bmp.modules.agent.support.AgentServiceCircuitBreaker;
import com.badminton.bmp.modules.agent.vo.AgentResponseVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于 {@link RestClient} 的 FastAPI 客户端实现。
 *
 * <p>连接与读取超时、基础地址均来自配置。上下文经 HMAC 签名后通过
 * {@code X-Agent-Context-Token} 头透传；网络瞬时错误统一转为 503（可重试），
 * 不向上层暴露供应商原始堆栈。</p>
 */
@Component
public class RestAgentServiceClient implements AgentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(RestAgentServiceClient.class);
    private static final String PROCESS_PATH = "/api/v1/agent/process";
    private static final String CONTEXT_TOKEN_HEADER = "X-Agent-Context-Token";
    private static final String TRACE_HEADER = "X-Agent-Trace-Id";

    private final AgentContextSigner signer;
    private final AgentServiceCircuitBreaker circuitBreaker;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RestAgentServiceClient(AgentProperties properties, AgentContextSigner signer,
                                  AgentServiceCircuitBreaker circuitBreaker) {
        this.signer = signer;
        this.circuitBreaker = circuitBreaker;
        java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
        org.springframework.http.client.JdkClientHttpRequestFactory factory =
                new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(java.time.Duration.ofMillis(properties.getReadTimeoutMs()));

        this.restClient = RestClient.builder()
                .baseUrl(properties.getFastapiBaseUrl())
                .requestFactory(factory)
                .build();
    }

    @Override
    public AgentResponseVO process(String conversationId, AgentType agentType, String content,
                                   String messageId, AgentContext context) {
        if (!circuitBreaker.allowRequest()) {
            // 熔断打开：快速失败并静态降级，不再打通下游，避免拖垮主服务。
            log.warn("Agent 熔断已打开，快速失败, traceId={}", context.traceId());
            throw new ServiceUnavailableException("智能助手繁忙，请稍后重试");
        }
        String token = signer.sign(context);
        Map<String, Object> body = buildBody(conversationId, agentType, content, messageId);

        JsonNode root;
        try {
            root = restClient.post()
                    .uri(PROCESS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(CONTEXT_TOKEN_HEADER, token)
                    .header(TRACE_HEADER, context.traceId())
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            // 连接失败、超时或非 2xx，统一降级为可重试的服务不可用，避免暴露内部细节。
            circuitBreaker.recordFailure();
            log.warn("调用 FastAPI 失败, traceId={}: {}", context.traceId(), e.getMessage());
            throw new ServiceUnavailableException("智能助手暂时不可用，请稍后重试");
        }

        try {
            AgentResponseVO vo = mapResponse(conversationId, context.traceId(), root);
            circuitBreaker.recordSuccess();
            return vo;
        } catch (ServiceUnavailableException e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }

    private Map<String, Object> buildBody(String conversationId, AgentType agentType,
                                          String content, String messageId) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("content", content);
        if (messageId != null && !messageId.isBlank()) {
            message.put("message_id", messageId);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("conversation_id", conversationId);
        // Python 侧 AgentType 使用小写字符串枚举值。
        body.put("agent_type", agentType.name().toLowerCase());
        body.put("message", message);
        return body;
    }

    private AgentResponseVO mapResponse(String conversationId, String traceId, JsonNode root) {
        if (root == null) {
            throw new ServiceUnavailableException("智能助手暂时不可用，请稍后重试");
        }
        JsonNode data = root.get("data");
        if (data == null || data.isNull()) {
            log.warn("FastAPI 返回空 data, traceId={}", traceId);
            throw new ServiceUnavailableException("智能助手暂时不可用，请稍后重试");
        }

        AgentResponseVO vo = new AgentResponseVO();
        vo.setConversationId(conversationId);
        vo.setTraceId(traceId);
        vo.setResponse(text(data, "response"));
        String type = text(data, "type");
        vo.setType(type != null ? type : "text");
        vo.setRequiresAction(data.path("requires_action").asBoolean(false));
        vo.setActions(toMapList(data.get("actions")));
        vo.setReferences(toMapList(data.get("references")));
        return vo;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toMapList(JsonNode node) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        if (node == null || !node.isArray()) {
            return result;
        }
        for (JsonNode item : node) {
            if (item != null && item.isObject()) {
                result.add(new HashMap<>(objectMapper.convertValue(item, Map.class)));
            }
        }
        return result;
    }
}
