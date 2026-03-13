package com.badminton.bmp.modules.coach.cache;

import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 教练实体缓存：按 ID 缓存教练，供 Service 在权限校验前使用。
 */
@Component
public class CoachEntityCache {

    @Autowired
    private CoachMapper coachMapper;

    @Cacheable(cacheNames = "coach", key = "#id", unless = "#result == null")
    public Coach getById(Long id) {
        if (id == null) return null;
        return coachMapper.findById(id);
    }
}
