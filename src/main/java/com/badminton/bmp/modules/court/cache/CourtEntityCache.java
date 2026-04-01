package com.badminton.bmp.modules.court.cache;

import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 场地实体缓存：按 ID 缓存场地，供 Service 在权限校验前使用。
 */
@Component
public class CourtEntityCache {

    @Autowired
    private CourtMapper courtMapper;

    @Cacheable(
            cacheNames = "court",
            key = "#id",
            condition = "#id != null",
            unless = "#result == null"
    )
    public Court getById(Long id) {
        if (id == null) return null;
        return courtMapper.findById(id);
    }
}
