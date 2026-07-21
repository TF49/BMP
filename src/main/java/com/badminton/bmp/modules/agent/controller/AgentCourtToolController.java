package com.badminton.bmp.modules.agent.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.agent.dto.AgentCourtAvailabilityDTO;
import com.badminton.bmp.modules.agent.service.AgentCourtToolService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Agent Tool：场地可用性只读查询接口。
 *
 * <p>仅供 Python Agent 服务调用，身份由 {@code AgentToolAuthenticationFilter} 建立，
 * 控制器不接收任何身份参数。日期与时间参数按白名单格式绑定，越界校验在服务层完成。</p>
 */
@RestController
@RequestMapping("/api/agent-tools")
@Validated
public class AgentCourtToolController extends BaseController {

    private final AgentCourtToolService agentCourtToolService;

    public AgentCourtToolController(AgentCourtToolService agentCourtToolService) {
        this.agentCourtToolService = agentCourtToolService;
    }

    /** 查询指定场馆在给定日期与时间段内各场地的可预约情况。 */
    @GetMapping("/courts/availability")
    public Result<List<AgentCourtAvailabilityDTO>> queryAvailability(
            @RequestParam("venueId") @NotNull(message = "场馆 ID 不能为空") @Positive(message = "场馆 ID 必须大于 0") Long venueId,
            @RequestParam("date") @NotNull(message = "预约日期不能为空") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("startTime") @NotNull(message = "开始时间不能为空") @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
            @RequestParam("endTime") @NotNull(message = "结束时间不能为空") @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
            @RequestParam(value = "limit", required = false, defaultValue = "20") @Min(value = 1, message = "limit 必须大于等于 1") @Max(value = 50, message = "limit 不能超过 50") int limit) {
        return success(agentCourtToolService.queryAvailability(venueId, date, startTime, endTime, limit));
    }
}
