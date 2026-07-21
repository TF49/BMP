package com.badminton.bmp.modules.coach.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 管理端教练学员关系列表项 VO
 * 支持会长/场馆管理者在管理后台查看教练与学员的绑定关系
 */
@Data
public class AdminCoachStudentVO {
    // ---- 关系信息 ----
    /** 关系记录ID (biz_coach_student.id) */
    private Long id;
    /** 绑定类型: AUTO-自动(预约触发), MANUAL-手动绑定 */
    private String bindType;

    // ---- 教练信息 ----
    private Long coachId;
    private String coachName;
    private String coachPhone;
    private String coachAvatar;
    private Long venueId;
    private String venueName;

    // ---- 学员/会员信息 ----
    private Long memberId;
    private String memberName;
    private Integer gender;
    private String memberAvatar;
    private String maskedPhone;
    private String memberType;
    private Integer memberLevel;
    private Integer memberStatus;
    private LocalDateTime expireTime;

    // ---- 关联统计 ----
    private LocalDateTime firstLessonTime;
    private LocalDateTime lastLessonTime;
    private Integer totalBookings;
    private Integer attendedCount;
    private Integer absentCount;
    private BigDecimal attendanceRate;
    private BigDecimal totalHours;
    private BigDecimal totalPaidAmount;

    // ---- 关系元信息 ----
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
