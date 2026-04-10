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
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问后台汇总数据");
        }

        String start = (startDate != null && !startDate.isBlank()) ? startDate.trim() : LocalDate.now().format(ISO);
        String end = (endDate != null && !endDate.isBlank()) ? endDate.trim() : start;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("member", SecurityUtils.isPresident() ? memberService.getStatistics() : null);
        payload.put("booking", bookingService.getStatistics());
        payload.put("court", courtService.getStatistics());
        payload.put("finance", financeService.getStatistics(null, start, end));
        return payload;
    }
}
