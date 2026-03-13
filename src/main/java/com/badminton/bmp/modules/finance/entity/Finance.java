package com.badminton.bmp.modules.finance.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Finance {
    private Long id;
    private String financeNo;

    @NotBlank
    @Pattern(regexp = "BOOKING|COURSE|EQUIPMENT|TOURNAMENT|STRINGING|RECHARGE|OTHER")
    private String businessType;

    private Long businessId;

    @NotNull
    @Min(0) @Max(1)
    private Integer incomeExpenseType;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999999.99")
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "CASH|ALIPAY|WECHAT|BALANCE")
    private String paymentMethod;

    private Long venueId;

    @Size(max = 50)
    private String operator;

    private Long operatorId;

    @Size(max = 500)
    private String remark;

    @Pattern(regexp = "AUTO|MANUAL")
    private String recordSource;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Size(max = 50)
    private String lastModifier;

    private Long lastModifierId;
    private Integer delFlag;

    @Min(0) @Max(1)
    private Integer isReconciled;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reconcileTime;

    private Long reconcileUserId;

    @Size(max = 500)
    private String reconcileRemark;

    private String venueName;
    private String businessNo;

    public static final String TYPE_BOOKING = "BOOKING";
    public static final String TYPE_COURSE = "COURSE";
    public static final String TYPE_EQUIPMENT = "EQUIPMENT";
    public static final String TYPE_TOURNAMENT = "TOURNAMENT";
    public static final String TYPE_STRINGING = "STRINGING";
    public static final String TYPE_RECHARGE = "RECHARGE";
    public static final String TYPE_OTHER = "OTHER";

    public static final int EXPENSE = 0;
    public static final int INCOME = 1;

    public static final String SOURCE_AUTO = "AUTO";
    public static final String SOURCE_MANUAL = "MANUAL";
}
