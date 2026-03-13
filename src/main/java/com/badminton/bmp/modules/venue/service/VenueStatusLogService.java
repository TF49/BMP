package com.badminton.bmp.modules.venue.service;

import com.badminton.bmp.modules.venue.entity.VenueStatusLog;

import java.util.List;

/**
 * 场馆状态变更日志业务接口
 */
public interface VenueStatusLogService {

    /**
     * 根据场馆ID查找所有日志，按时间倒序
     * @param venueId 场馆ID
     * @return 日志列表
     */
    List<VenueStatusLog> findByVenueId(Long venueId);

    /**
     * 记录状态变更日志
     * @param venueId 场馆ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @param changeReason 变更原因
     * @return 影响的行数
     */
    int logStatusChange(Long venueId, Integer oldStatus, Integer newStatus, 
                       Long operatorId, String operatorName, String changeReason);
}
