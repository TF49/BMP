package com.badminton.bmp.modules.dashboard.service;

import java.util.Map;

/**
 * Dashboard 聚合统计（复用各模块 Service，减少前端多次往返）。
 */
public interface DashboardService {

    /**
     * 返回 member、booking、court、finance（无财务权限时 finance 为 null）。
     *
     * @param startDate 财务统计开始日 yyyy-MM-dd，空则默认当天
     * @param endDate   财务统计结束日 yyyy-MM-dd，空则与 start 相同
     */
    Map<String, Object> getSummary(String startDate, String endDate);
}
