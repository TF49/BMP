package com.badminton.bmp.modules.agent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 趋势分析结果（预约趋势 / 财务趋势）。
 */
public class AgentAnalyticsTrendDTO {

    private AgentAnalyticsMetaDTO meta;
    private List<AgentAnalyticsOverviewDTO.Kpi> kpis = new ArrayList<>();
    private List<AgentAnalyticsChartDTO> charts = new ArrayList<>();
    private List<TrendPoint> trendData = new ArrayList<>();

    public AgentAnalyticsTrendDTO() {
    }

    public AgentAnalyticsTrendDTO(AgentAnalyticsMetaDTO meta, List<AgentAnalyticsOverviewDTO.Kpi> kpis,
                                 List<AgentAnalyticsChartDTO> charts, List<TrendPoint> trendData) {
        this.meta = meta;
        this.kpis = kpis != null ? kpis : new ArrayList<>();
        this.charts = charts != null ? charts : new ArrayList<>();
        this.trendData = trendData != null ? trendData : new ArrayList<>();
    }

    public AgentAnalyticsMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(AgentAnalyticsMetaDTO meta) {
        this.meta = meta;
    }

    public List<AgentAnalyticsOverviewDTO.Kpi> getKpis() {
        return kpis;
    }

    public void setKpis(List<AgentAnalyticsOverviewDTO.Kpi> kpis) {
        this.kpis = kpis;
    }

    public List<AgentAnalyticsChartDTO> getCharts() {
        return charts;
    }

    public void setCharts(List<AgentAnalyticsChartDTO> charts) {
        this.charts = charts;
    }

    public List<TrendPoint> getTrendData() {
        return trendData;
    }

    public void setTrendData(List<TrendPoint> trendData) {
        this.trendData = trendData;
    }

    public static class TrendPoint {
        private String date;
        private Object value;
        private Object secondaryValue;

        public TrendPoint() {
        }

        public TrendPoint(String date, Object value, Object secondaryValue) {
            this.date = date;
            this.value = value;
            this.secondaryValue = secondaryValue;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getSecondaryValue() {
            return secondaryValue;
        }

        public void setSecondaryValue(Object secondaryValue) {
            this.secondaryValue = secondaryValue;
        }
    }
}
