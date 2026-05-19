package com.badminton.bmp.modules.finance.service;

import com.badminton.bmp.modules.finance.entity.Finance;

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
     * 记录系统自动取消等补充审计操作。
     * 该类日志仅作为审计事件，不绑定具体财务记录ID/单号。
     */
    void logSystemAutoCancel(String businessType, Long businessId, String businessNo, Object beforeData, String summary);
}
