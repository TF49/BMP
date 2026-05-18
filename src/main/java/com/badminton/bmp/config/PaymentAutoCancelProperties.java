package com.badminton.bmp.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 业务订单支付超时自动取消配置
 */
@Component
@Validated
@ConfigurationProperties(prefix = "bmp.payment.auto-cancel")
public class PaymentAutoCancelProperties {

    private boolean enabled = true;

    @Min(1)
    private long timeoutSeconds = 300;

    @Min(1)
    private long scanIntervalMs = 60000;

    /**
     * 兼容旧配置：若仍传 timeout-minutes，则优先按分钟值换算。
     */
    @Min(1)
    private Long timeoutMinutes;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTimeoutSeconds() {
        if (timeoutMinutes != null) {
            return timeoutMinutes * 60L;
        }
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public long getScanIntervalMs() {
        return scanIntervalMs;
    }

    public void setScanIntervalMs(long scanIntervalMs) {
        this.scanIntervalMs = scanIntervalMs;
    }

    public Long getTimeoutMinutes() {
        return timeoutMinutes;
    }

    public void setTimeoutMinutes(Long timeoutMinutes) {
        this.timeoutMinutes = timeoutMinutes;
    }

    public double getTimeoutMinutesForDisplay() {
        return getTimeoutSeconds() / 60.0;
    }

    public Duration getTimeoutDuration() {
        return Duration.ofSeconds(getTimeoutSeconds());
    }
}
