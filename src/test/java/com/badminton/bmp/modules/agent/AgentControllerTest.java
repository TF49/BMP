package com.badminton.bmp.modules.agent;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.modules.agent.controller.AgentController;
import com.badminton.bmp.modules.agent.dto.AgentConversationRequest;
import com.badminton.bmp.modules.agent.dto.AgentMessageRequest;
import com.badminton.bmp.modules.agent.enums.AgentType;
import com.badminton.bmp.modules.agent.service.AgentGatewayService;
import com.badminton.bmp.modules.agent.vo.AgentConversationVO;
import com.badminton.bmp.modules.agent.vo.AgentResponseVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class AgentControllerTest {

    private AgentController controller;
    private AgentGatewayService gatewayService;

    @BeforeEach
    void setUp() {
        gatewayService = Mockito.mock(AgentGatewayService.class);
        controller = new AgentController(gatewayService);
    }

    @Test
    void createConversationReturnsConversationVO() {
        AgentConversationRequest request = new AgentConversationRequest();
        request.setAgentType(AgentType.BOOKING);

        AgentConversationVO vo = new AgentConversationVO("conv_booking_123", AgentType.BOOKING);
        when(gatewayService.createConversation(AgentType.BOOKING)).thenReturn(vo);

        Result<AgentConversationVO> result = controller.createConversation(request);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("conv_booking_123", result.getData().getConversationId());
    }

    @Test
    void sendMessageDelegatesToGatewayService() {
        AgentMessageRequest request = new AgentMessageRequest();
        request.setContent("查询场地");
        request.setMessageId("msg_1");

        AgentResponseVO vo = new AgentResponseVO();
        vo.setConversationId("conv_booking_123");

        when(gatewayService.sendMessage(eq("conv_booking_123"), any())).thenReturn(vo);

        Result<AgentResponseVO> result = controller.sendMessage("conv_booking_123", request);
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("conv_booking_123", result.getData().getConversationId());
    }

    @Test
    void confirmAndRejectActionThrowsNotImplementedYet() {
        when(gatewayService.confirmAction(any(), any(), any())).thenThrow(new BusinessException(501, "动作确认功能尚未开放"));
        when(gatewayService.rejectAction(any(), any(), any())).thenThrow(new BusinessException(501, "动作拒绝功能尚未开放"));

        BusinessException ex1 = assertThrows(BusinessException.class, () -> controller.confirmAction("conv_1", "act_1", null));
        assertEquals(501, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class, () -> controller.rejectAction("conv_1", "act_1", null));
        assertEquals(501, ex2.getCode());
    }
}
