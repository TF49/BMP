package com.badminton.bmp.modules.coach.vo;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Data;

@Data
public class CoachStudentAttendanceVO {
    private Long bookingId;
    private String courseName;
    private LocalDate courseDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer bookingStatus;
    private Integer attendanceStatus;
    private Integer durationMinutes;
    private String remark;
}
