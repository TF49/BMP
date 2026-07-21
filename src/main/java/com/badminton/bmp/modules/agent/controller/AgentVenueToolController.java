package com.badminton.bmp.modules.agent.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.agent.dto.AgentVenueDTO;
import com.badminton.bmp.modules.agent.service.AgentVenueToolService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * Agent Tool：场馆只读查询接口。
 *
 * <p>仅供 Python Agent 服务调用，身份由 {@code AgentToolAuthenticationFilter} 建立，
 * 控制器不接收任何身份参数。</p>
 */
@RestController
@RequestMapping("/api/agent-tools")
@Validated
public class AgentVenueToolController extends BaseController {

    private final AgentVenueToolService agentVenueToolService;

    public AgentVenueToolController(AgentVenueToolService agentVenueToolService) {
        this.agentVenueToolService = agentVenueToolService;
    }

    /** 列出当前用户可访问的场馆。 */
    @GetMapping("/venues")
    public Result<List<AgentVenueDTO>> listVenues(
            @RequestParam(value = "status", required = false) @Min(0) @Max(2) Integer status,
            @RequestParam(value = "limit", required = false, defaultValue = "20") @Min(1) @Max(50) int limit) {
        return success(agentVenueToolService.listVenues(status, limit));
    }
}
