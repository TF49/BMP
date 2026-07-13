package com.badminton.bmp.modules.course.support;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class CoachAttendanceRateLimiter {
    private static final int MAX_COMMANDS_PER_SECOND = 5;

    private final Clock clock;
    private final ConcurrentMap<Long, Window> windows = new ConcurrentHashMap<>();

    public CoachAttendanceRateLimiter() {
        this(Clock.systemUTC());
    }

    public CoachAttendanceRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(Long coachId) {
        if (coachId == null) {
            return false;
        }
        long epochSecond = clock.instant().getEpochSecond();
        Window window = windows.compute(coachId, (ignored, current) -> {
            if (current == null || current.epochSecond != epochSecond) {
                return new Window(epochSecond, 1);
            }
            return new Window(epochSecond, current.count + 1);
        });
        return window.count <= MAX_COMMANDS_PER_SECOND;
    }

    private record Window(long epochSecond, int count) {
    }
}
