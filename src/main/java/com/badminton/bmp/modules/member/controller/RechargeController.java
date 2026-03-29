package com.badminton.bmp.modules.member.controller;

import com.badminton.bmp.common.JwtUtils;
import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.member.entity.RechargeRecord;
import com.badminton.bmp.modules.member.service.RechargeService;
import com.badminton.bmp.modules.member.service.MemberService;
import com.badminton.bmp.modules.member.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "充值管理模块", description = "充值记录、会员充值、统计")
@RestController
@RequestMapping("/api/recharge")
public class RechargeController extends BaseController {

    @Autowired
    private RechargeService rechargeService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private MemberService memberService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    /**
     * 从请求头中获取当前用户ID
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            return jwtUtils.getUserIdFromToken(token);
        }
        return null;
    }

    @Operation(summary = "用户自助充值")
    @PostMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> userRecharge(HttpServletRequest request, @RequestBody RechargeBody rechargeBody) {
        try {
            // 1. 获取当前用户ID
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return error("未登录或Token无效");
            }

            // 2. 验证参数
            if (rechargeBody.getAmount() == null || rechargeBody.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return error("充值金额必须大于0");
            }
            if (rechargeBody.getMethod() == null || rechargeBody.getMethod().trim().isEmpty()) {
                return error("充值方式不能为空");
            }

            // 3. 执行充值
            RechargeRecord record = rechargeService.userRecharge(
                    userId,
                    rechargeBody.getAmount(),
                    rechargeBody.getMethod()
            );

            // 4. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("rechargeRecord", record);
            Member member = memberService.findById(record.getMemberId());
            String message = "充值成功";
            if (record.getIsUpgraded() != null && record.getIsUpgraded() == 1) {
                message = "充值成功，恭喜您已升级为会员！";
            }
            if (record.getIsLevelUpgraded() != null && record.getIsLevelUpgraded() == 1 && member != null && member.getMemberLevel() != null) {
                message += " 会员等级已提升至 Lv" + member.getMemberLevel() + "！";
            }
            result.put("message", message.trim());
            if (member != null) {
                result.put("memberLevel", member.getMemberLevel());
                result.put("totalRecharge", member.getTotalRecharge());
            }

            return success(result);
        } catch (RuntimeException e) {
            // 捕获业务异常，直接返回错误信息（已包含详细说明）
            return error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他异常，返回通用错误信息
            String errorMsg = "充值失败：" + e.getMessage();
            // 如果是数据库相关异常，提供更友好的提示
            if (e.getMessage() != null && e.getMessage().contains("SQL")) {
                errorMsg = "充值失败：数据库操作异常，请稍后重试或联系管理员";
            }
            return error(errorMsg);
        }
    }

    @Operation(summary = "管理员为会员充值")
    @PostMapping("/admin")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> adminRecharge(HttpServletRequest request, @RequestBody AdminRechargeBody adminRechargeBody) {
        try {
            // 1. 检查管理员权限
            if (!isAdmin()) {
                return error("无权限执行此操作");
            }

            // 2. 获取当前管理员ID
            Long operatorId = getCurrentUserId(request);
            if (operatorId == null) {
                return error("未登录或Token无效");
            }

            // 3. 验证参数
            if (adminRechargeBody.getMemberId() == null) {
                return error("会员ID不能为空");
            }
            if (adminRechargeBody.getAmount() == null || adminRechargeBody.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return error("充值金额必须大于0");
            }
            if (adminRechargeBody.getMethod() == null || adminRechargeBody.getMethod().trim().isEmpty()) {
                return error("充值方式不能为空");
            }

            // 4. 执行充值
            RechargeRecord record = rechargeService.adminRecharge(
                    adminRechargeBody.getMemberId(),
                    adminRechargeBody.getAmount(),
                    adminRechargeBody.getMethod(),
                    operatorId,
                    adminRechargeBody.getRemark()
            );

            // 5. 返回结果（含当前会员等级与累计充值，便于前端即时更新）
            Map<String, Object> result = new HashMap<>();
            result.put("rechargeRecord", record);
            Member member = memberService.findById(record.getMemberId());
            String message = "充值成功";
            if (record.getIsUpgraded() != null && record.getIsUpgraded() == 1) {
                message = "充值成功，会员已自动升级！";
            }
            if (record.getIsLevelUpgraded() != null && record.getIsLevelUpgraded() == 1 && member != null && member.getMemberLevel() != null) {
                message += " 会员等级已提升至 Lv" + member.getMemberLevel() + "！";
            }
            result.put("message", message.trim());
            if (member != null) {
                result.put("memberLevel", member.getMemberLevel());
                result.put("totalRecharge", member.getTotalRecharge());
            }

            return success(result);
        } catch (Exception e) {
            return error("充值失败：" + e.getMessage());
        }
    }

    @Operation(summary = "充值记录列表", description = "管理员可查全部并筛选，普通用户仅本人")
    @GetMapping("/records")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getRechargeRecords(HttpServletRequest request,
                                             @RequestParam(value = "page", defaultValue = "1") int page,
                                             @RequestParam(value = "size", defaultValue = "10") int size,
                                             @RequestParam(value = "memberKeyword", required = false) String memberKeyword,
                                             @RequestParam(value = "startTime", required = false) String startTime,
                                             @RequestParam(value = "endTime", required = false) String endTime) {
        try {
            // 1. 获取当前用户ID
            Long userId = getCurrentUserId(request);
            if (userId == null) {
                return error("未登录或Token无效");
            }

            // 2. 如果是管理员，返回所有充值记录（支持筛选）；否则返回当前用户的充值记录
            List<RechargeRecord> records;
            int total;

            if (isAdmin()) {
                // 管理员：支持按会员和时间段筛选
                if (memberKeyword != null || startTime != null || endTime != null) {
                    records = rechargeService.getRechargeRecordsWithFilters(memberKeyword, startTime, endTime, page, size);
                    total = rechargeService.countRechargeRecordsWithFilters(memberKeyword, startTime, endTime);
                } else {
                    records = rechargeService.getAllRechargeRecords(page, size);
                    total = rechargeService.countAllRechargeRecords();
                }
            } else {
                // 普通用户：只能查看自己的充值记录
                // 注意：普通用户传入的memberKeyword参数会被忽略，只返回当前用户的记录
                Member member = memberService.findByUserId(userId);
                if (member == null) {
                    return error("未找到关联会员，无法查询充值记录。请先完成会员信息绑定或联系管理员处理");
                }
                records = rechargeService.getRechargeRecordsByMemberId(member.getId(), page, size);
                total = rechargeService.countRechargeRecordsByMemberId(member.getId());
            }

            // 3. 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", records);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (Exception e) {
            return error("查询充值记录失败：" + e.getMessage());
        }
    }

    @Operation(summary = "按会员ID查充值记录", description = "管理员使用")
    @GetMapping("/records/{memberId}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getRechargeRecordsByMemberId(@PathVariable("memberId") Long memberId,
                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            // 1. 检查管理员权限
            if (!isAdmin()) {
                return error("无权限执行此操作");
            }

            // 2. 查询充值记录
            List<RechargeRecord> records = rechargeService.getRechargeRecordsByMemberId(memberId, page, size);
            int total = rechargeService.countRechargeRecordsByMemberId(memberId);

            // 3. 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", records);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (Exception e) {
            return error("查询充值记录失败：" + e.getMessage());
        }
    }
}

/**
 * 用户充值请求体
 */
class RechargeBody {
    private BigDecimal amount;
    private String method; // CASH-现金，ALIPAY-支付宝，WECHAT-微信，BANK-银行转账

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}

/**
 * 管理员充值请求体
 */
class AdminRechargeBody {
    private Long memberId;
    private BigDecimal amount;
    private String method; // CASH-现金，ALIPAY-支付宝，WECHAT-微信，BANK-银行转账
    private String remark;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
