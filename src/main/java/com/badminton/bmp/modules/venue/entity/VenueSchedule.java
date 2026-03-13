package com.badminton.bmp.modules.venue.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class VenueSchedule {
    private Long id;

    @NotNull
    @Positive
    private Long venueId;

    @NotBlank
    @Pattern(regexp = "WORKDAY|WEEKEND|HOLIDAY")
    private String scheduleType;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Min(0) @Max(1)
    private Integer isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
