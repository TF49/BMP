package com.badminton.bmp.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 登录限流器
 * 基于内存的限流实现，防止暴力破解攻击
 *
 * 限流规则：
 * - 同一IP 1分钟内最多尝试10次登录
 * - 同一用户名 1分钟内最多尝试5次登录
 * - 连续失败5次后，锁定账户15分钟
 */
@Component
public class LoginRateLimiter {

    private static final Logger logger = LoggerFactory.getLogger(LoginRateLimiter.class);

    // IP登录尝试记录
    private final ConcurrentHashMap<String, LoginAttempt> ipAttempts = new ConcurrentHashMap<>();

    // 用户名登录尝试记录
    private final ConcurrentHashMap<String, LoginAttempt> usernameAttempts = new ConcurrentHashMap<>();

    // 账户锁定记录
    private final ConcurrentHashMap<String, Long> lockedAccounts = new ConcurrentHashMap<>();

    // IP限流配置
    private static final int IP_MAX_ATTEMPTS = 10;
    private static final long IP_WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(1);

    // 用户名限流配置
    private static final int USERNAME_MAX_ATTEMPTS = 5;
    private static final long USERNAME_WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(1);

    // 账户锁定配置 - 降低阈值便于测试
    private static final int LOCK_THRESHOLD = 3;  // 连续失败3次后锁定（原为5）
    private static final long LOCK_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(5);  // 锁定5分钟（原为15分钟）

    /**
     * 检查是否允许登录
     * @param ip 客户端IP
     * @param username 用户名
     * @return 限流结果
     */
    public RateLimitResult checkRateLimit(String ip, String username) {
        long now = System.currentTimeMillis();

        // 1. 检查账户是否被锁定
        Long lockExpireTime = lockedAccounts.get(username);
        if (lockExpireTime != null) {
            if (now < lockExpireTime) {
                long remainingSeconds = (lockExpireTime - now) / 1000;
                logger.warn("账户已锁定: {}, 剩余锁定时间: {}秒", username, remainingSeconds);
                return new RateLimitResult(false,
                        String.format("账户已锁定，请%d分钟后重试", remainingSeconds / 60 + 1));
            } else {
                // 锁定已过期，移除锁定记录
                lockedAccounts.remove(username);
            }
        }

        // 2. 检查IP限流
        LoginAttempt ipAttempt = ipAttempts.compute(ip, (k, v) -> {
            if (v == null || now - v.firstAttemptTime > IP_WINDOW_MILLIS) {
                return new LoginAttempt(now, 1);
            }
            v.attemptCount++;
            return v;
        });

        if (ipAttempt.attemptCount > IP_MAX_ATTEMPTS) {
            logger.warn("IP限流触发: {}, 尝试次数: {}", ip, ipAttempt.attemptCount);
            return new RateLimitResult(false, "请求过于频繁，请稍后重试");
        }

        // 3. 检查用户名限流
        LoginAttempt usernameAttempt = usernameAttempts.compute(username, (k, v) -> {
            if (v == null || now - v.firstAttemptTime > USERNAME_WINDOW_MILLIS) {
                return new LoginAttempt(now, 1);
            }
            v.attemptCount++;
            return v;
        });

        if (usernameAttempt.attemptCount > USERNAME_MAX_ATTEMPTS) {
            logger.warn("用户名限流触发: {}, 尝试次数: {}", username, usernameAttempt.attemptCount);
            return new RateLimitResult(false, "登录尝试次数过多，请稍后重试");
        }

        return new RateLimitResult(true, null);
    }

    /**
     * 记录登录失败
     * @param username 用户名
     */
    public void recordLoginFailure(String username) {
        long now = System.currentTimeMillis();

        // 更新失败次数
        LoginAttempt attempt = usernameAttempts.compute(username, (k, v) -> {
            if (v == null || now - v.firstAttemptTime > USERNAME_WINDOW_MILLIS) {
                return new LoginAttempt(now, 1);
            }
            v.attemptCount++;
            return v;
        });

        // 检查是否达到锁定阈值
        if (attempt.attemptCount >= LOCK_THRESHOLD) {
            // 达到锁定阈值，锁定账户
            long lockExpireTime = now + LOCK_DURATION_MILLIS;
            lockedAccounts.put(username, lockExpireTime);
            logger.warn("账户已被锁定: {}, 失败次数: {}, 锁定时间: {}分钟", username, attempt.attemptCount, LOCK_DURATION_MILLIS / 60000);
        }
    }

    /**
     * 登录成功，清除失败记录
     * @param ip 客户端IP
     * @param username 用户名
     */
    public void recordLoginSuccess(String ip, String username) {
        usernameAttempts.remove(username);
        // IP记录保留，防止换账号暴力破解
    }

    /**
     * 检查账户是否被锁定
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isAccountLocked(String username) {
        Long lockExpireTime = lockedAccounts.get(username);
        if (lockExpireTime != null) {
            if (System.currentTimeMillis() < lockExpireTime) {
                return true;
            } else {
                lockedAccounts.remove(username);
            }
        }
        return false;
    }

    /**
     * 获取账户剩余锁定时间（秒）
     * @param username 用户名
     * @return 剩余锁定时间（秒），未锁定返回0
     */
    public long getRemainingLockTime(String username) {
        Long lockExpireTime = lockedAccounts.get(username);
        if (lockExpireTime != null) {
            long remaining = (lockExpireTime - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        }
        return 0;
    }

    /**
     * 手动解锁账户（管理员使用）
     * @param username 用户名
     */
    public void unlockAccount(String username) {
        lockedAccounts.remove(username);
        usernameAttempts.remove(username);
        logger.info("账户已手动解锁: {}", username);
    }

    /**
     * 清理过期记录（定期调用）
     */
    public void cleanupExpiredRecords() {
        long now = System.currentTimeMillis();

        // 清理过期的IP记录
        ipAttempts.entrySet().removeIf(entry ->
                now - entry.getValue().firstAttemptTime > IP_WINDOW_MILLIS);

        // 清理过期的用户名记录
        usernameAttempts.entrySet().removeIf(entry ->
                now - entry.getValue().firstAttemptTime > USERNAME_WINDOW_MILLIS);

        // 清理过期的锁定记录
        lockedAccounts.entrySet().removeIf(entry -> now > entry.getValue());
    }

    /**
     * 登录尝试记录
     */
    private static class LoginAttempt {
        long firstAttemptTime;
        int attemptCount;

        LoginAttempt(long firstAttemptTime, int attemptCount) {
            this.firstAttemptTime = firstAttemptTime;
            this.attemptCount = attemptCount;
        }
    }

    /**
     * 限流结果
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final String message;

        public RateLimitResult(boolean allowed, String message) {
            this.allowed = allowed;
            this.message = message;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getMessage() {
            return message;
        }
    }
}
