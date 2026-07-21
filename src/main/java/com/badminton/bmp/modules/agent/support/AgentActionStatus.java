package com.badminton.bmp.modules.agent.support;

/**
 * Agent 写动作的幂等状态。
 *
 * <p>状态机：{@link #PENDING} 只能一次性流转到 {@link #CONFIRMED} 或 {@link #REJECTED}，
 * 用于防止确认/拒绝动作被重复执行。</p>
 */
public enum AgentActionStatus {
    PENDING,
    CONFIRMED,
    REJECTED
}
