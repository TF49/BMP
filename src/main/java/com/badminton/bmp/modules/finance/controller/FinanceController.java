package com.badminton.bmp.modules.finance.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.mapper.FinanceMapper;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "财务管理模块", description = "财务记录 CRUD、统计、对账状态")
@RestController
@RequestMapping("/api/finance")
public class FinanceController extends BaseController {

    @Autowired
    private FinanceService financeService;

    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private VenueService venueService;

    @Operation(summary = "财务记录列表", description = "分页，支持场馆/业务类型/收支/日期/关键词筛选")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getFinanceList(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "businessType", required = false) String businessType,
            @RequestParam(value = "incomeExpenseType", required = false) Integer incomeExpenseType,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            if (keyword != null && keyword.trim().isEmpty()) keyword = null;

            List<Finance> list = financeService.findAll(venueId, businessType, incomeExpenseType,
                    startDate, endDate, keyword, page, size);
            int total = financeService.count(venueId, businessType, incomeExpenseType,
                    startDate, endDate, keyword);

            Map<String, Object> result = new HashMap<>();
            result.put("data", list);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取财务记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "财务记录详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Finance> getFinanceInfo(@PathVariable("id") Long id) {
        try {
            Finance finance = financeService.findById(id);
            return finance != null ? success(finance) : error("财务记录不存在");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取财务记录信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "新增财务记录")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> addFinance(@Valid @RequestBody Finance finance) {
        try {
            int result = financeService.add(finance);
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", finance.getId());
                data.put("financeNo", finance.getFinanceNo());
                return success(data);
            } else {
                return error("添加财务记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加财务记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新财务记录")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateFinance(@Valid @RequestBody Finance finance) {
        try {
            if (finance.getId() == null) {
                return error("财务记录ID不能为空");
            }

            int result = financeService.update(finance);
            if (result > 0) {
                return success(null);
            } else {
                return error("更新财务记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新财务记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除财务记录", description = "仅会长可操作")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> deleteFinance(@PathVariable("id") Long id) {
        try {
            int result = financeService.deleteById(id);
            if (result > 0) {
                return success(null);
            } else {
                return error("删除财务记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除财务记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "财务统计概览")
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getStatistics(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        try {
            Map<String, Object> stats = financeService.getStatistics(venueId, startDate, endDate);
            return success(stats);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取统计数据时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "财务趋势", description = "折线图数据")
    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getTrendData(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        try {
            Map<String, Object> data = financeService.getTrendData(venueId, startDate, endDate);
            return success(data);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取趋势数据时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "各场馆收入趋势", description = "Dashboard 数据")
    @GetMapping("/venue-trend")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getVenueTrend() {
        try {
            java.time.LocalDate end = java.time.LocalDate.now();
            java.time.LocalDate start28 = end.minusDays(27);
            java.time.LocalDate start7 = end.minusDays(6);

            Long venueFilter = null;
            if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            }

            String startDate = start28.format(java.time.format.DateTimeFormatter.ISO_DATE);
            String endDate = end.format(java.time.format.DateTimeFormatter.ISO_DATE);
            List<Map<String, Object>> rows = financeMapper.sumIncomeByVenueAndDate(venueFilter, startDate, endDate);

            Map<Long, String> venueNames = new HashMap<>();
            Map<Long, Map<java.time.LocalDate, java.math.BigDecimal>> venueDateAmount = new HashMap<>();

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    if (row == null) continue;
                    Long venueId = toLong(row.get("venueId"));
                    if (venueId == null) continue;
                    String venueName = row.get("venueName") != null ? row.get("venueName").toString() : null;
                    venueNames.put(venueId, venueName);

                    java.time.LocalDate d = parseLocalDate(row.get("date"));
                    if (d == null) continue;

                    java.math.BigDecimal amt = java.math.BigDecimal.ZERO;
                    Object amtObj = row.get("amount");
                    if (amtObj != null) {
                        try { amt = new java.math.BigDecimal(amtObj.toString()); } catch (Exception ignored) {}
                    }

                    venueDateAmount.computeIfAbsent(venueId, k -> new HashMap<>()).put(d, amt);
                }
            }

            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Map.Entry<Long, Map<java.time.LocalDate, java.math.BigDecimal>> entry : venueDateAmount.entrySet()) {
                Long venueId = entry.getKey();
                Map<java.time.LocalDate, java.math.BigDecimal> byDate = entry.getValue();

                // week
                List<String> weekCats = new java.util.ArrayList<>();
                List<Integer> weekVals = new java.util.ArrayList<>();
                for (java.time.LocalDate d = start7; !d.isAfter(end); d = d.plusDays(1)) {
                    weekCats.add(weekdayLabel(d));
                    java.math.BigDecimal v = byDate.getOrDefault(d, java.math.BigDecimal.ZERO);
                    weekVals.add(v.setScale(0, java.math.RoundingMode.HALF_UP).intValue());
                }

                // month (4 weeks)
                int[] buckets = new int[]{0, 0, 0, 0};
                int idx = 0;
                for (java.time.LocalDate d = start28; !d.isAfter(end); d = d.plusDays(1)) {
                    int b = Math.min(3, idx / 7);
                    java.math.BigDecimal v = byDate.getOrDefault(d, java.math.BigDecimal.ZERO);
                    buckets[b] += v.setScale(0, java.math.RoundingMode.HALF_UP).intValue();
                    idx++;
                }

                Map<String, Object> week = new HashMap<>();
                week.put("categories", weekCats);
                week.put("values", weekVals);
                Map<String, Object> month = new HashMap<>();
                month.put("categories", java.util.Arrays.asList("第1周", "第2周", "第3周", "第4周"));
                month.put("values", java.util.Arrays.asList(buckets[0], buckets[1], buckets[2], buckets[3]));

                Map<String, Object> data = new HashMap<>();
                data.put("week", week);
                data.put("month", month);

                Map<String, Object> item = new HashMap<>();
                item.put("venueId", venueId);
                item.put("venueName", venueNames.get(venueId));
                item.put("data", data);
                result.add(item);
            }

            return success(result);
        } catch (Exception e) {
            return error("获取场馆收入趋势时发生错误：" + e.getMessage());
        }
    }

    private String weekdayLabel(java.time.LocalDate d) {
        switch (d.getDayOfWeek()) {
            case MONDAY: return "周一";
            case TUESDAY: return "周二";
            case WEDNESDAY: return "周三";
            case THURSDAY: return "周四";
            case FRIDAY: return "周五";
            case SATURDAY: return "周六";
            case SUNDAY: return "周日";
            default: return "";
        }
    }

    private java.time.LocalDate parseLocalDate(Object v) {
        if (v == null) return null;
        if (v instanceof java.time.LocalDate) return (java.time.LocalDate) v;
        if (v instanceof java.sql.Date) return ((java.sql.Date) v).toLocalDate();
        String s = v.toString();
        if (s.length() >= 10) s = s.substring(0, 10);
        try { return java.time.LocalDate.parse(s); } catch (Exception e) { return null; }
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    @Operation(summary = "业务占比", description = "饼图数据")
    @GetMapping("/ratio")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getBusinessTypeRatio(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        try {
            List<Map<String, Object>> data = financeService.getBusinessTypeRatio(venueId, startDate, endDate);
            return success(data);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取业务占比数据时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场馆下拉列表")
    @GetMapping("/venues")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> getVenueList() {
        try {
            List<Venue> venues = venueService.findByVenueNameOrAddress(null, null, null, 1, 1000);
            List<Map<String, Object>> venueList = venues.stream()
                    .map(venue -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", venue.getId());
                        item.put("venueName", venue.getVenueName());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(venueList);
        } catch (Exception e) {
            return error("获取场馆列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "业务类型选项")
    @GetMapping("/businessTypes")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getBusinessTypes() {
        List<Map<String, String>> types = new java.util.ArrayList<>();

        Map<String, String> type1 = new HashMap<>();
        type1.put("value", Finance.TYPE_BOOKING);
        type1.put("label", "场地预约");
        types.add(type1);

        Map<String, String> type2 = new HashMap<>();
        type2.put("value", Finance.TYPE_COURSE);
        type2.put("label", "课程预约");
        types.add(type2);

        Map<String, String> type3 = new HashMap<>();
        type3.put("value", Finance.TYPE_EQUIPMENT);
        type3.put("label", "器材租借");
        types.add(type3);

        Map<String, String> type4 = new HashMap<>();
        type4.put("value", Finance.TYPE_TOURNAMENT);
        type4.put("label", "赛事报名");
        types.add(type4);

        Map<String, String> type4b = new HashMap<>();
        type4b.put("value", Finance.TYPE_STRINGING);
        type4b.put("label", "穿线服务");
        types.add(type4b);

        Map<String, String> type5 = new HashMap<>();
        type5.put("value", Finance.TYPE_RECHARGE);
        type5.put("label", "会员充值");
        types.add(type5);

        Map<String, String> type6 = new HashMap<>();
        type6.put("value", Finance.TYPE_OTHER);
        type6.put("label", "其他收支");
        types.add(type6);

        return success(types);
    }
}
