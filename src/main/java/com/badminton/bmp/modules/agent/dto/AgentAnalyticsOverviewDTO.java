package com.badminton.bmp.modules.agent.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Agent 经营总览结果：核心 KPI 卡片 + 受控图表 + 元数据。
 *
 * <p>KPI 值一律由服务端聚合计算，模型只做转述与图形化编排，不得自行计算。</p>
 */
public class AgentAnalyticsOverviewDTO {

    private AgentAnalyticsMetaDTO meta;
    private List<Kpi> kpis = new ArrayList<>();
    private List<AgentAnalyticsChartDTO> charts = new ArrayList<>();

    public AgentAnalyticsOverviewDTO() {
    }

    public AgentAnalyticsMetaDTO getMeta() {
        return meta;
    }

    public void setMeta(AgentAnalyticsMetaDTO meta) {
        this.meta = meta;
    }

    public List<Kpi> getKpis() {
        return kpis;
    }

    public void setKpis(List<Kpi> kpis) {
        this.kpis = kpis;
    }

    public List<AgentAnalyticsChartDTO> getCharts() {
        return charts;
    }

    public void setCharts(List<AgentAnalyticsChartDTO> charts) {
        this.charts = charts;
    }

    /** 单张 KPI 卡片：关联指标口径键、名称、数值与单位。 */
    public static class Kpi {
        private String key;
        private String name;
        private BigDecimal value;
        private String unit;

        public Kpi() {
        }

        public Kpi(String key, String name, BigDecimal value, String unit) {
            this.key = key;
            this.name = name;
            this.value = value;
            this.unit = unit;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
