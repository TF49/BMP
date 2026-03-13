package com.badminton.bmp.modules.course.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class CourseBooking {
    private Long id;

    private String bookingNo;

    @NotNull
    @Positive
    private Long memberId;

    @NotNull
    @Positive
    private Long courseId;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("99999.99")
    private BigDecimal orderAmount;

    @Size(max = 20)
    private String paymentMethod;

    @Min(0) @Max(2)
    private Integer paymentStatus;

    @Min(0) @Max(4)
    private Integer status;

    @Size(max = 500)
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer delFlag;

    private String memberName;
    private String courseName;
    private String coachName;
    private LocalDate courseDate;
    private LocalTime courseStartTime;
    private LocalTime courseEndTime;
}
