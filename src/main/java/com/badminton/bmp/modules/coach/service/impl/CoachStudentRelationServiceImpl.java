package com.badminton.bmp.modules.coach.service.impl;

import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CoachStudentRelationServiceImpl implements CoachStudentRelationService {
    private static final int RELATION_RETENTION_DAYS = 180;

    @Autowired
    private CoachStudentMapper coachStudentMapper;
    @Autowired
    private CacheManager cacheManager;
    private Clock clock = Clock.systemDefaultZone();

    @Override
    public boolean canCoachViewStudent(Long coachId, Long memberId) {
        if (coachId == null || memberId == null || coachId <= 0 || memberId <= 0) {
            return false;
        }
        String key = "coachStudentRelation::" + coachId + ':' + memberId;
        Cache cache = cacheManager.getCache("coachStudentRelation");
        Cache.ValueWrapper cached = cache == null ? null : cache.get(key);
        Object cachedValue = cached == null ? null : cached.get();
        long nowMillis = clock.millis();
        if (cachedValue instanceof Number number) {
            long cachedValidUntil = number.longValue();
            return cachedValidUntil > 0 && nowMillis <= cachedValidUntil;
        }
        if (cachedValue != null && cache != null) {
            cache.evict(key);
        }

        LocalDateTime validUntil = coachStudentMapper.findCoachStudentRelationValidUntil(
                coachId, memberId, RELATION_RETENTION_DAYS);
        boolean allowed = validUntil != null && !LocalDateTime.now(clock).isAfter(validUntil);
        long value = allowed
                ? validUntil.atZone(clock.getZone()).toInstant().toEpochMilli()
                : 0L;
        if (cache != null) cache.put(key, value);
        return allowed;
    }
}
