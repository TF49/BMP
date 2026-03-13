package com.badminton.bmp.modules.equipment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Equipment {
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String equipmentCode;

    @Size(max = 500)
    private String equipmentImage;

    @NotBlank
    @Size(max = 100)
    private String equipmentName;

    @NotBlank
    @Pattern(regexp = "RACKET|SHUTTLE|STRING|OTHER")
    private String equipmentType;

    @Size(max = 50)
    private String brand;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("99999.99")
    private BigDecimal price;

    @DecimalMin("0.00")
    @DecimalMax("9999.99")
    private BigDecimal rentalPrice;

    @DecimalMin("0.00")
    @DecimalMax("9999.99")
    private BigDecimal rentalDeposit;

    @NotNull
    @Min(0)
    private Integer totalQuantity;

    @Min(0)
    private Integer availableQuantity;

    private Integer version;

    @Min(0) @Max(2)
    private Integer status;

    @Size(max = 500)
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer delFlag;

    private Long venueId;
    private String venueName;
}
