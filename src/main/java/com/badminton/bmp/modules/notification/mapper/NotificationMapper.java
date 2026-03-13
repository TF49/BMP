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
     * 分页查询通知列表（按创建时间倒序）
     */
    @Select("SELECT * FROM sys_notification ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Notification> findByPage(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计通知总数
     */
    @Select("SELECT COUNT(*) FROM sys_notification")
    int countAll();

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
