package com.badminton.bmp.modules.finance.mapper;

import com.badminton.bmp.modules.finance.entity.FinanceAuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

/**
 * 财务审计日志Mapper接口
 */
@Mapper
public interface FinanceAuditLogMapper extends BaseMapper<FinanceAuditLog> {

    /**
     * 根据操作时间删除旧的审计日志（用于定期清理）
     */
    @Delete("DELETE FROM biz_finance_audit_log WHERE operation_time < #{beforeDate}")
    int deleteBeforeDate(@Param("beforeDate") String beforeDate);
}
