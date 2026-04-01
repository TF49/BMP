package com.badminton.bmp.modules.course.cache;

import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 课程实体缓存：按 ID 缓存课程，供 Service 在权限校验前使用。
 */
@Component
public class CourseEntityCache {

    @Autowired
    private CourseMapper courseMapper;

    @Cacheable(
            cacheNames = "course",
            key = "#id",
            condition = "#id != null",
            unless = "#result == null"
    )
    public Course getById(Long id) {
        if (id == null) return null;
        return courseMapper.findById(id);
    }
}
