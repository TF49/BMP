package com.badminton.bmp.modules.agent.dto;

import jakarta.validation.constraints.Size;

/**
 * 确认或拒绝一次待执行动作的请求体。动作本身由 {@code actionId} 路径参数标识，
 * 请求体仅承载可选的用户备注（例如拒绝原因）。
 */
public class AgentActionRequest {

    /** 用户备注或拒绝原因，可选。 */
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
