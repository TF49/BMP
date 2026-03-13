package com.badminton.bmp.modules.finance.service.impl;

import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.mapper.FinanceMapper;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.modules.finance.service.FinanceAuditService;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 财务Service实现类
 */
@Service
public class FinanceServiceImpl implements FinanceService {

    private static final Logger log = LoggerFactory.getLogger(FinanceServiceImpl.class);

    @Autowired
    private FinanceMapper financeMapper;

    @Autowired
    private FinanceAuditService financeAuditService;

    @Override
    public Finance findById(Long id) {
        Finance finance = financeMapper.findById(id);
        if (finance == null) {
            return null;
        }

        // 数据范围过滤
        if (SecurityUtils.isPresident()) {
            return finance;
        } else if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId != null && currentVenueId.equals(finance.getVenueId())) {
                return finance;
            }
            throw new AccessDeniedException("权限不足，拒绝访问");
        } else {
            // 普通用户无权访问财务记录
            throw new AccessDeniedException("权限不足，拒绝访问");
        }
    }

    @Override
    public List<Finance> findAll(Long venueId, String businessType, Integer incomeExpenseType,
                                 String startDate, String endDate, String keyword, int page, int size) {
        // 验证分页参数
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        int offset = (page - 1) * size;

        // 数据范围过滤
        Long venueFilter = venueId;
        if (SecurityUtils.isPresident()) {
            // 会长可以查看所有数据，使用传入的venueId
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理者只能查看自己场馆的数据
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户无权访问
            throw new AccessDeniedException("权限不足，拒绝访问");
        }

        return financeMapper.findAll(venueFilter, businessType, incomeExpenseType,
                startDate, endDate, keyword, offset, size);
    }

    @Override
    public int count(Long venueId, String businessType, Integer incomeExpenseType,
                     String startDate, String endDate, String keyword) {
        // 数据范围过滤
        Long venueFilter = venueId;
        if (SecurityUtils.isPresident()) {
            // 会长可以查看所有数据
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            throw new AccessDeniedException("权限不足，拒绝访问");
        }

        return financeMapper.count(venueFilter, businessType, incomeExpenseType,
                startDate, endDate, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(Finance finance) {
        // 权限校验
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new BusinessException("权限不足，只有管理员可以添加财务记录");
        }

        // 场馆管理者只能为自己场馆添加记录
        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (finance.getVenueId() != null && !currentVenueId.equals(finance.getVenueId())) {
                throw new BusinessException("权限不足，只能为自己场馆添加财务记录");
            }
            finance.setVenueId(currentVenueId);
        }

        // 手动添加的财务记录限制为"其他收支"类型，防止与业务数据不一致
        if (finance.getBusinessType() == null || finance.getBusinessType().trim().isEmpty()) {
            finance.setBusinessType(Finance.TYPE_OTHER);
        } else if (!Finance.TYPE_OTHER.equals(finance.getBusinessType())) {
            throw new BusinessException("手动添加的财务记录只能使用\"其他收支\"类型，业务相关的财务记录由系统自动生成");
        }

        // 生成财务单号
        if (finance.getFinanceNo() == null || finance.getFinanceNo().trim().isEmpty()) {
            finance.setFinanceNo(generateFinanceNo());
        }

        // 验证单号唯一性
        if (financeMapper.existsByFinanceNo(finance.getFinanceNo(), null)) {
            throw new BusinessException("财务单号已存在");
        }

        // 设置记录来源为手动添加
        finance.setRecordSource(Finance.SOURCE_MANUAL);

        // 设置操作人信息
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            finance.setOperator(currentUser.getUsername());
            finance.setOperatorId(currentUser.getId());
        }

        // 设置创建时间
        finance.setCreateTime(LocalDateTime.now());

        int result = financeMapper.insert(finance);

        // 记录审计日志
        try {
            financeAuditService.logCreate(finance);
        } catch (Exception e) {
            // 审计日志失败不影响主业务
            log.error("记录财务审计日志失败: financeId={}", finance.getId(), e);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(Finance finance) {
        Finance existing = financeMapper.findById(finance.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("财务记录不存在");
        }

        // 权限校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (!currentVenueId.equals(existing.getVenueId())) {
                    throw new BusinessException("权限不足，只能修改自己场馆的财务记录");
                }
            } else {
                throw new BusinessException("权限不足");
            }
        }

        // 自动生成的财务记录不允许修改关键字段
        if (Finance.SOURCE_AUTO.equals(existing.getRecordSource())) {
            throw new BusinessException("自动生成的财务记录不允许修改，如需调整请联系系统管理员");
        }

        // 金额校验：涉及资金，不允许改为非正数
        if (finance.getAmount() != null && finance.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }

        // 场馆管理员不能修改记录的所属场馆，防止越权
        if (SecurityUtils.isVenueManager()) {
            finance.setVenueId(existing.getVenueId());
        }

        // 更新时记录当前操作人（审计用）
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            finance.setLastModifier(currentUser.getUsername());
            finance.setLastModifierId(currentUser.getId());
        }

        // 设置更新时间
        finance.setUpdateTime(LocalDateTime.now());

        int result = financeMapper.update(finance);

        // 记录审计日志
        try {
            Finance updatedFinance = financeMapper.findById(finance.getId());
            financeAuditService.logUpdate(existing, updatedFinance);
        } catch (Exception e) {
            // 审计日志失败不影响主业务
            log.error("记录财务审计日志失败: financeId={}", finance.getId(), e);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        Finance existing = financeMapper.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("财务记录不存在");
        }

        // 只有会长可以删除财务记录
        if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足，只有协会会长可以删除财务记录");
        }

        // 记录审计日志（删除前记录）
        try {
            financeAuditService.logDelete(existing);
        } catch (Exception e) {
            // 审计日志失败不影响主业务
            log.error("记录财务审计日志失败: financeId={}", id, e);
        }

        return financeMapper.deleteById(id);
    }

    @Override
    public synchronized String generateFinanceNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "FN" + dateStr;
        int count = financeMapper.getMaxSequenceToday(prefix);
        return prefix + String.format("%03d", count + 1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromBusiness(String businessType, Long businessId, BigDecimal amount,
                                   Integer incomeExpenseType, String paymentMethod, Long venueId, String remark) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("业务生成财务记录时金额必须大于0");
        }
        Finance finance = new Finance();
        finance.setFinanceNo(generateFinanceNo());
        finance.setBusinessType(businessType);
        finance.setBusinessId(businessId);
        finance.setIncomeExpenseType(incomeExpenseType);
        finance.setAmount(amount);
        finance.setPaymentMethod(paymentMethod);
        finance.setVenueId(venueId);
        finance.setRemark(remark);
        finance.setCreateTime(LocalDateTime.now());

        // 设置记录来源为自动生成
        finance.setRecordSource(Finance.SOURCE_AUTO);

        // 设置操作人信息
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            finance.setOperator(currentUser.getUsername());
            finance.setOperatorId(currentUser.getId());
        }

        financeMapper.insert(finance);
    }

    @Override
    public Map<String, Object> getStatistics(Long venueId, String startDate, String endDate) {
        // 数据范围过滤
        Long venueFilter = venueId;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }

        Map<String, Object> stats = new HashMap<>();

        // 总收入和总支出（空值安全：部分数据库/驱动在无记录时可能返回 null）
        BigDecimal totalIncome = financeMapper.sumByType(venueFilter, Finance.INCOME, startDate, endDate);
        BigDecimal totalExpense = financeMapper.sumByType(venueFilter, Finance.EXPENSE, startDate, endDate);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        BigDecimal netIncome = totalIncome.subtract(totalExpense);

        stats.put("totalIncome", totalIncome);
        stats.put("totalExpense", totalExpense);
        stats.put("netIncome", netIncome);

        // 本月收支
        String monthStart = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);
        String monthEnd = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        BigDecimal monthIncome = financeMapper.sumByType(venueFilter, Finance.INCOME, monthStart, monthEnd);
        BigDecimal monthExpense = financeMapper.sumByType(venueFilter, Finance.EXPENSE, monthStart, monthEnd);
        if (monthIncome == null) monthIncome = BigDecimal.ZERO;
        if (monthExpense == null) monthExpense = BigDecimal.ZERO;

        stats.put("monthIncome", monthIncome);
        stats.put("monthExpense", monthExpense);

        // 上月收支（用于计算环比）
        LocalDate lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());
        BigDecimal lastMonthIncome = financeMapper.sumByType(venueFilter, Finance.INCOME,
                lastMonthStart.format(DateTimeFormatter.ISO_DATE),
                lastMonthEnd.format(DateTimeFormatter.ISO_DATE));
        BigDecimal lastMonthExpense = financeMapper.sumByType(venueFilter, Finance.EXPENSE,
                lastMonthStart.format(DateTimeFormatter.ISO_DATE),
                lastMonthEnd.format(DateTimeFormatter.ISO_DATE));
        if (lastMonthIncome == null) lastMonthIncome = BigDecimal.ZERO;
        if (lastMonthExpense == null) lastMonthExpense = BigDecimal.ZERO;
        stats.put("lastMonthIncome", lastMonthIncome);
        stats.put("lastMonthExpense", lastMonthExpense);

        // 计算环比变化百分比
        double incomeChange = 0;
        double expenseChange = 0;
        if (lastMonthIncome.compareTo(BigDecimal.ZERO) > 0) {
            incomeChange = monthIncome.subtract(lastMonthIncome)
                    .divide(lastMonthIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }
        if (lastMonthExpense.compareTo(BigDecimal.ZERO) > 0) {
            expenseChange = monthExpense.subtract(lastMonthExpense)
                    .divide(lastMonthExpense, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        stats.put("incomeChange", incomeChange);
        stats.put("expenseChange", expenseChange);

        // 记录总数
        stats.put("total", financeMapper.count(venueFilter, null, null, startDate, endDate, null));

        return stats;
    }

    @Override
    public Map<String, Object> getTrendData(Long venueId, String startDate, String endDate) {
        // 数据范围过滤
        Long venueFilter = venueId;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }

        List<Map<String, Object>> rawData = financeMapper.sumByDate(venueFilter, startDate, endDate);

        // 整理数据为前端需要的格式
        Map<String, BigDecimal> incomeMap = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseMap = new LinkedHashMap<>();

        for (Map<String, Object> item : rawData) {
            Object dateObj = item.get("date");
            if (dateObj == null) continue;
            String date = dateObj.toString();

            // 兼容不同数据库/驱动下 income_expense_type 可能返回 Boolean 或 Number 的情况
            Object typeObj = item.get("type");
            int type;
            if (typeObj instanceof Number) {
                type = ((Number) typeObj).intValue();
            } else if (typeObj instanceof Boolean) {
                type = ((Boolean) typeObj) ? 1 : 0;
            } else {
                type = Integer.parseInt(String.valueOf(typeObj));
            }

            Object amountObj = item.get("amount");
            if (amountObj == null) continue;
            BigDecimal amount = new BigDecimal(amountObj.toString());

            if (type == Finance.INCOME) {
                incomeMap.put(date, amount);
            } else {
                expenseMap.put(date, amount);
            }
        }

        // 确保所有日期都有数据
        Set<String> allDates = new TreeSet<>();
        allDates.addAll(incomeMap.keySet());
        allDates.addAll(expenseMap.keySet());

        List<String> dates = new ArrayList<>(allDates);
        List<BigDecimal> incomes = new ArrayList<>();
        List<BigDecimal> expenses = new ArrayList<>();

        for (String date : dates) {
            incomes.add(incomeMap.getOrDefault(date, BigDecimal.ZERO));
            expenses.add(expenseMap.getOrDefault(date, BigDecimal.ZERO));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("incomes", incomes);
        result.put("expenses", expenses);

        return result;
    }

    @Override
    public List<Map<String, Object>> getBusinessTypeRatio(Long venueId, String startDate, String endDate) {
        // 数据范围过滤
        Long venueFilter = venueId;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }

        List<Map<String, Object>> rawData = financeMapper.sumByBusinessType(venueFilter, startDate, endDate);

        // 计算总金额（空值安全）
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : rawData) {
            Object amt = item.get("amount");
            if (amt != null) total = total.add(new BigDecimal(amt.toString()));
        }

        // 添加名称和占比
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : rawData) {
            Object amtObj = item.get("amount");
            if (amtObj == null) continue;
            String type = (String) item.get("type");
            if (type == null || type.isEmpty()) continue;
            Map<String, Object> newItem = new HashMap<>();
            BigDecimal amount = new BigDecimal(amtObj.toString());

            newItem.put("type", type);
            newItem.put("name", getBusinessTypeName(type));
            newItem.put("amount", amount);

            // 计算占比
            double ratio = 0;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                ratio = amount.divide(total, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
            newItem.put("ratio", ratio);

            result.add(newItem);
        }

        return result;
    }

    /**
     * 获取业务类型名称
     */
    private String getBusinessTypeName(String type) {
        switch (type) {
            case Finance.TYPE_BOOKING:
                return "场地预约";
            case Finance.TYPE_COURSE:
                return "课程预约";
            case Finance.TYPE_EQUIPMENT:
                return "器材租借";
            case Finance.TYPE_TOURNAMENT:
                return "赛事报名";
            case Finance.TYPE_STRINGING:
                return "穿线服务";
            case Finance.TYPE_RECHARGE:
                return "会员充值";
            case Finance.TYPE_OTHER:
                return "其他收支";
            default:
                return type;
        }
    }
}
