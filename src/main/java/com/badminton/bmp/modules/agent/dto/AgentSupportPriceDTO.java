package com.badminton.bmp.modules.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

/**
 * Agent Tool：场馆客服 服务价格信息 DTO。
 */
public class AgentSupportPriceDTO {

    @JsonProperty("venue_id")
    private String venueId;

    @JsonProperty("price_list")
    private List<PriceItem> priceList;

    @JsonProperty("updated_at")
    private String updatedAt;

    public AgentSupportPriceDTO() {
    }

    public AgentSupportPriceDTO(String venueId, List<PriceItem> priceList, String updatedAt) {
        this.venueId = venueId;
        this.priceList = priceList;
        this.updatedAt = updatedAt;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public List<PriceItem> getPriceList() {
        return priceList;
    }

    public void setPriceList(List<PriceItem> priceList) {
        this.priceList = priceList;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class PriceItem {
        @JsonProperty("court_type")
        private String courtType;

        @JsonProperty("time_slot")
        private String timeSlot;

        private BigDecimal price;

        private String unit;

        public PriceItem() {
        }

        public PriceItem(String courtType, String timeSlot, BigDecimal price, String unit) {
            this.courtType = courtType;
            this.timeSlot = timeSlot;
            this.price = price;
            this.unit = unit;
        }

        public String getCourtType() {
            return courtType;
        }

        public void setCourtType(String courtType) {
            this.courtType = courtType;
        }

        public String getTimeSlot() {
            return timeSlot;
        }

        public void setTimeSlot(String timeSlot) {
            this.timeSlot = timeSlot;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
}
