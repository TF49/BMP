package com.badminton.bmp.modules.agent.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsHeatmapDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsOverviewDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsRevenueBreakdownDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsTrendDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsVenueComparisonDTO;
import com.badminton.bmp.modules.agent.service.AgentAnalyticsToolService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent Tool：经营分析只读查询接口。
 *
 * <p>仅供 Python 经营分析 Agent 调用，身份由 {@code AgentToolAuthenticationFilter} 建立，
 * 控制器不接收任何身份参数，场馆范围由服务端根据登录角色确定，忽略客户端伪造范围。
 * 工具只返回 pre-calculated 聚合结果，不允许模型生成或执行任意 SQL。</p>
 */
@RestController
@RequestMapping("/api/agent-tools")
@Validated
public class AgentAnalyticsToolController extends BaseController {

    private final AgentAnalyticsToolService agentAnalyticsToolService;

    public AgentAnalyticsToolController(AgentAnalyticsToolService agentAnalyticsToolService) {
        this.agentAnalyticsToolService = agentAnalyticsToolService;
    }

    /** 经营总览接口。 */
    @GetMapping("/analytics/overview")
    public Result<AgentAnalyticsOverviewDTO> getOverview(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "venueId", required = false) Long venueId) {
        return success(agentAnalyticsToolService.getOverview(startDate, endDate, venueId));
    }

    /** 预约趋势接口。 */
    @GetMapping("/analytics/booking-trend")
    public Result<AgentAnalyticsTrendDTO> getBookingTrend(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "granularity", required = false) String granularity,
            @RequestParam(value = "venueId", required = false) Long venueId) {
        return success(agentAnalyticsToolService.getBookingTrend(startDate, endDate, granularity, venueId));
    }

    /** 场地热力图接口。 */
    @GetMapping("/analytics/court-heatmap")
    public Result<AgentAnalyticsHeatmapDTO> getCourtHeatmap(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "venueId", required = false) Long venueId) {
        return success(agentAnalyticsToolService.getCourtHeatmap(startDate, endDate, venueId));
    }

    /** 财务趋势接口。 */
    @GetMapping("/analytics/finance-trend")
    public Result<AgentAnalyticsTrendDTO> getFinanceTrend(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "venueId", required = false) Long venueId) {
        return success(agentAnalyticsToolService.getFinanceTrend(startDate, endDate, venueId));
    }

    /** 收入构成接口。 */
    @GetMapping("/analytics/revenue-breakdown")
    public Result<AgentAnalyticsRevenueBreakdownDTO> getRevenueBreakdown(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "venueId", required = false) Long venueId) {
        return success(agentAnalyticsToolService.getRevenueBreakdown(startDate, endDate, venueId));
    }

    /** 场馆对比接口。 */
    @GetMapping("/analytics/venue-comparison")
    public Result<AgentAnalyticsVenueComparisonDTO> getVenueComparison(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        return success(agentAnalyticsToolService.getVenueComparison(startDate, endDate));
    }
}
