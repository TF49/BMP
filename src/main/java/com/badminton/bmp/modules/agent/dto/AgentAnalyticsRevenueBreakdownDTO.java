package com.badminton.bmp.modules.agent.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 收入构成 / 业务占比分析结果。
 */
public class AgentAnalyticsRevenueBreakdownDTO {

    private AgentAnalyticsMetaDTO meta;
    private BigDecimal totalIncome = BigDecimal.ZERO;
    private List<Item> items = new ArrayList<>();
    private List<AgentAnalyticsChartDTO> charts = new ArrayList<>();

    public AgentAnalyticsRevenueBreakdownDTO() {
    }

    public AgentAnalyticsRevenueBreakdownDTO(AgentAnalyticsMetaDTO meta, BigDecimal totalIncome,
                                             List<Item> items, List<AgentAnalyticsChartDTO> charts) {
        this.meta = meta;
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.items = items != null ? items : new ArrayList<>();
        this.charts = charts != null ? charts : new ArrayList<>();
    }

    public AgentAnalyticsMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(AgentAnalyticsMetaDTO meta) {
        this.meta = meta;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<AgentAnalyticsChartDTO> getCharts() {
        return charts;
    }

    public void setCharts(List<AgentAnalyticsChartDTO> charts) {
        this.charts = charts;
    }

    public static class Item {
        private String type;
        private String name;
        private BigDecimal amount;
        private double ratio;

        public Item() {
        }

        public Item(String type, String name, BigDecimal amount, double ratio) {
            this.type = type;
            this.name = name;
            this.amount = amount;
            this.ratio = ratio;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public double getRatio() {
            return ratio;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
        }
    }
}
