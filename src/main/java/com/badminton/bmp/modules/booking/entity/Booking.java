package com.badminton.bmp.modules.booking.entity;

import com.badminton.bmp.common.validation.StatusValid;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 场地预约实体类
 * 对应数据库表 biz_booking
 */
@Data
public class Booking {
    /**
     * 预约ID（主键，自增）
     */
    private Long id;

    /**
     * 预约单号（唯一，自动生成，格式：BK+日期+序号）
     */
    private String bookingNo;

    /**
     * 会员ID（外键关联sys_member）
     */
    @NotNull(message = "会员ID不能为空")
    @Positive(message = "会员ID必须为正数")
    private Long memberId;

    /**
     * 场地ID（外键关联sys_court）
     */
    @NotNull(message = "场地ID不能为空")
    @Positive(message = "场地ID必须为正数")
    private Long courtId;

    /**
     * 预约日期（DATE）
     */
    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "预约日期不能是过去时间")
    private LocalDate bookingDate;

    /**
     * 开始时间（TIME）
     */
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    /**
     * 结束时间（TIME）
     */
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    /**
     * 实际开始时间（DATETIME，可为空）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间（DATETIME，可为空）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;

    /**
     * 预约时长（小时）
     */
    private Integer duration;

    /**
     * 订单金额
     */
    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0", message = "订单金额不能为负数")
    private BigDecimal orderAmount;

    /**
     * 支付方式（业务订单仅允许 BALANCE）
     */
    @Pattern(regexp = "^(BALANCE)$", message = "业务订单支付方式必须为BALANCE")
    private String paymentMethod;

    /**
     * 支付状态（0-未支付，1-已支付，2-已退款）
     */
    @StatusValid(allowedValues = {0, 1, 2}, message = "支付状态必须为0(未支付)、1(已支付)或2(已退款)")
    private Integer paymentStatus;

    /**
     * 预约状态（0-已取消，1-待支付，2-已支付，3-进行中，4-已完成）
     */
    @StatusValid(allowedValues = {0, 1, 2, 3, 4}, message = "预约状态必须为0-4之间的值")
    private Integer status;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

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

    // ========== 关联字段（非数据库字段，用于联表查询） ==========

    /**
     * 会员姓名（联表查询时填充）
     */
    private String memberName;

    /**
     * 场地名称（联表查询时填充）
     */
    private String courtName;

    /**
     * 场馆名称（联表查询时填充）
     */
    private String venueName;
}
