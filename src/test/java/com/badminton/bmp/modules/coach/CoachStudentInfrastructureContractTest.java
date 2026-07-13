package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badminton.bmp.common.exception.RateLimitException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

class CoachStudentInfrastructureContractTest {
    @Test
    void localCacheUsesCaffeineAndCoachStudentCachesExpireInFiveMinutes() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"), StandardCharsets.UTF_8);
        String redis = Files.readString(Path.of(
                "src/main/java/com/badminton/bmp/config/RedisCacheConfig.java"), StandardCharsets.UTF_8);
        String local = Files.readString(Path.of(
                "src/main/java/com/badminton/bmp/config/SimpleCacheConfig.java"), StandardCharsets.UTF_8);

        assertTrue(pom.contains("com.github.ben-manes.caffeine"));
        assertTrue(redis.contains("coachStudentRelation"));
        assertTrue(redis.contains("Duration.ofMinutes(5)"));
        assertTrue(local.contains("CaffeineCache"));
        assertTrue(local.contains("coachStudentAttendance"));
        assertTrue(local.contains("Duration.ofMinutes(5)"));
    }

    @Test
    void rateLimitExceptionMapsToHttp429() {
        ResponseStatus status = RateLimitException.class.getAnnotation(ResponseStatus.class);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, status.value());
    }
}
