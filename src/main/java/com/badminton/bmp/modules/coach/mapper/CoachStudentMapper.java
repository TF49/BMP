package com.badminton.bmp.modules.coach.mapper;

import com.badminton.bmp.modules.coach.dto.CoachStudentListQuery;
import com.badminton.bmp.modules.coach.dto.CoachStudentScheduleQuery;
import com.badminton.bmp.modules.coach.vo.CoachStudentAttendanceVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentConsumeRecordVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentDetailVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentListItemVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentProgressVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentScheduleVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CoachStudentMapper {

    @Select("SELECT COUNT(*) FROM biz_course_booking cb " +
            "INNER JOIN biz_course c ON cb.course_id = c.id " +
            "INNER JOIN sys_member m ON cb.member_id = m.id " +
            "WHERE cb.del_flag = 0 AND c.del_flag = 0 AND m.del_flag = 0 " +
            "AND c.status != 0 AND c.coach_id = #{coachId} AND m.id = #{memberId} " +
            "AND (cb.status IN (2, 3, 4) OR COALESCE(cb.attendance_status, 0) IN (1, 2, 3)) " +
            "AND TIMESTAMP(c.course_date, c.end_time) >= DATE_SUB(NOW(), INTERVAL #{retentionDays} DAY)")
    int existsValidCoachStudentRelation(@Param("coachId") Long coachId,
                                        @Param("memberId") Long memberId,
                                        @Param("retentionDays") int retentionDays);

    @Select("SELECT MAX(DATE_ADD(TIMESTAMP(c.course_date, c.end_time), " +
            "INTERVAL #{retentionDays} DAY)) " +
            "FROM biz_course_booking cb " +
            "INNER JOIN biz_course c ON cb.course_id = c.id " +
            "INNER JOIN sys_member m ON cb.member_id = m.id " +
            "WHERE cb.del_flag = 0 AND c.del_flag = 0 AND m.del_flag = 0 " +
            "AND c.status != 0 AND c.coach_id = #{coachId} AND m.id = #{memberId} " +
            "AND (cb.status IN (2, 3, 4) OR COALESCE(cb.attendance_status, 0) IN (1, 2, 3))")
    java.time.LocalDateTime findCoachStudentRelationValidUntil(@Param("coachId") Long coachId,
                                                               @Param("memberId") Long memberId,
                                                               @Param("retentionDays") int retentionDays);

    @Select("""
            <script>
            SELECT m.id, m.member_name, m.gender,
                   COALESCE(u.avatar, '/static/placeholders/avatar.svg') AS avatar,
                   CASE WHEN m.phone REGEXP '^1[0-9]{10}$'
                        THEN CONCAT(LEFT(m.phone, 3), '****', RIGHT(m.phone, 4))
                        WHEN m.phone IS NULL OR m.phone = '' THEN '未填写' ELSE m.phone END AS masked_phone,
                   m.member_type, m.member_level, m.status AS member_status, m.expire_time,
                   MIN(TIMESTAMP(c.course_date, c.start_time)) AS first_lesson_time,
                   MAX(CASE WHEN COALESCE(cb.attendance_status, 0) = 2
                            THEN TIMESTAMP(c.course_date, c.end_time) END) AS last_lesson_time,
                   MIN(CASE WHEN TIMESTAMP(c.course_date, c.start_time) >= NOW()
                                 AND cb.status IN (2, 3)
                                 AND COALESCE(cb.attendance_status, 0) NOT IN (2, 3)
                            THEN TIMESTAMP(c.course_date, c.start_time) END) AS next_lesson_time,
                   COUNT(cb.id) AS total_bookings,
                   SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) AS attended_count,
                   SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 3 THEN 1 ELSE 0 END) AS absent_count,
                   SUM(CASE WHEN cb.status = 0 THEN 1 ELSE 0 END) AS cancelled_count,
                   SUM(CASE WHEN cb.status IN (2, 3)
                                 AND TIMESTAMP(c.course_date, c.start_time) >= NOW()
                                 AND COALESCE(cb.attendance_status, 0) NOT IN (2, 3)
                            THEN 1 ELSE 0 END) AS scheduled_count,
                   COALESCE(ROUND(
                       SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) * 100.0 /
                       NULLIF(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) IN (2, 3) THEN 1 ELSE 0 END), 0), 2
                   ), 0) AS attendance_rate,
                   COALESCE(ROUND(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2
                                           THEN c.course_duration ELSE 0 END) / 60.0, 2), 0) AS total_hours,
                   COALESCE((SELECT SUM(r.amount)
                       FROM biz_member_consume_record r
                       INNER JOIN biz_course_booking rb ON r.business_id = rb.id AND rb.del_flag = 0
                       INNER JOIN biz_course rc ON rb.course_id = rc.id AND rc.del_flag = 0
                       WHERE r.business_type = 'COURSE' AND r.member_id = m.id
                         AND rc.coach_id = #{coachId}), 0) AS total_paid_amount
            FROM sys_member m
            INNER JOIN (
                SELECT DISTINCT vb.member_id
                FROM biz_course_booking vb
                INNER JOIN biz_course vc ON vb.course_id = vc.id
                WHERE vb.del_flag = 0 AND vc.del_flag = 0 AND vc.status != 0
                  AND vc.coach_id = #{coachId}
                  AND (vb.status IN (2, 3, 4) OR COALESCE(vb.attendance_status, 0) IN (1, 2, 3))
                  AND TIMESTAMP(vc.course_date, vc.end_time) >= DATE_SUB(NOW(), INTERVAL #{retentionDays} DAY)
            ) relation ON relation.member_id = m.id
            LEFT JOIN sys_user u ON m.user_id = u.id
            LEFT JOIN (biz_course_booking cb
                INNER JOIN biz_course c ON cb.course_id = c.id AND c.del_flag = 0
                    AND c.status != 0 AND c.coach_id = #{coachId})
                ON cb.member_id = m.id AND cb.del_flag = 0
            WHERE m.del_flag = 0
            <if test='query.keyword != null and query.keyword != ""'>
              AND (m.member_name LIKE CONCAT('%', #{query.keyword}, '%')
                   OR m.phone LIKE CONCAT('%', #{query.keyword}, '%'))
            </if>
            <if test='query.memberType != null and query.memberType != ""'>
              AND m.member_type = #{query.memberType}
            </if>
            <if test='query.todayOnly != null and query.todayOnly'>
              AND EXISTS (SELECT 1 FROM biz_course_booking tcb
                          INNER JOIN biz_course tc ON tcb.course_id = tc.id
                          WHERE tcb.member_id = m.id AND tcb.del_flag = 0 AND tc.del_flag = 0
                            AND tc.status != 0 AND tc.coach_id = #{coachId}
                            AND (tcb.status IN (2, 3, 4)
                                 OR COALESCE(tcb.attendance_status, 0) IN (1, 2, 3))
                            AND tc.course_date = CURRENT_DATE)
            </if>
            GROUP BY m.id, m.member_name, m.gender, u.avatar, m.phone, m.member_type,
                     m.member_level, m.status, m.expire_time
            <if test='query.riskOnly != null and query.riskOnly'>
            HAVING ((attended_count + absent_count > 0 AND attendance_rate &lt; #{riskAttendanceRate})
                    OR (last_lesson_time IS NOT NULL
                        AND last_lesson_time &lt; DATE_SUB(NOW(), INTERVAL #{riskInactiveDays} DAY)))
            </if>
            ORDER BY
            <choose>
              <when test='query.orderBy == "attendanceRate"'>attendance_rate</when>
              <when test='query.orderBy == "totalPaidAmount"'>total_paid_amount</when>
              <when test='query.orderBy == "totalHours"'>total_hours</when>
              <otherwise>last_lesson_time</otherwise>
            </choose>
            <choose><when test='query.orderDirection == "ASC"'>ASC</when><otherwise>DESC</otherwise></choose>,
            m.id DESC
            LIMIT #{offset}, #{query.size}
            </script>
            """)
    List<CoachStudentListItemVO> findStudents(@Param("coachId") Long coachId,
                                              @Param("query") CoachStudentListQuery query,
                                              @Param("retentionDays") int retentionDays,
                                              @Param("riskAttendanceRate") int riskAttendanceRate,
                                              @Param("riskInactiveDays") int riskInactiveDays,
                                              @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(*) FROM (
            SELECT m.id,
                   SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) AS attended_count,
                   SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 3 THEN 1 ELSE 0 END) AS absent_count,
                   COALESCE(ROUND(
                       SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) * 100.0 /
                       NULLIF(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) IN (2, 3) THEN 1 ELSE 0 END), 0), 2
                   ), 0) AS attendance_rate,
                   MAX(CASE WHEN COALESCE(cb.attendance_status, 0) = 2
                            THEN TIMESTAMP(c.course_date, c.end_time) END) AS last_lesson_time
            FROM sys_member m
            INNER JOIN (
                SELECT DISTINCT vb.member_id
                FROM biz_course_booking vb
                INNER JOIN biz_course vc ON vb.course_id = vc.id
                WHERE vb.del_flag = 0 AND vc.del_flag = 0 AND vc.status != 0
                  AND vc.coach_id = #{coachId}
                  AND (vb.status IN (2, 3, 4) OR COALESCE(vb.attendance_status, 0) IN (1, 2, 3))
                  AND TIMESTAMP(vc.course_date, vc.end_time) >= DATE_SUB(NOW(), INTERVAL #{retentionDays} DAY)
            ) relation ON relation.member_id = m.id
            LEFT JOIN (biz_course_booking cb
                INNER JOIN biz_course c ON cb.course_id = c.id AND c.del_flag = 0
                    AND c.status != 0 AND c.coach_id = #{coachId})
                ON cb.member_id = m.id AND cb.del_flag = 0
            WHERE m.del_flag = 0
            <if test='query.keyword != null and query.keyword != ""'>
              AND (m.member_name LIKE CONCAT('%', #{query.keyword}, '%')
                   OR m.phone LIKE CONCAT('%', #{query.keyword}, '%'))
            </if>
            <if test='query.memberType != null and query.memberType != ""'>AND m.member_type = #{query.memberType}</if>
            <if test='query.todayOnly != null and query.todayOnly'>
              AND EXISTS (SELECT 1 FROM biz_course_booking tcb
                          INNER JOIN biz_course tc ON tcb.course_id = tc.id
                          WHERE tcb.member_id = m.id AND tcb.del_flag = 0 AND tc.del_flag = 0
                            AND tc.status != 0 AND tc.coach_id = #{coachId}
                            AND (tcb.status IN (2, 3, 4)
                                 OR COALESCE(tcb.attendance_status, 0) IN (1, 2, 3))
                            AND tc.course_date = CURRENT_DATE)
            </if>
            GROUP BY m.id
            <if test='query.riskOnly != null and query.riskOnly'>
            HAVING ((attended_count + absent_count > 0 AND attendance_rate &lt; #{riskAttendanceRate})
                    OR (last_lesson_time IS NOT NULL
                        AND last_lesson_time &lt; DATE_SUB(NOW(), INTERVAL #{riskInactiveDays} DAY)))
            </if>
            ) filtered_students
            </script>
            """)
    int countStudents(@Param("coachId") Long coachId,
                      @Param("query") CoachStudentListQuery query,
                      @Param("retentionDays") int retentionDays,
                      @Param("riskAttendanceRate") int riskAttendanceRate,
                      @Param("riskInactiveDays") int riskInactiveDays);

    @Select("""
            SELECT COUNT(DISTINCT m.id) AS totalStudents,
                   COUNT(DISTINCT CASE WHEN DATE(c.course_date) = CURRENT_DATE THEN m.id END) AS todayStudents,
                   COALESCE(ROUND(
                       SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) * 100.0 /
                       NULLIF(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) IN (2, 3) THEN 1 ELSE 0 END), 0), 2
                   ), 0) AS averageAttendanceRate
            FROM sys_member m
            INNER JOIN biz_course_booking cb ON cb.member_id = m.id AND cb.del_flag = 0
            INNER JOIN biz_course c ON cb.course_id = c.id AND c.del_flag = 0
            WHERE m.del_flag = 0 AND c.status != 0 AND c.coach_id = #{coachId}
              AND (cb.status IN (2, 3, 4) OR COALESCE(cb.attendance_status, 0) IN (1, 2, 3))
              AND TIMESTAMP(c.course_date, c.end_time) >= DATE_SUB(NOW(), INTERVAL #{retentionDays} DAY)
            """)
    Map<String, Object> summarizeStudents(@Param("coachId") Long coachId,
                                          @Param("retentionDays") int retentionDays);

    @Select("""
            SELECT COUNT(*) FROM (
                SELECT cb.member_id
                FROM biz_course_booking cb
                INNER JOIN biz_course c ON cb.course_id = c.id
                INNER JOIN sys_member m ON cb.member_id = m.id
                WHERE cb.del_flag = 0 AND c.del_flag = 0 AND m.del_flag = 0
                  AND c.status != 0 AND c.coach_id = #{coachId}
                  AND (cb.status IN (2, 3, 4) OR COALESCE(cb.attendance_status, 0) IN (1, 2, 3))
                  AND TIMESTAMP(c.course_date, c.end_time) >= DATE_SUB(NOW(), INTERVAL #{retentionDays} DAY)
                GROUP BY cb.member_id
                HAVING ((SUM(CASE WHEN COALESCE(cb.attendance_status, 0) IN (2, 3) THEN 1 ELSE 0 END) > 0
                         AND COALESCE(ROUND(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) * 100.0 /
                             NULLIF(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) IN (2, 3) THEN 1 ELSE 0 END), 0), 2), 0)
                             &lt; #{riskAttendanceRate})
                    OR MAX(CASE WHEN COALESCE(cb.attendance_status, 0) = 2
                                THEN TIMESTAMP(c.course_date, c.end_time) END)
                                &lt; DATE_SUB(NOW(), INTERVAL #{riskInactiveDays} DAY))
            ) risk_students
            """)
    int countRiskStudents(@Param("coachId") Long coachId,
                          @Param("retentionDays") int retentionDays,
                          @Param("riskAttendanceRate") int riskAttendanceRate,
                          @Param("riskInactiveDays") int riskInactiveDays);

    @Select("""
            SELECT m.id, m.member_name, m.gender,
                   COALESCE(u.avatar, '/static/placeholders/avatar.svg') AS avatar,
                   m.phone, RIGHT(m.id_card, 4) AS id_card_tail, m.member_type, m.member_level,
                   m.status AS member_status, m.register_time, m.expire_time,
                   MIN(TIMESTAMP(c.course_date, c.start_time)) AS first_lesson_time,
                   MAX(CASE WHEN COALESCE(cb.attendance_status, 0) = 2
                            THEN TIMESTAMP(c.course_date, c.end_time) END) AS last_lesson_time,
                   MIN(CASE WHEN TIMESTAMP(c.course_date, c.start_time) >= NOW() AND cb.status IN (2, 3)
                            THEN TIMESTAMP(c.course_date, c.start_time) END) AS next_lesson_time,
                   COUNT(cb.id) AS total_bookings,
                   SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) AS attended_count,
                   SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 3 THEN 1 ELSE 0 END) AS absent_count,
                   COALESCE(ROUND(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) * 100.0 /
                       NULLIF(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) IN (2, 3) THEN 1 ELSE 0 END), 0), 2), 0) AS attendance_rate,
                   COALESCE(ROUND(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN c.course_duration ELSE 0 END) / 60.0, 2), 0) AS total_hours,
                   COALESCE((SELECT SUM(r.amount) FROM biz_member_consume_record r
                       INNER JOIN biz_course_booking rb ON r.business_id = rb.id
                       INNER JOIN biz_course rc ON rb.course_id = rc.id
                       WHERE r.business_type = 'COURSE' AND r.member_id = m.id
                         AND rb.del_flag = 0 AND rc.del_flag = 0 AND rc.coach_id = #{coachId}), 0)
                       AS total_consumption_for_coach
            FROM sys_member m
            LEFT JOIN sys_user u ON m.user_id = u.id
            LEFT JOIN (biz_course_booking cb
                INNER JOIN biz_course c ON cb.course_id = c.id AND c.del_flag = 0
                    AND c.status != 0 AND c.coach_id = #{coachId})
                ON cb.member_id = m.id AND cb.del_flag = 0
            WHERE m.id = #{memberId} AND m.del_flag = 0
            GROUP BY m.id, m.member_name, m.gender, u.avatar, m.phone, m.id_card,
                     m.member_type, m.member_level, m.status, m.register_time, m.expire_time
            """)
    CoachStudentDetailVO findStudentDetail(@Param("coachId") Long coachId,
                                           @Param("memberId") Long memberId);

    @Select("""
            SELECT c.course_name, COUNT(cb.id) AS total_bookings,
                   SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN 1 ELSE 0 END) AS completed_lessons,
                   ROUND(SUM(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN c.course_duration ELSE 0 END) / 60.0, 2) AS total_hours,
                   MAX(CASE WHEN COALESCE(cb.attendance_status, 0) = 2 THEN TIMESTAMP(c.course_date, c.end_time) END) AS last_lesson_time
            FROM biz_course_booking cb INNER JOIN biz_course c ON cb.course_id = c.id
            WHERE cb.del_flag = 0 AND c.del_flag = 0 AND c.status != 0
              AND c.coach_id = #{coachId} AND cb.member_id = #{memberId}
            GROUP BY c.course_name ORDER BY last_lesson_time DESC
            """)
    List<CoachStudentProgressVO> findProgress(@Param("coachId") Long coachId,
                                             @Param("memberId") Long memberId);

    @Select("""
            <script>
            SELECT cb.id AS booking_id, c.id AS course_id, c.course_name, c.course_date,
                   c.start_time, c.end_time, cb.status AS booking_status,
                   COALESCE(cb.attendance_status, 0) AS attendance_status,
                   cb.actual_checkin_time, cb.actual_finish_time, cb.remark
            FROM biz_course_booking cb INNER JOIN biz_course c ON cb.course_id = c.id
            WHERE cb.del_flag = 0 AND c.del_flag = 0 AND c.status != 0
              AND c.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test='query.startDate != null'>AND c.course_date &gt;= #{query.startDate}</if>
            <if test='query.endDate != null'>AND c.course_date &lt;= #{query.endDate}</if>
            <if test='query.status != null'>AND cb.status = #{query.status}</if>
            ORDER BY c.course_date DESC, c.start_time DESC LIMIT #{offset}, #{query.size}
            </script>
            """)
    List<CoachStudentScheduleVO> findSchedule(@Param("coachId") Long coachId,
                                              @Param("memberId") Long memberId,
                                              @Param("query") CoachStudentScheduleQuery query,
                                              @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_course_booking cb INNER JOIN biz_course c ON cb.course_id = c.id
            WHERE cb.del_flag = 0 AND c.del_flag = 0 AND c.status != 0
              AND c.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test='query.startDate != null'>AND c.course_date &gt;= #{query.startDate}</if>
            <if test='query.endDate != null'>AND c.course_date &lt;= #{query.endDate}</if>
            <if test='query.status != null'>AND cb.status = #{query.status}</if>
            </script>
            """)
    int countSchedule(@Param("coachId") Long coachId,
                      @Param("memberId") Long memberId,
                      @Param("query") CoachStudentScheduleQuery query);

    @Select("""
            <script>
            SELECT cb.id AS booking_id, c.course_name, c.course_date, c.start_time, c.end_time,
                   cb.status AS booking_status, COALESCE(cb.attendance_status, 0) AS attendance_status,
                   c.course_duration AS duration_minutes, cb.remark
            FROM biz_course_booking cb INNER JOIN biz_course c ON cb.course_id = c.id
            WHERE cb.del_flag = 0 AND c.del_flag = 0 AND c.status != 0
              AND c.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test='query.startDate != null'>AND c.course_date &gt;= #{query.startDate}</if>
            <if test='query.endDate != null'>AND c.course_date &lt;= #{query.endDate}</if>
            <if test='query.attendanceStatus != null'>AND COALESCE(cb.attendance_status, 0) = #{query.attendanceStatus}</if>
            ORDER BY c.course_date DESC, c.start_time DESC LIMIT #{offset}, #{query.size}
            </script>
            """)
    List<CoachStudentAttendanceVO> findAttendance(@Param("coachId") Long coachId,
                                                  @Param("memberId") Long memberId,
                                                  @Param("query") CoachStudentScheduleQuery query,
                                                  @Param("offset") int offset);

    @Select("""
            <script>
            SELECT COUNT(*) FROM biz_course_booking cb INNER JOIN biz_course c ON cb.course_id = c.id
            WHERE cb.del_flag = 0 AND c.del_flag = 0 AND c.status != 0
              AND c.coach_id = #{coachId} AND cb.member_id = #{memberId}
            <if test='query.startDate != null'>AND c.course_date &gt;= #{query.startDate}</if>
            <if test='query.endDate != null'>AND c.course_date &lt;= #{query.endDate}</if>
            <if test='query.attendanceStatus != null'>
              AND COALESCE(cb.attendance_status, 0) = #{query.attendanceStatus}
            </if>
            </script>
            """)
    int countAttendance(@Param("coachId") Long coachId,
                        @Param("memberId") Long memberId,
                        @Param("query") CoachStudentScheduleQuery query);

    @Select("""
            SELECT r.id, r.business_id AS booking_id, c.course_name, r.amount,
                   r.payment_method, r.payment_status, r.description, r.remark, r.create_time
            FROM biz_member_consume_record r
            INNER JOIN biz_course_booking cb ON r.business_id = cb.id AND cb.del_flag = 0
            INNER JOIN biz_course c ON cb.course_id = c.id AND c.del_flag = 0
            WHERE r.business_type = 'COURSE' AND r.member_id = #{memberId}
              AND c.coach_id = #{coachId}
            ORDER BY r.create_time DESC LIMIT #{offset}, #{limit}
            """)
    List<CoachStudentConsumeRecordVO> findConsumeRecordsForCoach(@Param("coachId") Long coachId,
                                                                 @Param("memberId") Long memberId,
                                                                 @Param("offset") int offset,
                                                                 @Param("limit") int limit);

    @Select("""
            SELECT COUNT(*) FROM biz_member_consume_record r
            INNER JOIN biz_course_booking cb ON r.business_id = cb.id AND cb.del_flag = 0
            INNER JOIN biz_course c ON cb.course_id = c.id AND c.del_flag = 0
            WHERE r.business_type = 'COURSE' AND r.member_id = #{memberId}
              AND c.coach_id = #{coachId}
            """)
    int countConsumeRecordsForCoach(@Param("coachId") Long coachId,
                                    @Param("memberId") Long memberId);
}
