package com.badminton.bmp.modules.coach.support;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class CoachStudentQueryRateLimiter {
    private final Clock clock;
    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();

    public CoachStudentQueryRateLimiter() {
        this(Clock.systemUTC());
    }

    public CoachStudentQueryRateLimiter(Clock clock) {
        this.clock = clock;
    }

    public boolean tryAcquireList(Long coachId) {
        return acquire("list:" + coachId, 5);
    }

    public boolean tryAcquireDetail(Long coachId, Long memberId) {
        return acquire("detail:" + coachId + ':' + memberId, 10);
    }

    private boolean acquire(String key, int limit) {
        long epochSecond = clock.instant().getEpochSecond();
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || current.epochSecond != epochSecond) {
                return new Window(epochSecond, 1);
            }
            return new Window(epochSecond, current.count + 1);
        });
        return window.count <= limit;
    }

    private record Window(long epochSecond, int count) {
    }
}
