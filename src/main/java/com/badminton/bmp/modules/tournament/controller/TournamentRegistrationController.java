package com.badminton.bmp.modules.tournament.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.service.MemberService;
import com.badminton.bmp.modules.tournament.entity.Tournament;
import com.badminton.bmp.modules.tournament.entity.TournamentRegistration;
import com.badminton.bmp.modules.tournament.service.TournamentRegistrationService;
import com.badminton.bmp.modules.tournament.service.TournamentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "赛事报名模块", description = "赛事报名 CRUD、支付、统计")
@RestController
@RequestMapping("/api/tournament/registration")
public class TournamentRegistrationController extends BaseController {
    @Autowired
    private TournamentRegistrationService tournamentRegistrationService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private MemberService memberService;

    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    @Operation(summary = "赛事报名列表", description = "支持赛事/会员/状态/单号/时间筛选与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllRegistrations(
            @RequestParam(value = "tournamentId", required = false) Long tournamentId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "registrationNo", required = false) String registrationNo,
            @RequestParam(value = "memberKeyword", required = false) String memberKeyword,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            // 清理参数：去除空字符串和空格
            if (registrationNo != null && registrationNo.trim().isEmpty()) {
                registrationNo = null;
            }
            if (memberKeyword != null && memberKeyword.trim().isEmpty()) {
                memberKeyword = null;
            }
            LocalDateTime start = null;
            LocalDateTime end = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                if (startTime != null && !startTime.trim().isEmpty()) {
                    start = LocalDateTime.parse(startTime.trim(), formatter);
                }
                if (endTime != null && !endTime.trim().isEmpty()) {
                    end = LocalDateTime.parse(endTime.trim(), formatter);
                }
            } catch (DateTimeParseException e) {
                return error("时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }

            List<TournamentRegistration> registrations =
                    tournamentRegistrationService.findAll(tournamentId, memberId, status,
                            registrationNo, memberKeyword, start, end, page, size);
            int total = tournamentRegistrationService.count(tournamentId, memberId, status,
                    registrationNo, memberKeyword, start, end);
            Map<String, Object> result = new HashMap<>();
            result.put("data", registrations);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取赛事报名记录时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "赛事报名详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<TournamentRegistration> getRegistrationInfo(@PathVariable("id") Long id) {
        try {
            TournamentRegistration registration = tournamentRegistrationService.findById(id);
            return registration != null ? success(registration) : error("报名记录不存在");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取报名记录信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 添加赛事报名记录
     * @param registration 报名记录对象
     * @return 添加结果
     */
    @Operation(summary = "新增赛事报名")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> addRegistration(@Valid @RequestBody TournamentRegistration registration) {
        try {

            // 验证必填字段（普通用户可不传会员ID，由后端按当前用户补全）
            if (registration.getMemberId() == null && isAdmin()) {
                return error("会员ID不能为空");
            }

            // 添加报名记录
            int result = tournamentRegistrationService.add(registration);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", registration.getId());
                data.put("registrationNo", registration.getRegistrationNo());
                return success(data);
            } else {
                return error("添加报名记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加报名记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新赛事报名记录
     * @param registration 报名记录对象
     * @return 更新结果
     */
    @Operation(summary = "更新赛事报名")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> updateRegistration(@Valid @RequestBody TournamentRegistration registration) {
        try {

            if (registration.getId() == null) {
                return error("报名记录ID不能为空");
            }
            
            // 更新报名记录
            int result = tournamentRegistrationService.update(registration);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("报名记录更新失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新报名记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除赛事报名记录（需要ADMIN权限）
     * @param id 报名记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除赛事报名")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteRegistration(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除报名记录
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除报名记录
            int result = tournamentRegistrationService.deleteById(id);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("删除报名记录失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除报名记录时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新赛事报名状态
     * @param id 报名记录ID
     * @param status 状态值（0-已取消，1-待支付，2-已支付，3-已参赛，4-已退赛）
     * @return 更新结果
     */
    @Operation(summary = "更新赛事报名状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER','USER','MEMBER')")
    public Result<Object> updateRegistrationStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {

            if (id == null) {
                return error("报名记录ID不能为空");
            }
            
            // 验证状态值是否有效
            if (status == null || status < 0 || status > 4) {
                return error("状态值无效，必须是0（已取消）、1（待支付）、2（已支付）、3（已参赛）或4（已退赛）");
            }
            
            // 更新报名状态
            int result = tournamentRegistrationService.updateStatus(id, status);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("更新报名状态失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新报名状态时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "赛事报名统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getRegistrationStatistics() {
        try {
            return success(tournamentRegistrationService.getStatistics());
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 处理支付（需要ADMIN权限）
     * @param registrationId 报名ID
     * @param paymentMethod 支付方式（CASH/ALIPAY/WECHAT/BALANCE）
     * @return 处理结果
     */
    @Operation(summary = "赛事报名支付")
    @PostMapping("/payment")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processPayment(@RequestParam("registrationId") Long registrationId, @RequestParam("paymentMethod") String paymentMethod) {
        try {
            // 权限验证：仅管理员可处理支付
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (registrationId == null) {
                return error("报名ID不能为空");
            }
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return error("支付方式不能为空");
            }

            // 验证支付方式
            if (!paymentMethod.equals("CASH") && !paymentMethod.equals("ALIPAY") &&
                !paymentMethod.equals("WECHAT") && !paymentMethod.equals("BALANCE")) {
                return error("支付方式无效，必须是CASH、ALIPAY、WECHAT或BALANCE");
            }

            // 处理支付
            int result = tournamentRegistrationService.processPayment(registrationId, paymentMethod);

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

    /**
     * 处理退款（需要ADMIN权限）
     * @param registrationId 报名ID
     * @return 处理结果
     */
    @Operation(summary = "赛事报名退款")
    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> processRefund(@RequestParam("registrationId") Long registrationId) {
        try {
            // 权限验证：仅管理员可处理退款
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (registrationId == null) {
                return error("报名ID不能为空");
            }

            // 处理退款
            int result = tournamentRegistrationService.processRefund(registrationId);

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

    /**
     * 获取赛事下拉列表（供选择赛事使用）
     * @return 赛事列表（未取消的赛事均可展示，提交时校验仅允许对「报名中」的赛事报名）
     */
    @Operation(summary = "赛事下拉列表")
    @GetMapping("/tournaments")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getTournamentList() {
        try {
            // 不按状态筛选，返回所有未删除且未取消的赛事，避免下拉为空；新增报名时再校验赛事为「报名中」
            List<Tournament> tournaments = tournamentService.findAll(null, null, null, null, null, null, 1, 200);
            List<Map<String, Object>> tournamentList = tournaments.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus() != 0)
                    .map(tournament -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", tournament.getId());
                        item.put("tournamentName", tournament.getTournamentName());
                        item.put("tournamentType", tournament.getTournamentType());
                        item.put("tournamentStart", tournament.getTournamentStart());
                        item.put("status", tournament.getStatus());
                        item.put("entryFee", tournament.getEntryFee());
                        item.put("venueId", tournament.getVenueId());
                        item.put("venueName", tournament.getVenueName());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(tournamentList);
        } catch (Exception e) {
            return error("获取赛事列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取会员下拉列表（供选择会员使用，支持姓名关键词筛选）
     * 与课程预约模块一致：keyword 只传姓名参数，否则后端 AND 条件会导致无结果
     */
    @Operation(summary = "会员下拉列表")
    @GetMapping("/members")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getMemberList(@RequestParam(value = "keyword", required = false) String keyword) {
        try {
            String nameOrPhone = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
            List<Member> members = memberService.findByConditions(nameOrPhone, null, null, null, null, 1, 1000);
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
