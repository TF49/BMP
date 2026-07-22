package com.badminton.bmp.modules.agent.service;

import com.badminton.bmp.modules.agent.dto.AgentBookingCreateRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteResultDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingResultDTO;

/**
 * Agent Tool 预约写服务。
 *
 * <p>供 Python 预订 Agent 调用：先报价，用户明确确认后再创建待支付预约。
 * 金额一律由 Java 计算（复用 {@code BookingService} 定价逻辑），模型不得自行计算；
 * 创建须携带幂等键、仅生成待支付订单、不自动支付。身份由
 * {@code AgentToolAuthenticationFilter} 建立，服务从安全上下文取登录用户 ID。</p>
 */
public interface AgentBookingToolService {

    /**
     * 预约报价：校验并计算所选场地/时段的金额，不落库。
     *
     * @param request 报价请求
     * @return 报价结果（含总金额、逐场地明细、时长）
     */
    AgentBookingQuoteResultDTO quote(AgentBookingQuoteRequestDTO request);

    /**
     * 创建待支付预约：幂等地创建仅待支付订单，不自动支付。
     *
     * @param request        创建请求
     * @param idempotencyKey 幂等键（来自 {@code Idempotency-Key} 请求头），不能为空
     * @return 创建结果（含预约 ID、单号、待支付状态、总金额）
     */
    AgentBookingResultDTO create(AgentBookingCreateRequestDTO request, String idempotencyKey);
}
