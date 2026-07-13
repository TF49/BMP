package com.badminton.bmp.modules.course;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.mapper.CourseBookingMapper;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

class CourseBookingAttendanceContractTest {

    @Test
    void entityExposesAttendanceFields() throws Exception {
        assertNotNull(CourseBooking.class.getDeclaredField("attendanceStatus"));
        assertTrue(CourseBooking.class.getDeclaredField("actualCheckinTime").getType().equals(LocalDateTime.class));
        assertTrue(CourseBooking.class.getDeclaredField("actualFinishTime").getType().equals(LocalDateTime.class));
    }

    @Test
    void coreCoachQueriesReturnCourseStatusAndAttendanceColumns() throws Exception {
        assertSelectContains("findById", "cb.*", "course_status");
        assertSelectContains("findByIdAndCoachId", "cb.*", "course_status");
        assertSelectContains("findAllByCoachId", "cb.*", "course_status");
    }

    @Test
    void attendanceUpdateUsesBothOldBookingAndAttendanceStateConditions() throws Exception {
        Method method = Arrays.stream(CourseBookingMapper.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("updateAttendanceWithExpectedState"))
                .findFirst()
                .orElseThrow();
        String sql = String.join(" ", method.getAnnotation(Update.class).value()).toLowerCase(Locale.ROOT);

        assertTrue(sql.contains("status = #{expectedbookingstatus}"));
        assertTrue(sql.contains("coalesce(cb.attendance_status, 0) = #{expectedattendancestatus}"));
        assertTrue(sql.contains("actual_checkin_time"));
        assertTrue(sql.contains("actual_finish_time"));
        assertTrue(sql.contains("inner join biz_course c"));
        assertTrue(sql.contains("c.coach_id = #{coachid}"));
        assertTrue(sql.contains("c.del_flag = 0"));
        assertTrue(sql.contains("c.status != 0"));
        assertTrue(sql.contains("#{operatedat}"));
        assertTrue(sql.contains("interval 24 hour"));
    }

    @Test
    void automaticTransitionsAreConditionalAndNeverInferAbsence() throws Exception {
        Method start = CourseBookingMapper.class.getDeclaredMethod(
                "startBookingIfPaid", Long.class, LocalDateTime.class);
        String startSql = String.join(" ", start.getAnnotation(Update.class).value()).toLowerCase(Locale.ROOT);
        assertTrue(startSql.contains("set cb.status = 3"));
        assertTrue(startSql.contains("cb.status = 2"));

        Method finish = CourseBookingMapper.class.getDeclaredMethod(
                "finishBookingAndAttendanceIfOngoing", Long.class, LocalDateTime.class);
        String finishSql = String.join(" ", finish.getAnnotation(Update.class).value()).toLowerCase(Locale.ROOT);
        assertTrue(finishSql.contains("set cb.status = 4"));
        assertTrue(finishSql.contains("cb.status = 3"));
        assertTrue(finishSql.contains("cb.attendance_status = 1 then 2"));
        assertTrue(finishSql.contains("else coalesce(cb.attendance_status, 0)"));
        assertTrue(!finishSql.contains("then 3"), "automatic finish must never infer absence");
        assertTrue(finishSql.indexOf("actual_finish_time") < finishSql.indexOf("attendance_status = case"),
                "finish time must be assigned before attendance status changes in MySQL");
    }

    @Test
    void automaticTransitionSelectionAndWritesRejectCancelledOrDeletedCourses() throws Exception {
        assertSelectContains("findBookingIdsToStart", "c.del_flag = 0", "c.status != 0");
        assertSelectContains("findBookingIdsToFinish", "c.del_flag = 0", "c.status != 0");

        Method start = Arrays.stream(CourseBookingMapper.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("startBookingIfPaid"))
                .findFirst().orElseThrow();
        String startSql = String.join(" ", start.getAnnotation(Update.class).value()).toLowerCase(Locale.ROOT);
        assertTrue(startSql.contains("inner join biz_course c"));
        assertTrue(startSql.contains("c.del_flag = 0"));
        assertTrue(startSql.contains("c.status != 0"));

        Method finish = Arrays.stream(CourseBookingMapper.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals("finishBookingAndAttendanceIfOngoing"))
                .findFirst().orElseThrow();
        String finishSql = String.join(" ", finish.getAnnotation(Update.class).value()).toLowerCase(Locale.ROOT);
        assertTrue(finishSql.contains("inner join biz_course c"));
        assertTrue(finishSql.contains("c.del_flag = 0"));
        assertTrue(finishSql.contains("c.status != 0"));
    }

    private static void assertSelectContains(String methodName, String... fragments) throws Exception {
        Method method = Arrays.stream(CourseBookingMapper.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        String sql = String.join(" ", method.getAnnotation(Select.class).value()).toLowerCase(Locale.ROOT);
        for (String fragment : fragments) {
            assertTrue(sql.contains(fragment.toLowerCase(Locale.ROOT)), methodName + " must select " + fragment);
        }
    }
}
