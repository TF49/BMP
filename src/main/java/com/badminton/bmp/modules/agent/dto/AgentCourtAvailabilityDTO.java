package com.badminton.bmp.modules.agent.dto;

import java.math.BigDecimal;

/**
 * Agent Tool 场地可用性只读视图。
 *
 * <p>表示某场地在请求的日期与时间段内是否可预约，供预订 Agent 使用。
 * 专用稳定 DTO，不直接返回数据库实体。</p>
 */
public class AgentCourtAvailabilityDTO {

    private Long courtId;
    private String courtCode;
    private String courtName;
    private Long venueId;
    /** 计费方式：HOUR-按小时，TIME-按时段。 */
    private String billingType;
    private BigDecimal pricePerHour;
    /** 请求时间段内是否可预约。 */
    private boolean available;
    /** 查询日期 yyyy-MM-dd。 */
    private String date;
    /** 查询开始时间 HH:mm。 */
    private String startTime;
    /** 查询结束时间 HH:mm。 */
    private String endTime;

    public AgentCourtAvailabilityDTO() {
    }

    public Long getCourtId() {
        return courtId;
    }

    public void setCourtId(Long courtId) {
        this.courtId = courtId;
    }

    public String getCourtCode() {
        return courtCode;
    }

    public void setCourtCode(String courtCode) {
        this.courtCode = courtCode;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
