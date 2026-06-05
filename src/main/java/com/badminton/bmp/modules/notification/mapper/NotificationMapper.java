package com.badminton.bmp.modules.notification.mapper;

import com.badminton.bmp.modules.notification.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统通知 Mapper 接口
 */
@Mapper
public interface NotificationMapper {

    /**
     * 分页查询通知列表（按创建时间倒序），并根据venueId进行数据隔离
     * 如果 userVenueId 为 null，则查询所有通知（包含全局和场馆的） - 适用于会长
     * 如果 userVenueId 不为 null，则只查询全局通知和该用户所在场馆的通知 - 适用于普通用户和场馆管理员
     */
    @Select("<script>" +
            "SELECT * FROM sys_notification " +
            "<where> " +
            "   <if test='userVenueId != null'> " +
            "       AND (venue_id IS NULL OR venue_id = #{userVenueId}) " +
            "   </if> " +
            "</where> " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Notification> findByPage(@Param("offset") int offset, @Param("limit") int limit, @Param("userVenueId") Long userVenueId);

    /**
     * 统计通知总数，并根据venueId进行数据隔离
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_notification " +
            "<where> " +
            "   <if test='userVenueId != null'> " +
            "       AND (venue_id IS NULL OR venue_id = #{userVenueId}) " +
            "   </if> " +
            "</where>" +
            "</script>")
    int countAll(@Param("userVenueId") Long userVenueId);

    /**
     * 根据ID查找通知
     */
    @Select("SELECT * FROM sys_notification WHERE id = #{id}")
    Notification findById(@Param("id") Long id);

    /**
     * 插入新通知
     */
    @Insert("INSERT INTO sys_notification (title, content, publisher_id, publisher_name, venue_id, create_time) " +
            "VALUES (#{title}, #{content}, #{publisherId}, #{publisherName}, #{venueId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);

    /**
     * 根据ID删除通知
     */
    @Delete("DELETE FROM sys_notification WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新通知
     */
    @Update("UPDATE sys_notification SET title = #{title}, content = #{content} WHERE id = #{id}")
    int update(Notification notification);
}
