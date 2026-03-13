package com.badminton.bmp.modules.member.entity;

import com.badminton.bmp.common.validation.IdCardValid;
import com.badminton.bmp.common.validation.PhoneValid;
import com.badminton.bmp.common.validation.StatusValid;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员实体类
 * 对应数据库表 sys_member
 */
@Data
public class Member {
    /**
     * 会员ID（主键，自增）
     */
    private Long id;

    /**
     * 用户ID（外键关联sys_user，可为空）
     */
    private Long userId;

    /**
     * 姓名（必填）
     */
    @NotBlank(message = "会员姓名不能为空")
    @Size(max = 50, message = "会员姓名长度不能超过50个字符")
    private String memberName;

    /**
     * 性别（0-女，1-男）
     */
    @StatusValid(allowedValues = {0, 1}, message = "性别值必须为0(女)或1(男)")
    private Integer gender;

    /**
     * 电话
     */
    @PhoneValid
    private String phone;

    /**
     * 身份证号
     */
    @IdCardValid
    private String idCard;

    /**
     * 会员类型（NORMAL-普通用户，MEMBER-会员）
     */
    @NotNull(message = "会员类型不能为空")
    @Pattern(regexp = "^(NORMAL|MEMBER)$", message = "会员类型必须为NORMAL或MEMBER")
    private String memberType;

    /**
     * 会员等级（1-5级，用于会员等级划分）
     */
    @Min(value = 1, message = "会员等级最小为1")
    @Max(value = 5, message = "会员等级最大为5")
    private Integer memberLevel;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registerTime;

    /**
     * 到期时间（仅会员MEMBER需要，普通用户NORMAL可为空）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 状态（0-冻结，1-正常，2-到期）
     */
    @StatusValid(allowedValues = {0, 1, 2}, message = "状态值必须为0(冻结)、1(正常)或2(到期)")
    private Integer status;

    /**
     * 累计消费金额
     */
    @DecimalMin(value = "0", message = "累计消费金额不能为负数")
    private BigDecimal totalConsumption;

    /**
     * 账户余额
     */
    @DecimalMin(value = "0", message = "账户余额不能为负数")
    private BigDecimal balance;

    /**
     * 累计充值金额
     */
    @DecimalMin(value = "0", message = "累计充值金额不能为负数")
    private BigDecimal totalRecharge;

    /**
     * 版本号（乐观锁，用于并发控制）
     */
    private Integer version;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0-正常，1-删除）
     */
    private Integer delFlag;
}
