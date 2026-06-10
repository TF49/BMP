package com.badminton.bmp.config;

import com.badminton.bmp.common.util.LoginRateLimiter;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.course.service.CourseBookingService;
import com.badminton.bmp.modules.course.service.CourseService;
import com.badminton.bmp.modules.equipment.service.EquipmentRentalService;
import com.badminton.bmp.modules.finance.mapper.FinanceAuditLogMapper;
import com.badminton.bmp.modules.finance.service.ExportTaskService;
import com.badminton.bmp.modules.stringing.service.StringingServiceService;
import com.badminton.bmp.modules.tournament.service.TournamentRegistrationService;
import com.badminton.bmp.modules.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;

/**
     * 定时任务配置
     * 定期清理登录限流器中的过期记录；将超过预约时间段的进行中预约自动改为已完成并重算场地状态；
     * 课程/赛事状态按时间自动推进；课程预约按课程时间自动推进状态；器材租借超应还时间自动标为逾期。
     */
@Configuration
@EnableScheduling
public class ScheduleConfig {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduleConfig.class);
    private volatile boolean redisLockAvailable = true;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @Autowired
    private PaymentAutoCancelProperties paymentAutoCancelProperties;

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

    @Autowired
    private StringingServiceService stringingServiceService;

    @Autowired
    private ExportTaskService exportTaskService;

    @Autowired
    private FinanceAuditLogMapper financeAuditLogMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private static final String AUTO_CANCEL_LOCK_KEY = "bmp:payment:auto-cancel:lock";

    /**
     * 每5分钟清理一次过期的登录记录
     * 防止内存泄漏
     */
    @Scheduled(fixedRate = 300000)  // 300000毫秒 = 5分钟
    public void cleanupExpiredLoginRecords() {
        loginRateLimiter.cleanupExpiredRecords();
    }

    /**
     * 每30分钟清理一次过期的导出任务（内存 + 物理文件）
     */
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredExportTasks() {
        try {
            exportTaskService.cleanExpiredTasks();
        } catch (Exception e) {
            log.warn("清理过期导出任务失败: {}", e.getMessage());
        }
    }

    /**
     * 每天凌晨2点清理超过180天的审计日志数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldAuditLogs() {
        try {
            String beforeDate = LocalDateTime.now().minusDays(180)
                    .toLocalDate().toString();
            int deleted = financeAuditLogMapper.deleteBeforeDate(beforeDate);
            if (deleted > 0) {
                log.info("清理180天前审计日志 {} 条", deleted);
            }
        } catch (Exception e) {
            log.warn("清理过期审计日志失败: {}", e.getMessage());
        }
    }

    /**
     * 按配置频率自动取消超时未支付业务订单
     */
    @Scheduled(fixedRateString = "#{@paymentAutoCancelProperties.scanIntervalMs}")
    public void autoCancelExpiredUnpaidOrders() {
        if (!paymentAutoCancelProperties.isEnabled()) {
            return;
        }
        String lockToken = tryAcquireAutoCancelLock();
        if (lockToken == null) {
            return;
        }
        long startedAt = System.currentTimeMillis();
        int totalCancelled = 0;
        LocalDateTime cutoff = LocalDateTime.now().minus(paymentAutoCancelProperties.getTimeoutDuration());
        try {
            totalCancelled += runAutoCancel("场地预约", () -> bookingService.autoCancelExpiredUnpaidOrders(cutoff));
            totalCancelled += runAutoCancel("课程预约", () -> courseBookingService.autoCancelExpiredUnpaidOrders(cutoff));
            totalCancelled += runAutoCancel("器材租借", () -> equipmentRentalService.autoCancelExpiredUnpaidOrders(cutoff));
            totalCancelled += runAutoCancel("赛事报名", () -> tournamentRegistrationService.autoCancelExpiredUnpaidOrders(cutoff));
            totalCancelled += runAutoCancel("穿线服务", () -> stringingServiceService.autoCancelExpiredUnpaidOrders(cutoff));
            long durationMs = System.currentTimeMillis() - startedAt;
            if (totalCancelled > 0) {
                log.info("支付自动取消扫描完成，共取消 {} 笔，耗时 {} ms", totalCancelled, durationMs);
            } else {
                log.debug("支付自动取消扫描完成，无需取消，耗时 {} ms", durationMs);
            }
        } finally {
            releaseAutoCancelLock(lockToken);
        }
    }

    /**
     * 每分钟将已过结束时间的“进行中”预约改为已完成，并重算涉及场地的状态
     */
    @Scheduled(fixedRate = 60000)  // 60000毫秒 = 1分钟
    public void completeOverdueBookings() {
        try {
            bookingService.completeOverdueInProgressBookings();
        } catch (Exception e) {
            log.warn("定时完成超时预约失败: {}", e.getMessage());
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
            log.warn("课程状态自动更新失败: {}", e.getMessage());
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
            log.warn("课程预约状态自动更新失败: {}", e.getMessage());
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
            log.warn("赛事状态自动更新失败: {}", e.getMessage());
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
            log.warn("赛事报名状态自动更新失败: {}", e.getMessage());
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
            log.warn("器材租借逾期自动标记失败: {}", e.getMessage());
        }
    }

    private int runAutoCancel(String moduleName, IntSupplier supplier) {
        try {
            long startedAt = System.currentTimeMillis();
            int cancelled = supplier.getAsInt();
            long durationMs = System.currentTimeMillis() - startedAt;
            if (cancelled > 0) {
                log.info("{}自动取消超时未支付订单 {} 笔，耗时 {} ms", moduleName, cancelled, durationMs);
            }
            return cancelled;
        } catch (Exception e) {
            log.warn("{}自动取消超时未支付订单失败", moduleName, e);
            return 0;
        }
    }

    private String tryAcquireAutoCancelLock() {
        if (stringRedisTemplate == null || !redisLockAvailable) {
            return UUID.randomUUID().toString();
        }
        String token = UUID.randomUUID().toString();
        try {
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(
                    AUTO_CANCEL_LOCK_KEY,
                    token,
                    Math.max(paymentAutoCancelProperties.getScanIntervalMs() * 2, 120000L),
                    TimeUnit.MILLISECONDS
            );
            if (Boolean.TRUE.equals(locked)) {
                return token;
            }
        } catch (Exception e) {
            redisLockAvailable = false;
            log.warn("Redis 不可用，支付自动取消任务切换为单实例兜底模式；如需启用分布式锁，请启动 Redis 后重启应用。");
            return UUID.randomUUID().toString();
        }
        return null;
    }

    private void releaseAutoCancelLock(String token) {
        if (token == null || stringRedisTemplate == null || !redisLockAvailable) {
            return;
        }
        try {
            String currentToken = stringRedisTemplate.opsForValue().get(AUTO_CANCEL_LOCK_KEY);
            if (token.equals(currentToken)) {
                stringRedisTemplate.delete(AUTO_CANCEL_LOCK_KEY);
            }
        } catch (Exception e) {
            redisLockAvailable = false;
            log.warn("Redis 不可用，停止尝试释放支付自动取消分布式锁；当前实例继续按单机模式运行。");
        }
    }
}
