package com.badminton.bmp.modules.agent.service;

import com.badminton.bmp.modules.agent.dto.AgentCourtAvailabilityDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Agent Tool 场地可用性只读服务。
 *
 * <p>复用现有 {@code CourtService} 与预约占用查询能力，补充白名单/边界校验、
 * 场馆营业状态与用户可访问范围校验，不复制预约业务逻辑。</p>
 */
public interface AgentCourtToolService {

    /**
     * 查询指定场馆在给定日期与时间段内各场地的可预约情况。
     *
     * @param venueId   场馆 ID
     * @param date      预约日期（不得早于今天，且在允许窗口内）
     * @param startTime 开始时间
     * @param endTime   结束时间（须晚于开始时间）
     * @param limit     返回数量上限（1..50）
     * @return 场地可用性只读列表
     */
    List<AgentCourtAvailabilityDTO> queryAvailability(Long venueId, LocalDate date,
                                                      LocalTime startTime, LocalTime endTime, int limit);
}
