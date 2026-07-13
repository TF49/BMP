package com.badminton.bmp.modules.coach.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CoachStudentConsumeRecordVO {
    private Long id;
    private Long bookingId;
    private String courseName;
    private BigDecimal amount;
    private String paymentMethod;
    private Integer paymentStatus;
    private String description;
    private String remark;
    private LocalDateTime createTime;
}
