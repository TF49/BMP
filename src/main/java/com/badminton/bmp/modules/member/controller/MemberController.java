package com.badminton.bmp.modules.member.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberService;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.common.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "会员管理模块", description = "会员 CRUD、统计、消费记录")
@RestController
@RequestMapping("/api/member")
public class MemberController extends BaseController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberMapper memberMapper;

    @Operation(summary = "会员列表", description = "支持姓名/手机/类型/状态筛选与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> list(@RequestParam(value = "memberName", required = false) String memberName,
                               @RequestParam(value = "phone", required = false) String phone,
                               @RequestParam(value = "memberId", required = false) Long memberId,
                               @RequestParam(value = "memberType", required = false) String memberType,
                               @RequestParam(value = "status", required = false) Integer status,
                               @RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            List<Member> data = memberService.findByConditions(memberName, phone, memberId, memberType, status, page, size);
            int total = memberService.countByConditions(memberName, phone, memberId, memberType, status);
            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (Exception e) {
            return error("获取会员列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "会员详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> info(@PathVariable("id") Long id) {
        Member member = memberService.findById(id);
        if (member == null || (member.getDelFlag() != null && member.getDelFlag() == 1)) {
            return error("会员不存在");
        }
        return success(member);
    }

    @Operation(summary = "当前登录用户的会员信息", description = "普通用户获取自己的会员信息（含余额），需登录")
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCurrentMember() {
        User current = SecurityUtils.getCurrentUser();
        if (current == null || current.getId() == null) {
            return error("未登录或Token无效");
        }
        Member member = memberService.findByUserId(current.getId());
        if (member == null) {
            return error("未找到关联会员信息，请联系管理员");
        }
        if (member.getDelFlag() != null && member.getDelFlag() == 1) {
            return error("会员已失效");
        }
        return success(member);
    }

    @Operation(summary = "新增会员")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> add(@Valid @RequestBody Member member) {
        try {
            member.setCreateTime(LocalDateTime.now());
            member.setUpdateTime(LocalDateTime.now());
            Member saved = memberService.add(member);
            return success(saved);
        } catch (Exception e) {
            return error("创建会员失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新会员")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> update(@Valid @RequestBody Member member) {
        if (member.getId() == null) {
            return error("会员ID不能为空");
        }
        try {
            memberService.update(member);
            return success(null);
        } catch (Exception e) {
            return error("更新会员失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除会员", description = "逻辑删除")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> delete(@PathVariable("id") Long id) {
        try {
            memberService.deleteById(id);
            return success(null);
        } catch (Exception e) {
            return error("删除会员失败：" + e.getMessage());
        }
    }

    @Operation(summary = "会员统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> statistics(@RequestParam(value = "period", required = false) String period) {
        try {
            Map<String, Object> stats = memberService.getStatistics();

            if (period != null && !period.trim().isEmpty()) {
                java.time.LocalDate end = java.time.LocalDate.now();
                java.time.LocalDate start = "month".equalsIgnoreCase(period) ? end.minusDays(29) : end.minusDays(6);

                // 按日期新增
                List<Map<String, Object>> rows = memberMapper.countRegisteredByDate(start, end);
                Map<java.time.LocalDate, Integer> byDate = new HashMap<>();
                if (rows != null) {
                    for (Map<String, Object> r : rows) {
                        if (r == null) continue;
                        Object dObj = r.get("date");
                        Object cObj = r.get("cnt");
                        if (dObj == null || cObj == null) continue;
                        java.time.LocalDate d = (dObj instanceof java.time.LocalDate)
                                ? (java.time.LocalDate) dObj
                                : java.time.LocalDate.parse(dObj.toString().substring(0, 10));
                        int cnt = (cObj instanceof Number) ? ((Number) cObj).intValue() : Integer.parseInt(cObj.toString());
                        byDate.put(d, cnt);
                    }
                }

                // 累计：start 之前的累计 + 逐日新增累加
                int baseTotal = memberMapper.countRegisteredBeforeDate(start);

                List<String> categories = new java.util.ArrayList<>();
                List<Integer> newMembers = new java.util.ArrayList<>();
                List<Integer> totalMembers = new java.util.ArrayList<>();
                int running = baseTotal;

                for (java.time.LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                    int add = byDate.getOrDefault(d, 0);
                    running += add;
                    categories.add(label(period, d));
                    newMembers.add(add);
                    totalMembers.add(running);
                }

                Map<String, Object> trends = new HashMap<>();
                trends.put("categories", categories);
                trends.put("newMembers", newMembers);
                trends.put("totalMembers", totalMembers);
                stats.put("trends", trends);

                int sumNew = newMembers.stream().mapToInt(Integer::intValue).sum();
                if ("month".equalsIgnoreCase(period)) {
                    stats.put("newThisMonth", sumNew);
                    // Dashboard 会员流失图：近12个月每月新增与流失（流失暂无数据填0）
                    java.time.LocalDate start12 = end.minusMonths(11).withDayOfMonth(1);
                    List<Map<String, Object>> rows12 = memberMapper.countRegisteredByDate(start12, end);
                    Map<java.time.LocalDate, Integer> byDate12 = new HashMap<>();
                    if (rows12 != null) {
                        for (Map<String, Object> r : rows12) {
                            if (r == null) continue;
                            Object dObj = r.get("date");
                            Object cObj = r.get("cnt");
                            if (dObj == null || cObj == null) continue;
                            java.time.LocalDate d = (dObj instanceof java.time.LocalDate)
                                ? (java.time.LocalDate) dObj
                                : java.time.LocalDate.parse(dObj.toString().substring(0, 10));
                            int cnt = (cObj instanceof Number) ? ((Number) cObj).intValue() : Integer.parseInt(cObj.toString());
                            byDate12.put(d, cnt);
                        }
                    }
                    List<Map<String, Object>> monthlyChurn = new java.util.ArrayList<>();
                    for (int i = 11; i >= 0; i--) {
                        java.time.LocalDate monthEnd = end.minusMonths(i);
                        java.time.LocalDate monthStart = monthEnd.withDayOfMonth(1);
                        int sum = 0;
                        for (java.time.LocalDate d = monthStart; !d.isAfter(monthEnd); d = d.plusDays(1)) {
                            sum += byDate12.getOrDefault(d, 0);
                        }
                        String monthLabel = (12 - i) + "月";
                        Map<String, Object> item = new HashMap<>();
                        item.put("month", monthLabel);
                        item.put("label", monthLabel);
                        item.put("newMembers", sum);
                        item.put("new", sum);
                        item.put("churnMembers", 0);
                        item.put("churn", 0);
                        monthlyChurn.add(item);
                    }
                    stats.put("monthlyChurn", monthlyChurn);
                } else {
                    stats.put("newThisWeek", sumNew);
                }
                stats.put("totalMembers", running);
            }

            return success(stats);
        } catch (Exception e) {
            return error("获取统计信息失败：" + e.getMessage());
        }
    }

    private String label(String period, java.time.LocalDate d) {
        if ("month".equalsIgnoreCase(period)) {
            return d.getDayOfMonth() + "日";
        }
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

    /**
     * 会员分布（Dashboard 饼图）
     */
    @Operation(summary = "会员分布")
    @GetMapping("/distribution")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> distribution() {
        try {
            return success(memberService.getDistribution());
        } catch (Exception e) {
            return error("获取会员分布失败：" + e.getMessage());
        }
    }

    @Operation(summary = "会员漏斗", description = "Dashboard 漏斗图")
    @GetMapping("/funnel")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> funnel() {
        try {
            return success(memberService.getFunnel());
        } catch (Exception e) {
            return error("获取会员漏斗失败：" + e.getMessage());
        }
    }

    @Operation(summary = "即将到期会员", description = "Dashboard 预警，days 默认 30")
    @GetMapping("/expiring")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> expiring(@RequestParam(value = "days", defaultValue = "30") int days) {
        try {
            List<Map<String, Object>> list = memberService.getExpiringMembers(days);
            return success(list);
        } catch (Exception e) {
            return error("获取即将到期会员失败：" + e.getMessage());
        }
    }

    @Operation(summary = "会员来源分布", description = "Dashboard 饼图")
    @GetMapping("/source")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> source() {
        try {
            return success(memberService.getSource());
        } catch (Exception e) {
            return error("获取会员来源分布失败：" + e.getMessage());
        }
    }

    @Operation(summary = "会员消费记录", description = "分页查询")
    @GetMapping("/{id}/consume-records")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> consumeRecords(@PathVariable("id") Long id,
                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Map<String, Object> data = memberService.getConsumeRecords(id, page, size);
            Map<String, Object> result = new HashMap<>();
            result.put("data", data.get("data"));
            result.put("total", data.get("total"));
            result.put("page", page);
            result.put("size", size);
            int total = (data.get("total") instanceof Number) ? ((Number) data.get("total")).intValue() : 0;
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (Exception e) {
            return error("获取消费记录失败：" + e.getMessage());
        }
    }
}
