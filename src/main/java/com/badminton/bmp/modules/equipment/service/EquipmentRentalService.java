package com.badminton.bmp.modules.equipment.service;

import com.badminton.bmp.modules.equipment.entity.EquipmentRental;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 器材租借业务接口
 */
public interface EquipmentRentalService {

    /**
     * 根据ID查找器材租借记录
     * @param id 租借ID
     * @return 租借对象
     */
    EquipmentRental findById(Long id);

    /**
     * 分页查询器材租借记录列表
     * @param memberId 会员ID（可选）
     * @param equipmentId 器材ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 租借记录列表
     */
    List<EquipmentRental> findAll(Long memberId, Long equipmentId, Integer status, String keyword, int page, int size);

    /**
     * 统计器材租借记录数量
     * @param memberId 会员ID（可选）
     * @param equipmentId 器材ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（可选）
     * @return 租借记录数量
     */
    int count(Long memberId, Long equipmentId, Integer status, String keyword);

    /**
     * 添加器材租借记录
     * @param rental 租借对象
     * @return 影响的行数
     */
    int add(EquipmentRental rental);

    /**
     * 更新器材租借记录
     * @param rental 租借对象
     * @return 影响的行数
     */
    int update(EquipmentRental rental);

    /**
     * 删除器材租借记录（逻辑删除）
     * @param id 租借ID
     * @return 影响的行数
     */
    int deleteById(Long id);

    /**
     * 更新器材租借状态
     * @param id 租借ID
     * @param status 新状态（0-已取消，1-租借中，2-已归还，3-逾期）
     * @return 影响的行数
     */
    int updateStatus(Long id, Integer status);

    /**
     * 获取器材租借统计信息
     * @return 统计信息Map，包含总租借数、各状态数量
     */
    Map<String, Object> getStatistics();

    /**
     * 生成租借单号
     * @return 租借单号
     */
    String generateRentalNo();

    /**
     * 根据日期范围查询租借记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 租借记录列表
     */
    List<EquipmentRental> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 处理支付（含余额扣减、消费记录）
     * @param rentalId 租借ID
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    int processPayment(Long rentalId, String paymentMethod);

    /**
     * 处理退款（含余额回滚、消费记录冲正）
     * @param rentalId 租借ID
     * @return 处理结果
     */
    int processRefund(Long rentalId);

    /**
     * 定时任务：将应还时间已过且仍为「租借中」的记录自动标记为「逾期」
     */
    void autoMarkOverdueRentals();
}
