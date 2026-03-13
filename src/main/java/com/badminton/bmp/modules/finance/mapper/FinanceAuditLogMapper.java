package com.badminton.bmp.modules.finance.mapper;

import com.badminton.bmp.modules.finance.entity.FinanceAuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 财务审计日志Mapper接口
 */
@Mapper
public interface FinanceAuditLogMapper extends BaseMapper<FinanceAuditLog> {

    /**
     * 根据ID查询审计日志
     */
    @Select("SELECT * FROM biz_finance_audit_log WHERE id = #{id}")
    FinanceAuditLog findById(@Param("id") Long id);

    /**
     * 根据财务记录ID查询审计日志列表
     */
    @Select("SELECT * FROM biz_finance_audit_log WHERE finance_id = #{financeId} ORDER BY operation_time DESC")
    List<FinanceAuditLog> findByFinanceId(@Param("financeId") Long financeId);

    /**
     * 根据财务单号查询审计日志列表
     */
    @Select("SELECT * FROM biz_finance_audit_log WHERE finance_no = #{financeNo} ORDER BY operation_time DESC")
    List<FinanceAuditLog> findByFinanceNo(@Param("financeNo") String financeNo);

    /**
     * 分页查询审计日志
     */
    @Select("<script>" +
            "SELECT * FROM biz_finance_audit_log " +
            "WHERE 1=1 " +
            "<if test='financeId != null'> AND finance_id = #{financeId} </if>" +
            "<if test='operationType != null and operationType != \"\"'> AND operation_type = #{operationType} </if>" +
            "<if test='operatorId != null'> AND operator_id = #{operatorId} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(operation_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(operation_time) &lt;= #{endDate} </if>" +
            "ORDER BY operation_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<FinanceAuditLog> findAll(@Param("financeId") Long financeId,
                                   @Param("operationType") String operationType,
                                   @Param("operatorId") Long operatorId,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    /**
     * 统计审计日志数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM biz_finance_audit_log " +
            "WHERE 1=1 " +
            "<if test='financeId != null'> AND finance_id = #{financeId} </if>" +
            "<if test='operationType != null and operationType != \"\"'> AND operation_type = #{operationType} </if>" +
            "<if test='operatorId != null'> AND operator_id = #{operatorId} </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND DATE(operation_time) &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND DATE(operation_time) &lt;= #{endDate} </if>" +
            "</script>")
    int count(@Param("financeId") Long financeId,
              @Param("operationType") String operationType,
              @Param("operatorId") Long operatorId,
              @Param("startDate") String startDate,
              @Param("endDate") String endDate);

    /**
     * 根据财务ID删除审计日志（物理删除，谨慎使用）
     */
    @Delete("DELETE FROM biz_finance_audit_log WHERE finance_id = #{financeId}")
    int deleteByFinanceId(@Param("financeId") Long financeId);

    /**
     * 根据操作时间删除旧的审计日志（用于定期清理）
     */
    @Delete("DELETE FROM biz_finance_audit_log WHERE operation_time < #{beforeDate}")
    int deleteBeforeDate(@Param("beforeDate") String beforeDate);
}
