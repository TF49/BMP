package com.badminton.bmp.modules.venue.mapper;

import com.badminton.bmp.modules.venue.entity.VenueStatusLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 场馆状态变更日志Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface VenueStatusLogMapper {

    /**
     * 根据ID查找日志
     * @param id 日志ID
     * @return 日志对象
     */
    @Select("SELECT * FROM sys_venue_status_log WHERE id = #{id}")
    VenueStatusLog findById(@Param("id") Long id);

    /**
     * 根据场馆ID查找所有日志，按时间倒序
     * @param venueId 场馆ID
     * @return 日志列表
     */
    @Select("SELECT * FROM sys_venue_status_log WHERE venue_id = #{venueId} ORDER BY create_time DESC")
    List<VenueStatusLog> findByVenueId(@Param("venueId") Long venueId);

    /**
     * 插入新日志
     * @param log 日志对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_venue_status_log (venue_id, old_status, new_status, operator_id, operator_name, change_reason, create_time) " +
            "VALUES (#{venueId}, #{oldStatus}, #{newStatus}, #{operatorId}, #{operatorName}, #{changeReason}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VenueStatusLog log);

    /**
     * 根据场馆ID删除所有日志（慎用，通常用于数据清理）
     * @param venueId 场馆ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM sys_venue_status_log WHERE venue_id = #{venueId}")
    int deleteByVenueId(@Param("venueId") Long venueId);
}
