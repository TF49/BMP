package com.badminton.bmp.modules.agent.security;

import com.badminton.bmp.modules.agent.config.AgentProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 短期 Agent 上下文签名器。
 *
 * <p>令牌格式：{@code base64url(payloadJson) + "." + base64url(hmacSha256(secret, payloadJsonBytes))}。
 * 校验时对传输的原始 payload 字节重新计算 HMAC，因此不依赖 JSON 字段顺序。
 * payload 字段名使用 snake_case，与 Python 侧验签保持一致。</p>
 *
 * <p>签名密钥仅通过 {@link AgentProperties#getContextSecret()}（环境变量）注入，日志中不记录原始签名或密钥。</p>
 */
@Component
public class AgentContextSigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final AgentProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom secureRandom = new SecureRandom();

    public AgentContextSigner(AgentProperties properties) {
        this.properties = properties;
    }

    /**
     * 基于已认证身份签发一个新的短期上下文（自动填充签发时间、过期时间与随机 nonce）。
     */
    public AgentContext issue(String userId, String role, Long venueId, String traceId) {
        long now = System.currentTimeMillis();
        long expiresAt = now + properties.getContextTtlSeconds() * 1000L;
        return new AgentContext(userId, role, venueId, now, expiresAt, newNonce(), traceId);
    }

    /** 对上下文签名，返回可放入 {@code X-Agent-Context-Token} 头的令牌。 */
    public String sign(AgentContext context) {
        byte[] payloadBytes = toPayloadBytes(context);
        byte[] signature = hmac(payloadBytes);
        return URL_ENCODER.encodeToString(payloadBytes) + "." + URL_ENCODER.encodeToString(signature);
    }

    /**
     * 校验令牌并还原上下文。校验签名、结构与有效期；失败时抛出 {@link AgentContextException}。
     */
    public AgentContext verify(String token) {
        if (token == null || token.isBlank()) {
            throw new AgentContextException(AgentContextException.Reason.MISSING, "missing agent context token");
        }
        int dot = token.indexOf('.');
        if (dot <= 0 || dot != token.lastIndexOf('.') || dot == token.length() - 1) {
            throw new AgentContextException(AgentContextException.Reason.MALFORMED, "malformed agent context token");
        }

        byte[] payloadBytes;
        byte[] signature;
        try {
            payloadBytes = URL_DECODER.decode(token.substring(0, dot));
            signature = URL_DECODER.decode(token.substring(dot + 1));
        } catch (IllegalArgumentException e) {
            throw new AgentContextException(AgentContextException.Reason.MALFORMED, "invalid base64 in agent context token");
        }

        byte[] expected = hmac(payloadBytes);
        if (!MessageDigest.isEqual(expected, signature)) {
            throw new AgentContextException(AgentContextException.Reason.TAMPERED, "agent context signature mismatch");
        }

        AgentContext context = parsePayload(payloadBytes);
        if (context.expiresAt() <= System.currentTimeMillis()) {
            throw new AgentContextException(AgentContextException.Reason.EXPIRED, "agent context expired");
        }
        return context;
    }

    private byte[] toPayloadBytes(AgentContext context) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("user_id", context.userId());
        node.put("role", context.role());
        if (context.venueId() == null) {
            node.putNull("venue_id");
        } else {
            node.put("venue_id", context.venueId());
        }
        node.put("issued_at", context.issuedAt());
        node.put("expires_at", context.expiresAt());
        node.put("nonce", context.nonce());
        node.put("trace_id", context.traceId());
        try {
            return objectMapper.writeValueAsBytes(node);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // ObjectNode 序列化不会发生此异常，仅为满足编译。
            throw new IllegalStateException("failed to serialize agent context", e);
        }
    }

    private AgentContext parsePayload(byte[] payloadBytes) {
        try {
            JsonNode node = objectMapper.readTree(payloadBytes);
            String userId = requireText(node, "user_id");
            String role = requireText(node, "role");
            JsonNode venueNode = node.get("venue_id");
            Long venueId = (venueNode == null || venueNode.isNull()) ? null : venueNode.asLong();
            long issuedAt = requireNumber(node, "issued_at");
            long expiresAt = requireNumber(node, "expires_at");
            String nonce = requireText(node, "nonce");
            String traceId = requireText(node, "trace_id");
            return new AgentContext(userId, role, venueId, issuedAt, expiresAt, nonce, traceId);
        } catch (AgentContextException e) {
            throw e;
        } catch (Exception e) {
            throw new AgentContextException(AgentContextException.Reason.MALFORMED, "unable to parse agent context payload");
        }
    }

    private static String requireText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !value.isTextual() || value.asText().isBlank()) {
            throw new AgentContextException(AgentContextException.Reason.MALFORMED, "missing field: " + field);
        }
        return value.asText();
    }

    private static long requireNumber(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !value.isNumber()) {
            throw new AgentContextException(AgentContextException.Reason.MALFORMED, "missing field: " + field);
        }
        return value.asLong();
    }

    private byte[] hmac(byte[] data) {
        String secret = properties.getContextSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("bmp.agent.context-secret is not configured");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("failed to compute agent context signature", e);
        }
    }

    private String newNonce() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
