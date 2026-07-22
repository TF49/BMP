package com.badminton.bmp.modules.agent.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Agent Tool 创建待支付预约结果。
 *
 * <p>创建成功后返回待支付订单信息（status=1 待支付，paymentStatus=0 未支付），不自动支付。
 * 金额由服务端计算，与报价一致。</p>
 */
public class AgentBookingResultDTO {

    private Long bookingId;
    private String bookingNo;
    /** 预约状态：1-待支付。 */
    private Integer status;
    /** 状态可读文案。 */
    private String statusText;
    /** 支付状态：0-未支付。 */
    private Integer paymentStatus;
    private Long venueId;
    private List<Long> courtIds;
    private String date;
    private String startTime;
    private String endTime;
    private String bookingMode;
    private String pricingMode;
    private Integer duration;
    private BigDecimal totalAmount;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Long getVenueId() {
        return venueId;
    }

    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }

    public List<Long> getCourtIds() {
        return courtIds;
    }

    public void setCourtIds(List<Long> courtIds) {
        this.courtIds = courtIds;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getBookingMode() {
        return bookingMode;
    }

    public void setBookingMode(String bookingMode) {
        this.bookingMode = bookingMode;
    }

    public String getPricingMode() {
        return pricingMode;
    }

    public void setPricingMode(String pricingMode) {
        this.pricingMode = pricingMode;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
