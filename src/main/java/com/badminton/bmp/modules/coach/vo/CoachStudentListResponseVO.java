package com.badminton.bmp.modules.coach.vo;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CoachStudentListResponseVO {
    private CoachStudentPageVO<CoachStudentListItemVO> page;
    private Integer totalStudents;
    private Integer riskStudents;
    private Integer todayStudents;
    private BigDecimal averageAttendanceRate;
}
