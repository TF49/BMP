package com.badminton.bmp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置：默认 TTL、key 前缀、JSON 序列化。
 * 仅当 RedisConnectionFactory 存在时生效（即已配置 spring.data.redis.*）。
 * 若未配置 Redis，可注释 application.properties 中的 spring.cache.type 与 redis 配置，使用内存缓存。
 */
@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class RedisCacheConfig {

    public static final String CACHE_KEY_PREFIX = "bmp:";
    public static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith(CACHE_KEY_PREFIX)
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 按 cacheName 设置不同 TTL（可选）
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("venue", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("court", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("coach", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("course", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
