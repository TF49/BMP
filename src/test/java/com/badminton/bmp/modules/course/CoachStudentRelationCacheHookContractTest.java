package com.badminton.bmp.modules.course;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CoachStudentRelationCacheHookContractTest {

    @Test
    void writePathsExposeCallableRelationCacheInvalidationHooks() throws IOException {
        assertContains("src/main/java/com/badminton/bmp/modules/course/service/impl/CourseBookingServiceImpl.java",
                "invalidateForBooking");
        assertContains("src/main/java/com/badminton/bmp/modules/course/service/impl/CourseServiceImpl.java",
                "invalidateForCourse");
        assertContains("src/main/java/com/badminton/bmp/modules/coach/service/impl/CoachServiceImpl.java",
                "invalidateForCoach");
        assertContains("src/main/java/com/badminton/bmp/modules/member/service/impl/MemberServiceImpl.java",
                "invalidateForMember");
        assertTrue(Files.exists(Path.of(
                "src/main/java/com/badminton/bmp/modules/course/cache/CoachStudentRelationCacheInvalidator.java")));
    }

    @Test
    void implementationPlanContainsFrozenP0Override() throws IOException {
        String document = Files.readString(
                Path.of("教练学员查看功能-实施计划.md"), StandardCharsets.UTF_8);
        assertTrue(document.contains("P0 冻结覆盖说明"));
        assertTrue(document.contains("首版 P1—P5 不实现 WebSocket"));
        assertTrue(document.contains("不得自动把未签到推断为缺席"));
        assertTrue(document.contains("Controller 不得捕获 AccessDeniedException"));
    }

    private static void assertContains(String file, String expected) throws IOException {
        String source = Files.readString(Path.of(file), StandardCharsets.UTF_8);
        assertTrue(source.contains(expected), file + " must call " + expected);
    }
}
