package com.badminton.bmp.modules.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Agent Tool 预约报价请求。
 *
 * <p>由 Python 预订 Agent 提交，字段为 camelCase 以对齐 Python DTO。日期与时间为字符串，
 * 由服务层做白名单解析（date=yyyy-MM-dd，time=HH:mm），避免绑定期异常泄漏。</p>
 */
public class AgentBookingQuoteRequestDTO {

    @NotNull(message = "场馆 ID 不能为空")
    @Positive(message = "场馆 ID 必须大于 0")
    private Long venueId;

    /** 场地 ID 列表：拼场为单块场地，包场为多块场地。 */
    @NotEmpty(message = "至少需要选择一块场地")
    private List<Long> courtIds;

    /** 预约日期 yyyy-MM-dd。 */
    @NotBlank(message = "预约日期不能为空")
    private String date;

    /** 开始时间 HH:mm。 */
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    /** 结束时间 HH:mm。 */
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /** 预约模式（PACKAGE-包场，SHARED-拼场），可为空由服务层按场地数量推断。 */
    private String bookingMode;

    /** 定价模式（PACKAGE_HOUR/SHARED_HOUR/SHARED_TIME），可为空由服务层按模式推断。 */
    private String pricingMode;

    /** 目标会员 ID：仅会长/场馆管理员代预约时使用，普通会员留空即为自己。 */
    private Long memberId;

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

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
