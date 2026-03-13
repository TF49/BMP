package com.badminton.bmp.modules.finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_finance_audit_log")
public class FinanceAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long financeId;

    @Size(max = 50)
    private String financeNo;

    @NotBlank
    @Pattern(regexp = "CREATE|UPDATE|DELETE|RECONCILE")
    private String operationType;

    @NotBlank
    @Size(max = 50)
    private String operator;

    @NotNull
    @Positive
    private Long operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime operationTime;

    private String beforeData;
    private String afterData;

    @Size(max = 200)
    private String changeSummary;

    @Size(max = 50)
    private String ipAddress;

    @Size(max = 500)
    private String userAgent;

    @Size(max = 500)
    private String remark;
}
