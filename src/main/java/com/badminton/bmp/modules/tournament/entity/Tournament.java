package com.badminton.bmp.modules.tournament.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Tournament {
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String tournamentName;

    @NotBlank
    @Size(max = 50)
    private String tournamentType;

    @NotNull
    @Positive
    private Long venueId;

    @NotNull
    @Min(2)
    @Max(10000)
    private Integer maxParticipants;

    @Min(0)
    private Integer currentParticipants;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationStart;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registrationEnd;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tournamentStart;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tournamentEnd;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("99999.99")
    private java.math.BigDecimal entryFee;

    @Size(max = 500)
    private String prizeInfo;

    @Size(max = 1000)
    private String description;

    @Min(0) @Max(3)
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer delFlag;
    private String venueName;
}
