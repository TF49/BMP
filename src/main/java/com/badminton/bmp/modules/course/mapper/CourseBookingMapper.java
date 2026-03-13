package com.badminton.bmp.modules.course.mapper;

import com.badminton.bmp.modules.course.entity.CourseBooking;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 课程预约Mapper接口
 */
@Mapper
public interface CourseBookingMapper {

    @Select("SELECT cb.*, m.member_name, c.course_name, co.coach_name, c.course_date, c.start_time AS course_start_time, c.end_time AS course_end_time FROM biz_course_booking cb " +
            "LEFT JOIN sys_member m ON cb.member_id = m.id " +
            "LEFT JOIN biz_course c ON cb.course_id = c.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "WHERE cb.id = #{id} AND cb.del_flag = 0")
    CourseBooking findById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT cb.*, m.member_name, c.course_name, co.coach_name, c.course_date, c.start_time AS course_start_time, c.end_time AS course_end_time FROM biz_course_booking cb " +
            "LEFT JOIN sys_member m ON cb.member_id = m.id " +
            "LEFT JOIN biz_course c ON cb.course_id = c.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "WHERE cb.del_flag = 0 " +
            "<if test='venueId != null'> AND ct.venue_id = #{venueId} </if>" +
            "<if test='memberId != null'> AND cb.member_id = #{memberId} </if>" +
            "<if test='courseId != null'> AND cb.course_id = #{courseId} </if>" +
            "<if test='status != null'> AND cb.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (cb.booking_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.course_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY cb.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<CourseBooking> findAll(@Param("venueId") Long venueId,
                               @Param("memberId") Long memberId,
                               @Param("courseId") Long courseId,
                               @Param("status") Integer status,
                               @Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_course_booking cb " +
            "LEFT JOIN sys_member m ON cb.member_id = m.id " +
            "LEFT JOIN biz_course c ON cb.course_id = c.id " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "WHERE cb.del_flag = 0 " +
            "<if test='venueId != null'> AND ct.venue_id = #{venueId} </if>" +
            "<if test='memberId != null'> AND cb.member_id = #{memberId} </if>" +
            "<if test='courseId != null'> AND cb.course_id = #{courseId} </if>" +
            "<if test='status != null'> AND cb.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (cb.booking_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.course_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int count(@Param("venueId") Long venueId,
              @Param("memberId") Long memberId,
              @Param("courseId") Long courseId,
              @Param("status") Integer status,
              @Param("keyword") String keyword);

    @Insert("INSERT INTO biz_course_booking (booking_no, member_id, course_id, order_amount, " +
            "payment_method, payment_status, status, remark, create_time, update_time, del_flag) " +
            "VALUES (#{bookingNo}, #{memberId}, #{courseId}, #{orderAmount}, " +
            "#{paymentMethod}, #{paymentStatus}, #{status}, #{remark}, #{createTime}, #{updateTime}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CourseBooking booking);

    @Update("UPDATE biz_course_booking SET member_id = #{memberId}, course_id = #{courseId}, " +
            "order_amount = #{orderAmount}, payment_method = #{paymentMethod}, " +
            "payment_status = #{paymentStatus}, status = #{status}, remark = #{remark}, " +
            "update_time = #{updateTime} WHERE id = #{id} AND del_flag = 0")
    int update(CourseBooking booking);

    @Update("UPDATE biz_course_booking SET del_flag = 1, update_time = NOW() WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    @Update("UPDATE biz_course_booking SET status = #{status}, update_time = NOW() " +
            "WHERE id = #{id} AND del_flag = 0")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM biz_course_booking " +
            "WHERE booking_no = #{bookingNo} AND del_flag = 0 " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    boolean existsByBookingNo(@Param("bookingNo") String bookingNo,
                             @Param("excludeId") Long excludeId);

    @Select("SELECT CAST(status AS SIGNED) AS status, COUNT(*) as count FROM biz_course_booking " +
            "WHERE del_flag = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();

    @Select("SELECT COUNT(*) FROM biz_course_booking WHERE del_flag = 0")
    int countAll();

    /**
     * 获取指定前缀下最大的预约单号（不区分是否逻辑删除，避免重复）
     */
    @Select("SELECT booking_no FROM biz_course_booking " +
            "WHERE booking_no LIKE CONCAT(#{prefix}, '%') " +
            "ORDER BY booking_no DESC LIMIT 1")
    String findMaxBookingNoByPrefix(@Param("prefix") String prefix);

    /** 按场馆过滤的预约总数（场馆管理员用） */
    @Select("SELECT COUNT(*) FROM biz_course_booking cb " +
            "INNER JOIN biz_course c ON cb.course_id = c.id " +
            "INNER JOIN sys_court ct ON c.court_id = ct.id " +
            "WHERE cb.del_flag = 0 AND ct.venue_id = #{venueId}")
    int countAllFiltered(@Param("venueId") Long venueId);

    /** 按场馆过滤的按状态统计（场馆管理员用） */
    @Select("SELECT CAST(cb.status AS SIGNED) AS status, COUNT(*) as count FROM biz_course_booking cb " +
            "INNER JOIN biz_course c ON cb.course_id = c.id " +
            "INNER JOIN sys_court ct ON c.court_id = ct.id " +
            "WHERE cb.del_flag = 0 AND ct.venue_id = #{venueId} GROUP BY cb.status")
    List<Map<String, Object>> countByStatusFiltered(@Param("venueId") Long venueId);

    /** 按教练过滤：当前教练所教课程的预约列表（教练端用） */
    @Select("<script>" +
            "SELECT cb.*, m.member_name, c.course_name, co.coach_name, c.course_date, c.start_time AS course_start_time, c.end_time AS course_end_time FROM biz_course_booking cb " +
            "LEFT JOIN sys_member m ON cb.member_id = m.id " +
            "INNER JOIN biz_course c ON cb.course_id = c.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "WHERE cb.del_flag = 0 AND c.coach_id = #{coachId} " +
            "<if test='status != null'> AND cb.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (cb.booking_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.course_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY cb.create_time DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<CourseBooking> findAllByCoachId(@Param("coachId") Long coachId,
                                         @Param("status") Integer status,
                                         @Param("keyword") String keyword,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_course_booking cb " +
            "INNER JOIN biz_course c ON cb.course_id = c.id " +
            "LEFT JOIN sys_member m ON cb.member_id = m.id " +
            "WHERE cb.del_flag = 0 AND c.coach_id = #{coachId} " +
            "<if test='status != null'> AND cb.status = #{status} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (cb.booking_no LIKE CONCAT('%', #{keyword}, '%') " +
            "OR m.member_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR c.course_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "</script>")
    int countByCoachId(@Param("coachId") Long coachId,
                       @Param("status") Integer status,
                       @Param("keyword") String keyword);

    /**
     * 定时任务：查询「已支付」且关联课程已到开始时间的预约ID（用于自动改为进行中）
     * 条件：cb.status=2 且 (course_date &lt; 今日 或 (course_date=今日 且 start_time &lt;= 当前时间))
     */
    @Select("SELECT cb.id FROM biz_course_booking cb INNER JOIN biz_course c ON cb.course_id = c.id " +
            "WHERE cb.del_flag = 0 AND cb.status = 2 " +
            "AND (c.course_date < #{today} OR (c.course_date = #{today} AND c.start_time <= #{nowTime}))")
    List<Long> findBookingIdsToStart(@Param("today") LocalDate today, @Param("nowTime") LocalTime nowTime);

    /**
     * 定时任务：查询「进行中」且关联课程已到结束时间的预约ID（用于自动改为已完成）
     * 条件：cb.status=3 且 (course_date &lt; 今日 或 (course_date=今日 且 end_time &lt;= 当前时间))
     */
    @Select("SELECT cb.id FROM biz_course_booking cb INNER JOIN biz_course c ON cb.course_id = c.id " +
            "WHERE cb.del_flag = 0 AND cb.status = 3 " +
            "AND (c.course_date < #{today} OR (c.course_date = #{today} AND c.end_time <= #{nowTime}))")
    List<Long> findBookingIdsToFinish(@Param("today") LocalDate today, @Param("nowTime") LocalTime nowTime);

    /**
     * 按课程名称汇总出勤数（status=4 已完成，表示已签到/已上完课），用于与课程表汇总的报名人数合并计算出勤率
     */
    @Select("<script>" +
            "SELECT c.course_name AS courseName, COUNT(CASE WHEN cb.status = 4 THEN 1 END) AS attendanceCount " +
            "FROM biz_course c " +
            "INNER JOIN biz_course_booking cb ON c.id = cb.course_id AND cb.del_flag = 0 " +
            "LEFT JOIN sys_court ct ON c.court_id = ct.id " +
            "LEFT JOIN sys_coach co ON c.coach_id = co.id " +
            "WHERE c.del_flag = 0 " +
            "<if test='venueId != null'> AND (ct.venue_id = #{venueId} OR (c.court_id IS NULL AND co.venue_id = #{venueId})) </if>" +
            "GROUP BY c.course_name" +
            "</script>")
    List<Map<String, Object>> attendanceCountByCourseName(@Param("venueId") Long venueId);
}
