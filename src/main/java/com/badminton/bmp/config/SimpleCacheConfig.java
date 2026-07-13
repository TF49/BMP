package com.badminton.bmp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.support.SimpleCacheManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 当没有任何 CacheManager（例如未启用 Redis）时，提供内存缓存并显式注册 coach、venue、court、course、user，
 * 避免 @Cacheable(cacheNames = "coach") 报错：Cannot find cache named 'coach'。
 */
@Configuration
@ConditionalOnMissingBean(CacheManager.class)
public class SimpleCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        List<org.springframework.cache.Cache> caches = new ArrayList<>();
        for (String name : new String[]{"coach", "venue", "venueOptions", "court", "course", "user"}) {
            caches.add(caffeineCache(name, Duration.ofMinutes(30)));
        }
        for (String name : new String[]{"coachStudentRelation", "coachStudentProgress", "coachStudentAttendance"}) {
            caches.add(caffeineCache(name, Duration.ofMinutes(5)));
        }
        caches.add(caffeineCache("coachStudentLastSuccess", Duration.ofMinutes(15)));
        manager.setCaches(caches);
        return manager;
    }

    private CaffeineCache caffeineCache(String name, Duration ttl) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(10_000)
                .build());
    }
}
