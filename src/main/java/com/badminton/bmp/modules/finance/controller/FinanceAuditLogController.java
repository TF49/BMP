package com.badminton.bmp.modules.finance.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.modules.finance.entity.FinanceAuditLog;
import com.badminton.bmp.modules.finance.service.ExportTaskService;
import com.badminton.bmp.modules.finance.service.FinanceAuditExportAsyncService;
import com.badminton.bmp.modules.finance.service.FinanceAuditLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "财务审计日志", description = "审计日志分页查询、导出")
@RestController
@RequestMapping("/api/finance/audit")
public class FinanceAuditLogController {

    @Autowired
    private FinanceAuditLogService financeAuditLogService;

    @Autowired
    private ExportTaskService exportTaskService;

    @Autowired
    private FinanceAuditExportAsyncService financeAuditExportAsyncService;

    @Operation(summary = "分页查询审计日志")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Page<FinanceAuditLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long financeId,
            @RequestParam(required = false) String financeNo,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        Page<FinanceAuditLog> page = financeAuditLogService.pageQuery(
                pageNum, pageSize, financeId, financeNo, operationType, operator, startTime, endTime
        );
        return Result.success(page);
    }

    @Operation(summary = "按财务记录ID查询审计日志")
    @GetMapping("/finance/{financeId}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Page<FinanceAuditLog>> getByFinanceId(
            @PathVariable Long financeId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<FinanceAuditLog> page = financeAuditLogService.pageQuery(
                pageNum, pageSize, financeId, null, null, null, null, null
        );
        return Result.success(page);
    }

    @Operation(summary = "按操作人查询审计日志")
    @GetMapping("/operator/{operator}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Page<FinanceAuditLog>> getByOperator(
            @PathVariable String operator,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<FinanceAuditLog> page = financeAuditLogService.pageQuery(
                pageNum, pageSize, null, null, null, operator, null, null
        );
        return Result.success(page);
    }

    @Operation(summary = "按操作类型查询审计日志")
    @GetMapping("/type/{operationType}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Page<FinanceAuditLog>> getByOperationType(
            @PathVariable String operationType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        Page<FinanceAuditLog> page = financeAuditLogService.pageQuery(
                pageNum, pageSize, null, null, operationType, null, null, null
        );
        return Result.success(page);
    }

    @Operation(summary = "审计日志详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<FinanceAuditLog> getById(@PathVariable Long id) {
        FinanceAuditLog log = financeAuditLogService.getById(id);
        if (log == null) {
            return Result.error("审计日志不存在");
        }
        return Result.success(log);
    }

    @Operation(summary = "创建审计日志导出任务")
    @PostMapping("/export/create")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, String>> createExportTask(
            @RequestParam(required = false) Long financeId,
            @RequestParam(required = false) String financeNo,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        String taskId = exportTaskService.createTask();
        exportTaskService.updateTaskStatus(taskId, ExportTaskService.TaskStatus.PROCESSING);

        financeAuditExportAsyncService.executeExportAsync(taskId, financeId, financeNo, operationType, operator, startTime, endTime);

        Map<String, String> result = new HashMap<>();
        result.put("taskId", taskId);
        return Result.success(result);
    }

    @Operation(summary = "查询导出任务状态")
    @GetMapping("/export/status/{taskId}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, Object>> getExportTaskStatus(@PathVariable String taskId) {
        ExportTaskService.TaskInfo taskInfo = exportTaskService.getTask(taskId);
        if (taskInfo == null) {
            return Result.error("任务不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskInfo.getTaskId());
        result.put("status", taskInfo.getStatus().name());
        result.put("errorMessage", taskInfo.getErrorMessage());
        result.put("createTime", taskInfo.getCreateTime());
        result.put("completeTime", taskInfo.getCompleteTime());
        return Result.success(result);
    }

    @Operation(summary = "下载导出文件")
    @GetMapping("/export/download/{taskId}")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public void downloadExportFile(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ExportTaskService.TaskInfo taskInfo = exportTaskService.getTask(taskId);
        if (taskInfo == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("任务不存在");
            return;
        }

        if (taskInfo.getStatus() != ExportTaskService.TaskStatus.COMPLETED) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("任务未完成，当前状态: " + taskInfo.getStatus().name());
            return;
        }

        String filePath = taskInfo.getFilePath();
        if (filePath == null || !new File(filePath).exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("文件不存在");
            return;
        }

        // 设置响应头
        File file = new File(filePath);
        String fileName = file.getName();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        response.setContentLengthLong(file.length());

        // 写入文件流
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
    }
}
