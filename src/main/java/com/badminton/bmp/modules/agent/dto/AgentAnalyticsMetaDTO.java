package com.badminton.bmp.modules.agent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 经营分析结果元数据。
 *
 * <p>每个分析 Tool 的响应都必须携带此元数据，明确回答四个问题：统计周期（{@link Period}）、
 * 数据范围（{@link Scope}，由服务端按角色确定，忽略客户端传入范围）、指标口径
 * （{@link Metric} 列表，固化在服务端）以及生成时间。模型不得自行推导或篡改这些口径。</p>
 */
public class AgentAnalyticsMetaDTO {

    /** 统计周期。 */
    private Period period;
    /** 数据范围（服务端按角色裁定）。 */
    private Scope scope;
    /** 指标口径字典。 */
    private List<Metric> metrics = new ArrayList<>();

    public AgentAnalyticsMetaDTO() {
    }

    public AgentAnalyticsMetaDTO(Period period, Scope scope, List<Metric> metrics) {
        this.period = period;
        this.scope = scope;
        this.metrics = metrics != null ? metrics : new ArrayList<>();
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    /** 统计周期：起止日期与聚合粒度。 */
    public static class Period {
        private String startDate;
        private String endDate;
        /** 聚合粒度：DAY / WEEK / MONTH。 */
        private String granularity;
        /** 可读描述，如“2026-07-16 至 2026-07-22，按天聚合”。 */
        private String description;

        public Period() {
        }

        public Period(String startDate, String endDate, String granularity, String description) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.granularity = granularity;
            this.description = description;
        }

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getGranularity() {
            return granularity;
        }

        public void setGranularity(String granularity) {
            this.granularity = granularity;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /** 数据范围：级别（全部/单场馆）与场馆标识。 */
    public static class Scope {
        /** ALL-全部场馆（会长）；VENUE-单一场馆（场馆管理员）。 */
        private String level;
        private Long venueId;
        private String venueName;
        /** 可读描述，如“全部场馆”或“场馆 1（望京馆）”。 */
        private String description;

        public Scope() {
        }

        public Scope(String level, Long venueId, String venueName, String description) {
            this.level = level;
            this.venueId = venueId;
            this.venueName = venueName;
            this.description = description;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /** 指标口径：键、名称、单位与计算口径说明。 */
    public static class Metric {
        private String key;
        private String name;
        private String unit;
        /** 计算口径说明，固化在服务端，禁止模型改写。 */
        private String definition;

        public Metric() {
        }

        public Metric(String key, String name, String unit, String definition) {
            this.key = key;
            this.name = name;
            this.unit = unit;
            this.definition = definition;
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

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }
    }
}
