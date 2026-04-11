package com.badminton.bmp.modules.finance.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.modules.finance.service.FinanceReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "财务对账", description = "全面对账及各业务模块对账")
@RestController
@RequestMapping("/api/finance/reconciliation")
public class FinanceReconciliationController {

    @Autowired
    private FinanceReconciliationService reconciliationService;

    @Operation(summary = "全面对账", description = "仅协会会长可用")
    @GetMapping("/full")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Map<String, Object>> performFullReconciliation() {
        try {
            Map<String, Object> result = reconciliationService.performFullReconciliation();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("对账失败，请稍后重试");
        }
    }

    @Operation(summary = "场地预约对账")
    @GetMapping("/booking")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, Object>> reconcileBookingFinance() {
        try {
            Map<String, Object> result = reconciliationService.reconcileBookingFinance();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("对账失败，请稍后重试");
        }
    }

    @Operation(summary = "课程预约对账")
    @GetMapping("/course")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, Object>> reconcileCourseFinance() {
        try {
            Map<String, Object> result = reconciliationService.reconcileCourseFinance();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("对账失败，请稍后重试");
        }
    }

    @Operation(summary = "器材租借对账")
    @GetMapping("/equipment")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, Object>> reconcileEquipmentFinance() {
        try {
            Map<String, Object> result = reconciliationService.reconcileEquipmentFinance();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("对账失败，请稍后重试");
        }
    }

    @Operation(summary = "赛事报名对账")
    @GetMapping("/tournament")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, Object>> reconcileTournamentFinance() {
        try {
            Map<String, Object> result = reconciliationService.reconcileTournamentFinance();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("对账失败，请稍后重试");
        }
    }

    @Operation(summary = "穿线服务对账")
    @GetMapping("/stringing")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, Object>> reconcileStringingFinance() {
        try {
            Map<String, Object> result = reconciliationService.reconcileStringingFinance();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("对账失败，请稍后重试");
        }
    }

    @Operation(summary = "会员充值对账")
    @GetMapping("/recharge")
    @PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
    public Result<Map<String, Object>> reconcileRechargeFinance() {
        try {
            Map<String, Object> result = reconciliationService.reconcileRechargeFinance();
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("对账失败，请稍后重试");
        }
    }
}
