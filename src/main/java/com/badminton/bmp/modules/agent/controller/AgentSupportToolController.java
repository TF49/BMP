package com.badminton.bmp.modules.agent.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.agent.dto.AgentSupportHandoffCreateDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportHandoffDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportPriceDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportVenueDTO;
import com.badminton.bmp.modules.agent.service.AgentSupportToolService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent Tool：场馆客服只读/转接接口。
 *
 * <p>仅供 Python 场馆客服 Agent 调用，身份由 {@code AgentToolAuthenticationFilter} 建立。</p>
 */
@RestController
@RequestMapping("/api/agent-tools")
@Validated
public class AgentSupportToolController extends BaseController {

    private final AgentSupportToolService agentSupportToolService;

    public AgentSupportToolController(AgentSupportToolService agentSupportToolService) {
        this.agentSupportToolService = agentSupportToolService;
    }

    /** 查询场馆实时信息。 */
    @GetMapping("/support/venues/{venueId}")
    public Result<AgentSupportVenueDTO> getVenueRealtimeStatus(@PathVariable("venueId") Long venueId) {
        return success(agentSupportToolService.getVenueRealtimeStatus(venueId));
    }

    /** 查询当前服务价格。 */
    @GetMapping("/support/venues/{venueId}/prices")
    public Result<AgentSupportPriceDTO> getVenuePrices(@PathVariable("venueId") Long venueId) {
        return success(agentSupportToolService.getVenuePrices(venueId));
    }

    /** 创建人工客服转接单。 */
    @PostMapping("/support/handoffs")
    public Result<AgentSupportHandoffDTO> createHandoff(
            @Valid @RequestBody AgentSupportHandoffCreateDTO createDTO,
            @RequestHeader(value = "X-Agent-Idempotency-Key", required = false) String idempotencyKey) {
        return success(agentSupportToolService.createHandoff(createDTO, idempotencyKey));
    }
}
