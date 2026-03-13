package com.badminton.bmp.modules.activity.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.booking.mapper.BookingMapper;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.mapper.FinanceMapper;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "活动/运营", description = "近期动态、Dashboard 数据")
@RestController
@RequestMapping("/api/activity")
public class ActivityController extends BaseController {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private FinanceMapper financeMapper;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Operation(summary = "近期动态", description = "limit 默认 10")
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> recent(@RequestParam(required = false, defaultValue = "10") int limit) {
        try {
            int safeLimit = Math.max(1, Math.min(limit, 50));

            Long venueFilter = null;
            if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            }

            List<Map<String, Object>> items = new ArrayList<>();

            // 最近会员注册
            List<Map<String, Object>> members = memberMapper.findRecentMembers(safeLimit);
            if (members != null) {
                for (Map<String, Object> m : members) {
                    Long id = toLong(m.get("id"));
                    String name = m.get("member_name") != null ? m.get("member_name").toString() : "会员";
                    LocalDateTime t = toLdt(m.get("register_time"));
                    if (t == null) t = toLdt(m.get("create_time"));
                    items.add(activity("member-" + id, "member", "新会员" + name + "注册成功", t));
                }
            }

            // 最近预订
            List<Map<String, Object>> bookings = bookingMapper.findRecentBookings(venueFilter, safeLimit);
            if (bookings != null) {
                for (Map<String, Object> b : bookings) {
                    Long id = toLong(b.get("id"));
                    String courtName = b.get("court_name") != null ? b.get("court_name").toString() : null;
                    String courtCode = b.get("court_code") != null ? b.get("court_code").toString() : null;
                    String court = (courtName != null && !courtName.isEmpty()) ? courtName : (courtCode != null ? courtCode : "场地");
                    LocalDateTime t = toLdt(b.get("create_time"));
                    items.add(activity("booking-" + id, "booking", "场地 " + court + " 预订成功", t));
                }
            }

            // 最近财务
            List<Map<String, Object>> finances = financeMapper.findRecentFinances(venueFilter, safeLimit);
            if (finances != null) {
                for (Map<String, Object> f : finances) {
                    Long id = toLong(f.get("id"));
                    BigDecimal amount = toBd(f.get("amount"));
                    Object typeObj = f.get("income_expense_type");
                    // 兼容不同数据库/驱动下 income_expense_type 可能返回 Boolean 或 Number 的情况
                    Integer type = null;
                    if (typeObj instanceof Number) {
                        type = ((Number) typeObj).intValue();
                    } else if (typeObj instanceof Boolean) {
                        type = ((Boolean) typeObj) ? Finance.INCOME : Finance.EXPENSE;
                    } else if (typeObj != null) {
                        try {
                            type = Integer.parseInt(typeObj.toString());
                        } catch (NumberFormatException e) {
                            type = null;
                        }
                    }
                    // 使用常量判断：1=收入，0=支出
                    boolean income = type != null && type.equals(Finance.INCOME);
                    LocalDateTime t = toLdt(f.get("create_time"));
                    String desc = income ? ("新增收入 ¥" + amount.stripTrailingZeros().toPlainString())
                            : ("新增支出 ¥" + amount.stripTrailingZeros().toPlainString());
                    items.add(activity("finance-" + id, "finance", desc, t));
                }
            }

            // 排序 + 截断
            items.sort(Comparator.comparing((Map<String, Object> x) -> (LocalDateTime) x.get("_ts"),
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            if (items.size() > safeLimit) {
                items = items.subList(0, safeLimit);
            }
            // 去除排序字段
            for (Map<String, Object> it : items) {
                it.remove("_ts");
            }

            return success(items);
        } catch (Exception e) {
            return error("获取最近活动失败：" + e.getMessage());
        }
    }

    private Map<String, Object> activity(String id, String type, String desc, LocalDateTime t) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("type", type);
        m.put("description", desc);
        m.put("time", t != null ? t.format(TIME_FMT) : "--:--");
        m.put("_ts", t);
        return m;
    }

    private LocalDateTime toLdt(Object v) {
        if (v == null) return null;
        if (v instanceof LocalDateTime) return (LocalDateTime) v;
        if (v instanceof java.sql.Timestamp) return ((java.sql.Timestamp) v).toLocalDateTime();
        if (v instanceof java.util.Date) return new java.sql.Timestamp(((java.util.Date) v).getTime()).toLocalDateTime();
        String s = v.toString();
        try {
            // 兼容 2026-02-18T12:34:56 / 2026-02-18 12:34:56
            if (s.contains("T")) return LocalDateTime.parse(s);
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private BigDecimal toBd(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}

