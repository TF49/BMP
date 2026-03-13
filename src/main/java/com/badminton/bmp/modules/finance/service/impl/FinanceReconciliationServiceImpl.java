package com.badminton.bmp.modules.finance.service.impl;

import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.mapper.FinanceMapper;
import com.badminton.bmp.modules.finance.service.FinanceReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 财务对账服务实现
 */
@Service
public class FinanceReconciliationServiceImpl implements FinanceReconciliationService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FinanceMapper financeMapper;

    @Override
    public Map<String, Object> performFullReconciliation() {
        Map<String, Object> result = new HashMap<>();
        result.put("reconciliationTime", LocalDateTime.now());
        result.put("bookingReconciliation", reconcileBookingFinance());
        result.put("courseReconciliation", reconcileCourseFinance());
        result.put("equipmentReconciliation", reconcileEquipmentFinance());
        result.put("tournamentReconciliation", reconcileTournamentFinance());
        result.put("stringingReconciliation", reconcileStringingFinance());
        result.put("rechargeReconciliation", reconcileRechargeFinance());

        // 计算总体对账结果
        boolean allPassed = true;
        List<String> issues = new ArrayList<>();

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> moduleResult = (Map<String, Object>) entry.getValue();
                if (!(Boolean) moduleResult.getOrDefault("passed", false)) {
                    allPassed = false;
                    issues.add(entry.getKey() + ": " + moduleResult.get("message"));
                }
            }
        }

        result.put("overallPassed", allPassed);
        result.put("issues", issues);

        return result;
    }

    @Override
    public Map<String, Object> reconcileBookingFinance() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 查询所有已支付的场地预约
            String sql = "SELECT COUNT(*) as paid_count, SUM(order_amount) as total_amount " +
                        "FROM biz_booking WHERE payment_status = 1 AND del_flag = 0";
            Map<String, Object> bookingData = jdbcTemplate.queryForMap(sql);

            // 查询对应的财务收入记录
            String financeSql = "SELECT COUNT(*) as finance_count, SUM(amount) as finance_amount " +
                               "FROM biz_finance WHERE business_type = 'BOOKING' AND income_expense_type = 1 " +
                               "AND record_source = 'AUTO' AND del_flag = 0";
            Map<String, Object> financeData = jdbcTemplate.queryForMap(financeSql);

            long paidCount = ((Number) bookingData.get("paid_count")).longValue();
            BigDecimal totalAmount = (BigDecimal) bookingData.get("total_amount");
            if (totalAmount == null) totalAmount = BigDecimal.ZERO;

            long financeCount = ((Number) financeData.get("finance_count")).longValue();
            BigDecimal financeAmount = (BigDecimal) financeData.get("finance_amount");
            if (financeAmount == null) financeAmount = BigDecimal.ZERO;

            result.put("moduleName", "场地预约");
            result.put("paidRecordCount", paidCount);
            result.put("financeRecordCount", financeCount);
            result.put("businessTotalAmount", totalAmount);
            result.put("financeTotalAmount", financeAmount);
            result.put("countMatched", paidCount == financeCount);
            result.put("amountMatched", totalAmount.compareTo(financeAmount) == 0);
            result.put("passed", paidCount == financeCount && totalAmount.compareTo(financeAmount) == 0);

            if (paidCount != financeCount) {
                result.put("message", "记录数量不匹配：业务记录" + paidCount + "条，财务记录" + financeCount + "条");
            } else if (totalAmount.compareTo(financeAmount) != 0) {
                result.put("message", "金额不匹配：业务金额" + totalAmount + "，财务金额" + financeAmount);
            } else {
                result.put("message", "对账通过");
            }

        } catch (Exception e) {
            result.put("passed", false);
            result.put("message", "对账失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> reconcileCourseFinance() {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT COUNT(*) as paid_count, SUM(order_amount) as total_amount " +
                        "FROM biz_course_booking WHERE payment_status = 1 AND del_flag = 0";
            Map<String, Object> bookingData = jdbcTemplate.queryForMap(sql);

            String financeSql = "SELECT COUNT(*) as finance_count, SUM(amount) as finance_amount " +
                               "FROM biz_finance WHERE business_type = 'COURSE' AND income_expense_type = 1 " +
                               "AND record_source = 'AUTO' AND del_flag = 0";
            Map<String, Object> financeData = jdbcTemplate.queryForMap(financeSql);

            long paidCount = ((Number) bookingData.get("paid_count")).longValue();
            BigDecimal totalAmount = (BigDecimal) bookingData.get("total_amount");
            if (totalAmount == null) totalAmount = BigDecimal.ZERO;

            long financeCount = ((Number) financeData.get("finance_count")).longValue();
            BigDecimal financeAmount = (BigDecimal) financeData.get("finance_amount");
            if (financeAmount == null) financeAmount = BigDecimal.ZERO;

            result.put("moduleName", "课程预约");
            result.put("paidRecordCount", paidCount);
            result.put("financeRecordCount", financeCount);
            result.put("businessTotalAmount", totalAmount);
            result.put("financeTotalAmount", financeAmount);
            result.put("countMatched", paidCount == financeCount);
            result.put("amountMatched", totalAmount.compareTo(financeAmount) == 0);
            result.put("passed", paidCount == financeCount && totalAmount.compareTo(financeAmount) == 0);

            if (paidCount != financeCount) {
                result.put("message", "记录数量不匹配：业务记录" + paidCount + "条，财务记录" + financeCount + "条");
            } else if (totalAmount.compareTo(financeAmount) != 0) {
                result.put("message", "金额不匹配：业务金额" + totalAmount + "，财务金额" + financeAmount);
            } else {
                result.put("message", "对账通过");
            }

        } catch (Exception e) {
            result.put("passed", false);
            result.put("message", "对账失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> reconcileEquipmentFinance() {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT COUNT(*) as paid_count, SUM(rental_amount) as total_amount " +
                        "FROM biz_equipment_rental WHERE payment_status = 1 AND del_flag = 0";
            Map<String, Object> rentalData = jdbcTemplate.queryForMap(sql);

            String financeSql = "SELECT COUNT(*) as finance_count, SUM(amount) as finance_amount " +
                               "FROM biz_finance WHERE business_type = 'EQUIPMENT' AND income_expense_type = 1 " +
                               "AND record_source = 'AUTO' AND del_flag = 0";
            Map<String, Object> financeData = jdbcTemplate.queryForMap(financeSql);

            long paidCount = ((Number) rentalData.get("paid_count")).longValue();
            BigDecimal totalAmount = (BigDecimal) rentalData.get("total_amount");
            if (totalAmount == null) totalAmount = BigDecimal.ZERO;

            long financeCount = ((Number) financeData.get("finance_count")).longValue();
            BigDecimal financeAmount = (BigDecimal) financeData.get("finance_amount");
            if (financeAmount == null) financeAmount = BigDecimal.ZERO;

            result.put("moduleName", "器材租借");
            result.put("paidRecordCount", paidCount);
            result.put("financeRecordCount", financeCount);
            result.put("businessTotalAmount", totalAmount);
            result.put("financeTotalAmount", financeAmount);
            result.put("countMatched", paidCount == financeCount);
            result.put("amountMatched", totalAmount.compareTo(financeAmount) == 0);
            result.put("passed", paidCount == financeCount && totalAmount.compareTo(financeAmount) == 0);

            if (paidCount != financeCount) {
                result.put("message", "记录数量不匹配：业务记录" + paidCount + "条，财务记录" + financeCount + "条");
            } else if (totalAmount.compareTo(financeAmount) != 0) {
                result.put("message", "金额不匹配：业务金额" + totalAmount + "，财务金额" + financeAmount);
            } else {
                result.put("message", "对账通过");
            }

        } catch (Exception e) {
            result.put("passed", false);
            result.put("message", "对账失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> reconcileTournamentFinance() {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT COUNT(*) as paid_count, SUM(entry_fee) as total_amount " +
                        "FROM biz_tournament_registration WHERE payment_status = 1 AND del_flag = 0";
            Map<String, Object> registrationData = jdbcTemplate.queryForMap(sql);

            String financeSql = "SELECT COUNT(*) as finance_count, SUM(amount) as finance_amount " +
                               "FROM biz_finance WHERE business_type = 'TOURNAMENT' AND income_expense_type = 1 " +
                               "AND record_source = 'AUTO' AND del_flag = 0";
            Map<String, Object> financeData = jdbcTemplate.queryForMap(financeSql);

            long paidCount = ((Number) registrationData.get("paid_count")).longValue();
            BigDecimal totalAmount = (BigDecimal) registrationData.get("total_amount");
            if (totalAmount == null) totalAmount = BigDecimal.ZERO;

            long financeCount = ((Number) financeData.get("finance_count")).longValue();
            BigDecimal financeAmount = (BigDecimal) financeData.get("finance_amount");
            if (financeAmount == null) financeAmount = BigDecimal.ZERO;

            result.put("moduleName", "赛事报名");
            result.put("paidRecordCount", paidCount);
            result.put("financeRecordCount", financeCount);
            result.put("businessTotalAmount", totalAmount);
            result.put("financeTotalAmount", financeAmount);
            result.put("countMatched", paidCount == financeCount);
            result.put("amountMatched", totalAmount.compareTo(financeAmount) == 0);
            result.put("passed", paidCount == financeCount && totalAmount.compareTo(financeAmount) == 0);

            if (paidCount != financeCount) {
                result.put("message", "记录数量不匹配：业务记录" + paidCount + "条，财务记录" + financeCount + "条");
            } else if (totalAmount.compareTo(financeAmount) != 0) {
                result.put("message", "金额不匹配：业务金额" + totalAmount + "，财务金额" + financeAmount);
            } else {
                result.put("message", "对账通过");
            }

        } catch (Exception e) {
            result.put("passed", false);
            result.put("message", "对账失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> reconcileStringingFinance() {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT COUNT(*) as paid_count, SUM(service_price) as total_amount " +
                        "FROM biz_stringing_service WHERE payment_status = 1 AND del_flag = 0";
            Map<String, Object> serviceData = jdbcTemplate.queryForMap(sql);

            String financeSql = "SELECT COUNT(*) as finance_count, SUM(amount) as finance_amount " +
                               "FROM biz_finance WHERE business_type = 'STRINGING' AND income_expense_type = 1 " +
                               "AND record_source = 'AUTO' AND del_flag = 0";
            Map<String, Object> financeData = jdbcTemplate.queryForMap(financeSql);

            long paidCount = ((Number) serviceData.get("paid_count")).longValue();
            BigDecimal totalAmount = (BigDecimal) serviceData.get("total_amount");
            if (totalAmount == null) totalAmount = BigDecimal.ZERO;

            long financeCount = ((Number) financeData.get("finance_count")).longValue();
            BigDecimal financeAmount = (BigDecimal) financeData.get("finance_amount");
            if (financeAmount == null) financeAmount = BigDecimal.ZERO;

            result.put("moduleName", "穿线服务");
            result.put("paidRecordCount", paidCount);
            result.put("financeRecordCount", financeCount);
            result.put("businessTotalAmount", totalAmount);
            result.put("financeTotalAmount", financeAmount);
            result.put("countMatched", paidCount == financeCount);
            result.put("amountMatched", totalAmount.compareTo(financeAmount) == 0);
            result.put("passed", paidCount == financeCount && totalAmount.compareTo(financeAmount) == 0);

            if (paidCount != financeCount) {
                result.put("message", "记录数量不匹配：业务记录" + paidCount + "条，财务记录" + financeCount + "条");
            } else if (totalAmount.compareTo(financeAmount) != 0) {
                result.put("message", "金额不匹配：业务金额" + totalAmount + "，财务金额" + financeAmount);
            } else {
                result.put("message", "对账通过");
            }

        } catch (Exception e) {
            result.put("passed", false);
            result.put("message", "对账失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> reconcileRechargeFinance() {
        Map<String, Object> result = new HashMap<>();

        try {
            String sql = "SELECT COUNT(*) as recharge_count, SUM(recharge_amount) as total_amount " +
                        "FROM biz_recharge_record WHERE del_flag = 0";
            Map<String, Object> rechargeData = jdbcTemplate.queryForMap(sql);

            String financeSql = "SELECT COUNT(*) as finance_count, SUM(amount) as finance_amount " +
                               "FROM biz_finance WHERE business_type = 'RECHARGE' AND income_expense_type = 1 " +
                               "AND record_source = 'AUTO' AND del_flag = 0";
            Map<String, Object> financeData = jdbcTemplate.queryForMap(financeSql);

            long rechargeCount = ((Number) rechargeData.get("recharge_count")).longValue();
            BigDecimal totalAmount = (BigDecimal) rechargeData.get("total_amount");
            if (totalAmount == null) totalAmount = BigDecimal.ZERO;

            long financeCount = ((Number) financeData.get("finance_count")).longValue();
            BigDecimal financeAmount = (BigDecimal) financeData.get("finance_amount");
            if (financeAmount == null) financeAmount = BigDecimal.ZERO;

            result.put("moduleName", "会员充值");
            result.put("paidRecordCount", rechargeCount);
            result.put("financeRecordCount", financeCount);
            result.put("businessTotalAmount", totalAmount);
            result.put("financeTotalAmount", financeAmount);
            result.put("countMatched", rechargeCount == financeCount);
            result.put("amountMatched", totalAmount.compareTo(financeAmount) == 0);
            result.put("passed", rechargeCount == financeCount && totalAmount.compareTo(financeAmount) == 0);

            if (rechargeCount != financeCount) {
                result.put("message", "记录数量不匹配：业务记录" + rechargeCount + "条，财务记录" + financeCount + "条");
            } else if (totalAmount.compareTo(financeAmount) != 0) {
                result.put("message", "金额不匹配：业务金额" + totalAmount + "，财务金额" + financeAmount);
            } else {
                result.put("message", "对账通过");
            }

        } catch (Exception e) {
            result.put("passed", false);
            result.put("message", "对账失败：" + e.getMessage());
        }

        return result;
    }
}
