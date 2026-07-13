package com.badminton.bmp.modules.coach.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;

@Data
public class CoachStudentScheduleVO {
    private Long bookingId;
    private Long courseId;
    private String courseName;
    private LocalDate courseDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer bookingStatus;
    private Integer attendanceStatus;
    private LocalDateTime actualCheckinTime;
    private LocalDateTime actualFinishTime;
    private String remark;
}
