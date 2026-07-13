package com.badminton.bmp.modules.coach.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CoachStudentProgressVO {
    private String courseName;
    private Integer totalBookings;
    private Integer completedLessons;
    private BigDecimal totalHours;
    private LocalDateTime lastLessonTime;
}
