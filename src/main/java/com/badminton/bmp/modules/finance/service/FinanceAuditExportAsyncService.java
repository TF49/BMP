package com.badminton.bmp.modules.finance.service;

import java.time.LocalDateTime;

/**
 * 财务审计日志导出异步服务
 * 用于通过代理调用使 @Async 生效
 */
public interface FinanceAuditExportAsyncService {

    /**
     * 异步执行导出任务
     */
    void executeExportAsync(String taskId, Long financeId, String financeNo, String operationType,
                            String operator, LocalDateTime startTime, LocalDateTime endTime);
}
