package com.badminton.bmp.modules.member.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberConsumeRecord {
    private Long id;

    @NotNull
    @Positive
    private Long memberId;

    @NotBlank
    @Pattern(regexp = "BOOKING|COURSE|EQUIPMENT|TOURNAMENT|STRINGING|OTHER")
    private String businessType;

    private Long businessId;

    @Size(max = 200)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("99999.99")
    private BigDecimal amount;

    @Size(max = 20)
    private String paymentMethod;

    @Min(0) @Max(2)
    private Integer paymentStatus;

    @DecimalMin("0.00")
    private BigDecimal beforeBalance;

    @DecimalMin("0.00")
    private BigDecimal afterBalance;

    @Size(max = 500)
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Long venueId;
}
