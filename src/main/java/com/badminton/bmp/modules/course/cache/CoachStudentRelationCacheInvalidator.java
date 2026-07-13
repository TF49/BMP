package com.badminton.bmp.modules.course.cache;

import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CoachStudentRelationCacheInvalidator {
    private static final List<String> COACH_STUDENT_CACHES = List.of(
            "coachStudentRelation", "coachStudentProgress", "coachStudentAttendance");

    private final CacheManager cacheManager;

    public CoachStudentRelationCacheInvalidator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void invalidateForBooking(Long memberId) {
        clearCoachStudentCaches();
    }

    public void invalidateForCourse(Long courseId) {
        clearCoachStudentCaches();
    }

    public void invalidateForCoach(Long coachId) {
        clearCoachStudentCaches();
    }

    public void invalidateForMember(Long memberId) {
        clearCoachStudentCaches();
    }

    private void clearCoachStudentCaches() {
        for (String cacheName : COACH_STUDENT_CACHES) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }
}
