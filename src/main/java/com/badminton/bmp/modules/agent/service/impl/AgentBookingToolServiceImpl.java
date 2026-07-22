package com.badminton.bmp.modules.agent.service.impl;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.agent.dto.AgentBookingCreateRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteResultDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteResultDTO.AgentBookingLineItemDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingResultDTO;
import com.badminton.bmp.modules.agent.service.AgentBookingToolService;
import com.badminton.bmp.modules.agent.support.AgentActionIdempotencyService;
import com.badminton.bmp.modules.agent.support.AgentActionStatus;
import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.entity.BookingCourt;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.booking.service.BookingService.BookingQuotation;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link AgentBookingToolService} 默认实现。
 *
 * <p>在服务层完成日期/时间白名单、场馆范围与营业状态的前置校验，随后委派
 * {@link BookingService} 的 Agent 专用报价/创建方法完成会员解析、冲突校验与定价，
 * 不复制预约业务逻辑。创建通过 {@link AgentActionIdempotencyService} 保证同一幂等键
 * 只创建一个待支付预约，重复请求回放首次结果。</p>
 */
@Service
public class AgentBookingToolServiceImpl implements AgentBookingToolService {

    /** 专用审计日志：仅记录动作、用户、结果，不记录任何凭证或明细金额之外的敏感信息。 */
    private static final Logger auditLog = LoggerFactory.getLogger("agent.booking.audit");

    private static final int MAX_ADVANCE_DAYS = 30;
    private static final int VENUE_OPEN = 1;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final BookingService bookingService;
    private final VenueService venueService;
    private final AgentActionIdempotencyService idempotencyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentBookingToolServiceImpl(BookingService bookingService, VenueService venueService,
                                       AgentActionIdempotencyService idempotencyService) {
        this.bookingService = bookingService;
        this.venueService = venueService;
        this.idempotencyService = idempotencyService;
    }

    @Override
    public AgentBookingQuoteResultDTO quote(AgentBookingQuoteRequestDTO request) {
        Long loginUserId = currentLoginUserId();
        Booking booking = validateAndBuildBooking(request, null);
        BookingQuotation quotation = bookingService.quoteForAgent(loginUserId, booking);
        return toQuoteResult(quotation, request.getVenueId());
    }

    @Override
    public AgentBookingResultDTO create(AgentBookingCreateRequestDTO request, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new BusinessException(400, "缺少幂等键");
        }
        Long loginUserId = currentLoginUserId();

        Optional<AgentActionIdempotencyService.Record> existing = idempotencyService.begin(idempotencyKey);
        if (existing.isPresent()) {
            return replayOrReject(existing.get(), idempotencyKey);
        }

        try {
            Booking booking = validateAndBuildBooking(request, request.getRemark());
            BookingQuotation quotation = bookingService.createPendingForAgent(loginUserId, booking);
            AgentBookingResultDTO result = toCreateResult(quotation, request.getVenueId());
            idempotencyService.complete(idempotencyKey, AgentActionStatus.CONFIRMED, writeResult(result));
            auditLog.info("action=create outcome=success userId={} bookingId={} venueId={} amount={}",
                    loginUserId, result.getBookingId(), result.getVenueId(), result.getTotalAmount());
            return result;
        } catch (RuntimeException e) {
            idempotencyService.complete(idempotencyKey, AgentActionStatus.REJECTED, null);
            auditLog.warn("action=create outcome=rejected userId={} reason={}", loginUserId, e.getMessage());
            throw e;
        }
    }

    /** 幂等重复请求：确认态回放首次结果，其余（进行中/已拒绝）拒绝重复提交。 */
    private AgentBookingResultDTO replayOrReject(AgentActionIdempotencyService.Record record, String idempotencyKey) {
        if (record.status() == AgentActionStatus.CONFIRMED && StringUtils.hasText(record.resultJson())) {
            auditLog.info("action=create outcome=replay key-present");
            return readResult(record.resultJson());
        }
        throw new BusinessException(409, "该操作正在处理或已处理，请勿重复提交");
    }

    /** 从安全上下文解析登录用户 ID（principal 为 AgentContext.userId 数字串）。 */
    private Long currentLoginUserId() {
        String principal = SecurityUtils.getCurrentUsername();
        if (!StringUtils.hasText(principal)) {
            throw new BusinessException(401, "缺少用户上下文");
        }
        try {
            return Long.parseLong(principal.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "用户上下文不合法");
        }
    }

    private Booking validateAndBuildBooking(AgentBookingQuoteRequestDTO request, String remark) {
        if (request.getVenueId() == null) {
            throw new BusinessException(400, "场馆 ID 不能为空");
        }
        if (request.getCourtIds() == null || request.getCourtIds().isEmpty()) {
            throw new BusinessException(400, "至少需要选择一块场地");
        }
        LocalDate date = parseDate(request.getDate());
        LocalTime startTime = parseTime(request.getStartTime(), "开始时间");
        LocalTime endTime = parseTime(request.getEndTime(), "结束时间");
        validateDate(date);
        validateTimeRange(startTime, endTime);
        enforceVenueScope(request.getVenueId());
        validateVenueOpen(request.getVenueId());

        Booking booking = new Booking();
        booking.setCourtIds(new ArrayList<>(request.getCourtIds()));
        booking.setBookingDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setBookingMode(request.getBookingMode());
        booking.setPricingMode(request.getPricingMode());
        booking.setMemberId(request.getMemberId());
        booking.setRemark(remark);
        return booking;
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim());
        } catch (RuntimeException e) {
            throw new BusinessException(400, "预约日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    private LocalTime parseTime(String value, String label) {
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT);
        } catch (RuntimeException e) {
            throw new BusinessException(400, label + "格式不正确，应为 HH:mm");
        }
    }

    private void validateDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BusinessException(400, "预约日期不能早于今天");
        }
        if (date.isAfter(today.plusDays(MAX_ADVANCE_DAYS))) {
            throw new BusinessException(400, "预约日期超出可预约范围");
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }
    }

    /** 场馆管理员只能操作其所属场馆。 */
    private void enforceVenueScope(Long venueId) {
        if (SecurityUtils.isVenueManager()) {
            Long own = SecurityUtils.getCurrentUserVenueId();
            if (own == null || !own.equals(venueId)) {
                throw new BusinessException(403, "无权操作该场馆的预约");
            }
        }
    }

    private void validateVenueOpen(Long venueId) {
        Venue venue = venueService.findById(venueId);
        if (venue == null) {
            throw new ResourceNotFoundException("场馆不存在");
        }
        if (venue.getStatus() == null || venue.getStatus() != VENUE_OPEN) {
            throw new BusinessException(400, "场馆当前未营业，暂不可预约");
        }
    }

    private AgentBookingQuoteResultDTO toQuoteResult(BookingQuotation quotation, Long venueId) {
        Booking booking = quotation.booking();
        AgentBookingQuoteResultDTO result = new AgentBookingQuoteResultDTO();
        result.setVenueId(venueId);
        applyCommonFields(result, booking);
        result.setDuration(booking.getDuration());
        result.setTotalAmount(booking.getOrderAmount());
        result.setLineItems(toLineItems(quotation.details()));
        result.setAvailable(true);
        return result;
    }

    private AgentBookingResultDTO toCreateResult(BookingQuotation quotation, Long venueId) {
        Booking booking = quotation.booking();
        AgentBookingResultDTO result = new AgentBookingResultDTO();
        result.setBookingId(booking.getId());
        result.setBookingNo(booking.getBookingNo());
        result.setStatus(booking.getStatus());
        result.setStatusText("待支付");
        result.setPaymentStatus(booking.getPaymentStatus());
        result.setVenueId(venueId);
        result.setCourtIds(booking.getCourtIds());
        result.setDate(booking.getBookingDate() != null ? booking.getBookingDate().toString() : null);
        result.setStartTime(formatTime(booking.getStartTime()));
        result.setEndTime(formatTime(booking.getEndTime()));
        result.setBookingMode(booking.getBookingMode());
        result.setPricingMode(booking.getPricingMode());
        result.setDuration(booking.getDuration());
        result.setTotalAmount(booking.getOrderAmount());
        return result;
    }

    private void applyCommonFields(AgentBookingQuoteResultDTO result, Booking booking) {
        result.setCourtIds(booking.getCourtIds());
        result.setDate(booking.getBookingDate() != null ? booking.getBookingDate().toString() : null);
        result.setStartTime(formatTime(booking.getStartTime()));
        result.setEndTime(formatTime(booking.getEndTime()));
        result.setBookingMode(booking.getBookingMode());
        result.setPricingMode(booking.getPricingMode());
    }

    private List<AgentBookingLineItemDTO> toLineItems(List<BookingCourt> details) {
        List<AgentBookingLineItemDTO> items = new ArrayList<>();
        if (details == null) {
            return items;
        }
        for (BookingCourt detail : details) {
            items.add(new AgentBookingLineItemDTO(detail.getCourtId(), detail.getUnitPrice(), detail.getLineAmount()));
        }
        return items;
    }

    private String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : null;
    }

    private String writeResult(AgentBookingResultDTO result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BusinessException(500, "预约结果序列化失败");
        }
    }

    private AgentBookingResultDTO readResult(String json) {
        try {
            return objectMapper.readValue(json, AgentBookingResultDTO.class);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new BusinessException(500, "预约结果解析失败");
        }
    }
}
