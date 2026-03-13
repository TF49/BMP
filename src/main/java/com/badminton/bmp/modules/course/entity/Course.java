package com.badminton.bmp.modules.course.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class Course {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String courseName;

    @NotNull
    @Positive
    private Long coachId;

    private Long courtId;

    @Size(max = 500)
    private String courseContent;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("99999.99")
    private BigDecimal coursePrice;

    @NotNull
    @Min(1)
    @Max(480)
    private Integer courseDuration;

    @NotNull
    private LocalDate courseDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    @Min(1)
    @Max(1000)
    private Integer maxStudents;

    @Min(0)
    private Integer currentStudents;

    private Integer version;

    @Min(0) @Max(3)
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer delFlag;

    private String coachName;
    private String courtName;
}
