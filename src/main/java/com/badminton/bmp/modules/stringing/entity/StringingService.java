package com.badminton.bmp.modules.stringing.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StringingService {
    private Long id;
    private String serviceNo;

    private Long memberId;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long venueId;

    @NotBlank
    @Size(max = 50)
    private String racketBrand;

    @NotBlank
    @Size(max = 50)
    private String racketModel;

    @Size(max = 200)
    private String racketDescription;

    private Long stringId;

    @Size(max = 100)
    private String stringName;

    @Min(0) @Max(1)
    private Integer isOwnString;

    @NotNull
    @DecimalMin("10.0")
    @DecimalMax("40.0")
    private BigDecimal pound;

    @NotBlank
    @Pattern(regexp = "TWO_SECTION|FOUR_SECTION|AUTO")
    private String stringingMethod;

    @Min(0) @Max(1)
    private Integer hasBreakage;

    @Min(0) @Max(1)
    private Integer hasCollapse;

    @Min(0) @Max(3)
    private Integer status;

    @DecimalMin("0.00")
    @DecimalMax("9999.99")
    private BigDecimal servicePrice;

    @Size(max = 20)
    private String paymentMethod;

    @Min(0) @Max(2)
    private Integer paymentStatus;

    @Size(max = 500)
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    private Integer delFlag;

    private String memberName;
    private String memberPhone;
    private String userName;
    private String venueName;
    private String stringEquipmentName;
}
