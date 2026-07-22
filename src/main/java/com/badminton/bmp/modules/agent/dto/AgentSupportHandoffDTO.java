package com.badminton.bmp.modules.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Agent Tool：人工客服转接单 结果 DTO。
 */
public class AgentSupportHandoffDTO {

    @JsonProperty("handoff_id")
    private String handoffId;

    private String status;

    @JsonProperty("estimated_wait_time")
    private Integer estimatedWaitTime;

    @JsonProperty("created_at")
    private String createdAt;

    public AgentSupportHandoffDTO() {
    }

    public AgentSupportHandoffDTO(String handoffId, String status, Integer estimatedWaitTime, String createdAt) {
        this.handoffId = handoffId;
        this.status = status;
        this.estimatedWaitTime = estimatedWaitTime;
        this.createdAt = createdAt;
    }

    public String getHandoffId() {
        return handoffId;
    }

    public void setHandoffId(String handoffId) {
        this.handoffId = handoffId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getEstimatedWaitTime() {
        return estimatedWaitTime;
    }

    public void setEstimatedWaitTime(Integer estimatedWaitTime) {
        this.estimatedWaitTime = estimatedWaitTime;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
