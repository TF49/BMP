package com.badminton.bmp.modules.coach.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CoachStudentQueryRateLimiterTest {
    @Test
    void listAllowsFiveAndDetailAllowsTenPerSecond() {
        CoachStudentQueryRateLimiter limiter = new CoachStudentQueryRateLimiter(
                Clock.fixed(Instant.parse("2026-07-13T05:10:00Z"), ZoneOffset.UTC));

        for (int i = 0; i < 5; i++) assertTrue(limiter.tryAcquireList(9L));
        assertFalse(limiter.tryAcquireList(9L));

        for (int i = 0; i < 10; i++) assertTrue(limiter.tryAcquireDetail(9L, 100L));
        assertFalse(limiter.tryAcquireDetail(9L, 100L));
        assertFalse(limiter.tryAcquireDetail(9L, 101L));
        assertTrue(limiter.tryAcquireDetail(10L, 101L));
    }
}
