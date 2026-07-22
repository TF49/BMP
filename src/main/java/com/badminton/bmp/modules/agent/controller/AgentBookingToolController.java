package com.badminton.bmp.modules.agent.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.agent.dto.AgentBookingCreateRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteResultDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingResultDTO;
import com.badminton.bmp.modules.agent.service.AgentBookingToolService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Agent Tool：预约报价与创建待支付接口。
 *
 * <p>仅供 Python 预订 Agent 调用，身份由 {@code AgentToolAuthenticationFilter} 建立，
 * 控制器不接收任何身份参数。报价只计算不落库；创建须携带 {@code Idempotency-Key} 头，
 * 幂等地生成待支付订单（status=1 / paymentStatus=0），不自动支付。金额一律由服务端计算。</p>
 */
@RestController
@RequestMapping("/api/agent-tools")
@Validated
public class AgentBookingToolController extends BaseController {

    private final AgentBookingToolService agentBookingToolService;

    public AgentBookingToolController(AgentBookingToolService agentBookingToolService) {
        this.agentBookingToolService = agentBookingToolService;
    }

    /** 预约报价：校验并计算所选场地/时段金额，不落库。 */
    @PostMapping("/bookings/quote")
    public Result<AgentBookingQuoteResultDTO> quote(@Valid @RequestBody AgentBookingQuoteRequestDTO request) {
        return success(agentBookingToolService.quote(request));
    }

    /** 创建待支付预约：读取幂等键，幂等地生成待支付订单，不自动支付。 */
    @PostMapping("/bookings")
    public Result<AgentBookingResultDTO> create(
            @Valid @RequestBody AgentBookingCreateRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return success(agentBookingToolService.create(request, idempotencyKey));
    }
}
