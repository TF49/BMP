package com.badminton.bmp.modules.booking.service.impl;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.entity.BookingCourt;
import com.badminton.bmp.modules.booking.mapper.BookingCourtMapper;
import com.badminton.bmp.modules.booking.mapper.BookingMapper;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import com.badminton.bmp.modules.court.service.CourtService;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import com.badminton.bmp.websocket.WebSocketPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private static final long PACKAGE_MIN_ADVANCE_HOURS = 2L;

    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private BookingCourtMapper bookingCourtMapper;
    @Autowired
    private CourtMapper courtMapper;
    @Autowired
    private CourtService courtService;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberConsumeRecordService memberConsumeRecordService;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private WebSocketPushService webSocketPushService;

    @Override
    public Booking findById(Long id) {
        Booking booking = bookingMapper.findById(id);
        if (booking == null) {
            return null;
        }
        List<BookingCourt> details = bookingCourtMapper.findByBookingId(id);
        ensureAccess(booking, details);
        enrichBookings(Collections.singletonList(booking), Collections.singletonMap(id, details));
        return booking;
    }

    @Override
    public List<Booking> findAll(Long venueId,
                                 Long memberId,
                                 Long courtId,
                                 Integer status,
                                 String memberKeyword,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 int page,
                                 int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        int offset = (page - 1) * size;
        Long venueFilter = null;
        Long memberFilter = memberId;
        if (SecurityUtils.isPresident()) {
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            memberFilter = getCurrentMemberId();
        }

        List<Booking> bookings = bookingMapper.findAll(venueFilter, memberFilter, courtId, status, memberKeyword, startDate, endDate, offset, size);
        enrichBookings(bookings);
        return bookings;
    }

    @Override
    public int count(Long venueId,
                     Long memberId,
                     Long courtId,
                     Integer status,
                     String memberKeyword,
                     LocalDate startDate,
                     LocalDate endDate) {
        Long venueFilter = null;
        Long memberFilter = memberId;
        if (SecurityUtils.isPresident()) {
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            memberFilter = getCurrentMemberId();
        }
        return bookingMapper.count(venueFilter, memberFilter, courtId, status, memberKeyword, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(Booking booking) {
        Long effectiveMemberId = resolveAndValidateMemberId(booking.getMemberId());
        booking.setMemberId(effectiveMemberId);
        booking.setBookingMode(normalizeBookingMode(booking.getBookingMode(), booking.getCourtIds(), booking.getCourtId()));
        booking.setPricingMode(normalizePricingMode(booking.getBookingMode(), booking.getPricingMode()));
        validateBookingTime(booking.getBookingDate(), booking.getStartTime(), booking.getEndTime());

        List<Long> selectedCourtIds = normalizeCourtIds(booking.getCourtIds(), booking.getCourtId(), booking.getBookingMode());
        List<Court> selectedCourts = loadAndValidateCourts(selectedCourtIds);
        ensureSingleVenue(selectedCourts);
        validatePackageScopeAndLeadTime(booking, selectedCourts);
        validateModeAndConflicts(booking, selectedCourts, null);

        int duration = calculateDuration(booking.getStartTime(), booking.getEndTime());
        LocalDateTime now = LocalDateTime.now();

        if (booking.getBookingNo() == null || booking.getBookingNo().trim().isEmpty()) {
            booking.setBookingNo(generateBookingNo());
        }
        if (bookingMapper.existsByBookingNo(booking.getBookingNo(), null)) {
            throw new BusinessException("预约单号已存在");
        }

        booking.setCourtId(selectedCourtIds.get(0));
        booking.setDuration(duration);
        booking.setOrderAmount(BigDecimal.ZERO);
        booking.setCreateTime(now);
        booking.setUpdateTime(now);
        booking.setStatus(1);
        booking.setPaymentStatus(0);

        List<BookingCourt> details = buildDetailsForBooking(booking, selectedCourts, duration, now);
        booking.setOrderAmount(sumLineAmount(details));

        int inserted = bookingMapper.insert(booking);
        if (inserted <= 0) {
            return inserted;
        }
        for (BookingCourt detail : details) {
            detail.setBookingId(booking.getId());
        }
        bookingCourtMapper.insertBatch(details);
        recomputeCourtStatuses(extractCourtIds(details));
        pushBookingUpdates();
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(Booking booking) {
        Booking existing = bookingMapper.findById(booking.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        List<BookingCourt> existingDetails = bookingCourtMapper.findByBookingId(existing.getId());
        ensureAccess(existing, existingDetails);

        String existingMode = normalizeBookingMode(existing.getBookingMode(), existing.getCourtIds(), existing.getCourtId());
        String newMode = booking.getBookingMode() == null ? existingMode : normalizeBookingMode(booking.getBookingMode(), booking.getCourtIds(), booking.getCourtId());
        if (!existingMode.equals(newMode)) {
            throw new BusinessException("不支持切换预约模式");
        }

        existing.setRemark(booking.getRemark() != null ? booking.getRemark() : existing.getRemark());
        if (booking.getMemberId() != null && !booking.getMemberId().equals(existing.getMemberId())) {
            if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
                throw new BusinessException("权限不足，只能修改自己的预约");
            }
            Member member = memberMapper.findById(booking.getMemberId());
            if (member == null) {
                throw new ResourceNotFoundException("会员不存在");
            }
            existing.setMemberId(booking.getMemberId());
        }

        boolean structuralChange = booking.getBookingDate() != null || booking.getStartTime() != null
                || booking.getEndTime() != null || booking.getCourtId() != null
                || (booking.getCourtIds() != null && !booking.getCourtIds().isEmpty())
                || booking.getPricingMode() != null;

        if (structuralChange) {
            if (existing.getStatus() == null || existing.getStatus() != 1) {
                throw new BusinessException("仅待支付预约支持修改场地与时间");
            }
            LocalDate newDate = booking.getBookingDate() != null ? booking.getBookingDate() : existing.getBookingDate();
            LocalTime newStart = booking.getStartTime() != null ? booking.getStartTime() : existing.getStartTime();
            LocalTime newEnd = booking.getEndTime() != null ? booking.getEndTime() : existing.getEndTime();
            validateBookingTime(newDate, newStart, newEnd);

            List<Long> selectedCourtIds = normalizeCourtIds(
                    booking.getCourtIds(),
                    booking.getCourtId(),
                    existingMode.equals(Booking.MODE_PACKAGE) ? existingDetails.stream().map(BookingCourt::getCourtId).collect(Collectors.toList()) : Collections.singletonList(existing.getCourtId()),
                    existingMode
            );
            List<Court> courts = loadAndValidateCourts(selectedCourtIds);
            ensureSingleVenue(courts);

            existing.setBookingDate(newDate);
            existing.setStartTime(newStart);
            existing.setEndTime(newEnd);
            existing.setPricingMode(normalizePricingMode(existingMode, booking.getPricingMode() != null ? booking.getPricingMode() : existing.getPricingMode()));
            validatePackageScopeAndLeadTime(existing, courts);
            validateModeAndConflicts(existing, courts, existing.getId());

            int duration = calculateDuration(newStart, newEnd);
            existing.setDuration(duration);
            existing.setCourtId(selectedCourtIds.get(0));
            existing.setBookingMode(existingMode);
            existing.setUpdateTime(LocalDateTime.now());

            List<BookingCourt> newDetails = buildDetailsForBooking(existing, courts, duration, existing.getUpdateTime());
            existing.setOrderAmount(sumLineAmount(newDetails));

            bookingMapper.update(existing);
            bookingCourtMapper.deleteByBookingId(existing.getId());
            for (BookingCourt detail : newDetails) {
                detail.setBookingId(existing.getId());
            }
            bookingCourtMapper.insertBatch(newDetails);

            Set<Long> affected = new LinkedHashSet<>(extractCourtIds(existingDetails));
            affected.addAll(extractCourtIds(newDetails));
            recomputeCourtStatuses(affected);
        } else {
            existing.setUpdateTime(LocalDateTime.now());
            bookingMapper.update(existing);
        }

        pushBookingUpdates();
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        Booking existing = bookingMapper.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        List<BookingCourt> details = bookingCourtMapper.findByBookingId(id);
        ensureAccess(existing, details);

        if (existing.getStatus() != null && existing.getStatus() == 3) {
            throw new BusinessException("进行中的预约不可直接删除");
        }

        bookingMapper.deleteById(id);
        Set<Long> courtIds = extractCourtIds(details);
        recomputeCourtStatuses(courtIds);
        pushBookingUpdates();
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Long id, Integer status) {
        Booking booking = bookingMapper.findById(id);
        if (booking == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        List<BookingCourt> details = bookingCourtMapper.findByBookingId(id);
        ensureAccess(booking, details);

        bookingMapper.updateStatus(id, status);
        recomputeCourtStatuses(extractCourtIds(details));
        pushBookingUpdates();
        return 1;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Map<String, Object>> statusCounts = bookingMapper.countByStatus();
        int total = 0;
        int cancelled = 0;
        int pending = 0;
        int paid = 0;
        int inProgress = 0;
        int completed = 0;
        for (Map<String, Object> item : statusCounts) {
            int st = ((Number) item.get("status")).intValue();
            int cnt = ((Number) item.get("count")).intValue();
            total += cnt;
            switch (st) {
                case 0: cancelled = cnt; break;
                case 1: pending = cnt; break;
                case 2: paid = cnt; break;
                case 3: inProgress = cnt; break;
                case 4: completed = cnt; break;
                default: break;
            }
        }
        stats.put("total", total);
        stats.put("cancelled", cancelled);
        stats.put("pending", pending);
        stats.put("paid", paid);
        stats.put("inProgress", inProgress);
        stats.put("completed", completed);
        stats.put("unpaidOrders", pending);
        stats.put("pendingIssues", 0);

        Long venueFilter = SecurityUtils.isVenueManager() ? SecurityUtils.getCurrentUserVenueId() : null;
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        int todayBookings = bookingMapper.countByBookingDate(venueFilter, today);
        int yesterdayBookings = bookingMapper.countByBookingDate(venueFilter, yesterday);
        double bookingTrend = 0;
        if (yesterdayBookings > 0) {
            bookingTrend = ((double) (todayBookings - yesterdayBookings) / yesterdayBookings) * 100.0;
        } else if (todayBookings > 0) {
            bookingTrend = 100.0;
        }
        stats.put("todayBookings", todayBookings);
        stats.put("bookingTrend", bookingTrend);

        String peakHours = "--";
        List<Map<String, Object>> hourRows = bookingMapper.countStartHourForDate(venueFilter, today);
        if (hourRows != null && !hourRows.isEmpty()) {
            Object hourObj = hourRows.get(0).get("hour");
            int hour = hourObj instanceof Number ? ((Number) hourObj).intValue() : Integer.parseInt(hourObj.toString());
            peakHours = String.format("%02d:00-%02d:00", hour, (hour + 2) % 24);
        }
        stats.put("peakHours", peakHours);
        return stats;
    }

    @Override
    public synchronized String generateBookingNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BK" + dateStr;
        List<String> existingNos = bookingMapper.findBookingNosByPrefix(prefix);
        int maxSequence = 0;
        for (String no : existingNos) {
            if (no != null && no.startsWith(prefix)) {
                try {
                    maxSequence = Math.max(maxSequence, Integer.parseInt(no.substring(prefix.length())));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return prefix + String.format("%03d", maxSequence + 1);
    }

    @Override
    public int countBookingsForTimeRange(Long courtId,
                                         LocalDate bookingDate,
                                         LocalTime startTime,
                                         LocalTime endTime) {
        if (courtId == null || bookingDate == null || startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            return 0;
        }
        return bookingCourtMapper.countBookingsForTimeRange(courtId, bookingDate, startTime, endTime);
    }

    @Override
    public int countSharedBookingsForTimeRange(Long courtId,
                                               LocalDate bookingDate,
                                               LocalTime startTime,
                                               LocalTime endTime) {
        if (courtId == null || bookingDate == null || startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            return 0;
        }
        return bookingCourtMapper.countSharedBookingsForTimeRange(courtId, bookingDate, startTime, endTime);
    }

    @Override
    public List<CourtBookingUserDTO> getCurrentOccupancy(Long courtId) {
        if (courtId == null) {
            return new ArrayList<>();
        }
        return convertToOccupancyDTOs(bookingCourtMapper.findCurrentOccupancy(courtId, LocalDate.now(), LocalTime.now()));
    }

    @Override
    public List<CourtBookingUserDTO> getRangeOccupancy(Long courtId,
                                                       LocalDate bookingDate,
                                                       LocalTime startTime,
                                                       LocalTime endTime) {
        if (courtId == null || bookingDate == null || startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            return new ArrayList<>();
        }
        return convertToOccupancyDTOs(bookingCourtMapper.findOccupancyByTimeRange(courtId, bookingDate, startTime, endTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPayment(Long bookingId, String paymentMethod) {
        return processPaymentInternal(bookingId, paymentMethod, null, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processMemberPayment(Long bookingId, String paymentMethod, Long userId) {
        return processPaymentInternal(bookingId, paymentMethod, userId, false);
    }

    @Transactional(rollbackFor = Exception.class)
    protected int processPaymentInternal(Long bookingId, String paymentMethod, Long userId, boolean adminMode) {
        if (!"BALANCE".equals(paymentMethod)) {
            throw new BusinessException("业务订单仅支持余额支付");
        }
        Booking booking = bookingMapper.findById(bookingId);
        if (booking == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        List<BookingCourt> details = bookingCourtMapper.findByBookingId(bookingId);
        ensurePaymentAccess(booking, details, adminMode, userId);

        Integer status = booking.getStatus();
        if (status == null || status != 1) {
            if (status == null) throw new BusinessException("预约状态异常，无法支付");
            if (status == 0) throw new BusinessException("该预约已取消，无法支付");
            if (status == 2) throw new BusinessException("该预约已支付，无需重复支付");
            if (status == 3) throw new BusinessException("该预约进行中，无法重复支付");
            if (status == 4) throw new BusinessException("该预约已完成，无法支付");
            throw new BusinessException("预约状态不正确，只能对待支付状态的预约进行支付");
        }

        memberConsumeRecordService.createConsumeRecord(
                booking.getMemberId(),
                booking.getOrderAmount(),
                Finance.TYPE_BOOKING,
                bookingId,
                paymentMethod,
                buildConsumeRemark(booking)
        );

        Long venueId = resolveVenueId(details);
        financeService.createFromBusiness(
                Finance.TYPE_BOOKING,
                bookingId,
                booking.getOrderAmount(),
                Finance.INCOME,
                paymentMethod,
                venueId,
                buildConsumeRemark(booking)
        );

        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentStatus(1);
        int nextStatus = resolveStatusAfterPayment(booking);
        booking.setStatus(nextStatus);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);
        recomputeCourtStatuses(extractCourtIds(details));
        notifyStatusChange(booking, nextStatus, nextStatus == 3 ? "进行中" : "已支付");
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(Long bookingId) {
        Booking booking = bookingMapper.findById(bookingId);
        if (booking == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        List<BookingCourt> details = bookingCourtMapper.findByBookingId(bookingId);
        ensureAdminAccess(booking, details);

        Integer payStatus = booking.getPaymentStatus();
        if (payStatus == null || payStatus != 1) {
            if (payStatus == null || payStatus == 0) throw new BusinessException("该预约尚未支付，无法退款");
            if (payStatus == 2) throw new BusinessException("该预约已退款，无需重复退款");
            throw new BusinessException("只能对已支付的预约进行退款");
        }

        if ("BALANCE".equals(booking.getPaymentMethod())) {
            memberConsumeRecordService.createRefundRecord(
                    booking.getMemberId(),
                    booking.getOrderAmount(),
                    Finance.TYPE_BOOKING,
                    bookingId,
                    booking.getPaymentMethod(),
                    "场地预约退款：" + booking.getBookingNo()
            );
        }

        financeService.createFromBusiness(
                Finance.TYPE_BOOKING,
                bookingId,
                booking.getOrderAmount(),
                Finance.EXPENSE,
                booking.getPaymentMethod(),
                resolveVenueId(details),
                "场地预约退款：" + booking.getBookingNo()
        );

        booking.setPaymentStatus(2);
        booking.setStatus(0);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);
        recomputeCourtStatuses(extractCourtIds(details));
        notifyStatusChange(booking, 0, "已取消");
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOverdueInProgressBookings() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Booking> duePaid = bookingMapper.findDuePaidBookings(today, now);
        Set<Long> affectedCourtIds = new LinkedHashSet<>();
        if (duePaid != null) {
            for (Booking booking : duePaid) {
                bookingMapper.updateStatus(booking.getId(), 3);
                List<BookingCourt> details = bookingCourtMapper.findByBookingId(booking.getId());
                affectedCourtIds.addAll(extractCourtIds(details));
            }
        }
        List<Booking> overdue = bookingMapper.findOverdueInProgressBookings(today, now);
        if ((duePaid == null || duePaid.isEmpty()) && (overdue == null || overdue.isEmpty())) {
            return;
        }
        for (Booking booking : overdue) {
            bookingMapper.updateStatus(booking.getId(), 4);
            List<BookingCourt> details = bookingCourtMapper.findByBookingId(booking.getId());
            affectedCourtIds.addAll(extractCourtIds(details));
        }
        recomputeCourtStatuses(affectedCourtIds);
        pushBookingUpdates();
    }

    @Override
    public Map<String, Object> getOperationTodoCount() {
        Map<String, Object> stats = getStatistics();
        Map<String, Object> todo = new HashMap<>();
        todo.put("pendingBookings", stats.getOrDefault("pending", 0));
        todo.put("pending", stats.getOrDefault("pending", 0));
        todo.put("unpaidOrders", stats.getOrDefault("unpaidOrders", 0));
        todo.put("unpaid", 0);
        todo.put("pendingIssues", stats.getOrDefault("pendingIssues", 0));
        todo.put("issues", 0);
        return todo;
    }

    @Override
    public List<Map<String, Object>> getTodoList() {
        Long venueFilter = SecurityUtils.isVenueManager() ? SecurityUtils.getCurrentUserVenueId() : null;
        List<Booking> pending = findAll(venueFilter, null, null, 1, null, null, null, 1, 30);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking booking : pending) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "booking");
            item.put("id", booking.getId());
            item.put("bookingNo", booking.getBookingNo());
            item.put("title", booking.getBookingNo() != null ? booking.getBookingNo() : "场地预约 " + booking.getId());
            list.add(item);
        }
        return list;
    }

    private void enrichBookings(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return;
        }
        List<Long> ids = bookings.stream().map(Booking::getId).collect(Collectors.toList());
        Map<Long, List<BookingCourt>> detailsMap = bookingCourtMapper.findByBookingIds(ids)
                .stream()
                .collect(Collectors.groupingBy(BookingCourt::getBookingId));
        enrichBookings(bookings, detailsMap);
    }

    private void enrichBookings(List<Booking> bookings, Map<Long, List<BookingCourt>> detailsMap) {
        for (Booking booking : bookings) {
            List<BookingCourt> details = detailsMap.getOrDefault(booking.getId(), new ArrayList<>());
            if ((booking.getBookingMode() == null || booking.getBookingMode().trim().isEmpty())) {
                booking.setBookingMode(details.size() > 1 ? Booking.MODE_PACKAGE : Booking.MODE_SHARED);
            }
            List<Long> courtIds = details.stream().map(BookingCourt::getCourtId).distinct().collect(Collectors.toList());
            List<String> courtNames = details.stream()
                    .map(BookingCourt::getCourtName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            booking.setCourtIds(courtIds);
            booking.setCourtNames(courtNames);
            booking.setCourtCount(courtIds.size());
            if (!courtIds.isEmpty()) {
                booking.setCourtId(courtIds.get(0));
            }
            String primaryCourtName = courtNames.isEmpty() ? booking.getCourtName() : courtNames.get(0);
            booking.setPrimaryCourtName(primaryCourtName);
            booking.setCourtName(primaryCourtName);
            if (!details.isEmpty()) {
                booking.setVenueName(details.get(0).getVenueName());
                booking.setPricingMode(details.get(0).getPricingMode());
                booking.setPricingModeSummary(details.stream()
                        .map(BookingCourt::getPricingMode)
                        .filter(mode -> mode != null && !mode.trim().isEmpty())
                        .distinct()
                        .collect(Collectors.joining(",")));
            }
        }
    }

    private Long resolveAndValidateMemberId(Long requestedMemberId) {
        Long effectiveMemberId = requestedMemberId;
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            Long currentMemberId = getCurrentMemberId();
            if (currentMemberId == null) {
                throw new BusinessException("当前用户未绑定会员信息，无法创建预约");
            }
            if (effectiveMemberId == null) {
                effectiveMemberId = currentMemberId;
            } else if (!effectiveMemberId.equals(currentMemberId)) {
                throw new BusinessException("权限不足，只能为自己创建预约");
            }
        }
        Member member = memberMapper.findById(effectiveMemberId);
        if (member == null) {
            throw new ResourceNotFoundException("会员不存在");
        }
        return effectiveMemberId;
    }

    private Long getCurrentMemberId() {
        com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
        if (current == null) {
            return null;
        }
        Member member = memberMapper.findByUserId(current.getId());
        return member != null ? member.getId() : null;
    }

    private String normalizeBookingMode(String bookingMode, List<Long> courtIds, Long courtId) {
        if (bookingMode != null && !bookingMode.trim().isEmpty()) {
            return bookingMode.trim().toUpperCase();
        }
        int size = courtIds != null && !courtIds.isEmpty() ? courtIds.size() : (courtId != null ? 1 : 0);
        return size > 1 ? Booking.MODE_PACKAGE : Booking.MODE_SHARED;
    }

    private String normalizePricingMode(String bookingMode, String pricingMode) {
        if (pricingMode == null || pricingMode.trim().isEmpty()) {
            return Booking.MODE_PACKAGE.equals(bookingMode) ? Booking.PRICING_PACKAGE_HOUR : Booking.PRICING_SHARED_HOUR;
        }
        String normalized = pricingMode.trim().toUpperCase();
        if (Booking.MODE_PACKAGE.equals(bookingMode) && !Booking.PRICING_PACKAGE_HOUR.equals(normalized)) {
            throw new BusinessException("包场仅支持按小时计费");
        }
        if (Booking.MODE_SHARED.equals(bookingMode)
                && !Booking.PRICING_SHARED_HOUR.equals(normalized)
                && !Booking.PRICING_SHARED_TIME.equals(normalized)) {
            throw new BusinessException("拼场仅支持按小时或按次计费");
        }
        return normalized;
    }

    private List<Long> normalizeCourtIds(List<Long> courtIds, Long courtId, String bookingMode) {
        return normalizeCourtIds(courtIds, courtId, null, bookingMode);
    }

    private List<Long> normalizeCourtIds(List<Long> courtIds, Long courtId, List<Long> fallbackCourtIds, String bookingMode) {
        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        if (courtIds != null) {
            normalized.addAll(courtIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toList()));
        }
        if (courtId != null && courtId > 0) {
            normalized.add(courtId);
        }
        if (normalized.isEmpty() && fallbackCourtIds != null) {
            normalized.addAll(fallbackCourtIds);
        }
        if (normalized.isEmpty()) {
            throw new BusinessException("至少需要选择一块场地");
        }
        if (Booking.MODE_SHARED.equals(bookingMode) && normalized.size() != 1) {
            throw new BusinessException("拼场预约只能选择一块场地");
        }
        return new ArrayList<>(normalized);
    }

    private List<Court> loadAndValidateCourts(List<Long> courtIds) {
        List<Court> courts = new ArrayList<>();
        for (Long courtId : courtIds) {
            Court court = courtMapper.findById(courtId);
            if (court == null) {
                throw new ResourceNotFoundException("场地不存在");
            }
            if (court.getStatus() != null && court.getStatus() == 0) {
                throw new BusinessException("场地[" + court.getCourtName() + "]正在维护中，暂不可预约");
            }
            courts.add(court);
        }
        return courts;
    }

    private void ensureSingleVenue(List<Court> courts) {
        Set<Long> venueIds = courts.stream().map(Court::getVenueId).collect(Collectors.toSet());
        if (venueIds.size() > 1) {
            throw new BusinessException("同一笔预约只能选择同一场馆下的场地");
        }
    }

    private void validatePackageScopeAndLeadTime(Booking booking, List<Court> courts) {
        if (!Booking.MODE_PACKAGE.equals(booking.getBookingMode())) {
            return;
        }
        if (courts == null || courts.isEmpty()) {
            throw new BusinessException("包场至少需要选择一块场地");
        }

        Long venueId = courts.get(0).getVenueId();
        int totalVenueCourts = venueId != null ? courtMapper.countByVenueId(venueId) : 0;
        if (totalVenueCourts > 0 && courts.size() >= totalVenueCourts) {
            throw new BusinessException("包场仅支持部分场地，不允许包下整个场馆");
        }

        if (booking.getBookingDate() == null || booking.getStartTime() == null) {
            throw new BusinessException("包场申请缺少开始时间");
        }
        LocalDateTime bookingStart = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
        LocalDateTime latestAllowedSubmitTime = bookingStart.minusHours(PACKAGE_MIN_ADVANCE_HOURS);
        if (LocalDateTime.now().isAfter(latestAllowedSubmitTime)) {
            throw new BusinessException("包场申请需至少提前" + PACKAGE_MIN_ADVANCE_HOURS + "小时提交");
        }
    }

    private void validateModeAndConflicts(Booking booking, List<Court> courts, Long excludeBookingId) {
        if (Booking.MODE_SHARED.equals(booking.getBookingMode())) {
            Court court = courts.get(0);
            ensurePricingModeEnabled(court, booking.getPricingMode());
            List<BookingCourt> packageConflicts = bookingCourtMapper.findPackageConflicts(
                    court.getId(), booking.getBookingDate(), booking.getStartTime(), booking.getEndTime(), excludeBookingId);
            if (!packageConflicts.isEmpty()) {
                throw new BusinessException("该场地在所选时间段已存在包场预约，无法拼场");
            }
        } else if (Booking.MODE_PACKAGE.equals(booking.getBookingMode())) {
            for (Court court : courts) {
                ensurePricingModeEnabled(court, Booking.PRICING_PACKAGE_HOUR);
                List<BookingCourt> conflicts = bookingCourtMapper.findActiveConflicts(
                        court.getId(), booking.getBookingDate(), booking.getStartTime(), booking.getEndTime(), excludeBookingId);
                if (!conflicts.isEmpty()) {
                    throw new BusinessException("场地[" + court.getCourtName() + "]在所选时间段已有预约，无法加入包场");
                }
            }
        } else {
            throw new BusinessException("未知预约模式");
        }
    }

    private void ensurePricingModeEnabled(Court court, String pricingMode) {
        if (Booking.PRICING_PACKAGE_HOUR.equals(pricingMode) && !Boolean.TRUE.equals(court.getEnablePackageHour())) {
            throw new BusinessException("场地[" + court.getCourtName() + "]未开放包场按小时预约");
        }
        if (Booking.PRICING_SHARED_HOUR.equals(pricingMode) && !Boolean.TRUE.equals(court.getEnableSharedHour())) {
            throw new BusinessException("场地[" + court.getCourtName() + "]未开放拼场按小时预约");
        }
        if (Booking.PRICING_SHARED_TIME.equals(pricingMode) && !Boolean.TRUE.equals(court.getEnableSharedTime())) {
            throw new BusinessException("场地[" + court.getCourtName() + "]未开放拼场按次预约");
        }
    }

    private List<BookingCourt> buildDetailsForBooking(Booking booking, List<Court> courts, int duration, LocalDateTime now) {
        List<BookingCourt> details = new ArrayList<>();
        for (Court court : courts) {
            BookingCourt detail = new BookingCourt();
            detail.setBookingId(booking.getId());
            detail.setCourtId(court.getId());
            detail.setBookingDate(booking.getBookingDate());
            detail.setStartTime(booking.getStartTime());
            detail.setEndTime(booking.getEndTime());
            detail.setPricingMode(booking.getPricingMode());
            detail.setDuration(duration);
            detail.setUnitPrice(resolveUnitPrice(court, booking.getPricingMode()));
            detail.setLineAmount(calculateLineAmount(detail.getUnitPrice(), booking.getPricingMode(), duration));
            detail.setCreateTime(now);
            details.add(detail);
        }
        return details;
    }

    private BigDecimal resolveUnitPrice(Court court, String pricingMode) {
        BigDecimal price;
        switch (pricingMode) {
            case Booking.PRICING_PACKAGE_HOUR:
                price = court.getPackagePricePerHour();
                break;
            case Booking.PRICING_SHARED_TIME:
                price = court.getSharedPricePerTime();
                break;
            case Booking.PRICING_SHARED_HOUR:
            default:
                price = court.getSharedPricePerHour();
                break;
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            price = court.getPricePerHour();
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("场地[" + court.getCourtName() + "]缺少有效价格配置");
        }
        return price;
    }

    private BigDecimal calculateLineAmount(BigDecimal unitPrice, String pricingMode, int duration) {
        if (Booking.PRICING_SHARED_TIME.equals(pricingMode)) {
            return unitPrice;
        }
        return unitPrice.multiply(BigDecimal.valueOf(duration));
    }

    private BigDecimal sumLineAmount(List<BookingCourt> details) {
        BigDecimal total = BigDecimal.ZERO;
        for (BookingCourt detail : details) {
            total = total.add(detail.getLineAmount() != null ? detail.getLineAmount() : BigDecimal.ZERO);
        }
        return total;
    }

    private int calculateDuration(LocalTime startTime, LocalTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        if (minutes > 0) {
            hours++;
        }
        return (int) hours;
    }

    private void validateBookingTime(LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        if (bookingDate == null) throw new BusinessException("预约日期不能为空");
        if (bookingDate.isBefore(LocalDate.now())) throw new BusinessException("预约日期不能是过去时间");
        if (startTime == null || endTime == null) throw new BusinessException("开始时间和结束时间不能为空");
        if (!endTime.isAfter(startTime)) throw new BusinessException("结束时间必须晚于开始时间");
    }

    private int resolveStatusAfterPayment(Booking booking) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (booking.getBookingDate() == null || booking.getStartTime() == null || booking.getEndTime() == null) {
            return 2;
        }
        if (booking.getBookingDate().isBefore(today)) {
            return 3;
        }
        if (booking.getBookingDate().isEqual(today)
                && !now.isBefore(booking.getStartTime())
                && now.isBefore(booking.getEndTime())) {
            return 3;
        }
        return 2;
    }

    private void ensureAccess(Booking booking, List<BookingCourt> details) {
        if (SecurityUtils.isPresident()) {
            return;
        }
        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            Long bookingVenueId = resolveVenueId(details);
            if (currentVenueId != null && currentVenueId.equals(bookingVenueId)) {
                return;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
        Long currentMemberId = getCurrentMemberId();
        if (currentMemberId != null && currentMemberId.equals(booking.getMemberId())) {
            return;
        }
        throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
    }

    private void ensureAdminAccess(Booking booking, List<BookingCourt> details) {
        if (SecurityUtils.isPresident()) {
            return;
        }
        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            Long bookingVenueId = resolveVenueId(details);
            if (currentVenueId != null && currentVenueId.equals(bookingVenueId)) {
                return;
            }
            throw new BusinessException("权限不足，只能处理自己场馆的预约");
        }
        throw new BusinessException("权限不足，仅管理员可执行此操作");
    }

    private void ensurePaymentAccess(Booking booking, List<BookingCourt> details, boolean adminMode, Long userId) {
        if (adminMode) {
            ensureAdminAccess(booking, details);
            return;
        }
        if (userId == null) {
            throw new BusinessException("未登录或Token无效");
        }
        Member currentMember = memberMapper.findByUserId(userId);
        if (currentMember == null) {
            throw new BusinessException("当前用户未绑定会员信息，无法支付预约");
        }
        if (!currentMember.getId().equals(booking.getMemberId())) {
            throw new BusinessException("权限不足，只能支付自己的预约订单");
        }
    }

    private Long resolveVenueId(List<BookingCourt> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }
        BookingCourt first = details.get(0);
        if (first.getVenueId() != null) {
            return first.getVenueId();
        }
        Court court = courtMapper.findById(first.getCourtId());
        return court != null ? court.getVenueId() : null;
    }

    private Set<Long> extractCourtIds(Collection<BookingCourt> details) {
        if (details == null) {
            return new HashSet<>();
        }
        return details.stream().map(BookingCourt::getCourtId).filter(id -> id != null && id > 0).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void recomputeCourtStatuses(Collection<Long> courtIds) {
        if (courtIds == null) return;
        for (Long courtId : courtIds) {
            courtService.recomputeCourtStatus(courtId);
        }
    }

    private void pushBookingUpdates() {
        try {
            webSocketPushService.pushTodoUpdate(getOperationTodoCount(), getTodoList());
            webSocketPushService.pushDashboardRefresh();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
    }

    private void notifyStatusChange(Booking booking, int status, String statusText) {
        try {
            Member member = memberMapper.findById(booking.getMemberId());
            Long userId = member != null ? member.getUserId() : null;
            webSocketPushService.onOrderStatusChanged(userId, "booking", booking.getId(), status, statusText, "场地预约", getOperationTodoCount(), getTodoList());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
        pushBookingUpdates();
    }

    private String buildConsumeRemark(Booking booking) {
        if (Booking.MODE_PACKAGE.equals(booking.getBookingMode())) {
            return "包场预约支付：" + booking.getBookingNo();
        }
        return "拼场预约支付：" + booking.getBookingNo();
    }

    private List<CourtBookingUserDTO> convertToOccupancyDTOs(List<Map<String, Object>> rows) {
        List<CourtBookingUserDTO> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            CourtBookingUserDTO dto = new CourtBookingUserDTO();
            Object bookingIdObj = row.get("booking_id");
            if (bookingIdObj != null) {
                dto.setBookingId(toLong(bookingIdObj));
            }
            dto.setMemberName(maskName((String) row.get("member_name")));
            dto.setMemberType((String) row.get("member_type"));
            Object memberLevelObj = row.get("member_level");
            if (memberLevelObj != null) {
                dto.setMemberLevel(toInt(memberLevelObj));
            }
            dto.setStartTime(formatTime(row.get("start_time")));
            dto.setEndTime(formatTime(row.get("end_time")));
            Object statusObj = row.get("status");
            if (statusObj != null) {
                int status = toInt(statusObj);
                dto.setStatus(status);
                dto.setStatusText(getBookingStatusText(status));
            }
            result.add(dto);
        }
        return result;
    }

    private int toInt(Object obj) {
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof Boolean) return (Boolean) obj ? 1 : 0;
        return Integer.parseInt(String.valueOf(obj));
    }

    private long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof Boolean) return (Boolean) obj ? 1L : 0L;
        return Long.parseLong(String.valueOf(obj));
    }

    private String maskName(String name) {
        if (name == null || name.isEmpty()) return "***";
        int length = name.length();
        if (length == 1) return name;
        if (length == 2) return name.charAt(0) + "*";
        StringBuilder sb = new StringBuilder();
        sb.append(name.charAt(0));
        for (int i = 1; i < length - 1; i++) {
            sb.append("*");
        }
        sb.append(name.charAt(length - 1));
        return sb.toString();
    }

    private String formatTime(Object timeObj) {
        if (timeObj == null) return "";
        if (timeObj instanceof LocalTime) {
            LocalTime time = (LocalTime) timeObj;
            return String.format("%02d:%02d", time.getHour(), time.getMinute());
        }
        if (timeObj instanceof Time) {
            String s = timeObj.toString();
            return s.length() >= 5 ? s.substring(0, 5) : s;
        }
        String s = String.valueOf(timeObj);
        return s.length() >= 5 ? s.substring(0, 5) : s;
    }

    private String getBookingStatusText(int status) {
        switch (status) {
            case 0: return "已取消";
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "进行中";
            case 4: return "已完成";
            default: return "未知";
        }
    }
}
