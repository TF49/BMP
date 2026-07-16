package com.badminton.bmp.modules.finance.service;

import com.badminton.bmp.modules.finance.entity.Finance;

import java.util.Map;

/**
 * 财务审计服务接口
 */
public interface FinanceAuditService {

    /**
     * 记录财务创建操作
     * @param finance 财务记录
     */
    void logCreate(Finance finance);

    /**
     * 记录财务更新操作
     * @param oldFinance 更新前的财务记录
     * @param newFinance 更新后的财务记录
     */
    void logUpdate(Finance oldFinance, Finance newFinance);

    /**
     * 记录财务删除操作
     * @param finance 被删除的财务记录
     */
    void logDelete(Finance finance);

    /**
     * 记录退款操作（重要：退款是财务逆向操作，需单独审计）
     * @param originalFinance 原始财务记录（收入）
     * @param refundFinance 退款财务记录（支出）
     * @param refundReason 退款原因
     */
    void logRefund(Finance originalFinance, Finance refundFinance, String refundReason);

    /**
     * 记录对账操作
     * @param reconciliationType 对账类型（FULL-全面对账，BOOKING-场地预约对账等）
     * @param reconciliationResult 对账结果（包含是否通过、差异信息等）
     */
    void logReconciliation(String reconciliationType, Map<String, Object> reconciliationResult);

    /**
     * 记录系统自动取消等补充审计操作。
     * 该类日志仅作为审计事件，不绑定具体财务记录ID/单号。
     */
    void logSystemAutoCancel(String businessType, Long businessId, String businessNo, Object beforeData, String summary);
}
