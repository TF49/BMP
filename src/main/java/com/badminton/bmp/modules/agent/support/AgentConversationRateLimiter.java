package com.badminton.bmp.modules.agent.support;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/**
 * Agent 会话内存限流器（第五防线：请求限流）。
 *
 * <p>按用户维度使用固定窗口计数，开发/单机环境无需依赖 Redis。超过阈值时返回本窗口
 * 剩余的可重试等待秒数，供上层构造 429 响应。分布式令牌桶留待 P3 用 Redis + Lua 实现。</p>
 */
@Component
public class AgentConversationRateLimiter {

    /** 单用户每个时间窗口内允许的最大请求数。 */
    static final int MAX_REQUESTS_PER_WINDOW = 20;
    /** 时间窗口长度（秒）。 */
    static final long WINDOW_SECONDS = 60;
    /** 内存限流器存储的最大 Key 数量上限。 */
    static final int MAX_KEYS = 10000;

    private final Clock clock;
    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();

    public AgentConversationRateLimiter() {
        this(Clock.systemUTC());
    }

    public AgentConversationRateLimiter(Clock clock) {
        this.clock = clock;
    }

    /**
     * 尝试为指定用户放行一次请求。
     *
     * @param userId 当前用户标识，空值一律拒绝
     * @return 放行决策，含是否允许与可重试等待秒数
     */
    public Decision tryAcquire(String userId) {
        return tryAcquire("user", userId, MAX_REQUESTS_PER_WINDOW, WINDOW_SECONDS);
    }

    public Decision tryAcquire(String dimension, String key, int maxRequests, long windowSeconds) {
        if (dimension == null || dimension.isBlank() || key == null || key.isBlank()
                || maxRequests <= 0 || windowSeconds <= 0) {
            return new Decision(false, Math.max(windowSeconds, 1));
        }
        long now = clock.instant().getEpochSecond();
        long windowStart = now - Math.floorMod(now, windowSeconds);
        String compoundKey = dimension + ':' + key;

        windows.values().removeIf(w -> w.windowStart() + w.windowSeconds() <= now);

        // 内存上限防爆兜底
        if (!windows.containsKey(compoundKey) && windows.size() >= MAX_KEYS) {
            return new Decision(false, windowSeconds);
        }

        Window updated = windows.compute(compoundKey, (ignored, current) -> {
            if (current == null || current.windowStart() != windowStart
                    || current.windowSeconds() != windowSeconds) {
                return new Window(windowStart, windowSeconds, 1);
            }
            return new Window(windowStart, windowSeconds, current.count() + 1);
        });
        if (updated.count() <= maxRequests) {
            return new Decision(true, 0);
        }
        long retryAfter = windowStart + windowSeconds - now;
        return new Decision(false, Math.max(retryAfter, 1));
    }

    /** 限流决策结果。 */
    public record Decision(boolean allowed, long retryAfterSeconds) {
    }

    private record Window(long windowStart, long windowSeconds, int count) {
    }
}
