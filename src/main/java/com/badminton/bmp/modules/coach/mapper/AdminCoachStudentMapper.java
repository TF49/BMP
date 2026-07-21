package com.badminton.bmp.modules.coach.mapper;

import com.badminton.bmp.modules.coach.vo.AdminCoachStudentVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentAttendanceVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentConsumeRecordVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentScheduleVO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理端教练学员关系 Mapper
 * 提供 PRESIDENT / VENUE_MANAGER 使用的教练-学员管理查询能力
 * <p>
 * 表结构（biz_coach_student）：
 *   id, coach_id, member_id, bind_type(AUTO/MANUAL), del_flag, create_time, update_time
 * </p>
 */
@Mapper
public interface AdminCoachStudentMapper {

    // ================================================================
    // 管理端列表查询（支持场馆/教练/关键词过滤 + 分页）
    // ================================================================

    /**
     * 管理端分页查询教练学员关系列表
     * venueId = null 时查所有场馆（会长权限）
     */
    @Select("""
            <script>
            SELECT
                bcs.id, bcs.bind_type, bcs.create_time, bcs.update_time,
                c.id          AS coach_id,
                c.coach_name,
                CASE WHEN c.phone REGEXP '^1[0-9]{10}$'
                     THEN CONCAT(LEFT(c.phone, 3), '****', RIGHT(c.phone, 4))
                     WHEN c.phone IS NULL OR c.phone = '' THEN '未填写' ELSE c.phone END AS coach_phone,
                COALESCE(cu.avatar, '/static/placeholders/avatar.svg') AS coach_avatar,
                v.id          AS venue_id,
                v.venue_name,
                m.id          AS member_id,
                m.member_name,
                m.gender,
                COALESCE(mu.avatar, '/static/placeholders/avatar.svg') AS member_avatar,
                CASE WHEN m.phone REGEXP '^1[0-9]{10}$'
                     THEN CONCAT(LEFT(m.phone, 3), '****', RIGHT(m.phone, 4))
                     WHEN m.phone IS NULL OR m.phone = '' THEN '未填写' ELSE m.phone END AS masked_phone,
                m.member_type,
                m.member_level,
                m.status      AS member_status,
                m.expire_time,
                MIN(TIMESTAMP(co.course_date, co.start_time))       AS first_lesson_time,
                MAX(CASE WHEN COALESCE(cb.attendance_status, 0) = 2
                         THEN TIMESTAMP(co.course_date, co.end_time) END) AS last_lesson_time,
                COUNT(cb.id)  AS total_bookings,
                SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) AS attended_count,
                SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 3 THEN 1 ELSE 0 END) AS absent_count,
                COALESCE(ROUND(
                    SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) * 100.0 /
                    NULLIF(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) IN (2, 3) THEN 1 ELSE 0 END), 0), 2
                ), 0) AS attendance_rate,
                COALESCE(ROUND(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2
                                        THEN co.course_duration ELSE 0 END) / 60.0, 2), 0) AS total_hours,
                COALESCE((SELECT SUM(r.amount)
                    FROM biz_member_consume_record r
                    INNER JOIN biz_course_booking rb ON r.business_id = rb.id AND rb.del_flag = 0
                    INNER JOIN biz_course rc ON rb.course_id = rc.id AND rc.del_flag = 0
                    WHERE r.business_type = 'COURSE' AND r.member_id = m.id
                      AND rc.coach_id = c.id), 0) AS total_paid_amount
            FROM biz_coach_student bcs
            INNER JOIN sys_coach c  ON bcs.coach_id  = c.id  AND c.del_flag = 0
            INNER JOIN sys_member m ON bcs.member_id = m.id  AND m.del_flag = 0
            LEFT JOIN sys_venue v   ON c.venue_id = v.id
            LEFT JOIN sys_user cu   ON c.user_id  = cu.id
            LEFT JOIN sys_user mu   ON m.user_id  = mu.id
            LEFT JOIN (biz_course_booking cb
                INNER JOIN biz_course co
                    ON cb.course_id = co.id AND co.del_flag = 0 AND co.status != 0)
                ON cb.member_id = m.id AND cb.del_flag = 0 AND co.coach_id = c.id
            WHERE bcs.del_flag = 0
            <if test="venueId != null">AND c.venue_id = #{venueId}</if>
            <if test="coachId != null">AND c.id = #{coachId}</if>
            <if test="keyword != null and keyword != ''">
              AND (m.member_name LIKE CONCAT('%', #{keyword}, '%')
                   OR m.phone LIKE CONCAT('%', #{keyword}, '%')
                   OR c.coach_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            GROUP BY bcs.id, bcs.bind_type, bcs.create_time, bcs.update_time,
                     c.id, c.coach_name, c.phone, cu.avatar,
                     v.id, v.venue_name,
                     m.id, m.member_name, m.gender, mu.avatar, m.phone,
                     m.member_type, m.member_level, m.status, m.expire_time
            ORDER BY bcs.create_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<AdminCoachStudentVO> findList(@Param("venueId") Long venueId,
                                      @Param("coachId") Long coachId,
                                      @Param("keyword") String keyword,
                                      @Param("offset") int offset,
                                      @Param("size") int size);

    /**
     * 管理端统计教练学员关系总数
     */
    @Select("""
            <script>
            SELECT COUNT(bcs.id)
            FROM biz_coach_student bcs
            INNER JOIN sys_coach c  ON bcs.coach_id  = c.id  AND c.del_flag = 0
            INNER JOIN sys_member m ON bcs.member_id = m.id  AND m.del_flag = 0
            WHERE bcs.del_flag = 0
            <if test="venueId != null">AND c.venue_id = #{venueId}</if>
            <if test="coachId != null">AND c.id = #{coachId}</if>
            <if test="keyword != null and keyword != ''">
              AND (m.member_name LIKE CONCAT('%', #{keyword}, '%')
                   OR m.phone LIKE CONCAT('%', #{keyword}, '%')
                   OR c.coach_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            </script>
            """)
    int countList(@Param("venueId") Long venueId,
                  @Param("coachId") Long coachId,
                  @Param("keyword") String keyword);

    // ================================================================
    // 学员详情 - 课程排课课表 / 考勤历史 / 消费明细
    // （复用 CoachStudentMapper 中的相同 SQL，通过管理员权限覆盖 coachId 进行访问）
    // ================================================================

    /**
     * 管理端分页查询学员在指定教练下的课程排课记录
     */
    @Select("""
            <script>
            SELECT cb.id AS booking_id, co.id AS course_id, co.course_name, co.course_date,
                   co.start_time, co.end_time, cb.status AS booking_status,
                   COALESCE(cb.attendance_status, 0) AS attendance_status,
                   cb.actual_checkin_time, cb.actual_finish_time, cb.remark
            FROM biz_course_booking cb
            INNER JOIN biz_course co ON cb.course_id = co.id
            WHERE cb.del_flag = 0 AND co.del_flag = 0 AND co.status != 0
              AND co.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test="startDate != null">AND co.course_date &gt;= #{startDate}</if>
            <if test="endDate != null">AND co.course_date &lt;= #{endDate}</if>
            <if test="status != null">AND cb.status = #{status}</if>
            ORDER BY co.course_date DESC, co.start_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<CoachStudentScheduleVO> findSchedule(@Param("coachId") Long coachId,
                                              @Param("memberId") Long memberId,
                                              @Param("startDate") java.time.LocalDate startDate,
                                              @Param("endDate") java.time.LocalDate endDate,
                                              @Param("status") Integer status,
                                              @Param("offset") int offset,
                                              @Param("size") int size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_course_booking cb INNER JOIN biz_course co ON cb.course_id = co.id
            WHERE cb.del_flag = 0 AND co.del_flag = 0 AND co.status != 0
              AND co.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test="startDate != null">AND co.course_date &gt;= #{startDate}</if>
            <if test="endDate != null">AND co.course_date &lt;= #{endDate}</if>
            <if test="status != null">AND cb.status = #{status}</if>
            </script>
            """)
    int countSchedule(@Param("coachId") Long coachId,
                      @Param("memberId") Long memberId,
                      @Param("startDate") java.time.LocalDate startDate,
                      @Param("endDate") java.time.LocalDate endDate,
                      @Param("status") Integer status);

    /**
     * 管理端分页查询学员在指定教练下的考勤历史
     */
    @Select("""
            <script>
            SELECT cb.id AS booking_id, co.course_name, co.course_date, co.start_time, co.end_time,
                   cb.status AS booking_status, COALESCE(cb.attendance_status, 0) AS attendance_status,
                   co.course_duration AS duration_minutes, cb.remark
            FROM biz_course_booking cb INNER JOIN biz_course co ON cb.course_id = co.id
            WHERE cb.del_flag = 0 AND co.del_flag = 0 AND co.status != 0
              AND co.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test="startDate != null">AND co.course_date &gt;= #{startDate}</if>
            <if test="endDate != null">AND co.course_date &lt;= #{endDate}</if>
            <if test="attendanceStatus != null">AND COALESCE(cb.attendance_status, 0) = #{attendanceStatus}</if>
            ORDER BY co.course_date DESC, co.start_time DESC
            LIMIT #{offset}, #{size}
            </script>
            """)
    List<CoachStudentAttendanceVO> findAttendance(@Param("coachId") Long coachId,
                                                  @Param("memberId") Long memberId,
                                                  @Param("startDate") java.time.LocalDate startDate,
                                                  @Param("endDate") java.time.LocalDate endDate,
                                                  @Param("attendanceStatus") Integer attendanceStatus,
                                                  @Param("offset") int offset,
                                                  @Param("size") int size);

    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_course_booking cb INNER JOIN biz_course co ON cb.course_id = co.id
            WHERE cb.del_flag = 0 AND co.del_flag = 0 AND co.status != 0
              AND co.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test="startDate != null">AND co.course_date &gt;= #{startDate}</if>
            <if test="endDate != null">AND co.course_date &lt;= #{endDate}</if>
            <if test="attendanceStatus != null">AND COALESCE(cb.attendance_status, 0) = #{attendanceStatus}</if>
            </script>
            """)
    int countAttendance(@Param("coachId") Long coachId,
                        @Param("memberId") Long memberId,
                        @Param("startDate") java.time.LocalDate startDate,
                        @Param("endDate") java.time.LocalDate endDate,
                        @Param("attendanceStatus") Integer attendanceStatus);

    /**
     * 管理端分页查询学员在指定教练下的消费明细
     */
    @Select("""
            SELECT r.id, r.business_id AS booking_id, co.course_name, r.amount,
                   r.payment_method, r.payment_status, r.description, r.remark, r.create_time
            FROM biz_member_consume_record r
            INNER JOIN biz_course_booking cb ON r.business_id = cb.id AND cb.del_flag = 0
            INNER JOIN biz_course co ON cb.course_id = co.id AND co.del_flag = 0
            WHERE r.business_type = 'COURSE' AND r.member_id = #{memberId}
              AND co.coach_id = #{coachId}
            ORDER BY r.create_time DESC LIMIT #{offset}, #{size}
            """)
    List<CoachStudentConsumeRecordVO> findConsumeRecords(@Param("coachId") Long coachId,
                                                         @Param("memberId") Long memberId,
                                                         @Param("offset") int offset,
                                                         @Param("size") int size);

    @Select("""
            SELECT COUNT(*) FROM biz_member_consume_record r
            INNER JOIN biz_course_booking cb ON r.business_id = cb.id AND cb.del_flag = 0
            INNER JOIN biz_course co ON cb.course_id = co.id AND co.del_flag = 0
            WHERE r.business_type = 'COURSE' AND r.member_id = #{memberId}
              AND co.coach_id = #{coachId}
            """)
    int countConsumeRecords(@Param("coachId") Long coachId, @Param("memberId") Long memberId);
}
