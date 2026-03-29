package com.badminton.bmp.modules.dashboard.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Dashboard 聚合统计")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController extends BaseController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "Dashboard 统计汇总", description = "会员、预约、场地、财务（总裁/场馆经理可见财务）一次返回")
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> summary(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        try {
            return success(dashboardService.getSummary(startDate, endDate));
        } catch (Exception e) {
            return error("获取Dashboard汇总失败：" + e.getMessage());
        }
    }
}
