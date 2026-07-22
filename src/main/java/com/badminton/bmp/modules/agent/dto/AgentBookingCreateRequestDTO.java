package com.badminton.bmp.modules.agent.dto;

import jakarta.validation.constraints.Size;

/**
 * Agent Tool 创建待支付预约请求。
 *
 * <p>在报价请求基础上增加备注。创建接口须携带 {@code Idempotency-Key} 请求头，
 * 仅生成待支付订单，不自动支付。</p>
 */
public class AgentBookingCreateRequestDTO extends AgentBookingQuoteRequestDTO {

    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
