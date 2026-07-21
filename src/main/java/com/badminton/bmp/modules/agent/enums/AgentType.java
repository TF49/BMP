package com.badminton.bmp.modules.agent.enums;

/**
 * Agent 类型。由客户端显式指定，第一版不让模型自动猜测。
 */
public enum AgentType {
    /** 智能预订 */
    BOOKING,
    /** 经营分析 */
    ANALYTICS,
    /** 场馆客服 */
    SUPPORT
}
