package com.badminton.bmp.modules.agent.service.impl;

import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsChartDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsHeatmapDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsMetaDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsOverviewDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsRevenueBreakdownDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsTrendDTO;
import com.badminton.bmp.modules.agent.dto.AgentAnalyticsVenueComparisonDTO;
import com.badminton.bmp.modules.agent.service.AgentAnalyticsToolService;
import com.badminton.bmp.modules.booking.mapper.BookingMapper;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.mapper.VenueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Agent 经营分析 Tool 服务实现类。
 */
@Service
public class AgentAnalyticsToolServiceImpl implements AgentAnalyticsToolService {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private VenueMapper venueMapper;

    @Override
    public AgentAnalyticsOverviewDTO getOverview(String startDate, String endDate, Long venueId) {
        ScopeContext scopeCtx = resolveSecurityScope(venueId);
        DateRange dateRange = resolveDateRange(startDate, endDate);

        Map<String, Object> financeStats = financeService.getStatistics(
                scopeCtx.venueId, dateRange.startStr, dateRange.endStr);

        BigDecimal totalIncome = safeBigDecimal(financeStats.get("totalIncome"));
        BigDecimal totalExpense = safeBigDecimal(financeStats.get("totalExpense"));
        BigDecimal netIncome = safeBigDecimal(financeStats.get("netIncome"));

        int bookingCount = bookingMapper.count(
                scopeCtx.venueId, null, null, null, null,
                dateRange.startDate, dateRange.endDate);

        double utilizationRate = calculateUtilizationRate(scopeCtx.venueId, dateRange.startDate, dateRange.endDate, bookingCount);

        List<AgentAnalyticsOverviewDTO.Kpi> kpis = new ArrayList<>();
        kpis.add(new AgentAnalyticsOverviewDTO.Kpi("revenue", "总收入", totalIncome, "元"));
        kpis.add(new AgentAnalyticsOverviewDTO.Kpi("net_income", "净收益", netIncome, "元"));
        kpis.add(new AgentAnalyticsOverviewDTO.Kpi("booking_volume", "预约量", BigDecimal.valueOf(bookingCount), "单"));
        kpis.add(new AgentAnalyticsOverviewDTO.Kpi("court_utilization", "场地利用率",
                BigDecimal.valueOf(utilizationRate).setScale(2, RoundingMode.HALF_UP), "%"));

        AgentAnalyticsMetaDTO meta = buildMeta(dateRange.startStr, dateRange.endStr, "DAY",
                scopeCtx.level, scopeCtx.venueId, scopeCtx.venueName, getStandardMetrics());

        // 构造概览控制图表（收入 vs 支出卡片/柱状）
        AgentAnalyticsChartDTO overviewChart = new AgentAnalyticsChartDTO();
        overviewChart.setChartType("bar");
        overviewChart.setTitle("经营概览核心指标");
        overviewChart.setCategories(Arrays.asList("总收入", "总支出", "净收益"));
        List<Object> values = Arrays.asList(totalIncome, totalExpense, netIncome);
        overviewChart.getSeries().add(new AgentAnalyticsChartDTO.Series("金额", "元", "revenue", values));
        overviewChart.setEmpty(totalIncome.compareTo(BigDecimal.ZERO) == 0 && totalExpense.compareTo(BigDecimal.ZERO) == 0);
        if (overviewChart.isEmpty()) {
            overviewChart.setEmptyText("当前统计周期内无财务与经营数据");
        }

        AgentAnalyticsOverviewDTO dto = new AgentAnalyticsOverviewDTO();
        dto.setMeta(meta);
        dto.setKpis(kpis);
        dto.setCharts(Collections.singletonList(overviewChart));
        return dto;
    }

    @Override
    public AgentAnalyticsTrendDTO getBookingTrend(String startDate, String endDate, String granularity, Long venueId) {
        ScopeContext scopeCtx = resolveSecurityScope(venueId);
        DateRange dateRange = resolveDateRange(startDate, endDate);
        String gran = (granularity != null && !granularity.isBlank()) ? granularity.toUpperCase() : "DAY";

        List<Map<String, Object>> raw = bookingMapper.countByDate(
                scopeCtx.venueId, dateRange.startDate, dateRange.endDate);

        Map<String, Long> dateCountMap = new LinkedHashMap<>();
        for (LocalDate d = dateRange.startDate; !d.isAfter(dateRange.endDate); d = d.plusDays(1)) {
            dateCountMap.put(d.format(ISO_DATE), 0L);
        }

        long totalBookings = 0;
        if (raw != null) {
            for (Map<String, Object> item : raw) {
                Object dObj = item.get("date");
                Object cObj = item.get("cnt");
                if (dObj != null && cObj != null) {
                    String dStr = dObj.toString();
                    long cnt = ((Number) cObj).longValue();
                    dateCountMap.put(dStr, cnt);
                    totalBookings += cnt;
                }
            }
        }

        List<String> categories = new ArrayList<>(dateCountMap.keySet());
        List<Object> counts = new ArrayList<>(dateCountMap.values());

        AgentAnalyticsChartDTO chart = new AgentAnalyticsChartDTO();
        chart.setChartType("line");
        chart.setTitle(scopeCtx.venueName + " 预约量趋势 (" + dateRange.startStr + " ~ " + dateRange.endStr + ")");
        chart.setCategories(categories);
        chart.getSeries().add(new AgentAnalyticsChartDTO.Series("预约量", "单", "booking_volume", counts));
        chart.setEmpty(totalBookings == 0);
        if (chart.isEmpty()) {
            chart.setEmptyText("该时间范围内暂无预约数据");
        }

        AgentAnalyticsMetaDTO meta = buildMeta(dateRange.startStr, dateRange.endStr, gran,
                scopeCtx.level, scopeCtx.venueId, scopeCtx.venueName, getStandardMetrics());

        List<AgentAnalyticsOverviewDTO.Kpi> kpis = Collections.singletonList(
                new AgentAnalyticsOverviewDTO.Kpi("booking_volume", "区间总预约量", BigDecimal.valueOf(totalBookings), "单")
        );

        List<AgentAnalyticsTrendDTO.TrendPoint> points = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            points.add(new AgentAnalyticsTrendDTO.TrendPoint(categories.get(i), counts.get(i), null));
        }

        return new AgentAnalyticsTrendDTO(meta, kpis, Collections.singletonList(chart), points);
    }

    @Override
    public AgentAnalyticsHeatmapDTO getCourtHeatmap(String startDate, String endDate, Long venueId) {
        ScopeContext scopeCtx = resolveSecurityScope(venueId);
        DateRange dateRange = resolveDateRange(startDate, endDate);

        List<Map<String, Object>> raw = bookingMapper.countByHourAndDate(
                scopeCtx.venueId, dateRange.startDate, dateRange.endDate);

        List<String> hours = Arrays.asList(
                "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
                "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"
        );

        List<String> dates = new ArrayList<>();
        for (LocalDate d = dateRange.startDate; !d.isAfter(dateRange.endDate); d = d.plusDays(1)) {
            dates.add(d.format(ISO_DATE));
        }

        Map<String, Integer> matrix = new HashMap<>();
        int totalHeatCount = 0;

        if (raw != null) {
            for (Map<String, Object> item : raw) {
                Object dObj = item.get("date");
                Object hObj = item.get("hour");
                Object cObj = item.get("cnt");
                if (dObj != null && hObj != null && cObj != null) {
                    String dStr = dObj.toString();
                    int hourInt = ((Number) hObj).intValue();
                    int count = ((Number) cObj).intValue();
                    String key = dStr + "_" + hourInt;
                    matrix.put(key, count);
                    totalHeatCount += count;
                }
            }
        }

        List<AgentAnalyticsHeatmapDTO.HeatmapCell> cells = new ArrayList<>();
        List<Object> seriesData = new ArrayList<>();

        for (int y = 0; y < dates.size(); y++) {
            String dateStr = dates.get(y);
            for (int x = 0; x < hours.size(); x++) {
                int hourInt = 8 + x;
                int count = matrix.getOrDefault(dateStr + "_" + hourInt, 0);
                cells.add(new AgentAnalyticsHeatmapDTO.HeatmapCell(dateStr, hourInt, count, 0.0));
                // ECharts heatmap element: [x, y, value]
                seriesData.add(Arrays.asList(x, y, count));
            }
        }

        AgentAnalyticsChartDTO chart = new AgentAnalyticsChartDTO();
        chart.setChartType("heatmap");
        chart.setTitle(scopeCtx.venueName + " 场地使用时段热力图");
        chart.setCategories(hours);
        chart.setYCategories(dates);
        chart.getSeries().add(new AgentAnalyticsChartDTO.Series("预订热度", "场次", "court_utilization", seriesData));
        chart.setEmpty(totalHeatCount == 0);
        if (chart.isEmpty()) {
            chart.setEmptyText("选定时间范围内无预约热度数据");
        }

        AgentAnalyticsMetaDTO meta = buildMeta(dateRange.startStr, dateRange.endStr, "HOUR",
                scopeCtx.level, scopeCtx.venueId, scopeCtx.venueName, getStandardMetrics());

        return new AgentAnalyticsHeatmapDTO(meta, hours, dates, cells, Collections.singletonList(chart));
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentAnalyticsTrendDTO getFinanceTrend(String startDate, String endDate, Long venueId) {
        ScopeContext scopeCtx = resolveSecurityScope(venueId);
        DateRange dateRange = resolveDateRange(startDate, endDate);

        Map<String, Object> trendDataMap = financeService.getTrendData(
                scopeCtx.venueId, dateRange.startStr, dateRange.endStr);

        List<String> dates = (List<String>) trendDataMap.getOrDefault("dates", Collections.emptyList());
        List<BigDecimal> incomes = (List<BigDecimal>) trendDataMap.getOrDefault("incomes", Collections.emptyList());
        List<BigDecimal> expenses = (List<BigDecimal>) trendDataMap.getOrDefault("expenses", Collections.emptyList());

        if (dates == null) dates = Collections.emptyList();
        if (incomes == null) incomes = Collections.emptyList();
        if (expenses == null) expenses = Collections.emptyList();

        BigDecimal sumIncome = BigDecimal.ZERO;
        BigDecimal sumExpense = BigDecimal.ZERO;

        for (BigDecimal inc : incomes) sumIncome = sumIncome.add(inc != null ? inc : BigDecimal.ZERO);
        for (BigDecimal exp : expenses) sumExpense = sumExpense.add(exp != null ? exp : BigDecimal.ZERO);

        AgentAnalyticsChartDTO chart = new AgentAnalyticsChartDTO();
        chart.setChartType("line");
        chart.setTitle(scopeCtx.venueName + " 财务收支趋势");
        chart.setCategories(dates);

        List<Object> incomeObjects = new ArrayList<>(incomes);
        List<Object> expenseObjects = new ArrayList<>(expenses);

        chart.getSeries().add(new AgentAnalyticsChartDTO.Series("收入", "元", "revenue", incomeObjects));
        chart.getSeries().add(new AgentAnalyticsChartDTO.Series("支出", "元", "net_income", expenseObjects));
        chart.setEmpty(sumIncome.compareTo(BigDecimal.ZERO) == 0 && sumExpense.compareTo(BigDecimal.ZERO) == 0);
        if (chart.isEmpty()) {
            chart.setEmptyText("暂无财务收支趋势数据");
        }

        AgentAnalyticsMetaDTO meta = buildMeta(dateRange.startStr, dateRange.endStr, "DAY",
                scopeCtx.level, scopeCtx.venueId, scopeCtx.venueName, getStandardMetrics());

        List<AgentAnalyticsOverviewDTO.Kpi> kpis = Arrays.asList(
                new AgentAnalyticsOverviewDTO.Kpi("revenue", "区间总收入", sumIncome, "元"),
                new AgentAnalyticsOverviewDTO.Kpi("net_income", "区间净收益", sumIncome.subtract(sumExpense), "元")
        );

        List<AgentAnalyticsTrendDTO.TrendPoint> points = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            BigDecimal inc = i < incomes.size() ? incomes.get(i) : BigDecimal.ZERO;
            BigDecimal exp = i < expenses.size() ? expenses.get(i) : BigDecimal.ZERO;
            points.add(new AgentAnalyticsTrendDTO.TrendPoint(dates.get(i), inc, exp));
        }

        return new AgentAnalyticsTrendDTO(meta, kpis, Collections.singletonList(chart), points);
    }

    @Override
    public AgentAnalyticsRevenueBreakdownDTO getRevenueBreakdown(String startDate, String endDate, Long venueId) {
        ScopeContext scopeCtx = resolveSecurityScope(venueId);
        DateRange dateRange = resolveDateRange(startDate, endDate);

        List<Map<String, Object>> rawList = financeService.getBusinessTypeRatio(
                scopeCtx.venueId, dateRange.startStr, dateRange.endStr);

        BigDecimal totalIncome = BigDecimal.ZERO;
        List<AgentAnalyticsRevenueBreakdownDTO.Item> items = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<Object> seriesData = new ArrayList<>();

        if (rawList != null) {
            for (Map<String, Object> map : rawList) {
                String type = (String) map.get("type");
                String name = (String) map.get("name");
                BigDecimal amount = safeBigDecimal(map.get("amount"));
                double ratio = map.get("ratio") instanceof Number ? ((Number) map.get("ratio")).doubleValue() : 0.0;

                totalIncome = totalIncome.add(amount);
                items.add(new AgentAnalyticsRevenueBreakdownDTO.Item(type, name, amount, ratio));
                categories.add(name);

                Map<String, Object> pieDataPoint = new HashMap<>();
                pieDataPoint.put("name", name);
                pieDataPoint.put("value", amount);
                seriesData.add(pieDataPoint);
            }
        }

        AgentAnalyticsChartDTO chart = new AgentAnalyticsChartDTO();
        chart.setChartType("pie");
        chart.setTitle(scopeCtx.venueName + " 业务收入构成");
        chart.setCategories(categories);
        chart.getSeries().add(new AgentAnalyticsChartDTO.Series("收入占比", "元", "business_ratio", seriesData));
        chart.setEmpty(totalIncome.compareTo(BigDecimal.ZERO) == 0);
        if (chart.isEmpty()) {
            chart.setEmptyText("当前统计周期内无业务收入分类数据");
        }

        AgentAnalyticsMetaDTO meta = buildMeta(dateRange.startStr, dateRange.endStr, "ALL",
                scopeCtx.level, scopeCtx.venueId, scopeCtx.venueName, getStandardMetrics());

        return new AgentAnalyticsRevenueBreakdownDTO(meta, totalIncome, items, Collections.singletonList(chart));
    }

    @Override
    public AgentAnalyticsVenueComparisonDTO getVenueComparison(String startDate, String endDate) {
        // 会长可查看多场馆对比；若为场馆管理者则仅列出其所在场馆
        ScopeContext scopeCtx = resolveSecurityScope(null);
        DateRange dateRange = resolveDateRange(startDate, endDate);

        List<Venue> venuesToCompare = new ArrayList<>();
        if ("ALL".equals(scopeCtx.level)) {
            List<Venue> all = venueMapper.findAllOptions();
            if (all != null) venuesToCompare.addAll(all);
        } else if (scopeCtx.venueId != null) {
            Venue single = venueMapper.findById(scopeCtx.venueId);
            if (single != null) venuesToCompare.add(single);
        }

        List<AgentAnalyticsVenueComparisonDTO.VenueItem> venueItems = new ArrayList<>();
        List<String> venueNames = new ArrayList<>();
        List<Object> bookingCounts = new ArrayList<>();
        List<Object> incomes = new ArrayList<>();

        for (Venue v : venuesToCompare) {
            int bCount = bookingMapper.count(v.getId(), null, null, null, null,
                    dateRange.startDate, dateRange.endDate);
            Map<String, Object> fStats = financeService.getStatistics(v.getId(), dateRange.startStr, dateRange.endStr);
            BigDecimal totalInc = safeBigDecimal(fStats.get("totalIncome"));
            double utilRate = calculateUtilizationRate(v.getId(), dateRange.startDate, dateRange.endDate, bCount);

            String vName = (v.getVenueName() != null && !v.getVenueName().isBlank()) ? v.getVenueName() : "场馆" + v.getId();
            venueItems.add(new AgentAnalyticsVenueComparisonDTO.VenueItem(
                    v.getId(), vName, bCount, bCount * 2.0, utilRate, totalInc));

            venueNames.add(vName);
            bookingCounts.add(bCount);
            incomes.add(totalInc);
        }

        AgentAnalyticsChartDTO chart = new AgentAnalyticsChartDTO();
        chart.setChartType("bar");
        chart.setTitle("多场馆经营横向对比 (" + dateRange.startStr + " ~ " + dateRange.endStr + ")");
        chart.setCategories(venueNames);
        chart.getSeries().add(new AgentAnalyticsChartDTO.Series("预约量", "单", "booking_volume", bookingCounts));
        chart.getSeries().add(new AgentAnalyticsChartDTO.Series("总收入", "元", "revenue", incomes));
        chart.setEmpty(venueItems.isEmpty());
        if (chart.isEmpty()) {
            chart.setEmptyText("暂无可对比的场馆数据");
        }

        AgentAnalyticsMetaDTO meta = buildMeta(dateRange.startStr, dateRange.endStr, "ALL",
                scopeCtx.level, scopeCtx.venueId, scopeCtx.venueName, getStandardMetrics());

        return new AgentAnalyticsVenueComparisonDTO(meta, venueItems, Collections.singletonList(chart));
    }

    private ScopeContext resolveSecurityScope(Long requestedVenueId) {
        if (SecurityUtils.isPresident()) {
            if (requestedVenueId != null) {
                Venue venue = venueMapper.findById(requestedVenueId);
                String vName = (venue != null && venue.getVenueName() != null) ? venue.getVenueName() : "场馆" + requestedVenueId;
                return new ScopeContext("VENUE", requestedVenueId, vName);
            }
            return new ScopeContext("ALL", null, "全部场馆");
        } else if (SecurityUtils.isVenueManager()) {
            // 强约束：场馆管理者忽略客户端/模型传入的 requestedVenueId，必须锁定其所属场馆
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId == null) {
                throw new AccessDeniedException("当前场馆管理者账号未绑定具体场馆");
            }
            Venue venue = venueMapper.findById(currentVenueId);
            String vName = (venue != null && venue.getVenueName() != null) ? venue.getVenueName() : "场馆" + currentVenueId;
            return new ScopeContext("VENUE", currentVenueId, vName);
        } else {
            throw new AccessDeniedException("权限不足，只有管理员或会长可查看经营分析数据");
        }
    }

    private DateRange resolveDateRange(String startDate, String endDate) {
        LocalDate end = (endDate != null && !endDate.isBlank()) ? LocalDate.parse(endDate.trim()) : LocalDate.now();
        LocalDate start = (startDate != null && !startDate.isBlank()) ? LocalDate.parse(startDate.trim()) : end.minusDays(6);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        return new DateRange(start, end);
    }

    private BigDecimal safeBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private double calculateUtilizationRate(Long venueId, LocalDate start, LocalDate end, int bookingCount) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days <= 0) days = 1;
        // 假定开业 14 小时，场地按 8 块标准估算
        int courtCount = 8;
        if (venueId != null) {
            int cnt = courtMapper.count(venueId, null, null);
            if (cnt > 0) courtCount = cnt;
        }
        double totalCapacityHours = days * courtCount * 14.0;
        double estimatedBookedHours = bookingCount * 2.0; // 按每单 2 小时均值
        if (totalCapacityHours <= 0) return 0.0;
        double rate = (estimatedBookedHours / totalCapacityHours) * 100.0;
        return Math.min(100.0, Math.max(0.0, rate));
    }

    private AgentAnalyticsMetaDTO buildMeta(String startDate, String endDate, String granularity,
                                            String scopeLevel, Long venueId, String venueName,
                                            List<AgentAnalyticsMetaDTO.Metric> metrics) {
        String periodDesc = startDate + " 至 " + endDate + " (" + granularity + ")";
        AgentAnalyticsMetaDTO.Period period = new AgentAnalyticsMetaDTO.Period(startDate, endDate, granularity, periodDesc);
        AgentAnalyticsMetaDTO.Scope scope = new AgentAnalyticsMetaDTO.Scope(scopeLevel, venueId, venueName, venueName);
        return new AgentAnalyticsMetaDTO(period, scope, metrics);
    }

    private List<AgentAnalyticsMetaDTO.Metric> getStandardMetrics() {
        return Arrays.asList(
                new AgentAnalyticsMetaDTO.Metric("booking_volume", "预约量", "单/场次", "统计指定时间范围内有效预订的总单数"),
                new AgentAnalyticsMetaDTO.Metric("court_utilization", "场地利用率", "%", "预订时长 / (开放场地数 × 每日营业时长 14小时 × 天数)"),
                new AgentAnalyticsMetaDTO.Metric("revenue", "总收入", "元", "指定时间范围内所有入账财务记录总金额"),
                new AgentAnalyticsMetaDTO.Metric("net_income", "净收益", "元", "指定时间范围内总收入减去总支出"),
                new AgentAnalyticsMetaDTO.Metric("business_ratio", "业务构成", "%", "各项业务收入占总收入的比重")
        );
    }

    private static class ScopeContext {
        final String level;
        final Long venueId;
        final String venueName;

        ScopeContext(String level, Long venueId, String venueName) {
            this.level = level;
            this.venueId = venueId;
            this.venueName = venueName;
        }
    }

    private static class DateRange {
        final LocalDate startDate;
        final LocalDate endDate;
        final String startStr;
        final String endStr;

        DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.startStr = startDate.format(ISO_DATE);
            this.endStr = endDate.format(ISO_DATE);
        }
    }
}
