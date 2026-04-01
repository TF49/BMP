package com.badminton.bmp.modules.venue.cache;

import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.mapper.VenueMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 场馆实体缓存：按 ID 缓存场馆，供 Service 在权限校验前使用，避免重复查库。
 */
@Component
public class VenueEntityCache {

    @Autowired
    private VenueMapper venueMapper;

    @Cacheable(
            cacheNames = "venue",
            key = "#id",
            condition = "#id != null",
            unless = "#result == null"
    )
    public Venue getById(Long id) {
        if (id == null) return null;
        return venueMapper.findById(id);
    }
}
