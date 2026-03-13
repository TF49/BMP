package com.badminton.bmp.config;

import com.badminton.bmp.common.util.LoginRateLimiter;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.course.service.CourseBookingService;
import com.badminton.bmp.modules.course.service.CourseService;
import com.badminton.bmp.modules.equipment.service.EquipmentRentalService;
import com.badminton.bmp.modules.tournament.service.TournamentRegistrationService;
import com.badminton.bmp.modules.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
     * 定时任务配置
     * 定期清理登录限流器中的过期记录；将超过预约时间段的进行中预约自动改为已完成并重算场地状态；
     * 课程/赛事状态按时间自动推进；课程预约按课程时间自动推进状态；器材租借超应还时间自动标为逾期。
     */
@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseBookingService courseBookingService;

    @Autowired
    private EquipmentRentalService equipmentRentalService;

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TournamentRegistrationService tournamentRegistrationService;

    /**
     * 每5分钟清理一次过期的登录记录
     * 防止内存泄漏
     */
    @Scheduled(fixedRate = 300000)  // 300000毫秒 = 5分钟
    public void cleanupExpiredLoginRecords() {
        loginRateLimiter.cleanupExpiredRecords();
    }

    /**
     * 每分钟将已过结束时间的“进行中”预约改为已完成，并重算涉及场地的状态
     */
    @Scheduled(fixedRate = 60000)  // 60000毫秒 = 1分钟
    public void completeOverdueBookings() {
        try {
            bookingService.completeOverdueInProgressBookings();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleConfig.class).warn("定时完成超时预约失败: {}", e.getMessage());
        }
    }

    /**
     * 每分钟按当前时间自动更新课程状态：报名中→进行中（到开始时间）、进行中→已结束（到结束时间）
     */
    @Scheduled(fixedRate = 60000)  // 1分钟
    public void autoUpdateCourseStatus() {
        try {
            courseService.autoUpdateCourseStatusByTime();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleConfig.class).warn("课程状态自动更新失败: {}", e.getMessage());
        }
    }

    /**
     * 每分钟按课程开始/结束时间自动更新课程预约状态：已支付→进行中、进行中→已完成
     */
    @Scheduled(fixedRate = 60000)  // 1分钟
    public void autoUpdateCourseBookingStatus() {
        try {
            courseBookingService.autoUpdateCourseBookingStatusByTime();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleConfig.class).warn("课程预约状态自动更新失败: {}", e.getMessage());
        }
    }

    /**
     * 每分钟按比赛开始/结束时间自动更新赛事状态：报名中→进行中（到开始时间）、进行中→已结束（到结束时间）
     */
    @Scheduled(fixedRate = 60000)  // 1分钟
    public void autoUpdateTournamentStatus() {
        try {
            tournamentService.autoUpdateTournamentStatusByTime();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleConfig.class).warn("赛事状态自动更新失败: {}", e.getMessage());
        }
    }

    /**
     * 每分钟按赛事开始时间自动更新赛事报名状态：已支付→已参赛（关联赛事已到开始时间）
     */
    @Scheduled(fixedRate = 60000)  // 1分钟
    public void autoUpdateTournamentRegistrationStatus() {
        try {
            tournamentRegistrationService.autoUpdateTournamentRegistrationStatusByTime();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleConfig.class).warn("赛事报名状态自动更新失败: {}", e.getMessage());
        }
    }

    /**
     * 每分钟将应还时间已过且仍为租借中的器材租借自动标记为逾期
     */
    @Scheduled(fixedRate = 60000)  // 1分钟
    public void autoMarkOverdueRentals() {
        try {
            equipmentRentalService.autoMarkOverdueRentals();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(ScheduleConfig.class).warn("器材租借逾期自动标记失败: {}", e.getMessage());
        }
    }
}
