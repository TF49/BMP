package com.badminton.bmp.modules.course.service;

import com.badminton.bmp.modules.course.entity.CourseBooking;
import java.util.List;
import java.util.Map;

public interface CourseBookingService {
    CourseBooking findById(Long id);
    List<CourseBooking> findAll(Long memberId, Long courseId, Integer status, String keyword, int page, int size);
    int count(Long memberId, Long courseId, Integer status, String keyword);

    /** 教练端：当前教练所教课程的预约列表 */
    List<CourseBooking> findAllForCoach(Long coachId, Long courseId, Integer status, String keyword, int page, int size);
    int countForCoach(Long coachId, Long courseId, Integer status, String keyword);
    CourseBooking findByIdForCoach(Long coachId, Long id);
    int updateStatusForCoach(Long coachId, Long id, Integer status, String remark);
    int add(CourseBooking booking);
    int update(CourseBooking booking);
    int deleteById(Long id);
    int updateStatus(Long id, Integer status);
    Map<String, Object> getStatistics();
    String generateBookingNo();

    /**
     * 处理支付（含余额扣减、消费记录）
     * @param bookingId 预约ID
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    int processPayment(Long bookingId, String paymentMethod);

    /**
     * 普通用户支付本人课程预约
     * @param bookingId 预约ID
     * @param paymentMethod 支付方式
     * @param userId 当前登录用户ID
     * @return 处理结果
     */
    int processMemberPayment(Long bookingId, String paymentMethod, Long userId);

    /**
     * 处理退款（含余额回滚、消费记录冲正）
     * @param bookingId 预约ID
     * @return 处理结果
     */
    int processRefund(Long bookingId);

    /**
     * 定时任务：按课程开始/结束时间自动更新预约状态
     * 已支付→进行中（课程已到开始时间）、进行中→已完成（课程已到结束时间）
     */
    void autoUpdateCourseBookingStatusByTime();
}
