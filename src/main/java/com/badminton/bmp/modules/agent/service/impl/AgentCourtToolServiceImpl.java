package com.badminton.bmp.modules.agent.service.impl;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.agent.dto.AgentCourtAvailabilityDTO;
import com.badminton.bmp.modules.agent.service.AgentCourtToolService;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.service.CourtService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.entity.VenueSchedule;
import com.badminton.bmp.modules.venue.service.VenueService;
import com.badminton.bmp.modules.venue.service.VenueScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link AgentCourtToolService} 默认实现。
 *
 * <p>复用 {@link CourtService} 与 {@link BookingService#getRangeOccupancy} 判断可用性，
 * 不复制预约冲突逻辑。执行日期/时间白名单校验、场馆营业状态与场地状态校验，
 * 并对场馆管理员限定其所属场馆。</p>
 */
@Service
public class AgentCourtToolServiceImpl implements AgentCourtToolService {

    private static final int MAX_LIMIT = 50;
    private static final int MAX_ADVANCE_DAYS = 30;
    /** 场馆营业中状态值。 */
    private static final int VENUE_OPEN = 1;
    /** 场地维护中状态值。 */
    private static final int COURT_MAINTENANCE = 0;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final VenueService venueService;
    private final CourtService courtService;
    private final BookingService bookingService;
    private final VenueScheduleService venueScheduleService;

    public AgentCourtToolServiceImpl(VenueService venueService, CourtService courtService,
                                     BookingService bookingService,
                                     VenueScheduleService venueScheduleService) {
        this.venueService = venueService;
        this.courtService = courtService;
        this.bookingService = bookingService;
        this.venueScheduleService = venueScheduleService;
    }

    @Override
    public List<AgentCourtAvailabilityDTO> queryAvailability(Long venueId, LocalDate date,
                                                             LocalTime startTime, LocalTime endTime, int limit) {
        if (venueId == null) {
            throw new BusinessException(400, "场馆 ID 不能为空");
        }
        validateDate(date);
        validateTimeRange(startTime, endTime);
        int effectiveLimit = clampLimit(limit);

        enforceVenueScope(venueId);

        Venue venue = venueService.findById(venueId);
        if (venue == null) {
            throw new ResourceNotFoundException("场馆不存在");
        }
        if (venue.getStatus() == null || venue.getStatus() != VENUE_OPEN) {
            throw new BusinessException(400, "场馆当前未营业，暂不可预约");
        }
        validateBusinessHours(venue, date, startTime, endTime);

        List<Court> courts = courtService.findAll(venueId, null, null, 1, effectiveLimit);
        List<AgentCourtAvailabilityDTO> result = new ArrayList<>();
        if (courts == null) {
            return result;
        }
        for (Court court : courts) {
            if (court == null) {
                continue;
            }
            boolean maintenance = court.getStatus() != null && court.getStatus() == COURT_MAINTENANCE;
            boolean available = !maintenance
                    && bookingService.getRangeOccupancy(court.getId(), date, startTime, endTime).isEmpty();
            result.add(toDto(court, venueId, date, startTime, endTime, available));
        }
        return result;
    }

    /** 场馆管理员只能查询其所属场馆。 */
    private void enforceVenueScope(Long venueId) {
        if (SecurityUtils.isVenueManager()) {
            Long own = SecurityUtils.getCurrentUserVenueId();
            if (own == null || !own.equals(venueId)) {
                throw new BusinessException(403, "无权查询该场馆的场地");
            }
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BusinessException(400, "预约日期不能为空");
        }
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BusinessException(400, "预约日期不能早于今天");
        }
        if (date.isAfter(today.plusDays(MAX_ADVANCE_DAYS))) {
            throw new BusinessException(400, "预约日期超出可查询范围");
        }
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(400, "开始与结束时间不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(400, "结束时间必须晚于开始时间");
        }
    }

    private void validateBusinessHours(Venue venue, LocalDate date,
                                       LocalTime startTime, LocalTime endTime) {
        String scheduleType = switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> "WEEKEND";
            default -> "WORKDAY";
        };
        VenueSchedule schedule = venueScheduleService.findByVenueIdAndType(venue.getId(), scheduleType);
        LocalTime opensAt;
        LocalTime closesAt;
        if (schedule != null && schedule.getStartTime() != null && schedule.getEndTime() != null
                && (schedule.getIsActive() == null || schedule.getIsActive() == 1)) {
            opensAt = schedule.getStartTime();
            closesAt = schedule.getEndTime();
        } else {
            LocalTime[] fallback = parseBusinessHours(venue.getBusinessHours());
            if (fallback == null) {
                throw new BusinessException(400, "场馆营业时间未配置");
            }
            opensAt = fallback[0];
            closesAt = fallback[1];
        }
        if (startTime.isBefore(opensAt) || endTime.isAfter(closesAt)) {
            throw new BusinessException(400, "查询时间超出场馆营业时间");
        }
    }

    private LocalTime[] parseBusinessHours(String businessHours) {
        if (businessHours == null || businessHours.isBlank()) {
            return null;
        }
        String[] parts = businessHours.trim().split("\\s*-\\s*");
        if (parts.length != 2) {
            return null;
        }
        try {
            LocalTime opensAt = LocalTime.parse(parts[0], TIME_FORMAT);
            LocalTime closesAt = LocalTime.parse(parts[1], TIME_FORMAT);
            return closesAt.isAfter(opensAt) ? new LocalTime[]{opensAt, closesAt} : null;
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    private int clampLimit(int limit) {
        if (limit <= 0) {
            return 1;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private AgentCourtAvailabilityDTO toDto(Court court, Long venueId, LocalDate date,
                                            LocalTime startTime, LocalTime endTime, boolean available) {
        AgentCourtAvailabilityDTO dto = new AgentCourtAvailabilityDTO();
        dto.setCourtId(court.getId());
        dto.setCourtCode(court.getCourtCode());
        dto.setCourtName(court.getCourtName());
        dto.setVenueId(venueId);
        dto.setBillingType(court.getBillingType());
        dto.setPricePerHour(court.getPricePerHour());
        dto.setAvailable(available);
        dto.setDate(date.toString());
        dto.setStartTime(startTime.format(TIME_FORMAT));
        dto.setEndTime(endTime.format(TIME_FORMAT));
        return dto;
    }
}
