package com.badminton.bmp.modules.dashboard.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.booking.mapper.BookingMapper;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import com.badminton.bmp.modules.equipment.mapper.EquipmentMapper;
import com.badminton.bmp.modules.finance.mapper.FinanceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 官网公开概览接口
 * 无需认证，直接查询 Mapper 层，不触发 SecurityUtils 角色判断。
 * 仅返回汇总性展示数据，不含用户隐私或财务明细。
 */
@Tag(name = "Site", description = "官网公开统计概览")
@RestController
@RequestMapping("/api/site")
public class SiteController extends BaseController {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    /** income_expense_type = 1 表示收入 */
    private static final int INCOME_TYPE = 1;

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private FinanceMapper financeMapper;

    @Operation(summary = "官网运营概览", description = "公开接口，返回场地利用率、预约成功率、器材可用数及当日收入")
    @GetMapping("/overview")
    public Result<Object> overview() {
        try {
            Map<String, Object> data = new LinkedHashMap<>();

            // -------- 1. 场地利用率 --------
            // 利用率 = (预约中 status=2 + 使用中 status=3) / 总场地数
            List<Map<String, Object>> courtStatusList = courtMapper.countByStatus();
            int courtTotal = courtMapper.countAll();
            int courtReserved = 0;
            int courtInUse = 0;
            for (Map<String, Object> row : courtStatusList) {
                Object statusObj = row.get("status");
                if (statusObj == null) statusObj = row.get("STATUS");
                Object countObj = row.get("count");
                if (countObj == null) countObj = row.get("COUNT");
                if (statusObj == null || countObj == null) continue;
                int statusVal = ((Number) statusObj).intValue();
                int countVal = ((Number) countObj).intValue();
                if (statusVal == 2) courtReserved = countVal;
                else if (statusVal == 3) courtInUse = countVal;
            }
            int utilizationRate = 0;
            if (courtTotal > 0) {
                utilizationRate = (int) Math.round((double) (courtReserved + courtInUse) / courtTotal * 100);
            }
            data.put("courtUtilizationRate", utilizationRate);
            data.put("courtTotal", courtTotal);

            // -------- 2. 预约成功率 & 环比 --------
            // 成功率 = (已支付+进行中+已完成) / (总预约 - 已取消)
            List<Map<String, Object>> bookingStatusList = bookingMapper.countByStatus();
            int bTotal = 0, bCancelled = 0, bPaid = 0, bInProgress = 0, bCompleted = 0;
            for (Map<String, Object> row : bookingStatusList) {
                Object statusObj = row.get("status");
                if (statusObj == null) statusObj = row.get("STATUS");
                Object countObj = row.get("count");
                if (countObj == null) countObj = row.get("COUNT");
                if (statusObj == null || countObj == null) continue;
                int statusVal = ((Number) statusObj).intValue();
                int countVal = ((Number) countObj).intValue();
                bTotal += countVal;
                switch (statusVal) {
                    case 0: bCancelled = countVal; break;
                    case 2: bPaid = countVal; break;
                    case 3: bInProgress = countVal; break;
                    case 4: bCompleted = countVal; break;
                    default: break;
                }
            }
            int effectiveTotal = bTotal - bCancelled;
            double bookingSuccessRate = 0;
            if (effectiveTotal > 0) {
                bookingSuccessRate = BigDecimal.valueOf((double) (bPaid + bInProgress + bCompleted) / effectiveTotal * 100)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            }
            data.put("bookingSuccessRate", bookingSuccessRate);

            // 今日 vs 昨日预约量环比（排除已取消）
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            int todayBookings = bookingMapper.countByBookingDate(null, today);
            int yesterdayBookings = bookingMapper.countByBookingDate(null, yesterday);
            double bookingTrend = 0;
            if (yesterdayBookings > 0) {
                bookingTrend = BigDecimal.valueOf((double) (todayBookings - yesterdayBookings) / yesterdayBookings * 100)
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            } else if (todayBookings > 0) {
                bookingTrend = 100.0;
            }
            data.put("bookingTrend", bookingTrend);

            // -------- 3. 器材可用数 --------
            List<Map<String, Object>> equipStatusList = equipmentMapper.countByStatus();
            int equipTotal = equipmentMapper.countAll();
            int equipNormal = 0; // status=1 为正常/可用
            for (Map<String, Object> row : equipStatusList) {
                Object statusObj = row.get("status");
                if (statusObj == null) statusObj = row.get("STATUS");
                Object countObj = row.get("count");
                if (countObj == null) countObj = row.get("COUNT");
                if (statusObj == null || countObj == null) continue;
                int statusVal = ((Number) statusObj).intValue();
                int countVal = ((Number) countObj).intValue();
                if (statusVal == 1) equipNormal = countVal;
            }
            data.put("equipmentAvailable", equipNormal);
            data.put("equipmentTotal", equipTotal);

            // -------- 4. 当日收入 & 环比 --------
            String todayStr = today.format(ISO);
            String yesterdayStr = yesterday.format(ISO);

            BigDecimal todayIncome = financeMapper.sumByType(null, INCOME_TYPE, todayStr, todayStr);
            if (todayIncome == null) todayIncome = BigDecimal.ZERO;

            BigDecimal yesterdayIncome = financeMapper.sumByType(null, INCOME_TYPE, yesterdayStr, yesterdayStr);
            if (yesterdayIncome == null) yesterdayIncome = BigDecimal.ZERO;

            double incomeTrend = 0;
            if (yesterdayIncome.compareTo(BigDecimal.ZERO) > 0) {
                incomeTrend = todayIncome.subtract(yesterdayIncome)
                        .divide(yesterdayIncome, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP)
                        .doubleValue();
            } else if (todayIncome.compareTo(BigDecimal.ZERO) > 0) {
                incomeTrend = 100.0;
            }
            // 金额格式化为整数（元）
            data.put("todayIncome", todayIncome.setScale(0, RoundingMode.HALF_UP).toPlainString());
            data.put("incomeTrend", incomeTrend);

            return success(data);
        } catch (Exception e) {
            return error("获取运营概览失败：" + e.getMessage());
        }
    }
}
