package com.badminton.bmp.modules.finance.service.impl;

import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.entity.FinanceAuditLog;
import com.badminton.bmp.modules.finance.mapper.FinanceAuditLogMapper;
import com.badminton.bmp.modules.finance.service.FinanceAuditService;
import com.badminton.bmp.modules.system.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 财务审计日志服务实现
 */
@Service
public class FinanceAuditServiceImpl implements FinanceAuditService {

    private static final Logger log = LoggerFactory.getLogger(FinanceAuditServiceImpl.class);

    @Autowired
    private FinanceAuditLogMapper auditLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 记录财务创建操作
     * 使用REQUIRES_NEW传播级别，确保审计日志独立提交
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void logCreate(Finance finance) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(finance.getId());
            auditLog.setFinanceNo(finance.getFinanceNo());
            auditLog.setOperationType("CREATE");
            auditLog.setBeforeData(null);
            auditLog.setAfterData(objectMapper.writeValueAsString(finance));
            auditLog.setChangeSummary("创建财务记录：" + finance.getFinanceNo());

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            // 审计日志失败不应影响主业务，记录日志即可
            log.error("记录财务审计日志失败: financeId={}, financeNo={}", finance.getId(), finance.getFinanceNo(), e);
        }
    }

    /**
     * 记录财务更新操作
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void logUpdate(Finance oldFinance, Finance newFinance) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(newFinance.getId());
            auditLog.setFinanceNo(newFinance.getFinanceNo());
            auditLog.setOperationType("UPDATE");
            auditLog.setBeforeData(objectMapper.writeValueAsString(oldFinance));
            auditLog.setAfterData(objectMapper.writeValueAsString(newFinance));
            auditLog.setChangeSummary(buildChangeDescription(oldFinance, newFinance));

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录财务审计日志失败: financeId={}, financeNo={}", newFinance.getId(), newFinance.getFinanceNo(), e);
        }
    }

    /**
     * 记录财务删除操作
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void logDelete(Finance finance) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(finance.getId());
            auditLog.setFinanceNo(finance.getFinanceNo());
            auditLog.setOperationType("DELETE");
            auditLog.setBeforeData(objectMapper.writeValueAsString(finance));
            auditLog.setAfterData(null);
            auditLog.setChangeSummary("删除财务记录：" + finance.getFinanceNo());

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录财务审计日志失败: financeId={}, financeNo={}", finance.getId(), finance.getFinanceNo(), e);
        }
    }

    /**
     * 设置操作人信息
     */
    private void setOperatorInfo(FinanceAuditLog auditLog) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            auditLog.setOperator(currentUser.getUsername());
            auditLog.setOperatorId(currentUser.getId());
        }
    }

    /**
     * 构建变更描述
     */
    private String buildChangeDescription(Finance oldFinance, Finance newFinance) {
        StringBuilder desc = new StringBuilder("修改财务记录：");

        if (!equals(oldFinance.getAmount(), newFinance.getAmount())) {
            desc.append("金额从").append(oldFinance.getAmount()).append("改为").append(newFinance.getAmount()).append("; ");
        }
        if (!equals(oldFinance.getBusinessType(), newFinance.getBusinessType())) {
            desc.append("业务类型从").append(oldFinance.getBusinessType()).append("改为").append(newFinance.getBusinessType()).append("; ");
        }
        if (!equals(oldFinance.getIncomeExpenseType(), newFinance.getIncomeExpenseType())) {
            String oldType = oldFinance.getIncomeExpenseType() == 1 ? "收入" : "支出";
            String newType = newFinance.getIncomeExpenseType() == 1 ? "收入" : "支出";
            desc.append("收支类型从").append(oldType).append("改为").append(newType).append("; ");
        }
        if (!equals(oldFinance.getRemark(), newFinance.getRemark())) {
            desc.append("备注已修改; ");
        }

        return desc.toString();
    }

    /**
     * 安全的对象比较
     */
    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == null || obj2 == null) return false;
        return obj1.equals(obj2);
    }
}
