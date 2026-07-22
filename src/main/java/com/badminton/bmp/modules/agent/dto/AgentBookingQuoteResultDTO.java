package com.badminton.bmp.modules.agent.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Agent Tool 预约报价结果。
 *
 * <p>金额由 Java 服务端计算，模型不得改写。可用性为真表示所选时段可预约；
 * 不可用（冲突/维护）时服务层抛出业务异常并携带原因，Agent 据此追问或降级。</p>
 */
public class AgentBookingQuoteResultDTO {

    private Long venueId;
    private List<Long> courtIds;
    /** 预约日期 yyyy-MM-dd。 */
    private String date;
    /** 开始时间 HH:mm。 */
    private String startTime;
    /** 结束时间 HH:mm。 */
    private String endTime;
    private String bookingMode;
    private String pricingMode;
    /** 计费时长（小时，不足 1 小时进位）。 */
    private Integer duration;
    /** 报价总金额（服务端计算）。 */
    private BigDecimal totalAmount;
    /** 逐场地金额明细。 */
    private List<AgentBookingLineItemDTO> lineItems;
    /** 所选时段是否可预约。 */
    private boolean available;

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

    public List<AgentBookingLineItemDTO> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<AgentBookingLineItemDTO> lineItems) {
        this.lineItems = lineItems;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /** 单块场地的定价明细。 */
    public static class AgentBookingLineItemDTO {

        private Long courtId;
        private BigDecimal unitPrice;
        private BigDecimal lineAmount;

        public AgentBookingLineItemDTO() {
        }

        public AgentBookingLineItemDTO(Long courtId, BigDecimal unitPrice, BigDecimal lineAmount) {
            this.courtId = courtId;
            this.unitPrice = unitPrice;
            this.lineAmount = lineAmount;
        }

        public Long getCourtId() {
            return courtId;
        }

        public void setCourtId(Long courtId) {
            this.courtId = courtId;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(BigDecimal unitPrice) {
            this.unitPrice = unitPrice;
        }

        public BigDecimal getLineAmount() {
            return lineAmount;
        }

        public void setLineAmount(BigDecimal lineAmount) {
            this.lineAmount = lineAmount;
        }
    }
}
