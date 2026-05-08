package com.badminton.bmp.modules.tournament.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TournamentRegistration {
    private Long id;
    private String registrationNo;

    @NotNull
    @Positive
    private Long tournamentId;

    @NotNull
    @Positive
    private Long memberId;

    private Long partnerId;

    @Size(max = 50)
    private String registrantName;

    @Size(max = 20)
    private String registrantPhone;

    @Size(max = 18)
    private String registrantIdCard;

    @Size(max = 10)
    private String eventTypeSnapshot;

    @Size(max = 20)
    private String eventTypeNameSnapshot;

    @Size(max = 50)
    private String partnerNameSnapshot;

    @Size(max = 20)
    private String partnerPhoneSnapshot;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("99999.99")
    private BigDecimal entryFee;

    @Size(max = 20)
    private String paymentMethod;

    @Min(0) @Max(3)
    private Integer paymentStatus;

    @Min(0) @Max(4)
    private Integer status;

    @Size(max = 200)
    private String matchResult;

    @Size(max = 500)
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer delFlag;
    private String memberName;
    private String partnerName;
    private String tournamentName;
    private String venueName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tournamentStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tournamentEndTime;
}
