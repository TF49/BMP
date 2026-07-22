package com.badminton.bmp.modules.agent.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 场馆对比分析结果。
 */
public class AgentAnalyticsVenueComparisonDTO {

    private AgentAnalyticsMetaDTO meta;
    private List<VenueItem> venues = new ArrayList<>();
    private List<AgentAnalyticsChartDTO> charts = new ArrayList<>();

    public AgentAnalyticsVenueComparisonDTO() {
    }

    public AgentAnalyticsVenueComparisonDTO(AgentAnalyticsMetaDTO meta, List<VenueItem> venues,
                                           List<AgentAnalyticsChartDTO> charts) {
        this.meta = meta;
        this.venues = venues != null ? venues : new ArrayList<>();
        this.charts = charts != null ? charts : new ArrayList<>();
    }

    public AgentAnalyticsMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(AgentAnalyticsMetaDTO meta) {
        this.meta = meta;
    }

    public List<VenueItem> getVenues() {
        return venues;
    }

    public void setVenues(List<VenueItem> venues) {
        this.venues = venues;
    }

    public List<AgentAnalyticsChartDTO> getCharts() {
        return charts;
    }

    public void setCharts(List<AgentAnalyticsChartDTO> charts) {
        this.charts = charts;
    }

    public static class VenueItem {
        private Long venueId;
        private String venueName;
        private int bookingCount;
        private double totalHours;
        private double utilizationRate;
        private BigDecimal totalIncome;

        public VenueItem() {
        }

        public VenueItem(Long venueId, String venueName, int bookingCount, double totalHours,
                         double utilizationRate, BigDecimal totalIncome) {
            this.venueId = venueId;
            this.venueName = venueName;
            this.bookingCount = bookingCount;
            this.totalHours = totalHours;
            this.utilizationRate = utilizationRate;
            this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
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

        public int getBookingCount() {
            return bookingCount;
        }

        public void setBookingCount(int bookingCount) {
            this.bookingCount = bookingCount;
        }

        public double getTotalHours() {
            return totalHours;
        }

        public void setTotalHours(double totalHours) {
            this.totalHours = totalHours;
        }

        public double getUtilizationRate() {
            return utilizationRate;
        }

        public void setUtilizationRate(double utilizationRate) {
            this.utilizationRate = utilizationRate;
        }

        public BigDecimal getTotalIncome() {
            return totalIncome;
        }

        public void setTotalIncome(BigDecimal totalIncome) {
            this.totalIncome = totalIncome;
        }
    }
}
