package com.badminton.bmp.modules.court.service;

import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;
import com.badminton.bmp.modules.court.entity.Court;

import java.util.List;
import java.util.Map;

/**
 * 场地业务接口
 */
public interface CourtService {

    /**
     * 根据ID查找场地
     * @param id 场地ID
     * @return 场地对象
     */
    Court findById(Long id);

    /**
     * 分页查询场地列表
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 场地列表
     */
    List<Court> findAll(Long venueId, Integer status, String keyword, int page, int size);

    /**
     * 统计场地数量
     * @param venueId 场馆ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 场地数量
     */
    int count(Long venueId, Integer status, String keyword);

    /**
     * 添加场地
     * @param court 场地对象
     * @return 影响的行数
     */
    int add(Court court);

    /**
     * 更新场地信息
     * @param court 场地对象
     * @return 影响的行数
     */
    int update(Court court);

    /**
     * 删除场地（逻辑删除）
     * @param id 场地ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 更新场地状态
     * @param id 场地ID
     * @param status 新状态（0-维护中，1-空闲，2-预约中，3-使用中）
     * @return 影响的行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 获取场地统计信息
     * @return 统计信息Map，包含总场地数、各状态数量
     */
    Map<String, Object> getStatistics();

    /**
     * 统计指定场馆下的场地数量
     * @param venueId 场馆ID
     * @return 场地数量
     */
    int countByVenueId(Long venueId);

    /**
     * 获取指定场地当天的所有预约用户信息
     * @param courtId 场地ID
     * @param date 查询日期，格式 yyyy-MM-dd；null 则使用服务器当前日期
     * @return 预约用户列表（姓名已脱敏）
     */
    List<CourtBookingUserDTO> getTodayBookingUsers(Long courtId, String date);

    /**
     * 批量获取多个场地当天的预约数量
     * @param courtIds 场地ID列表
     * @param date 查询日期，格式 yyyy-MM-dd；null 则使用服务器当前日期
     * @return 场地ID与预约数量的映射
     */
    Map<Long, Integer> getTodayBookingCounts(List<Long> courtIds, String date);

    /**
     * 根据该场地当前有效预约重算场地状态并更新（1-空闲，2-预约中，3-使用中）
     * 用于预约完成后或定时任务将“进行中”改为“已完成”后，保证场地状态正确
     */
    void recomputeCourtStatus(Long courtId);

    /**
     * 获取指定日期各场地的占用时间轴（Dashboard 今日场地时间轴）
     * @param date 查询日期 yyyy-MM-dd；null 则使用今天
     * @return 列表，每项含 courtId, courtName, timeSlots（{ status: free|busy, duration }）
     */
    List<Map<String, Object>> getTimelineToday(String date);
}
