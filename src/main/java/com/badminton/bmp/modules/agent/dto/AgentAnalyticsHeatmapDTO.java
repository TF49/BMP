package com.badminton.bmp.modules.agent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 场地热力图分析结果。
 */
public class AgentAnalyticsHeatmapDTO {

    private AgentAnalyticsMetaDTO meta;
    private List<String> hours = new ArrayList<>();
    private List<String> dates = new ArrayList<>();
    private List<HeatmapCell> cells = new ArrayList<>();
    private List<AgentAnalyticsChartDTO> charts = new ArrayList<>();

    public AgentAnalyticsHeatmapDTO() {
    }

    public AgentAnalyticsHeatmapDTO(AgentAnalyticsMetaDTO meta, List<String> hours, List<String> dates,
                                   List<HeatmapCell> cells, List<AgentAnalyticsChartDTO> charts) {
        this.meta = meta;
        this.hours = hours != null ? hours : new ArrayList<>();
        this.dates = dates != null ? dates : new ArrayList<>();
        this.cells = cells != null ? cells : new ArrayList<>();
        this.charts = charts != null ? charts : new ArrayList<>();
    }

    public AgentAnalyticsMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(AgentAnalyticsMetaDTO meta) {
        this.meta = meta;
    }

    public List<String> getHours() {
        return hours;
    }

    public void setHours(List<String> hours) {
        this.hours = hours;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<HeatmapCell> getCells() {
        return cells;
    }

    public void setCells(List<HeatmapCell> cells) {
        this.cells = cells;
    }

    public List<AgentAnalyticsChartDTO> getCharts() {
        return charts;
    }

    public void setCharts(List<AgentAnalyticsChartDTO> charts) {
        this.charts = charts;
    }

    public static class HeatmapCell {
        private String date;
        private int hour;
        private int count;
        private double utilizationRate;

        public HeatmapCell() {
        }

        public HeatmapCell(String date, int hour, int count, double utilizationRate) {
            this.date = date;
            this.hour = hour;
            this.count = count;
            this.utilizationRate = utilizationRate;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getUtilizationRate() {
            return utilizationRate;
        }

        public void setUtilizationRate(double utilizationRate) {
            this.utilizationRate = utilizationRate;
        }
    }
}
