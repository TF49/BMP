package com.badminton.bmp.modules.equipment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquipmentRental {
    private Long id;
    private String rentalNo;

    @NotNull
    @Positive
    private Long memberId;

    @NotNull
    @Positive
    private Long equipmentId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private LocalDate rentalDate;

    @NotNull
    private LocalDate expectedReturnDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnDate;

    @DecimalMin("0.00")
    @DecimalMax("99999.99")
    private BigDecimal rentalAmount;

    @DecimalMin("0.00")
    @DecimalMax("9999.99")
    private BigDecimal unitPrice;

    @DecimalMin("0.00")
    @DecimalMax("9999.99")
    private BigDecimal depositAmount;

    @Min(0)
    private Integer durationHours;

    @Size(max = 20)
    private String paymentMethod;

    @Min(0) @Max(2)
    private Integer paymentStatus;

    @Min(0) @Max(3)
    private Integer status;

    @Size(max = 500)
    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private Integer delFlag;

    private String memberName;
    private String equipmentName;
    private String equipmentCode;
    private Long venueId;
}
