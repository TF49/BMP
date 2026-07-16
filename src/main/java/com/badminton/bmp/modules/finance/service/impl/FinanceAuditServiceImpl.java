package com.badminton.bmp.modules.finance.service.impl;

import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.common.util.WebUtils;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.entity.FinanceAuditLog;
import com.badminton.bmp.modules.finance.mapper.FinanceAuditLogMapper;
import com.badminton.bmp.modules.finance.service.FinanceAuditService;
import com.badminton.bmp.modules.system.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 财务审计日志服务实现
 */
@Service
public class FinanceAuditServiceImpl implements FinanceAuditService {

    private static final Logger log = LoggerFactory.getLogger(FinanceAuditServiceImpl.class);

    @Autowired
    private FinanceAuditLogMapper auditLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 审计日志失败计数器（用于监控）
    private static final java.util.concurrent.atomic.AtomicInteger auditLogFailureCount = 
        new java.util.concurrent.atomic.AtomicInteger(0);
    
    // 失败告警阈值
    private static final int FAILURE_ALERT_THRESHOLD = 10;

    /**
     * 记录财务创建操作
     * 使用默认传播级别，确保SecurityContext可用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logCreate(Finance finance) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(finance.getId());
            auditLog.setFinanceNo(finance.getFinanceNo());
            auditLog.setOperationType("CREATE");
            auditLog.setBeforeData(null);
            
            // 对敏感数据进行脱敏处理
            String afterData = objectMapper.writeValueAsString(finance);
            auditLog.setAfterData(com.badminton.bmp.common.util.SensitiveDataMasker.maskSensitiveFields(afterData));
            
            auditLog.setChangeSummary("创建财务记录：" + finance.getFinanceNo());

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
            
            // 插入成功，重置失败计数器
            auditLogFailureCount.set(0);
        } catch (Exception e) {
            // 审计日志失败不应影响主业务，记录日志并监控
            handleAuditLogFailure("CREATE", finance.getId(), finance.getFinanceNo(), e);
        }
    }

    /**
     * 记录财务更新操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logUpdate(Finance oldFinance, Finance newFinance) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(newFinance.getId());
            auditLog.setFinanceNo(newFinance.getFinanceNo());
            auditLog.setOperationType("UPDATE");
            
            // 对敏感数据进行脱敏处理
            String beforeData = objectMapper.writeValueAsString(oldFinance);
            String afterData = objectMapper.writeValueAsString(newFinance);
            auditLog.setBeforeData(com.badminton.bmp.common.util.SensitiveDataMasker.maskSensitiveFields(beforeData));
            auditLog.setAfterData(com.badminton.bmp.common.util.SensitiveDataMasker.maskSensitiveFields(afterData));
            
            auditLog.setChangeSummary(buildChangeDescription(oldFinance, newFinance));

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
            
            // 插入成功，重置失败计数器
            auditLogFailureCount.set(0);
        } catch (Exception e) {
            handleAuditLogFailure("UPDATE", newFinance.getId(), newFinance.getFinanceNo(), e);
        }
    }

    /**
     * 记录财务删除操作
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logDelete(Finance finance) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(finance.getId());
            auditLog.setFinanceNo(finance.getFinanceNo());
            auditLog.setOperationType("DELETE");
            
            // 对敏感数据进行脱敏处理
            String beforeData = objectMapper.writeValueAsString(finance);
            auditLog.setBeforeData(com.badminton.bmp.common.util.SensitiveDataMasker.maskSensitiveFields(beforeData));
            
            auditLog.setAfterData(null);
            auditLog.setChangeSummary("删除财务记录：" + finance.getFinanceNo());

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
            
            // 插入成功，重置失败计数器
            auditLogFailureCount.set(0);
        } catch (Exception e) {
            handleAuditLogFailure("DELETE", finance.getId(), finance.getFinanceNo(), e);
        }
    }

    /**
     * 处理审计日志失败（统一的失败处理和监控）
     */
    private void handleAuditLogFailure(String operationType, Long financeId, String financeNo, Exception e) {
        int count = auditLogFailureCount.incrementAndGet();
        
        // 记录详细错误日志
        log.error("记录财务审计日志失败[第{}次]: operationType={}, financeId={}, financeNo={}", 
                count, operationType, financeId, financeNo, e);
        
        // 达到阈值时发出告警
        if (count >= FAILURE_ALERT_THRESHOLD) {
            String alertMessage = String.format(
                "🔴 严重告警：财务审计日志已连续失败%d次！" +
                "最新失败: operationType=%s, financeId=%s, financeNo=%s, " +
                "错误: %s。请立即检查数据库连接和审计日志表状态！",
                count, operationType, financeId, financeNo, e.getMessage()
            );
            
            // 记录严重级别日志
            log.error(alertMessage);
            
            // TODO: 这里可以集成告警系统（如钉钉、邮件、短信等）
            // alertService.sendCriticalAlert("财务审计日志失败", alertMessage);
            
            // 超过阈值后每10次失败再次告警（避免告警风暴）
            if (count % 10 == 0) {
                log.error("⚠️ 财务审计日志持续失败，已累计{}次失败", count);
            }
        }
    }

    /**
     * 记录对账操作
     * 对账是重要的财务审计活动，需要记录执行人、时间和结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logReconciliation(String reconciliationType, Map<String, Object> reconciliationResult) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(null);
            auditLog.setFinanceNo(null);
            auditLog.setOperationType("RECONCILE");
            
            // 将对账结果序列化为JSON
            auditLog.setBeforeData(null);
            auditLog.setAfterData(objectMapper.writeValueAsString(reconciliationResult));
            
            // 构建对账摘要
            String summary = buildReconciliationSummary(reconciliationType, reconciliationResult);
            auditLog.setChangeSummary(summary);
            
            // 在备注中记录对账类型
            auditLog.setRemark("[RECONCILIATION] 类型:" + reconciliationType);

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录对账审计日志失败: reconciliationType={}", reconciliationType, e);
        }
    }

    /**
     * 构建对账摘要
     */
    private String buildReconciliationSummary(String reconciliationType, Map<String, Object> result) {
        StringBuilder summary = new StringBuilder();
        
        // 对账类型
        switch (reconciliationType) {
            case "FULL":
                summary.append("全面对账：检查所有业务模块");
                break;
            case "BOOKING":
                summary.append("场地预约对账");
                break;
            case "COURSE":
                summary.append("课程预约对账");
                break;
            case "EQUIPMENT":
                summary.append("器材租借对账");
                break;
            case "TOURNAMENT":
                summary.append("赛事报名对账");
                break;
            case "STRINGING":
                summary.append("穿线服务对账");
                break;
            case "RECHARGE":
                summary.append("会员充值对账");
                break;
            default:
                summary.append("对账操作：").append(reconciliationType);
        }
        
        // 对账结果
        Boolean passed = (Boolean) result.get("passed");
        if (passed != null) {
            summary.append(" - ");
            summary.append(passed ? "通过" : "不通过");
            
            // 如果不通过，添加差异信息
            if (!passed) {
                Object message = result.get("message");
                if (message != null) {
                    summary.append("，").append(message);
                }
            }
        }
        
        return summary.toString();
    }

    /**
     * 记录退款操作
     * 退款是财务的逆向操作，风险较高，需要单独审计追踪
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logRefund(Finance originalFinance, Finance refundFinance, String refundReason) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(refundFinance.getId());
            auditLog.setFinanceNo(refundFinance.getFinanceNo());
            auditLog.setOperationType("REFUND");
            
            // 记录原始收入和退款支出的关联信息（脱敏处理）
            String beforeData = originalFinance == null ? null : objectMapper.writeValueAsString(originalFinance);
            String afterData = objectMapper.writeValueAsString(refundFinance);
            
            auditLog.setBeforeData(beforeData == null ? null : 
                com.badminton.bmp.common.util.SensitiveDataMasker.maskSensitiveFields(beforeData));
            auditLog.setAfterData(com.badminton.bmp.common.util.SensitiveDataMasker.maskSensitiveFields(afterData));
            
            // 构建详细的退款摘要
            String summary = buildRefundSummary(originalFinance, refundFinance, refundReason);
            auditLog.setChangeSummary(summary);
            
            // 在备注中记录退款关键信息
            auditLog.setRemark(buildRefundRemark(originalFinance, refundFinance, refundReason));

            setOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());

            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录退款审计日志失败: refundFinanceId={}, refundFinanceNo={}", 
                    refundFinance.getId(), refundFinance.getFinanceNo(), e);
        }
    }

    /**
     * 构建退款摘要
     */
    private String buildRefundSummary(Finance originalFinance, Finance refundFinance, String refundReason) {
        StringBuilder summary = new StringBuilder("退款操作：");
        
        if (originalFinance != null) {
            summary.append("原单号 ").append(originalFinance.getFinanceNo())
                   .append("，原金额 ").append(originalFinance.getAmount()).append(" 元");
        }
        
        summary.append(" → 退款金额 ").append(refundFinance.getAmount()).append(" 元");
        
        if (refundReason != null && !refundReason.isEmpty()) {
            summary.append("，原因：").append(refundReason);
        }
        
        return summary.toString();
    }

    /**
     * 构建退款备注（结构化格式，便于后续解析）
     */
    private String buildRefundRemark(Finance originalFinance, Finance refundFinance, String refundReason) {
        StringBuilder remark = new StringBuilder("[REFUND]");
        
        if (originalFinance != null) {
            remark.append(" 原单号:").append(originalFinance.getFinanceNo());
            if (originalFinance.getBusinessId() != null) {
                remark.append(";原业务ID:").append(originalFinance.getBusinessId());
            }
        }
        
        remark.append(";退款单号:").append(refundFinance.getFinanceNo());
        remark.append(";退款金额:").append(refundFinance.getAmount());
        remark.append(";业务类型:").append(refundFinance.getBusinessType());
        
        if (refundFinance.getBusinessId() != null) {
            remark.append(";业务ID:").append(refundFinance.getBusinessId());
        }
        
        if (refundReason != null && !refundReason.isEmpty()) {
            remark.append(";退款原因:").append(refundReason);
        }
        
        return remark.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logSystemAutoCancel(String businessType, Long businessId, String businessNo, Object beforeData, String summary) {
        try {
            FinanceAuditLog auditLog = new FinanceAuditLog();
            auditLog.setFinanceId(null);
            auditLog.setFinanceNo(null);
            auditLog.setOperationType("SYSTEM_CANCEL");
            auditLog.setBeforeData(beforeData == null ? null : objectMapper.writeValueAsString(beforeData));
            auditLog.setAfterData(null);
            auditLog.setChangeSummary(summary);
            auditLog.setRemark(buildSystemAutoCancelRemark(businessType, businessId, businessNo));
            setSystemOperatorInfo(auditLog);
            auditLog.setOperationTime(LocalDateTime.now());
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("记录系统自动取消审计日志失败: businessType={}, businessId={}, businessNo={}", businessType, businessId, businessNo, e);
        }
    }

    /**
     * 设置普通操作的操作人信息
     */
    private void setOperatorInfo(FinanceAuditLog auditLog) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            auditLog.setOperator(currentUser.getUsername());
            auditLog.setOperatorId(currentUser.getId());
        } else {
            // 安全上下文缺失时设置默认值，避免 @NotBlank/@NotNull 导致插入失败
            auditLog.setOperator("UNKNOWN");
            auditLog.setOperatorId(0L);
            log.warn("审计日志记录时无法获取当前用户信息，使用默认值");
        }

        // 捕获IP地址和User-Agent（用于安全审计和追溯）
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(WebUtils.getClientIpAddress(request));
                auditLog.setUserAgent(WebUtils.getUserAgent(request));
            } else {
                auditLog.setIpAddress("UNKNOWN");
                auditLog.setUserAgent("UNKNOWN");
                log.debug("审计日志记录时无法获取HTTP请求上下文");
            }
        } catch (Exception e) {
            // IP和UA捕获失败不应影响审计日志记录
            auditLog.setIpAddress("ERROR");
            auditLog.setUserAgent("ERROR");
            log.warn("捕获IP地址或User-Agent失败", e);
        }
    }

    /**
     * 设置系统操作的操作人信息
     */
    private void setSystemOperatorInfo(FinanceAuditLog auditLog) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            auditLog.setOperator(currentUser.getUsername());
            auditLog.setOperatorId(currentUser.getId());
        } else {
            auditLog.setOperator("SYSTEM");
            auditLog.setOperatorId(0L);
        }

        // 系统操作也尝试捕获IP和UA（如果是通过API触发的定时任务）
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                auditLog.setIpAddress(WebUtils.getClientIpAddress(request));
                auditLog.setUserAgent(WebUtils.getUserAgent(request));
            } else {
                // 系统后台任务，无HTTP请求上下文
                auditLog.setIpAddress("SYSTEM_TASK");
                auditLog.setUserAgent("SYSTEM_TASK");
            }
        } catch (Exception e) {
            auditLog.setIpAddress("SYSTEM_TASK");
            auditLog.setUserAgent("SYSTEM_TASK");
        }
    }

    /**
     * 获取当前HTTP请求对象
     * 支持异步任务和非Web上下文场景
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildSystemAutoCancelRemark(String businessType, Long businessId, String businessNo) {
        StringBuilder remark = new StringBuilder("SYSTEM_AUTO_CANCEL");
        if (businessType != null && !businessType.isBlank()) {
            remark.append(":").append(businessType);
        }
        if (businessId != null) {
            remark.append(";businessId=").append(businessId);
        }
        if (businessNo != null && !businessNo.isBlank()) {
            remark.append(";businessNo=").append(businessNo);
        }
        return remark.toString();
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
