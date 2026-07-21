package com.badminton.bmp.modules.agent.security;

/**
 * Agent 上下文签名校验失败异常。仅携带失败原因分类，用于安全日志记录，
 * 不包含原始签名或密钥。统一由过滤器/异常处理转换为 401。
 */
public class AgentContextException extends RuntimeException {

    /** 失败原因分类，仅用于日志与监控，不直接暴露给终端用户。 */
    public enum Reason {
        /** 缺少上下文令牌 */
        MISSING,
        /** 格式非法（分段、Base64、JSON 解析失败等） */
        MALFORMED,
        /** 签名不匹配（篡改或密钥错误） */
        TAMPERED,
        /** 已过期 */
        EXPIRED
    }

    private final Reason reason;

    public AgentContextException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
