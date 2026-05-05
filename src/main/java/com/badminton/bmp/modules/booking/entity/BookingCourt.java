package com.badminton.bmp.modules.booking.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingCourt {
    private Long id;
    private Long bookingId;
    private Long courtId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String pricingMode;
    private BigDecimal unitPrice;
    private Integer duration;
    private BigDecimal lineAmount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Integer bookingStatus;
    private Integer bookingPaymentStatus;
    private String bookingMode;
    private Long memberId;
    private String courtName;
    private String courtCode;
    private Long venueId;
    private String venueName;
}
