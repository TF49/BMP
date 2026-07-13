package com.badminton.bmp.modules.coach.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CoachStudentDetailVO {
    private Long id;
    private String memberName;
    private Integer gender;
    private String avatar;
    private String phone;
    private String idCardTail;
    private String memberType;
    private Integer memberLevel;
    private Integer memberStatus;
    private LocalDateTime registerTime;
    private LocalDateTime expireTime;
    private LocalDateTime firstLessonTime;
    private LocalDateTime lastLessonTime;
    private LocalDateTime nextLessonTime;
    private Integer totalBookings;
    private Integer attendedCount;
    private Integer absentCount;
    private BigDecimal attendanceRate;
    private BigDecimal totalHours;
    private BigDecimal totalConsumptionForCoach;
    private Boolean risk;
}
