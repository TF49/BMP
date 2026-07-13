package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

class CoachStudentMapperContractTest {

    @Test
    void relationSqlEnforcesDeletionCancellationValidityAnd180DayBoundary() {
        Method method = Arrays.stream(CoachStudentMapper.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("existsValidCoachStudentRelation"))
                .findFirst().orElseThrow();
        String sql = String.join(" ", method.getAnnotation(Select.class).value()).toLowerCase(Locale.ROOT);

        assertTrue(sql.contains("cb.del_flag = 0"));
        assertTrue(sql.contains("c.del_flag = 0"));
        assertTrue(sql.contains("m.del_flag = 0"));
        assertTrue(sql.contains("c.status != 0"));
        assertTrue(sql.contains("cb.status in (2, 3, 4)"));
        assertTrue(sql.contains("attendance_status"));
        assertTrue(sql.contains("interval #{retentiondays} day"));
    }

    @Test
    void consumeSqlIsRestrictedToCourseBookingAndCurrentCoach() {
        Method method = Arrays.stream(CoachStudentMapper.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("findConsumeRecordsForCoach"))
                .findFirst().orElseThrow();
        String sql = String.join(" ", method.getAnnotation(Select.class).value()).toLowerCase(Locale.ROOT);

        assertTrue(sql.contains("r.business_type = 'course'"));
        assertTrue(sql.contains("r.business_id = cb.id"));
        assertTrue(sql.contains("c.coach_id = #{coachid}"));
        assertTrue(sql.contains("r.member_id = #{memberid}"));
        assertTrue(sql.contains("r.amount"));
    }

    @Test
    void listAggregationJoinsOnlyCurrentCoachBookings() {
        Method method = Arrays.stream(CoachStudentMapper.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("findStudents"))
                .findFirst().orElseThrow();
        String sql = String.join(" ", method.getAnnotation(Select.class).value()).toLowerCase(Locale.ROOT);

        assertTrue(sql.contains("inner join biz_course c on cb.course_id = c.id"));
        assertTrue(sql.contains("c.coach_id = #{coachid}"));
        assertTrue(sql.contains("left join (biz_course_booking cb"));
    }
}
