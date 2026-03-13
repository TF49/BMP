package com.badminton.bmp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 当没有任何 CacheManager（例如未启用 Redis）时，提供内存缓存并显式注册 coach、venue、court、course、user，
 * 避免 @Cacheable(cacheNames = "coach") 报错：Cannot find cache named 'coach'。
 */
@Configuration
@ConditionalOnMissingBean(CacheManager.class)
public class SimpleCacheConfig {

    private static final String[] CACHE_NAMES = {"coach", "venue", "court", "course", "user"};

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CACHE_NAMES);
    }
}
