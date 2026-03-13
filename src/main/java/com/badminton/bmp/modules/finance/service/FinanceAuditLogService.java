package com.badminton.bmp.modules.finance.service;

import com.badminton.bmp.modules.finance.dto.FinanceAuditLogExportDTO;
import com.badminton.bmp.modules.finance.entity.FinanceAuditLog;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 财务审计日志服务接口
 */
public interface FinanceAuditLogService {

    /**
     * 分页查询审计日志
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param financeId 财务记录ID
     * @param operationType 操作类型
     * @param operator 操作人
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<FinanceAuditLog> pageQuery(Integer pageNum, Integer pageSize, Long financeId,
                                     String financeNo, String operationType, String operator,
                                     LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID查询审计日志
     *
     * @param id 审计日志ID
     * @return 审计日志
     */
    FinanceAuditLog getById(Long id);

    /**
     * 查询导出数据（不分页，查询全部符合条件的数据）
     *
     * @param financeId 财务记录ID
     * @param operationType 操作类型
     * @param operator 操作人
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导出数据列表
     */
    List<FinanceAuditLogExportDTO> queryForExport(Long financeId, String financeNo,
                                                    String operationType, String operator,
                                                    LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 导出Excel文件
     *
     * @param financeId 财务记录ID
     * @param operationType 操作类型
     * @param operator 操作人
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 文件路径
     */
    String exportToExcel(Long financeId, String financeNo, String operationType, String operator,
                         LocalDateTime startTime, LocalDateTime endTime);
}
