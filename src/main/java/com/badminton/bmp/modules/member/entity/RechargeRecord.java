package com.badminton.bmp.modules.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeRecord {
    private Long id;
    private String rechargeNo;

    @NotNull
    @Positive
    private Long memberId;

    private Long userId;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("99999.99")
    private BigDecimal rechargeAmount;

    @NotBlank
    @Pattern(regexp = "CASH|ALIPAY|WECHAT|BANK|BALANCE", message = "充值方式必须为 CASH、ALIPAY、WECHAT、BANK 或 BALANCE")
    private String rechargeMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rechargeTime;

    @NotBlank
    @Pattern(regexp = "USER|ADMIN")
    private String operatorType;

    private Long operatorId;

    @Size(max = 500)
    private String remark;

    @Min(0) @Max(1)
    private Integer isUpgraded;

    /** 是否等级升级：本笔充值是否使会员等级（Lv1~Lv5）提升，0-否，1-是 */
    @Min(0) @Max(1)
    private Integer isLevelUpgraded;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Long venueId;
}
