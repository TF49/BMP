package com.badminton.bmp.modules.agent.security;

/**
 * 短期 Agent 用户上下文。由 Java 网关基于已认证的 JWT 身份签发，
 * 通过 HMAC-SHA256 签名后随 Tool 调用透传，供 Java Tool 层与 Python 侧校验。
 *
 * <p>时间字段使用 epoch 毫秒。JSON 序列化字段名为 snake_case，与 Python 侧保持一致。</p>
 *
 * @param userId    用户 ID
 * @param role      用户角色（不含 ROLE_ 前缀），例如 USER / MEMBER / VENUE_MANAGER / PRESIDENT
 * @param venueId   用户所属场馆 ID，可为 null（例如平台会长）
 * @param issuedAt  签发时间（epoch 毫秒）
 * @param expiresAt 过期时间（epoch 毫秒）
 * @param nonce     一次性随机数，用于防重放
 * @param traceId   贯穿链路的 TraceId
 */
public record AgentContext(
        String userId,
        String role,
        Long venueId,
        long issuedAt,
        long expiresAt,
        String nonce,
        String traceId) {
}
