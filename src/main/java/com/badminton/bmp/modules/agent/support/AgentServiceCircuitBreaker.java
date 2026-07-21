package com.badminton.bmp.modules.agent.support;

import java.time.Clock;
import org.springframework.stereotype.Component;

/**
 * Agent 服务调用的轻量熔断器（第六防线：熔断降级）。
 *
 * <p>三态机：{@code CLOSED}（正常放行）→ 连续失败达到阈值转 {@code OPEN}（快速失败）→
 * 冷却期结束后转 {@code HALF_OPEN}（放行一次试探）；试探成功回到 {@code CLOSED}，
 * 失败重新打开。与已有的超时与有限重试配合，避免下游故障拖垮 Java 主服务。</p>
 */
@Component
public class AgentServiceCircuitBreaker {

    /** 触发熔断的连续失败次数阈值。 */
    static final int FAILURE_THRESHOLD = 5;
    /** 熔断打开后的冷却时长（秒）。 */
    static final long OPEN_DURATION_SECONDS = 30L;

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final Clock clock;
    private State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private long openedAtEpochSecond = 0L;

    public AgentServiceCircuitBreaker() {
        this(Clock.systemUTC());
    }

    public AgentServiceCircuitBreaker(Clock clock) {
        this.clock = clock;
    }

    /**
     * 判断当前是否允许发起下游调用。
     *
     * <p>{@code OPEN} 状态在冷却期结束后会转入 {@code HALF_OPEN} 并放行一次试探。</p>
     */
    public synchronized boolean allowRequest() {
        if (state == State.OPEN) {
            long now = clock.instant().getEpochSecond();
            if (now - openedAtEpochSecond >= OPEN_DURATION_SECONDS) {
                state = State.HALF_OPEN;
                return true;
            }
            return false;
        }
        if (state == State.HALF_OPEN) {
            return false;
        }
        return true;
    }

    /** 记录一次成功调用：清零失败计数并关闭熔断。 */
    public synchronized void recordSuccess() {
        consecutiveFailures = 0;
        state = State.CLOSED;
    }

    /** 记录一次失败调用：达到阈值或试探失败时打开熔断。 */
    public synchronized void recordFailure() {
        consecutiveFailures++;
        if (state == State.HALF_OPEN || consecutiveFailures >= FAILURE_THRESHOLD) {
            state = State.OPEN;
            openedAtEpochSecond = clock.instant().getEpochSecond();
        }
    }

    /** 供测试与监控读取当前状态。 */
    public synchronized State currentState() {
        return state;
    }
}
