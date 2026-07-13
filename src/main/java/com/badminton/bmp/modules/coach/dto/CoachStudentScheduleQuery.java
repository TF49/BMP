package com.badminton.bmp.modules.coach.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CoachStudentScheduleQuery {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private Integer attendanceStatus;
    private int page = 1;
    private int size = 20;
}
