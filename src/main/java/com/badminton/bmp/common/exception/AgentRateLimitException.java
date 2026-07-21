package com.badminton.bmp.common.exception;

/**
 * Agent 网关限流异常，携带可重试等待秒数。
 *
 * <p>继承 {@link RateLimitException}（HTTP 429），由 {@code GlobalExceptionHandler}
 * 专用处理器把 {@code retryAfterSeconds} 写入响应，供前端做退避重试。</p>
 */
public class AgentRateLimitException extends RateLimitException {

    private final long retryAfterSeconds;

    public AgentRateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
