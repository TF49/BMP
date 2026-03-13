package com.badminton.bmp.modules.venue.service;

import com.badminton.bmp.modules.venue.entity.VenueSchedule;

import java.util.List;

/**
 * 场馆营业时间业务接口
 */
public interface VenueScheduleService {

    /**
     * 根据场馆ID查找所有时间表
     * @param venueId 场馆ID
     * @return 时间表列表
     */
    List<VenueSchedule> findByVenueId(Long venueId);

    /**
     * 根据场馆ID和时间类型查找时间表
     * @param venueId 场馆ID
     * @param scheduleType 时间类型（WORKDAY/WEEKEND/HOLIDAY）
     * @return 时间表对象
     */
    VenueSchedule findByVenueIdAndType(Long venueId, String scheduleType);

    /**
     * 添加时间表
     * @param schedule 时间表对象
     * @return 影响的行数
     */
    int add(VenueSchedule schedule);

    /**
     * 更新时间表信息
     * @param schedule 时间表对象
     * @return 影响的行数
     */
    int update(VenueSchedule schedule);

    /**
     * 删除时间表
     * @param id 时间表ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 根据场馆ID删除所有时间表
     * @param venueId 场馆ID
     * @return 影响的行数
     */
    int deleteByVenueId(Long venueId);

    /**
     * 批量保存时间表（先删除旧的，再添加新的）
     * @param venueId 场馆ID
     * @param schedules 时间表列表
     * @return 保存成功的数量
     */
    int batchSave(Long venueId, List<VenueSchedule> schedules);

    /**
     * 验证营业时间：开始时间必须小于结束时间（不允许跨夜）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否有效
     */
    boolean validateBusinessHours(String startTime, String endTime);

    /**
     * 根据ID查询时间表
     * @param id 时间表ID
     * @return 时间表对象或null
     */
    VenueSchedule findById(Long id);
}
