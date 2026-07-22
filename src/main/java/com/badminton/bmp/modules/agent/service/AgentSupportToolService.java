package com.badminton.bmp.modules.agent.service;

import com.badminton.bmp.modules.agent.dto.AgentSupportHandoffCreateDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportHandoffDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportPriceDTO;
import com.badminton.bmp.modules.agent.dto.AgentSupportVenueDTO;

/**
 * Agent 场馆客服 Tool 服务接口。
 *
 * <p>供 Python 场馆客服 Agent 调用，提供实时场馆营业状态、服务价格列表查询及人工客服转接单创建功能。</p>
 */
public interface AgentSupportToolService {

    /** 查询场馆实时营业信息。 */
    AgentSupportVenueDTO getVenueRealtimeStatus(Long venueId);

    /** 查询场馆当前服务价格。 */
    AgentSupportPriceDTO getVenuePrices(Long venueId);

    /** 创建人工客服转接单。 */
    AgentSupportHandoffDTO createHandoff(AgentSupportHandoffCreateDTO createDTO, String idempotencyKey);
}
