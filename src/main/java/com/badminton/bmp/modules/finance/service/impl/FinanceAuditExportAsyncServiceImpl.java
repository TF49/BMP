package com.badminton.bmp.modules.finance.service.impl;

import com.badminton.bmp.modules.finance.service.ExportTaskService;
import com.badminton.bmp.modules.finance.service.FinanceAuditExportAsyncService;
import com.badminton.bmp.modules.finance.service.FinanceAuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 财务审计日志导出异步服务实现
 */
@Service
public class FinanceAuditExportAsyncServiceImpl implements FinanceAuditExportAsyncService {

    @Autowired
    private FinanceAuditLogService financeAuditLogService;

    @Autowired
    private ExportTaskService exportTaskService;

    @Override
    @Async("taskExecutor")
    public void executeExportAsync(String taskId, Long financeId, String financeNo, String operationType,
                                   String operator, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String filePath = financeAuditLogService.exportToExcel(
                    financeId, financeNo, operationType, operator, startTime, endTime
            );
            exportTaskService.setTaskFilePath(taskId, filePath);
            exportTaskService.updateTaskStatus(taskId, ExportTaskService.TaskStatus.COMPLETED);
        } catch (Exception e) {
            exportTaskService.setTaskError(taskId, "导出失败，请稍后重试");
        }
    }
}
