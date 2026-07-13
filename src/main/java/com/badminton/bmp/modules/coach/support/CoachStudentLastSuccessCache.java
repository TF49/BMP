package com.badminton.bmp.modules.coach.support;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CoachStudentLastSuccessCache {
    private final Cache cache;

    public CoachStudentLastSuccessCache(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("coachStudentLastSuccess");
    }

    public void remember(String key, Object value) {
        if (cache != null && value != null) cache.put(key, value);
    }

    public Object get(String key) {
        return cache == null ? null : cache.get(key, Object.class);
    }
}
