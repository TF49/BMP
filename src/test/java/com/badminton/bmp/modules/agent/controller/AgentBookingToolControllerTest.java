package com.badminton.bmp.modules.agent.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.modules.agent.dto.AgentBookingCreateRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteRequestDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingQuoteResultDTO;
import com.badminton.bmp.modules.agent.dto.AgentBookingResultDTO;
import com.badminton.bmp.modules.agent.service.impl.AgentBookingToolServiceImpl;
import com.badminton.bmp.modules.agent.support.AgentActionIdempotencyService;
import com.badminton.bmp.modules.agent.support.AgentActionStatus;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.entity.BookingCourt;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.booking.service.BookingService.BookingQuotation;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AgentBookingToolController} 端到端行为测试。
 *
 * <p>控制器为薄委派层，因此本测试 {@code new} 真实的
 * {@link AgentBookingToolServiceImpl} 并注入 mock 依赖（{@code BookingService} /
 * {@code VenueService} / {@code AgentActionIdempotencyService}），配合对
 * {@link SecurityUtils} 的静态打桩，覆盖计划验收点：报价金额直接透传 Java、创建仅生成
 * 待支付、缺失/重复幂等键、越权场馆拒绝、无空场传播。金额一律由 Java 侧计算，模型不介入。</p>
 */
class AgentBookingToolControllerTest {

    private static final Long VENUE_ID = 1L;
    private static final String LOGIN_USER = "1001";

    private BookingService bookingService;
    private VenueService venueService;
    private AgentActionIdempotencyService idempotencyService;
    private AgentBookingToolController controller;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockedStatic<SecurityUtils> securityUtils;

    @BeforeEach
    void setUp() {
        bookingService = Mockito.mock(BookingService.class);
        venueService = Mockito.mock(VenueService.class);
        idempotencyService = Mockito.mock(AgentActionIdempotencyService.class);
        AgentBookingToolServiceImpl service =
                new AgentBookingToolServiceImpl(bookingService, venueService, idempotencyService);
        controller = new AgentBookingToolController(service);

        securityUtils = Mockito.mockStatic(SecurityUtils.class);
        securityUtils.when(SecurityUtils::getCurrentUsername).thenReturn(LOGIN_USER);
        securityUtils.when(SecurityUtils::isVenueManager).thenReturn(false);
    }

    @AfterEach
    void tearDown() {
        securityUtils.close();
    }

    @Test
    void quoteReturnsAmountComputedByJava() {
        stubOpenVenue();
        Booking quoted = buildPricedBooking(new BigDecimal("120.00"), 2);
        when(bookingService.quoteForAgent(eq(1001L), any(Booking.class)))
                .thenReturn(new BookingQuotation(quoted, buildDetails()));

        Result<AgentBookingQuoteResultDTO> result = controller.quote(buildQuoteRequest());

        assertEquals(200, result.getCode());
        AgentBookingQuoteResultDTO data = result.getData();
        assertTrue(data.isAvailable());
        assertEquals(new BigDecimal("120.00"), data.getTotalAmount());
        assertEquals(2, data.getDuration());
        assertEquals(1, data.getLineItems().size());
        assertEquals(new BigDecimal("120.00"), data.getLineItems().get(0).getLineAmount());
        assertEquals(VENUE_ID, data.getVenueId());
    }

    @Test
    void createGeneratesPendingBookingAndMarksConfirmed() {
        stubOpenVenue();
        when(idempotencyService.begin("idem-1")).thenReturn(Optional.empty());
        Booking created = buildPricedBooking(new BigDecimal("120.00"), 2);
        created.setId(500L);
        created.setBookingNo("BK20250101001");
        when(bookingService.createPendingForAgent(eq(1001L), any(Booking.class)))
                .thenReturn(new BookingQuotation(created, buildDetails()));

        Result<AgentBookingResultDTO> result = controller.create(buildCreateRequest(), "idem-1");

        AgentBookingResultDTO data = result.getData();
        assertEquals(500L, data.getBookingId());
        assertEquals(1, data.getStatus());
        assertEquals(0, data.getPaymentStatus());
        assertEquals(new BigDecimal("120.00"), data.getTotalAmount());
        verify(bookingService).createPendingForAgent(eq(1001L), any(Booking.class));
        verify(idempotencyService).complete(eq("idem-1"), eq(AgentActionStatus.CONFIRMED), any());
    }

    @Test
    void createWithoutIdempotencyKeyIsRejectedAndNeverWrites() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.create(buildCreateRequest(), null));
        assertEquals(400, ex.getCode());
        verify(bookingService, never()).createPendingForAgent(anyLong(), any(Booking.class));
        verify(idempotencyService, never()).begin(any());
    }

    @Test
    void duplicateIdempotencyKeyReplaysSingleCreate() throws Exception {
        AgentBookingResultDTO first = new AgentBookingResultDTO();
        first.setBookingId(500L);
        first.setStatus(1);
        first.setPaymentStatus(0);
        first.setTotalAmount(new BigDecimal("120.00"));
        String json = objectMapper.writeValueAsString(first);
        when(idempotencyService.begin("idem-1"))
                .thenReturn(Optional.of(new AgentActionIdempotencyService.Record(AgentActionStatus.CONFIRMED, json)));

        Result<AgentBookingResultDTO> result = controller.create(buildCreateRequest(), "idem-1");

        assertEquals(500L, result.getData().getBookingId());
        verify(bookingService, never()).createPendingForAgent(anyLong(), any(Booking.class));
    }

    @Test
    void inFlightIdempotencyKeyIsRejected() {
        when(idempotencyService.begin("idem-1"))
                .thenReturn(Optional.of(new AgentActionIdempotencyService.Record(AgentActionStatus.PENDING, null)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.create(buildCreateRequest(), "idem-1"));
        assertEquals(409, ex.getCode());
        verify(bookingService, never()).createPendingForAgent(anyLong(), any(Booking.class));
    }

    @Test
    void venueManagerCannotQuoteOtherVenue() {
        securityUtils.when(SecurityUtils::isVenueManager).thenReturn(true);
        securityUtils.when(SecurityUtils::getCurrentUserVenueId).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.quote(buildQuoteRequest()));
        assertEquals(403, ex.getCode());
        verify(bookingService, never()).quoteForAgent(anyLong(), any(Booking.class));
    }

    @Test
    void noAvailabilityFromJavaIsPropagated() {
        stubOpenVenue();
        when(bookingService.quoteForAgent(anyLong(), any(Booking.class)))
                .thenThrow(new BusinessException(409, "所选时段已被占用"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> controller.quote(buildQuoteRequest()));
        assertEquals(409, ex.getCode());
    }

    private void stubOpenVenue() {
        Venue venue = Mockito.mock(Venue.class);
        when(venue.getStatus()).thenReturn(1);
        when(venueService.findById(VENUE_ID)).thenReturn(venue);
    }

    private AgentBookingQuoteRequestDTO buildQuoteRequest() {
        AgentBookingQuoteRequestDTO request = new AgentBookingQuoteRequestDTO();
        applyCommonRequest(request);
        return request;
    }

    private AgentBookingCreateRequestDTO buildCreateRequest() {
        AgentBookingCreateRequestDTO request = new AgentBookingCreateRequestDTO();
        applyCommonRequest(request);
        request.setRemark("agent 代订");
        return request;
    }

    private void applyCommonRequest(AgentBookingQuoteRequestDTO request) {
        request.setVenueId(VENUE_ID);
        request.setCourtIds(List.of(10L));
        request.setDate(LocalDate.now().plusDays(1).toString());
        request.setStartTime("10:00");
        request.setEndTime("12:00");
        request.setBookingMode("WHOLE");
        request.setPricingMode("STANDARD");
    }

    private Booking buildPricedBooking(BigDecimal amount, int duration) {
        Booking booking = new Booking();
        booking.setCourtIds(List.of(10L));
        booking.setBookingDate(LocalDate.now().plusDays(1));
        booking.setStartTime(java.time.LocalTime.of(10, 0));
        booking.setEndTime(java.time.LocalTime.of(12, 0));
        booking.setBookingMode("WHOLE");
        booking.setPricingMode("STANDARD");
        booking.setDuration(duration);
        booking.setOrderAmount(amount);
        booking.setStatus(1);
        booking.setPaymentStatus(0);
        return booking;
    }

    private List<BookingCourt> buildDetails() {
        BookingCourt court = new BookingCourt();
        court.setCourtId(10L);
        court.setUnitPrice(new BigDecimal("60.00"));
        court.setLineAmount(new BigDecimal("120.00"));
        return List.of(court);
    }
}
