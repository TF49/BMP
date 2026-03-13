package com.badminton.bmp.modules.finance.service;

import com.badminton.bmp.modules.finance.entity.Finance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 财务Service接口
 */
public interface FinanceService {

    /**
     * 根据ID查询财务记录
     */
    Finance findById(Long id);

    /**
     * 分页查询财务记录（带数据范围过滤）
     */
    List<Finance> findAll(Long venueId, String businessType, Integer incomeExpenseType,
                          String startDate, String endDate, String keyword, int page, int size);

    /**
     * 统计总数
     */
    int count(Long venueId, String businessType, Integer incomeExpenseType,
              String startDate, String endDate, String keyword);

    /**
     * 新增财务记录
     */
    int add(Finance finance);

    /**
     * 更新财务记录
     */
    int update(Finance finance);

    /**
     * 删除财务记录
     */
    int deleteById(Long id);

    /**
     * 生成财务单号
     */
    String generateFinanceNo();

    /**
     * 从业务自动创建财务记录
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param amount 金额
     * @param incomeExpenseType 收支类型（0-支出，1-收入）
     * @param paymentMethod 支付方式
     * @param venueId 场馆ID
     * @param remark 备注
     */
    void createFromBusiness(String businessType, Long businessId, BigDecimal amount,
                            Integer incomeExpenseType, String paymentMethod, Long venueId, String remark);

    /**
     * 获取统计概览数据
     * @param venueId 场馆ID（可选，用于场馆级过滤）
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    Map<String, Object> getStatistics(Long venueId, String startDate, String endDate);

    /**
     * 获取趋势数据（用于折线图）
     */
    Map<String, Object> getTrendData(Long venueId, String startDate, String endDate);

    /**
     * 获取业务占比数据（用于饼图）
     */
    List<Map<String, Object>> getBusinessTypeRatio(Long venueId, String startDate, String endDate);
}
