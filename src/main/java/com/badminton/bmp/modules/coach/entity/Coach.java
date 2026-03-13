package com.badminton.bmp.modules.coach.entity;

import com.badminton.bmp.common.validation.IdCardValid;
import com.badminton.bmp.common.validation.PhoneValid;
import com.badminton.bmp.common.validation.StatusValid;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 教练实体类
 * 对应数据库表 sys_coach
 */
@Data
public class Coach {
    /**
     * 教练ID（主键，自增）
     */
    private Long id;

    /**
     * 姓名
     */
    @NotBlank(message = "教练姓名不能为空")
    @Size(max = 50, message = "教练姓名长度不能超过50个字符")
    private String coachName;

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
     * 专业特长
     */
    @Size(max = 200, message = "专业特长长度不能超过200个字符")
    private String specialty;

    /**
     * 教学经验
     */
    @Size(max = 500, message = "教学经验长度不能超过500个字符")
    private String experience;

    /**
     * 课时费（每小时）
     */
    @DecimalMin(value = "0", message = "课时费不能为负数")
    private BigDecimal hourlyPrice;

    /**
     * 评分（0-5分）
     */
    @DecimalMin(value = "0", message = "评分不能小于0")
    @DecimalMax(value = "5", message = "评分不能大于5")
    private BigDecimal rating;

    /**
     * 累计学员数
     */
    @Min(value = 0, message = "累计学员数不能为负数")
    private Integer totalStudents;

    /**
     * 状态（0-停用，1-正常）
     */
    @StatusValid(allowedValues = {0, 1}, message = "状态值必须为0(停用)或1(正常)")
    private Integer status;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

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

    /**
     * 所属场馆ID（可为空）
     */
    private Long venueId;

    /**
     * 用户ID（外键关联 sys_user，COACH 角色绑定；一对一，用于教练端登录身份）
     */
    private Long userId;

    // ========== 关联字段（非数据库字段，用于联表查询） ==========

    /**
     * 所属场馆名称（联表查询时填充）
     */
    private String venueName;

    /**
     * 关联账号用户名（联表 sys_user 时填充，用于列表展示）
     */
    private String boundUsername;
}
