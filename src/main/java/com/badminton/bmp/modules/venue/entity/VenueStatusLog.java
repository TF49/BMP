package com.badminton.bmp.modules.venue.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VenueStatusLog {
    private Long id;

    @NotNull
    @Positive
    private Long venueId;

    @Min(0) @Max(2)
    private Integer oldStatus;

    @NotNull
    @Min(0) @Max(2)
    private Integer newStatus;

    @NotNull
    @Positive
    private Long operatorId;

    @Size(max = 50)
    private String operatorName;

    @Size(max = 500)
    private String changeReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
