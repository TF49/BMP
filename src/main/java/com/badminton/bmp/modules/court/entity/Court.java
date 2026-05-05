package com.badminton.bmp.modules.court.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Court {
    private Long id;

    @NotBlank
    @Size(max = 20)
    private String courtCode;

    @NotNull
    @Positive
    private Long venueId;

    @NotBlank
    @Size(max = 50)
    private String courtName;

    @NotBlank
    @Pattern(regexp = "HOUR|TIME")
    private String billingType;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("9999.99")
    private BigDecimal pricePerHour;

    private BigDecimal packagePricePerHour;

    private BigDecimal sharedPricePerHour;

    private BigDecimal sharedPricePerTime;

    @Min(0) @Max(3)
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer delFlag;

    private String venueName;
}
