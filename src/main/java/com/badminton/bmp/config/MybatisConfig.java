package com.badminton.bmp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * 扫描Mapper接口
 */
@Configuration
@MapperScan(basePackages = {
    "com.badminton.bmp.modules.system.mapper",
    "com.badminton.bmp.modules.badminton.mapper",
    "com.badminton.bmp.modules.venue.mapper",
    "com.badminton.bmp.modules.member.mapper",
    "com.badminton.bmp.modules.court.mapper",
    "com.badminton.bmp.modules.equipment.mapper",
    "com.badminton.bmp.modules.coach.mapper",
    "com.badminton.bmp.modules.course.mapper",
    "com.badminton.bmp.modules.tournament.mapper",
    "com.badminton.bmp.modules.booking.mapper",
    "com.badminton.bmp.modules.finance.mapper",
    "com.badminton.bmp.modules.stringing.mapper",
    "com.badminton.bmp.modules.notification.mapper"
})
public class MybatisConfig {
}
