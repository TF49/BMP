package com.badminton.bmp.modules.coach.mapper;

import com.badminton.bmp.modules.coach.entity.Coach;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 教练Mapper接口
 * 用于数据库操作
 */
@Mapper
public interface CoachMapper {

    /**
     * 根据ID查找教练
     * @param id 教练ID
     * @return 教练对象
     */
    @Select("SELECT c.*, v.venue_name, u.username AS bound_username FROM sys_coach c " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE c.id = #{id} AND c.del_flag = 0")
    Coach findById(@Param("id") Long id);

    /**
     * 根据用户ID查找教练（用于 COACH 角色解析当前教练）
     * @param userId 用户ID（sys_user.id，role=COACH）
     * @return 教练对象，未绑定时为 null
     */
    @Select("SELECT c.*, v.venue_name, u.username AS bound_username FROM sys_coach c " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE c.user_id = #{userId} AND c.del_flag = 0 LIMIT 1")
    Coach findByUserId(@Param("userId") Long userId);

    /**
     * 查找所有教练（支持条件筛选和分页）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配姓名、电话）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 教练列表
     */
    @Select("<script>" +
            "SELECT c.*, v.venue_name, u.username AS bound_username FROM sys_coach c " +
            "LEFT JOIN sys_venue v ON c.venue_id = v.id " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "<if test='status != null'> AND c.status = #{status} </if>" +
            "<if test='gender != null'> AND c.gender = #{gender} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (c.coach_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.phone LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY c.create_time DESC, c.id DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<Coach> findAll(@Param("venueId") Long venueId,
                       @Param("status") Integer status,
                       @Param("keyword") String keyword,
                       @Param("gender") Integer gender,
                       @Param("offset") int offset,
                       @Param("limit") int limit);

    /**
     * 统计教练数量（支持条件筛选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配姓名、电话）
     * @param gender 性别（可选，0-女 1-男）
     * @return 教练数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_coach c " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "<if test='status != null'> AND c.status = #{status} </if>" +
            "<if test='gender != null'> AND c.gender = #{gender} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (c.coach_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.phone LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("status") Integer status,
              @Param("keyword") String keyword,
              @Param("gender") Integer gender);

    /**
     * 插入新教练
     * @param coach 教练对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO sys_coach (coach_name, gender, phone, id_card, specialty, experience, " +
            "hourly_price, rating, total_students, status, avatar, venue_id, user_id, create_time, update_time, del_flag) " +
            "VALUES (#{coachName}, #{gender}, #{phone}, #{idCard}, #{specialty}, #{experience}, " +
            "#{hourlyPrice}, #{rating}, #{totalStudents}, #{status}, #{avatar}, #{venueId}, #{userId}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Coach coach);

    /**
     * 更新教练信息
     * @param coach 教练对象
     * @return 影响的行数
     */
    @Update("UPDATE sys_coach SET coach_name = #{coachName}, gender = #{gender}, phone = #{phone}, " +
            "id_card = #{idCard}, specialty = #{specialty}, experience = #{experience}, " +
            "hourly_price = #{hourlyPrice}, rating = #{rating}, total_students = #{totalStudents}, " +
            "status = #{status}, avatar = #{avatar}, venue_id = #{venueId}, user_id = #{userId}, update_time = #{updateTime} " +
            "WHERE id = #{id} AND del_flag = 0")
    int update(Coach coach);

    /**
     * 逻辑删除教练
     * @param id 教练ID
     * @return 影响的行数
     */
    @Update("UPDATE sys_coach SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 更新教练状态
     * @param id 教练ID
     * @param status 新状态
     * @return 影响的行数
     */
    @Update("UPDATE sys_coach SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新累计学员数
     * @param id 教练ID
     * @param totalStudents 累计学员数
     * @return 影响的行数
     */
    @Update("UPDATE sys_coach SET total_students = #{totalStudents}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateTotalStudents(@Param("id") Long id, @Param("totalStudents") Integer totalStudents);

    /**
     * 统计各状态的教练数量（可选按场馆过滤）
     * @param venueId 场馆ID（可选，null 表示全部）
     * @return 各状态数量
     */
    @Select("<script>" +
            "SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM sys_coach " +
            "WHERE del_flag = 0 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            " GROUP BY status" +
            "</script>")
    List<Map<String, Object>> countByStatus(@Param("venueId") Long venueId);

    /**
     * 统计所有教练数量（可选按场馆过滤）
     * @param venueId 场馆ID（可选，null 表示全部）
     * @return 教练总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_coach WHERE del_flag = 0 " +
            "<if test='venueId != null'> AND venue_id = #{venueId} </if>" +
            "</script>")
    int countAll(@Param("venueId") Long venueId);

    @Select("<script>" +
            "SELECT c.id, c.coach_name AS coachName, c.avatar, c.specialty, c.rating, " +
            "COALESCE(course_stats.scheduled_courses, 0) AS scheduledCourses, " +
            "COALESCE(course_stats.completed_courses, 0) AS completedCourses, " +
            "COALESCE(booking_stats.booking_count, 0) AS bookingCount, " +
            "COALESCE(booking_stats.student_count, 0) AS studentCount " +
            "FROM sys_coach c " +
            "LEFT JOIN (" +
            "   SELECT course.coach_id, COUNT(*) AS scheduled_courses, " +
            "          SUM(CASE WHEN course.status = 3 THEN 1 ELSE 0 END) AS completed_courses " +
            "   FROM biz_course course " +
            "   WHERE course.del_flag = 0 " +
            "   GROUP BY course.coach_id" +
            ") course_stats ON course_stats.coach_id = c.id " +
            "LEFT JOIN (" +
            "   SELECT course.coach_id, COUNT(cb.id) AS booking_count, COUNT(DISTINCT cb.member_id) AS student_count " +
            "   FROM biz_course course " +
            "   LEFT JOIN biz_course_booking cb ON cb.course_id = course.id AND cb.del_flag = 0 AND cb.status IN (1, 2, 3, 4) " +
            "   WHERE course.del_flag = 0 " +
            "   GROUP BY course.coach_id" +
            ") booking_stats ON booking_stats.coach_id = c.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND c.venue_id = #{venueId} </if>" +
            "ORDER BY COALESCE(booking_stats.booking_count, 0) DESC, COALESCE(course_stats.completed_courses, 0) DESC, c.id ASC" +
            "</script>")
    List<Map<String, Object>> findCoachWorkload(@Param("venueId") Long venueId);

    /**
     * 查询已绑定账号的用户ID列表（用于管理端“未绑定 COACH”列表）
     */
    @Select("SELECT user_id FROM sys_coach WHERE user_id IS NOT NULL AND del_flag = 0")
    List<Long> findBoundUserIds();

    /**
     * 教练本人更新可编辑字段（姓名、电话、专长、经验、头像）
     */
    @Update("UPDATE sys_coach SET coach_name = #{coachName}, phone = #{phone}, specialty = #{specialty}, " +
            "experience = #{experience}, avatar = #{avatar}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateSelfProfile(@Param("id") Long id, @Param("coachName") String coachName, @Param("phone") String phone,
                          @Param("specialty") String specialty, @Param("experience") String experience, @Param("avatar") String avatar);
}
