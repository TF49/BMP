package com.badminton.bmp.modules.venue.mapper;

import com.badminton.bmp.modules.venue.entity.VenueSchedule;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 场馆营业时间Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface VenueScheduleMapper {

    /**
     * 根据ID查找时间表
     * @param id 时间表ID
     * @return 时间表对象
     */
    @Select("SELECT * FROM sys_venue_schedule WHERE id = #{id}")
    VenueSchedule findById(@Param("id") Long id);

    /**
     * 根据场馆ID查找所有时间表
     * @param venueId 场馆ID
     * @return 时间表列表
     */
    @Select("SELECT * FROM sys_venue_schedule WHERE venue_id = #{venueId} ORDER BY " +
            "CASE schedule_type WHEN 'WORKDAY' THEN 1 WHEN 'WEEKEND' THEN 2 WHEN 'HOLIDAY' THEN 3 END")
    List<VenueSchedule> findByVenueId(@Param("venueId") Long venueId);

    /**
     * 根据场馆ID和时间类型查找时间表
     * @param venueId 场馆ID
     * @param scheduleType 时间类型（WORKDAY/WEEKEND/HOLIDAY）
     * @return 时间表对象
     */
    @Select("SELECT * FROM sys_venue_schedule WHERE venue_id = #{venueId} AND schedule_type = #{scheduleType} AND is_active = 1")
    VenueSchedule findByVenueIdAndType(@Param("venueId") Long venueId, @Param("scheduleType") String scheduleType);

    /**
     * 插入新时间表
     * @param schedule 时间表对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_venue_schedule (venue_id, schedule_type, start_time, end_time, is_active, create_time, update_time) " +
            "VALUES (#{venueId}, #{scheduleType}, #{startTime}, #{endTime}, #{isActive}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VenueSchedule schedule);

    /**
     * 更新时间表信息
     * @param schedule 时间表对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_venue_schedule SET schedule_type = #{scheduleType}, start_time = #{startTime}, " +
            "end_time = #{endTime}, is_active = #{isActive}, update_time = #{updateTime} WHERE id = #{id}")
    int update(VenueSchedule schedule);

    /**
     * 删除时间表
     * @param id 时间表ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_venue_schedule WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据场馆ID删除所有时间表
     * @param venueId 场馆ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_venue_schedule WHERE venue_id = #{venueId}")
    int deleteByVenueId(@Param("venueId") Long venueId);
}
