package com.badminton.bmp.modules.agent.service;

import com.badminton.bmp.modules.agent.dto.AgentAnalyticsHeatmapDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsOverviewDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsRevenueBreakdownDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsTrendDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsVenueComparisonDTO;

/**
 * Agent 经营分析只读 Tool 服务接口。
 *
 * <p>供 Python 经营分析 Agent 调用：返回预计算与受控的经营数据、统计周期、
 * 范围说明、指标口径及受控 ECharts 数据。禁止模型生成或执行任意 SQL。</p>
 */
public interface AgentAnalyticsToolService {

    /** 经营总览。 */
    AgentAnalyticsOverviewDTO getOverview(String startDate, String endDate, Long venueId);

    /** 预约趋势。 */
    AgentAnalyticsTrendDTO getBookingTrend(String startDate, String endDate, String granularity, Long venueId);

    /** 场地热力图。 */
    AgentAnalyticsHeatmapDTO getCourtHeatmap(String startDate, String endDate, Long venueId);

    /** 财务趋势。 */
    AgentAnalyticsTrendDTO getFinanceTrend(String startDate, String endDate, Long venueId);

    /** 收入构成。 */
    AgentAnalyticsRevenueBreakdownDTO getRevenueBreakdown(String startDate, String endDate, Long venueId);

    /** 场馆对比。 */
    AgentAnalyticsVenueComparisonDTO getVenueComparison(String startDate, String endDate);
}
