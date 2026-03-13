package com.badminton.bmp.modules.court.dto;

import lombok.Data;

/**
 * 场地预约用户信息DTO
 * 用于展示当天使用场地的用户信息（脱敏处理）
 */
@Data
public class CourtBookingUserDTO {

    /**
     * 预约ID
     */
    private Long bookingId;

    /**
     * 会员姓名（脱敏后）
     */
    private String memberName;

    /**
     * 会员类型（NORMAL-普通用户，MEMBER-会员）
     */
    private String memberType;

    /**
     * 会员等级（1-5级）
     */
    private Integer memberLevel;

    /**
     * 开始时间（格式：HH:mm）
     */
    private String startTime;

    /**
     * 结束时间（格式：HH:mm）
     */
    private String endTime;

    /**
     * 预约状态（1-待支付，2-已支付，3-进行中，4-已完成）
     */
    private Integer status;

    /**
     * 状态文本描述
     */
    private String statusText;

    /**
     * 获取会员等级显示文本
     * @return 会员等级文本
     */
    public String getMemberLevelText() {
        if ("NORMAL".equals(memberType)) {
            return "普通用户";
        }
        if (memberLevel == null) {
            return "会员";
        }
        if (memberLevel >= 3) {
            return "VIP Lv." + memberLevel;
        }
        return "会员 Lv." + memberLevel;
    }

    /**
     * 获取状态对应的标签类型（用于前端展示）
     * @return 标签类型
     */
    public String getStatusTagType() {
        if (status == null) {
            return "info";
        }
        switch (status) {
            case 1: return "warning";   // 待支付 - 橙色
            case 2: return "success";   // 已支付 - 绿色
            case 3: return "primary";   // 进行中 - 蓝色
            case 4: return "info";      // 已完成 - 灰色
            default: return "info";
        }
    }

    /**
     * 获取会员等级对应的标签类型（用于前端展示）
     * @return 标签类型
     */
    public String getMemberTagType() {
        if ("NORMAL".equals(memberType)) {
            return "info";      // 普通用户 - 灰色
        }
        if (memberLevel == null) {
            return "primary";   // 默认会员 - 蓝色
        }
        switch (memberLevel) {
            case 1: return "primary";   // Lv.1 - 蓝色
            case 2: return "success";   // Lv.2 - 绿色
            case 3: return "warning";   // Lv.3 - 橙色
            case 4:                     // Lv.4 - 紫色（Element Plus 没有紫色，用 danger 替代）
            case 5: return "danger";    // Lv.5 - 金色（用 danger 表示高级）
            default: return "primary";
        }
    }
}
