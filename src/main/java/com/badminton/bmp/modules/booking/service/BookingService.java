package com.badminton.bmp.modules.booking.service;

import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.entity.BookingCourt;
import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     * Agent 报价：按登录用户 ID 显式解析会员，复用既有校验与定价逻辑计算金额，
     * 但不落库。模型不得自行计算金额，金额一律以本方法返回值为准。
     *
     * @param loginUserId 登录用户 ID（来自 AgentContext，而非 username）
     * @param booking     预约请求（日期、时间、场地、模式等）
     * @return 已计算金额但未持久化的预约与明细
     */
    BookingQuotation quoteForAgent(Long loginUserId, Booking booking);

    /**
     * Agent 创建待支付预约：按登录用户 ID 显式解析会员，复用 {@link #add} 的校验与定价逻辑，
     * 仅生成待支付订单（status=1 / paymentStatus=0），不自动支付。
     *
     * @param loginUserId 登录用户 ID（来自 AgentContext，而非 username）
     * @param booking     预约请求
     * @return 已持久化的预约与明细（含预约 ID 与单号）
     */
    BookingQuotation createPendingForAgent(Long loginUserId, Booking booking);

    /**
     * Agent 报价/创建结果载体：已计算金额的预约及其逐场地明细。
     *
     * @param booking 预约（报价时未持久化；创建时已持久化，含 ID 与单号）
     * @param details 逐场地明细（含单价、行金额、时长）
     */
    record BookingQuotation(Booking booking, List<BookingCourt> details) {
    }

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
     * 定时任务：自动取消创建时间早于截止时间且仍未支付的预约订单
     */
    int autoCancelExpiredUnpaidOrders(LocalDateTime cutoff);

    /**
     * 运营待办数量（与 operation-todo 接口返回结构一致，用于 WebSocket 推送）
     */
    Map<String, Object> getOperationTodoCount();

    /**
     * 待办列表（待支付预约等，用于 WebSocket 推送）
     */
    List<Map<String, Object>> getTodoList();
}
