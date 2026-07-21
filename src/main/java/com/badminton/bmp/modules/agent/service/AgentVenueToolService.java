package com.badminton.bmp.modules.agent.service;

import com.badminton.bmp.modules.agent.dto.AgentVenueDTO;

import java.util.List;

/**
 * Agent Tool 场馆只读服务。
 *
 * <p>复用现有 {@code VenueService}，在 Agent 调用场景下补充白名单校验与用户可访问范围控制，
 * 不复制场馆业务逻辑。</p>
 */
public interface AgentVenueToolService {

    /**
     * 列出当前签名上下文用户可访问的场馆。
     *
     * @param status 可选状态过滤（0-关闭，1-营业中，2-暂停）；为空表示不过滤
     * @param limit  返回数量上限（1..50）
     * @return 场馆只读列表
     */
    List<AgentVenueDTO> listVenues(Integer status, int limit);
}
