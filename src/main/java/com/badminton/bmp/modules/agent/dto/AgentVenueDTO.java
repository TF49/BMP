package com.badminton.bmp.modules.agent.dto;

/**
 * Agent Tool 场馆只读视图。
 *
 * <p>专用稳定 DTO，仅暴露 Agent 需要的字段，不直接返回数据库实体或 MyBatis 对象。</p>
 */
public class AgentVenueDTO {

    private Long venueId;
    private String venueName;
    private String address;
    private String businessHours;
    /** 状态：0-关闭，1-营业中，2-暂停。 */
    private Integer status;
    /** 状态可读文案。 */
    private String statusText;

    public AgentVenueDTO() {
    }

    public AgentVenueDTO(Long venueId, String venueName, String address, String businessHours,
                         Integer status, String statusText) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.address = address;
        this.businessHours = businessHours;
        this.status = status;
        this.statusText = statusText;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
}
