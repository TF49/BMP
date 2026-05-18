package com.badminton.bmp.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * 业务订单支付超时自动取消配置。
 *
 * <p>当前统一口径：</p>
 * <ul>
 *   <li>主配置项：{@code bmp.payment.auto-cancel.timeout-seconds}</li>
 *   <li>扫描频率：{@code bmp.payment.auto-cancel.scan-interval-ms}</li>
 *   <li>兼容旧配置：{@code timeout-minutes} 仅保留为兼容输入，不作为新配置主写法</li>
 * </ul>
 */
@Component
@Validated
@ConfigurationProperties(prefix = "bmp.payment.auto-cancel")
public class PaymentAutoCancelProperties {

    /**
     * 是否启用支付超时自动取消。
     */
    private boolean enabled = true;

    /**
     * 主配置项：支付超时秒数。
     */
    @Min(1)
    private long timeoutSeconds = 300;

    /**
     * 定时扫描频率，单位毫秒。
     */
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

    /**
     * 返回供接口展示和旧前端兼容使用的分钟值。
     */
    public double getTimeoutMinutesForDisplay() {
        return getTimeoutSeconds() / 60.0;
    }

    /**
     * 返回最终生效的超时时长。
     */
    public Duration getTimeoutDuration() {
        return Duration.ofSeconds(getTimeoutSeconds());
    }
}
