package com.badminton.bmp.modules.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Agent Tool：场馆客服 场馆实时营业信息 DTO。
 */
public class AgentSupportVenueDTO {

    @JsonProperty("venue_id")
    private String venueId;

    @JsonProperty("venue_name")
    private String venueName;

    private String status;

    @JsonProperty("business_hours")
    private Map<String, String> businessHours;

    @JsonProperty("current_time")
    private String currentTime;

    @JsonProperty("available_courts")
    private Integer availableCourts;

    @JsonProperty("total_courts")
    private Integer totalCourts;

    public AgentSupportVenueDTO() {
    }

    public AgentSupportVenueDTO(String venueId, String venueName, String status, Map<String, String> businessHours,
                                String currentTime, Integer availableCourts, Integer totalCourts) {
        this.venueId = venueId;
        this.venueName = venueName;
        this.status = status;
        this.businessHours = businessHours;
        this.currentTime = currentTime;
        this.availableCourts = availableCourts;
        this.totalCourts = totalCourts;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getBusinessHours() {
        return businessHours;
    }

    public void setBusinessHours(Map<String, String> businessHours) {
        this.businessHours = businessHours;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public Integer getAvailableCourts() {
        return availableCourts;
    }

    public void setAvailableCourts(Integer availableCourts) {
        this.availableCourts = availableCourts;
    }

    public Integer getTotalCourts() {
        return totalCourts;
    }

    public void setTotalCourts(Integer totalCourts) {
        this.totalCourts = totalCourts;
    }
}
