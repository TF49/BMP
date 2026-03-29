package com.badminton.bmp.modules.dashboard.service.impl;

import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.court.service.CourtService;
import com.badminton.bmp.modules.dashboard.service.DashboardService;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.modules.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private MemberService memberService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private CourtService courtService;
    @Autowired
    private FinanceService financeService;

    @Override
    public Map<String, Object> getSummary(String startDate, String endDate) {
        String start = (startDate != null && !startDate.isBlank()) ? startDate.trim() : LocalDate.now().format(ISO);
        String end = (endDate != null && !endDate.isBlank()) ? endDate.trim() : start;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("member", memberService.getStatistics());
        payload.put("booking", bookingService.getStatistics());
        payload.put("court", courtService.getStatistics());
        if (SecurityUtils.isPresident() || SecurityUtils.isVenueManager()) {
            payload.put("finance", financeService.getStatistics(null, start, end));
        } else {
            payload.put("finance", null);
        }
        return payload;
    }
}
