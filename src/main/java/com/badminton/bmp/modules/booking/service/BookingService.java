package com.badminton.bmp.modules.booking.service;

import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 场地预约服务接口
 */
public interface BookingService {

    /**
     * 根据ID查询预约记录
     */
    Booking findById(Long id);

    /**
     * 分页查询预约记录（支持条件筛选）
     */
    List<Booking> findAll(Long venueId,
                          Long memberId,
                          Long courtId,
                          Integer status,
                          String memberKeyword,
                          LocalDate startDate,
                          LocalDate endDate,
                          int page,
                          int size);

    /**
     * 统计预约记录数量（支持条件筛选）
     */
    int count(Long venueId,
              Long memberId,
              Long courtId,
              Integer status,
              String memberKeyword,
              LocalDate startDate,
              LocalDate endDate);

    /**
     * 添加预约记录（含冲突检测、金额计算、场地状态更新）
     */
    int add(Booking booking);

    /**
     * 更新预约记录（含冲突检测、场地状态更新）
     */
    int update(Booking booking);

    /**
     * 删除预约记录（含场地状态恢复）
     */
    int deleteById(Long id);

    /**
     * 更新预约状态（含场地状态同步）
     */
    int updateStatus(Long id, Integer status);

    /**
     * 获取统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 统计指定场地在某日期、时间段内的预约数量（排除已取消）
     */
    int countBookingsForTimeRange(Long courtId,
                                  LocalDate bookingDate,
                                  LocalTime startTime,
                                  LocalTime endTime);

    /**
     * 统计指定场地在某日期、时间段内的拼场预约数量（不含包场）
     */
    int countSharedBookingsForTimeRange(Long courtId,
                                        LocalDate bookingDate,
                                        LocalTime startTime,
                                        LocalTime endTime);

    /**
     * 生成预约单号（格式：BK+日期+序号，如BK20250118001）
     */
    String generateBookingNo();

    /**
     * 处理支付（含余额扣减、消费记录）
     * @param bookingId 预约ID
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    int processPayment(Long bookingId, String paymentMethod);

    /**
     * 普通用户支付本人预约（仅允许支付本人订单）
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
     * 获取指定场地当前时刻的占用情况（用于普通用户端“当前使用情况”）
     * 返回脱敏后的姓名、时间段及状态信息
     */
    List<CourtBookingUserDTO> getCurrentOccupancy(Long courtId);

    /**
     * 获取指定场地在给定日期和时间段内的占用情况（用于普通用户端“所选时段使用情况”）
     * 返回脱敏后的姓名、时间段及状态信息
     */
    List<CourtBookingUserDTO> getRangeOccupancy(Long courtId,
                                                LocalDate bookingDate,
                                                LocalTime startTime,
                                                LocalTime endTime);

    /**
     * 将已过结束时间的“进行中”预约批量改为已完成，并重算涉及场地的状态（定时任务调用）
     */
    void completeOverdueInProgressBookings();

    /**
     * 运营待办数量（与 operation-todo 接口返回结构一致，用于 WebSocket 推送）
     */
    Map<String, Object> getOperationTodoCount();

    /**
     * 待办列表（待支付预约等，用于 WebSocket 推送）
     */
    List<Map<String, Object>> getTodoList();
}
