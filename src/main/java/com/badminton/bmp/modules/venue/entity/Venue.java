package com.badminton.bmp.modules.venue.entity;

import com.badminton.bmp.common.validation.BusinessHoursValid;
import com.badminton.bmp.common.validation.StatusValid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 场馆实体类
 * 对应数据库表 sys_venue
 */
@Data
public class Venue {
    /**
     * 场馆ID（主键，自增）
     */
    private Long id;

    /**
     * 场馆名称（必填）
     */
    @NotBlank(message = "场馆名称不能为空")
    @Size(max = 100, message = "场馆名称长度不能超过100个字符")
    private String venueName;

    /**
     * 地址
     */
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    /**
     * 联系电话
     */
    @Pattern(
            regexp = "(^1[3-9]\\d{9}$)|(^0\\d{2,3}-?\\d{7,8}$)",
            message = "联系电话格式不正确，应为手机号或固定电话"
    )
    private String contactPhone;

    /**
     * 联系人
     */
    @Size(max = 50, message = "联系人姓名长度不能超过50个字符")
    private String contactPerson;

    /**
     * 营业时间（如：06:00-24:00）
     */
    @BusinessHoursValid
    private String businessHours;

    /**
     * 场馆描述
     */
    @Size(max = 500, message = "场馆描述长度不能超过500个字符")
    private String description;

    /**
     * 场馆图片URL
     */
    @Size(max = 255, message = "图片URL长度不能超过255个字符")
    private String venueImage;

    /**
     * 状态（0-关闭，1-营业中，2-暂停）
     */
    @StatusValid(allowedValues = {0, 1, 2}, message = "状态值必须为0(关闭)、1(营业中)或2(暂停)")
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
