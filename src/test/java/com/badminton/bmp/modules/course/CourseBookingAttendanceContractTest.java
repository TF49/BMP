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
        assertTrue(sql.contains("coalesce(attendance_status, 0) = #{expectedattendancestatus}"));
        assertTrue(sql.contains("actual_checkin_time"));
        assertTrue(sql.contains("actual_finish_time"));
    }

    @Test
    void automaticTransitionsAreConditionalAndNeverInferAbsence() throws Exception {
        Method start = CourseBookingMapper.class.getDeclaredMethod("startBookingIfPaid", Long.class);
        String startSql = String.join(" ", start.getAnnotation(Update.class).value()).toLowerCase(Locale.ROOT);
        assertTrue(startSql.contains("set status = 3"));
        assertTrue(startSql.contains("status = 2"));

        Method finish = CourseBookingMapper.class.getDeclaredMethod(
                "finishBookingAndAttendanceIfOngoing", Long.class, LocalDateTime.class);
        String finishSql = String.join(" ", finish.getAnnotation(Update.class).value()).toLowerCase(Locale.ROOT);
        assertTrue(finishSql.contains("set status = 4"));
        assertTrue(finishSql.contains("status = 3"));
        assertTrue(finishSql.contains("attendance_status = 1 then 2"));
        assertTrue(finishSql.contains("else coalesce(attendance_status, 0)"));
        assertTrue(!finishSql.contains("then 3"), "automatic finish must never infer absence");
        assertTrue(finishSql.indexOf("actual_finish_time") < finishSql.indexOf("attendance_status = case"),
                "finish time must be assigned before attendance status changes in MySQL");
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
