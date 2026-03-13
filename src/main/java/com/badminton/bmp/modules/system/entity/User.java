package com.badminton.bmp.modules.system.entity;

import com.badminton.bmp.common.validation.IdCardValid;
import com.badminton.bmp.common.validation.PhoneValid;
import com.badminton.bmp.common.validation.StatusValid;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于系统认证和用户管理
 */
@Data
public class User {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6个字符")
    private String password;

    @PhoneValid
    private String phone;

    @IdCardValid
    private String idCard;

    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "性别必须为MALE、FEMALE或OTHER")
    private String gender;

    @Size(max = 200, message = "个性签名长度不能超过200个字符")
    private String signature;

    /** 角色：PRESIDENT-协会会长，ADMIN-管理员，VENUE_MANAGER-场馆管理者，COACH-教练；USER-普通用户（新注册），MEMBER-会员（充值达标后由USER升级，与USER同属用户端角色，权限一致） */
    @NotNull(message = "角色不能为空")
    @Pattern(regexp = "^(PRESIDENT|ADMIN|VENUE_MANAGER|COACH|MEMBER|USER)$", message = "角色必须为PRESIDENT、ADMIN、VENUE_MANAGER、COACH、MEMBER或USER")
    private String role;

    @StatusValid(allowedValues = {0, 1}, message = "状态值必须为0(禁用)或1(启用)")
    private Integer status;

    private Long venueId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;
}
