package com.badminton.bmp.modules.booking.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.mapper.BookingMapper;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.service.CourtService;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.service.MemberService;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.badminton.bmp.common.util.SecurityUtils;

@Tag(name = "场地预约模块", description = "预约 CRUD、支付、退款、统计与运营数据")
@RestController
@RequestMapping("/api/booking")
public class BookingController extends BaseController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingMapper bookingMapper;
    @Autowired
    private CourtService courtService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private VenueService venueService;

    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    @Operation(summary = "预约列表", description = "支持场馆/会员/场地/状态/时间范围筛选与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllBookings(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "courtId", required = false) Long courtId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "memberKeyword", required = false, name = "memberKeyword") String memberKeyword,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;

            if (memberKeyword != null && memberKeyword.trim().isEmpty()) {
                memberKeyword = null;
            }

            // 前端传的是完整日期时间范围，这里按日期维度做过滤（预约日期在范围内）
            LocalDate startDate = null;
            LocalDate endDate = null;
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                if (startTime != null && !startTime.trim().isEmpty()) {
                    startDate = LocalDateTime.parse(startTime, dtf).toLocalDate();
                }
            } catch (Exception ignored) {
                // 格式异常时忽略时间过滤，避免直接报错
            }
            try {
                if (endTime != null && !endTime.trim().isEmpty()) {
                    endDate = LocalDateTime.parse(endTime, dtf).toLocalDate();
                }
            } catch (Exception ignored) {
                // 同上
            }

            List<Booking> bookings = bookingService.findAll(
                    venueId,
                    memberId,
                    courtId,
                    status,
                    memberKeyword,
                    startDate,
                    endDate,
                    page,
                    size);
            int total = bookingService.count(
                    venueId,
                    memberId,
                    courtId,
                    status,
                    memberKeyword,
                    startDate,
                    endDate);
            Map<String, Object> result = new HashMap<>();
            result.put("data", bookings);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取预约记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "预约详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Booking> getBookingInfo(@PathVariable("id") Long id) {
        try {
            Booking booking = bookingService.findById(id);
            return booking != null ? success(booking) : error("预约记录不存在");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取预约记录信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 添加预约记录
     * @param booking 预约记录对象
     * @return 添加结果
     */
    @Operation(summary = "新增预约")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> addBooking(@Valid @RequestBody Booking booking) {
        try {

            // 添加预约记录
            int result = bookingService.add(booking);

            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", booking.getId());
                data.put("bookingNo", booking.getBookingNo());
                return success(data);
            } else {
                return error("添加预约记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加预约记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新预约记录
     * @param booking 预约记录对象
     * @return 更新结果
     */
    @Operation(summary = "更新预约")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> updateBooking(@Valid @RequestBody Booking booking) {
        try {

            if (booking.getId() == null) {
                return error("预约记录ID不能为空");
            }

            // 更新预约记录
            int result = bookingService.update(booking);

            if (result > 0) {
                return success(null);
            } else {
                return error("预约记录更新失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新预约记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除预约记录
     * @param id 预约记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除预约")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> deleteBooking(@PathVariable("id") Long id) {
        try {

            // 删除预约记录
            int result = bookingService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除预约记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除预约记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新预约状态
     * @param id 预约记录ID
     * @param status 状态值（0-已取消，1-待支付，2-已支付，3-进行中，4-已完成）
     * @return 更新结果
     */
    @Operation(summary = "更新预约状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> updateBookingStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {

            if (id == null) {
                return error("预约记录ID不能为空");
            }

            // 验证状态值是否有效
            if (status == null || status < 0 || status > 4) {
                return error("状态值无效，必须是0（已取消）、1（待支付）、2（已支付）、3（进行中）或4（已完成）");
            }

            // 更新预约状态
            int result = bookingService.updateStatus(id, status);

            if (result > 0) {
                return success(null);
            } else {
                return error("更新预约状态失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新预约状态时发生错误：" + e.getMessage());
        }
    }

    /**
     * 处理支付（需要ADMIN权限）
     * @param bookingId 预约ID
     * @param paymentMethod 支付方式（仅支持 BALANCE）
     * @return 处理结果
     */
    @Operation(summary = "预约支付")
    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processPayment(@RequestParam("bookingId") Long bookingId, @RequestParam("paymentMethod") String paymentMethod) {
        try {
            // 权限验证：仅管理员可处理支付
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (bookingId == null) {
                return error("预约ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }

            if (!"BALANCE".equals(paymentMethod)) {
                return error("业务订单仅支持余额支付");
            }

            // 处理支付
            int result = bookingService.processPayment(bookingId, paymentMethod);

            if (result > 0) {
                return success(null);
            } else {
                return error("支付处理失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("支付处理时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "普通用户支付本人预约")
    @PostMapping("/member/payment")
    @PreAuthorize("hasAnyRole('USER','MEMBER','PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processMemberPayment(@RequestParam("bookingId") Long bookingId,
                                               @RequestParam("paymentMethod") String paymentMethod) {
        try {
            if (bookingId == null) {
                return error("预约ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }
            if (!"BALANCE".equals(paymentMethod)) {
                return error("业务订单仅支持余额支付");
            }

            if (isAdmin()) {
                int result = bookingService.processPayment(bookingId, paymentMethod);
                return result > 0 ? success(null) : error("支付处理失败");
            }

            User current = SecurityUtils.getCurrentUser();
            if (current == null || current.getId() == null) {
                return error("未登录或Token无效");
            }

            int result = bookingService.processMemberPayment(bookingId, paymentMethod, current.getId());
            return result > 0 ? success(null) : error("支付处理失败");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("支付处理时发生错误：" + e.getMessage());
        }
    }

    /**
     * 处理退款（需要ADMIN权限）
     * @param bookingId 预约ID
     * @return 处理结果
     */
    @Operation(summary = "预约退款")
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processRefund(@RequestParam("bookingId") Long bookingId) {
        try {
            // 权限验证：仅管理员可处理退款
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (bookingId == null) {
                return error("预约ID不能为空");
            }

            // 处理退款
            int result = bookingService.processRefund(bookingId);

            if (result > 0) {
                return success(null);
            } else {
                return error("退款处理失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("退款处理时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "预约统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getBookingStatistics() {
        try {
            return success(bookingService.getStatistics());
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 运营待办统计（Dashboard 待办总览：待确认预订、未付款订单、待处理事项）
     */
    @Operation(summary = "待办运营数据")
    @GetMapping("/operation-todo")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getOperationTodo() {
        try {
            Map<String, Object> stats = bookingService.getStatistics();
            Map<String, Object> todo = new HashMap<>();
            todo.put("pendingBookings", stats.get("pending") != null ? stats.get("pending") : 0);
            todo.put("pending", stats.get("pending") != null ? stats.get("pending") : 0);
            todo.put("unpaidOrders", stats.get("unpaidOrders") != null ? stats.get("unpaidOrders") : 0);
            todo.put("unpaid", 0);
            todo.put("pendingIssues", stats.get("pendingIssues") != null ? stats.get("pendingIssues") : 0);
            todo.put("issues", 0);
            return success(todo);
        } catch (Exception e) {
            return error("获取运营待办统计时发生错误：" + e.getMessage());
        }
    }

    /**
     * 预订趋势（week/month）
     * 返回：{ categories:[], values:[] }
     */
    @Operation(summary = "预约趋势")
    @GetMapping("/trend")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getBookingTrend(@RequestParam(value = "period", defaultValue = "week") String period) {
        try {
            LocalDate end = LocalDate.now();
            LocalDate start = "month".equalsIgnoreCase(period) ? end.minusDays(27) : end.minusDays(6);

            Long venueFilter = null;
            if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            }

            List<Map<String, Object>> rows = bookingMapper.countByDate(venueFilter, start, end);
            Map<LocalDate, Integer> byDate = new java.util.HashMap<>();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    if (row == null) continue;
                    Object dObj = row.get("date");
                    Object cObj = row.get("cnt");
                    if (dObj == null || cObj == null) continue;
                    LocalDate d = (dObj instanceof LocalDate) ? (LocalDate) dObj : LocalDate.parse(dObj.toString());
                    int cnt = (cObj instanceof Number) ? ((Number) cObj).intValue() : Integer.parseInt(cObj.toString());
                    byDate.put(d, cnt);
                }
            }

            if (!"month".equalsIgnoreCase(period)) {
                List<String> categories = new java.util.ArrayList<>();
                List<Integer> values = new java.util.ArrayList<>();
                for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                    categories.add(weekdayLabel(d));
                    values.add(byDate.getOrDefault(d, 0));
                }
                Map<String, Object> data = new HashMap<>();
                data.put("categories", categories);
                data.put("values", values);
                return success(data);
            }

            int[] buckets = new int[]{0, 0, 0, 0};
            int idx = 0;
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                int b = Math.min(3, idx / 7);
                buckets[b] += byDate.getOrDefault(d, 0);
                idx++;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("categories", java.util.Arrays.asList("第1周", "第2周", "第3周", "第4周"));
            data.put("values", java.util.Arrays.asList(buckets[0], buckets[1], buckets[2], buckets[3]));
            return success(data);
        } catch (Exception e) {
            return error("获取预订趋势失败：" + e.getMessage());
        }
    }

    /**
     * 预订热力图数据（按星期几 + 小时聚合）
     * period=week 最近7天，period=month 最近28天
     * 返回：heatmapData: [{ day: 0-6 周一至周日, hour: 8-21, count }]
     * 前端热力图 y 轴为周一..周日，x 轴为 08:00..21:00，day 对应 y 索引，hour 对应 x 索引（hour-8）
     */
    @Operation(summary = "预约热力图")
    @GetMapping("/heatmap")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getBookingHeatmap(@RequestParam(value = "period", defaultValue = "week") String period) {
        try {
            LocalDate end = LocalDate.now();
            LocalDate start = "month".equalsIgnoreCase(period) ? end.minusDays(27) : end.minusDays(6);

            Long venueFilter = null;
            if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            }

            List<Map<String, Object>> rows = bookingMapper.countByHourAndDate(venueFilter, start, end);
            // 按 (dayOfWeek 0=周一, hour 8-21) 聚合多天的数据
            int[][] grid = new int[7][14]; // 7 days, 14 hours (8-21)
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    if (row == null) continue;
                    Object dObj = row.get("date");
                    Object hObj = row.get("hour");
                    Object cObj = row.get("cnt");
                    if (dObj == null || hObj == null || cObj == null) continue;
                    LocalDate d = (dObj instanceof LocalDate) ? (LocalDate) dObj : LocalDate.parse(dObj.toString());
                    int hour = (hObj instanceof Number) ? ((Number) hObj).intValue() : Integer.parseInt(hObj.toString());
                    int cnt = (cObj instanceof Number) ? ((Number) cObj).intValue() : Integer.parseInt(cObj.toString());
                    if (hour < 8 || hour > 21) continue;
                    int dayIndex = d.getDayOfWeek().getValue() - 1; // 0=周一, 6=周日
                    int hourIndex = hour - 8;
                    grid[dayIndex][hourIndex] += cnt;
                }
            }

            List<Map<String, Object>> heatmapData = new java.util.ArrayList<>();
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                for (int hourIndex = 0; hourIndex < 14; hourIndex++) {
                    int count = grid[dayIndex][hourIndex];
                    Map<String, Object> point = new HashMap<>();
                    point.put("day", dayIndex);
                    point.put("hour", hourIndex + 8);
                    point.put("count", count);
                    heatmapData.add(point);
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("heatmapData", heatmapData);
            return success(data);
        } catch (Exception e) {
            return error("获取预订热力图失败：" + e.getMessage());
        }
    }

    /**
     * 各场馆预订趋势（Dashboard）
     * 返回：[{ venueId, venueName, data: { week:{categories,values}, month:{categories,values} } }]
     */
    @Operation(summary = "场馆预约趋势")
    @GetMapping("/venue-trend")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueBookingTrend() {
        try {
            LocalDate end = LocalDate.now();
            LocalDate start28 = end.minusDays(27);
            LocalDate start7 = end.minusDays(6);

            Long venueFilter = null;
            if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
                venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
            }

            List<Map<String, Object>> rows = bookingMapper.countByVenueAndDate(venueFilter, start28, end);
            Map<Long, String> venueNames = new HashMap<>();
            Map<Long, Map<LocalDate, Integer>> venueDateCounts = new HashMap<>();

            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    if (row == null) continue;
                    Long venueId = toLong(row.get("venueId"));
                    if (venueId == null) continue;
                    String venueName = row.get("venueName") != null ? row.get("venueName").toString() : null;
                    venueNames.put(venueId, venueName);
                    LocalDate d = (row.get("date") instanceof LocalDate)
                            ? (LocalDate) row.get("date")
                            : LocalDate.parse(row.get("date").toString());
                    int cnt = (row.get("cnt") instanceof Number)
                            ? ((Number) row.get("cnt")).intValue()
                            : Integer.parseInt(row.get("cnt").toString());
                    venueDateCounts.computeIfAbsent(venueId, k -> new HashMap<>()).put(d, cnt);
                }
            }

            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Map.Entry<Long, Map<LocalDate, Integer>> entry : venueDateCounts.entrySet()) {
                Long venueId = entry.getKey();
                Map<LocalDate, Integer> byDate = entry.getValue();

                // week
                List<String> weekCats = new java.util.ArrayList<>();
                List<Integer> weekVals = new java.util.ArrayList<>();
                for (LocalDate d = start7; !d.isAfter(end); d = d.plusDays(1)) {
                    weekCats.add(weekdayLabel(d));
                    weekVals.add(byDate.getOrDefault(d, 0));
                }

                // month (4 weeks)
                int[] buckets = new int[]{0, 0, 0, 0};
                int idx = 0;
                for (LocalDate d = start28; !d.isAfter(end); d = d.plusDays(1)) {
                    int b = Math.min(3, idx / 7);
                    buckets[b] += byDate.getOrDefault(d, 0);
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
            return error("获取场馆预订趋势失败：" + e.getMessage());
        }
    }

    private String weekdayLabel(LocalDate d) {
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

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    /**
     * 获取指定场地当前时刻的占用情况（普通用户可见，脱敏后的姓名列表）
     */
    @Operation(summary = "当前占用率")
    @GetMapping("/occupancy/current")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCurrentOccupancy(@RequestParam("courtId") Long courtId) {
        try {
            if (courtId == null) {
                return error("场地ID不能为空");
            }
            List<CourtBookingUserDTO> users = bookingService.getCurrentOccupancy(courtId);
            Map<String, Object> data = new HashMap<>();
            data.put("count", users != null ? users.size() : 0);
            data.put("users", users);
            return success(data);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取当前使用情况时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取指定场地在给定日期和时间段内的占用情况（普通用户可见，脱敏后的姓名列表）
     */
    @Operation(summary = "时间范围内占用率")
    @GetMapping("/occupancy/range")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getRangeOccupancy(@RequestParam("courtId") Long courtId,
                                            @RequestParam("bookingDate") String bookingDate,
                                            @RequestParam("startTime") String startTime,
                                            @RequestParam("endTime") String endTime) {
        try {
            if (courtId == null) {
                return error("场地ID不能为空");
            }
            if (bookingDate == null || bookingDate.trim().isEmpty()) {
                return error("预约日期不能为空");
            }
            if (startTime == null || startTime.trim().isEmpty()
                    || endTime == null || endTime.trim().isEmpty()) {
                return error("开始时间和结束时间不能为空");
            }

            LocalDate date = LocalDate.parse(bookingDate);
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime start = LocalTime.parse(startTime, tf);
            LocalTime end = LocalTime.parse(endTime, tf);

            if (!end.isAfter(start)) {
                return error("结束时间必须晚于开始时间");
            }

            List<CourtBookingUserDTO> users = bookingService.getRangeOccupancy(courtId, date, start, end);
            Map<String, Object> data = new HashMap<>();
            data.put("count", users != null ? users.size() : 0);
            data.put("users", users);
            return success(data);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取所选时段使用情况时发生错误：" + e.getMessage());
        }
    }

    /**
     * 按场地 + 日期 + 时间段统计当前预约人数（排除已取消）
     * 用于前端展示“当前时段已有多少人预约”
     */
    @Operation(summary = "预约数量统计")
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> countBookingsForTimeRange(@RequestParam("courtId") Long courtId,
                                                   @RequestParam("bookingDate") String bookingDate,
                                                   @RequestParam("startTime") String startTime,
                                                   @RequestParam("endTime") String endTime) {
        try {
            if (courtId == null) {
                return error("场地ID不能为空");
            }
            if (bookingDate == null || bookingDate.trim().isEmpty()) {
                return error("预约日期不能为空");
            }
            if (startTime == null || startTime.trim().isEmpty()
                    || endTime == null || endTime.trim().isEmpty()) {
                return error("开始时间和结束时间不能为空");
            }

            LocalDate date = LocalDate.parse(bookingDate);
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime start = LocalTime.parse(startTime, tf);
            LocalTime end = LocalTime.parse(endTime, tf);

            if (!end.isAfter(start)) {
                return error("结束时间必须晚于开始时间");
            }

            int count = bookingService.countBookingsForTimeRange(courtId, date, start, end);
            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            return success(data);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("统计当前时段预约人数时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取场馆下拉列表（供预约管理页面选择场馆使用）
     * @return 场馆列表（仅包含ID和名称、状态）
     */
    @Operation(summary = "场馆下拉列表")
    @GetMapping("/venues")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueList() {
        try {
            List<Venue> venues = venueService.findAll();
            List<Map<String, Object>> venueList = venues.stream()
                    .map(venue -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", venue.getId());
                        item.put("venueName", venue.getVenueName());
                        item.put("status", venue.getStatus());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(venueList);
        } catch (Exception e) {
            return error("获取场馆列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取场地下拉列表（供选择场地使用）
     * @return 场地列表（仅包含ID和名称，只返回正常状态的场地）
     */
    @Operation(summary = "场地下拉列表")
    @GetMapping("/courts")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCourtList(@RequestParam(value = "venueId", required = false) Long venueId) {
        try {
            // 返回指定场馆下的所有场地（除逻辑删除外）；维护中场地由后端 add/update 时校验拒绝
            List<Court> courts = courtService.findAll(venueId, null, null, 1, 200);
            List<Map<String, Object>> courtList = courts.stream()
                    .map(court -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", court.getId());
                        item.put("courtName", court.getCourtName());
                        item.put("courtCode", court.getCourtCode());
                        item.put("venueId", court.getVenueId());
                        item.put("status", court.getStatus());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(courtList);
        } catch (Exception e) {
            return error("获取场地列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取会员下拉列表（供选择会员使用）
     * @return 会员列表（仅包含ID和姓名）
     */
    @Operation(summary = "会员下拉列表")
    @GetMapping("/members")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getMemberList(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            String nameOrPhone = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            // 仅按姓名或手机号模糊匹配，避免姓名和手机号同时匹配导致过于严格
            // 这里将关键字传入姓名，手机号留空，SQL 为 (member_name LIKE '%keyword%')
            List<Member> members = memberService.findByConditions(nameOrPhone, null, null, null, 1, 1, 1000);
            List<Map<String, Object>> memberList = members.stream()
                    .map(member -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", member.getId());
                        item.put("memberName", member.getMemberName());
                        item.put("phone", member.getPhone());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(memberList);
        } catch (Exception e) {
            return error("获取会员列表时发生错误：" + e.getMessage());
        }
    }
}
