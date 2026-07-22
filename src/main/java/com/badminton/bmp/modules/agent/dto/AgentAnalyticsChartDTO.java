package com.badminton.bmp.modules.agent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 经营分析受控 ECharts 图表数据。
 *
 * <p>由服务端聚合后产出，仅暴露渲染图表所需的最小结构（类型、标题、坐标类目、系列）。
 * 模型不得生成或执行任意 SQL，也不得改写此结构；空数据以 {@code empty=true} 显式表达。</p>
 */
public class AgentAnalyticsChartDTO {

    /** 图表类型：kpi / line / bar / pie / heatmap。 */
    private String chartType;
    private String title;
    /** 类目轴标签（折线/柱状的 X 轴、饼图的图例、热力图的 X 轴）。 */
    private List<String> categories = new ArrayList<>();
    /** 热力图 Y 轴类目（仅热力图使用）。 */
    private List<String> yCategories = new ArrayList<>();
    /** 数据系列。 */
    private List<Series> series = new ArrayList<>();
    /** 是否为空数据。 */
    private boolean empty;
    /** 空数据可读提示。 */
    private String emptyText;

    public AgentAnalyticsChartDTO() {
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getYCategories() {
        return yCategories;
    }

    public void setYCategories(List<String> yCategories) {
        this.yCategories = yCategories;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public String getEmptyText() {
        return emptyText;
    }

    public void setEmptyText(String emptyText) {
        this.emptyText = emptyText;
    }

    /** 单个数据系列：名称、单位及数据点。折线/柱状为数值列表，热力图为 [x, y, value] 列表。 */
    public static class Series {
        private String name;
        private String unit;
        /** 关联的指标口径键，便于前端与模型对齐口径。 */
        private String metricKey;
        private List<Object> data = new ArrayList<>();

        public Series() {
        }

        public Series(String name, String unit, String metricKey, List<Object> data) {
            this.name = name;
            this.unit = unit;
            this.metricKey = metricKey;
            this.data = data != null ? data : new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getMetricKey() {
            return metricKey;
        }

        public void setMetricKey(String metricKey) {
            this.metricKey = metricKey;
        }

        public List<Object> getData() {
            return data;
        }

        public void setData(List<Object> data) {
            this.data = data;
        }
    }
}
