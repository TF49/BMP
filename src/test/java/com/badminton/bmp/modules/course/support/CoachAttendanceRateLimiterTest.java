package com.badminton.bmp.modules.course.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CoachAttendanceRateLimiterTest {

    @Test
    void allowsFiveCommandsPerCoachPerSecondAndRejectsTheSixth() {
        CoachAttendanceRateLimiter limiter = new CoachAttendanceRateLimiter(
                Clock.fixed(Instant.parse("2026-07-13T02:30:00Z"), ZoneOffset.UTC));

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.tryAcquire(9L));
        }
        assertFalse(limiter.tryAcquire(9L));
        assertTrue(limiter.tryAcquire(10L), "limit is isolated by coach id");
    }
}
