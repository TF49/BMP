package com.badminton.bmp.modules.coach.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CoachStudentListItemVO {
    private Long id;
    private String memberName;
    private Integer gender;
    private String avatar;
    private String maskedPhone;
    private String memberType;
    private Integer memberLevel;
    private Integer memberStatus;
    private LocalDateTime expireTime;
    private LocalDateTime firstLessonTime;
    private LocalDateTime lastLessonTime;
    private LocalDateTime nextLessonTime;
    private Integer totalBookings;
    private Integer attendedCount;
    private Integer absentCount;
    private Integer cancelledCount;
    private Integer scheduledCount;
    private BigDecimal attendanceRate;
    private BigDecimal totalHours;
    private BigDecimal totalPaidAmount;
    private Boolean risk;
}
