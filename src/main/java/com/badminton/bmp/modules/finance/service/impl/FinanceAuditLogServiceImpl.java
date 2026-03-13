package com.badminton.bmp.modules.finance.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.badminton.bmp.modules.finance.dto.FinanceAuditLogExportDTO;
import com.badminton.bmp.modules.finance.entity.FinanceAuditLog;
import com.badminton.bmp.modules.finance.mapper.FinanceAuditLogMapper;
import com.badminton.bmp.modules.finance.service.FinanceAuditLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 财务审计日志服务实现类
 */
@Service
public class FinanceAuditLogServiceImpl implements FinanceAuditLogService {

    @Autowired
    private FinanceAuditLogMapper financeAuditLogMapper;

    @Value("${bmp.upload-dir:./uploads}")
    private String uploadDir;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Page<FinanceAuditLog> pageQuery(Integer pageNum, Integer pageSize, Long financeId,
                                           String financeNo, String operationType, String operator,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        Page<FinanceAuditLog> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<FinanceAuditLog> queryWrapper = new LambdaQueryWrapper<>();

        if (financeId != null) {
            queryWrapper.eq(FinanceAuditLog::getFinanceId, financeId);
        }
        if (financeNo != null && !financeNo.trim().isEmpty()) {
            queryWrapper.like(FinanceAuditLog::getFinanceNo, financeNo.trim());
        }
        if (operationType != null && !operationType.trim().isEmpty()) {
            queryWrapper.eq(FinanceAuditLog::getOperationType, operationType);
        }
        if (operator != null && !operator.trim().isEmpty()) {
            queryWrapper.like(FinanceAuditLog::getOperator, operator);
        }
        if (startTime != null) {
            queryWrapper.ge(FinanceAuditLog::getOperationTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(FinanceAuditLog::getOperationTime, endTime);
        }

        queryWrapper.orderByDesc(FinanceAuditLog::getOperationTime);

        return financeAuditLogMapper.selectPage(page, queryWrapper);
    }

    @Override
    public FinanceAuditLog getById(Long id) {
        return financeAuditLogMapper.selectById(id);
    }

    @Override
    public List<FinanceAuditLogExportDTO> queryForExport(Long financeId, String financeNo,
                                                          String operationType, String operator,
                                                          LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<FinanceAuditLog> queryWrapper = new LambdaQueryWrapper<>();

        if (financeId != null) {
            queryWrapper.eq(FinanceAuditLog::getFinanceId, financeId);
        }
        if (financeNo != null && !financeNo.trim().isEmpty()) {
            queryWrapper.like(FinanceAuditLog::getFinanceNo, financeNo.trim());
        }
        if (operationType != null && !operationType.trim().isEmpty()) {
            queryWrapper.eq(FinanceAuditLog::getOperationType, operationType);
        }
        if (operator != null && !operator.trim().isEmpty()) {
            queryWrapper.like(FinanceAuditLog::getOperator, operator);
        }
        if (startTime != null) {
            queryWrapper.ge(FinanceAuditLog::getOperationTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(FinanceAuditLog::getOperationTime, endTime);
        }

        queryWrapper.orderByDesc(FinanceAuditLog::getOperationTime);

        List<FinanceAuditLog> logs = financeAuditLogMapper.selectList(queryWrapper);

        return logs.stream().map(this::convertToExportDTO).collect(Collectors.toList());
    }

    /** 导出条数上限，防止数据量过大导致 OOM 或超时 */
    private static final int EXPORT_MAX_ROWS = 20000;

    @Override
    public String exportToExcel(Long financeId, String financeNo, String operationType, String operator,
                               LocalDateTime startTime, LocalDateTime endTime) {
        List<FinanceAuditLogExportDTO> data = queryForExport(financeId, financeNo, operationType, operator, startTime, endTime);
        if (data.isEmpty()) {
            throw new IllegalStateException("没有符合条件的数据，无需导出");
        }
        if (data.size() > EXPORT_MAX_ROWS) {
            throw new IllegalStateException("导出数据超过 " + EXPORT_MAX_ROWS + " 条，请缩小筛选条件（如时间范围）后重试");
        }

        // 创建导出目录
        String exportDir = uploadDir + File.separator + "export" + File.separator + "audit-log";
        File dir = new File(exportDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String fileName = "audit-log-" + System.currentTimeMillis() + ".xlsx";
        String filePath = exportDir + File.separator + fileName;

        // 写入Excel
        EasyExcel.write(filePath, FinanceAuditLogExportDTO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("财务审计日志")
                .doWrite(data);

        return filePath;
    }

    /**
     * 转换为导出DTO
     */
    private FinanceAuditLogExportDTO convertToExportDTO(FinanceAuditLog log) {
        FinanceAuditLogExportDTO dto = new FinanceAuditLogExportDTO();
        dto.setId(log.getId());
        dto.setFinanceNo(log.getFinanceNo());
        dto.setOperationType(log.getOperationType()); // 会在getter中转换为中文
        dto.setOperator(log.getOperator());
        dto.setOperationTime(log.getOperationTime() != null 
                ? log.getOperationTime().format(DATE_TIME_FORMATTER) 
                : "");
        dto.setChangeSummary(log.getChangeSummary());
        dto.setIpAddress(log.getIpAddress());
        dto.setRemark(log.getRemark());
        return dto;
    }
}
