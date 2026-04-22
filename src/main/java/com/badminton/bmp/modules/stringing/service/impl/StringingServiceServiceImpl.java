package com.badminton.bmp.modules.stringing.service.impl;

import com.badminton.bmp.modules.stringing.entity.StringingService;
import com.badminton.bmp.modules.stringing.mapper.StringingServiceMapper;
import com.badminton.bmp.modules.stringing.service.StringingServiceService;
import com.badminton.bmp.modules.equipment.entity.Equipment;
import com.badminton.bmp.modules.equipment.mapper.EquipmentMapper;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.websocket.WebSocketPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 穿线服务业务实现类
 */
@Service
public class StringingServiceServiceImpl implements StringingServiceService {

    @Autowired
    private StringingServiceMapper stringingServiceMapper;

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberConsumeRecordService memberConsumeRecordService;

    @Autowired
    private FinanceService financeService;
    @Autowired
    private WebSocketPushService webSocketPushService;

    // 手工费固定为20元
    private static final BigDecimal HANDLING_FEE = new BigDecimal("20.00");

    /** 服务单号生成与插入的互斥锁，避免并发时重复单号 */
    private static final Object SERVICE_NO_LOCK = new Object();

    @Override
    public StringingService findById(Long id) {
        StringingService service = stringingServiceMapper.findById(id);
        if (service == null) {
            return null;
        }

        // 数据范围过滤
        if (SecurityUtils.isPresident()) {
            return service;
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId != null && vId.equals(service.getVenueId())) {
                return service;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // 普通用户：只能查看自己的服务记录
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null && current.getId().equals(service.getUserId())) {
                return service;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    @Override
    public StringingService findByServiceNo(String serviceNo) {
        StringingService service = stringingServiceMapper.findByServiceNo(serviceNo);
        if (service == null) {
            return null;
        }

        // 数据范围过滤（与findById相同的逻辑）
        if (SecurityUtils.isPresident()) {
            return service;
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId != null && vId.equals(service.getVenueId())) {
                return service;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null && current.getId().equals(service.getUserId())) {
                return service;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    @Override
    public List<StringingService> findAll(Long memberId, Long userId, Integer status, String keyword,
                                          Long venueId, String createTimeStart, String createTimeEnd,
                                          int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int offset = (page - 1) * size;

        // 权限过滤
        Long venueFilter = null;
        Long memberFilter = memberId;
        Long userFilter = userId;

        if (SecurityUtils.isPresident()) {
            // 协会会长：可选按场馆筛选；若传了 venueId 则按该场馆过滤
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理者：只能看到自己场馆的数据
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：只能看到自己的服务记录
            if (userFilter == null) {
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    userFilter = current.getId();
                }
            }
        }

        return stringingServiceMapper.findAll(venueFilter, memberFilter, userFilter, status, keyword,
                createTimeStart, createTimeEnd, offset, size);
    }

    @Override
    public int count(Long memberId, Long userId, Integer status, String keyword,
                     Long venueId, String createTimeStart, String createTimeEnd) {
        // 权限过滤
        Long venueFilter = null;
        Long memberFilter = memberId;
        Long userFilter = userId;

        if (SecurityUtils.isPresident()) {
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            if (userFilter == null) {
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    userFilter = current.getId();
                }
            }
        }

        return stringingServiceMapper.count(venueFilter, memberFilter, userFilter, status, keyword,
                createTimeStart, createTimeEnd);
    }

    @Override
    @Transactional
    public int add(StringingService service) {
        com.badminton.bmp.modules.system.entity.User currentUser = SecurityUtils.getCurrentUser();

        // 验证场馆是否存在
        if (service.getVenueId() == null) {
            throw new BusinessException("场馆ID不能为空");
        }

        // 权限验证：场馆管理者只能操作自己场馆的服务
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null || !vId.equals(service.getVenueId())) {
                throw new org.springframework.security.access.AccessDeniedException("权限不足，只能操作自己场馆的服务");
            }
        }

        // 普通用户侧提交强制绑定当前登录用户及其对应会员，避免伪造归属
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            if (currentUser == null || currentUser.getId() == null) {
                throw new BusinessException("当前登录用户无效，无法提交穿线服务");
            }

            service.setUserId(currentUser.getId());
            Member currentMember = memberMapper.findByUserId(currentUser.getId());
            if (currentMember == null || currentMember.getId() == null) {
                throw new BusinessException("当前用户未绑定会员，无法提交穿线服务");
            }
            service.setMemberId(currentMember.getId());
        } else if (service.getMemberId() != null) {
            // 管理端保留显式指定会员的能力
            Member member = memberMapper.findById(service.getMemberId());
            if (member == null) {
                throw new ResourceNotFoundException("会员不存在");
            }
        }

        // 如果从系统选择线材，验证线材是否存在
        if (service.getStringId() != null && service.getIsOwnString() != null && service.getIsOwnString() == 0) {
            Equipment equipment = equipmentMapper.findById(service.getStringId());
            if (equipment == null) {
                throw new ResourceNotFoundException("线材不存在");
            }
            // 验证线材类型是否为STRING
            if (!"STRING".equals(equipment.getEquipmentType())) {
                throw new BusinessException("所选器材不是线材类型");
            }
        }

        // 在锁内生成单号并完成插入，避免并发重复单号（Duplicate entry for service_no）
        synchronized (SERVICE_NO_LOCK) {
            if (service.getServiceNo() == null || service.getServiceNo().trim().isEmpty()) {
                service.setServiceNo(generateServiceNo());
            }
            if (stringingServiceMapper.existsByServiceNo(service.getServiceNo(), null)) {
                throw new BusinessException("服务单号已存在");
            }
            // 以下赋值在锁内完成，保证与单号一致
            if (service.getServicePrice() == null) {
                service.setServicePrice(calculatePrice(service.getStringId(), service.getIsOwnString()));
            }
            LocalDateTime now = LocalDateTime.now();
            service.setCreateTime(now);
            service.setUpdateTime(now);
            if (service.getStatus() == null) service.setStatus(1);
            // 新增订单强制为未支付（0），仅点击支付按钮后才扣款
            service.setPaymentStatus(0);
            if (service.getIsOwnString() == null) service.setIsOwnString(0);
            if (service.getHasBreakage() == null) service.setHasBreakage(0);
            if (service.getHasCollapse() == null) service.setHasCollapse(0);
            if (service.getUserId() == null) {
                if (currentUser != null) service.setUserId(currentUser.getId());
            }
            return stringingServiceMapper.insert(service);
        }
    }

    @Override
    @Transactional
    public int update(StringingService service) {
        if (service.getId() == null) {
            throw new BusinessException("服务ID不能为空");
        }

        // 验证服务是否存在
        StringingService existing = stringingServiceMapper.findById(service.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("服务记录不存在");
        }

        // 权限验证
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null || !vId.equals(existing.getVenueId())) {
                throw new org.springframework.security.access.AccessDeniedException("权限不足，只能操作自己场馆的服务");
            }
        } else if (!SecurityUtils.isPresident()) {
            // 普通用户只能更新自己的服务记录
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current == null || !current.getId().equals(existing.getUserId())) {
                throw new org.springframework.security.access.AccessDeniedException("权限不足，只能操作自己的服务记录");
            }
        }

        // 如果从系统选择线材，验证线材是否存在
        if (service.getStringId() != null && service.getIsOwnString() != null && service.getIsOwnString() == 0) {
            Equipment equipment = equipmentMapper.findById(service.getStringId());
            if (equipment == null) {
                throw new ResourceNotFoundException("线材不存在");
            }
            if (!"STRING".equals(equipment.getEquipmentType())) {
                throw new BusinessException("所选器材不是线材类型");
            }
        }

        // 重新计算价格（如果线材信息有变化）
        if (service.getStringId() != null || service.getIsOwnString() != null) {
            Long stringId = service.getStringId() != null ? service.getStringId() : existing.getStringId();
            Integer isOwnString = service.getIsOwnString() != null ? service.getIsOwnString() : existing.getIsOwnString();
            service.setServicePrice(calculatePrice(stringId, isOwnString));
        }

        // 设置更新时间
        service.setUpdateTime(LocalDateTime.now());

        // 更新服务记录
        return stringingServiceMapper.update(service);
    }

    @Override
    @Transactional
    public int deleteById(Long id) {
        // 验证服务是否存在
        StringingService existing = stringingServiceMapper.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("服务记录不存在");
        }

        // 权限验证
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null || !vId.equals(existing.getVenueId())) {
                throw new org.springframework.security.access.AccessDeniedException("权限不足，只能操作自己场馆的服务");
            }
        } else if (!SecurityUtils.isPresident()) {
            // 普通用户只能删除自己的服务记录
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current == null || !current.getId().equals(existing.getUserId())) {
                throw new org.springframework.security.access.AccessDeniedException("权限不足，只能操作自己的服务记录");
            }
        }

        return stringingServiceMapper.deleteById(id);
    }

    @Override
    @Transactional
    public int updateStatus(Long id, Integer status) {
        // 验证服务是否存在
        StringingService existing = stringingServiceMapper.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("服务记录不存在");
        }

        // 权限验证（只有管理员可以更新状态）
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new org.springframework.security.access.AccessDeniedException("权限不足，只有管理员可以更新状态");
        }

        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null || !vId.equals(existing.getVenueId())) {
                throw new org.springframework.security.access.AccessDeniedException("权限不足，只能操作自己场馆的服务");
            }
        }

        // 状态流转验证
        int result;
        if (status == 2) {
            LocalDateTime startTime = LocalDateTime.now();
            result = stringingServiceMapper.updateStatus(id, status, startTime);
        } else {
            result = stringingServiceMapper.updateStatus(id, status, null);
        }
        try {
            Long userId = existing.getUserId();
            if (userId == null && existing.getMemberId() != null) {
                Member m = memberMapper.findById(existing.getMemberId());
                if (m != null && m.getUserId() != null) userId = m.getUserId();
            }
            String statusText = stringingServiceStatusText(status);
            webSocketPushService.pushOrderStatusToUser(userId, "stringingService", id, status, statusText, "穿线服务");
            webSocketPushService.pushDashboardRefresh();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(StringingServiceServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public int cancelByUser(Long serviceId) {
        StringingService existing = stringingServiceMapper.findById(serviceId);
        if (existing == null) {
            throw new ResourceNotFoundException("服务记录不存在");
        }

        com.badminton.bmp.modules.system.entity.User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException("当前登录用户无效，无法取消穿线服务");
        }

        if (existing.getUserId() == null || !currentUser.getId().equals(existing.getUserId())) {
            throw new BusinessException("只能取消当前登录用户自己的穿线订单");
        }

        Integer status = existing.getStatus();
        if (status != null) {
            if (status == 0) {
                throw new BusinessException("该穿线服务已取消，无需重复操作");
            }
            if (status == 2) {
                throw new BusinessException("该穿线服务已开始处理，暂不支持用户取消");
            }
            if (status == 3) {
                throw new BusinessException("该穿线服务已完成，无法取消");
            }
            if (status != 1) {
                throw new BusinessException("当前订单状态不可取消");
            }
        }

        Integer paymentStatus = existing.getPaymentStatus();
        if (paymentStatus != null) {
            if (paymentStatus == 1) {
                throw new BusinessException("该穿线服务已支付，请联系管理员处理");
            }
            if (paymentStatus == 2) {
                throw new BusinessException("该穿线服务已退款，无需再次取消");
            }
        }

        int result = stringingServiceMapper.updateStatus(serviceId, 0, null);
        if (result > 0) {
            try {
                webSocketPushService.pushOrderStatusToUser(currentUser.getId(), "stringingService", serviceId, 0, "已取消", "穿线服务");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StringingServiceServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    private static String stringingServiceStatusText(int status) {
        switch (status) {
            case 0: return "已取消";
            case 1: return "等待穿线";
            case 2: return "正在穿线";
            case 3: return "已完成";
            default: return "未知";
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        // 权限过滤
        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }

        // 统计各状态数量
        List<Map<String, Object>> statusCounts = stringingServiceMapper.countByStatus(venueFilter);
        int total = stringingServiceMapper.countAll(venueFilter);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("total", total);
        statistics.put("waiting", 0);      // 等待穿线（状态1）
        statistics.put("inProgress", 0);    // 正在穿线（状态2）
        statistics.put("completed", 0);     // 已完成（状态3）
        statistics.put("cancelled", 0);    // 已取消（状态0）

        for (Map<String, Object> count : statusCounts) {
            Object statusObj = count.get("status");
            if (statusObj == null) statusObj = count.get("STATUS");
            Object countObj = count.get("count");
            if (countObj == null) countObj = count.get("COUNT");
            if (statusObj == null || countObj == null) continue;
            int status = ((Number) statusObj).intValue();
            Long countValue = ((Number) countObj).longValue();
            if (status == 0) {
                statistics.put("cancelled", countValue);
            } else if (status == 1) {
                statistics.put("waiting", countValue);
            } else if (status == 2) {
                statistics.put("inProgress", countValue);
            } else if (status == 3) {
                statistics.put("completed", countValue);
            }
        }

        return statistics;
    }

    @Override
    public synchronized String generateServiceNo() {
        // 生成格式：SR+日期+序号，如SR20250125001
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "SR" + dateStr;

        // 查询当天所有服务记录（用于查找最大序号，不进行权限过滤）
        List<StringingService> todayServices = stringingServiceMapper.findByServiceNoPrefix(prefix);

        int maxSequence = 0;
        for (StringingService service : todayServices) {
            if (service.getServiceNo() != null && service.getServiceNo().startsWith(prefix)) {
                try {
                    String sequenceStr = service.getServiceNo().substring(prefix.length());
                    int sequence = Integer.parseInt(sequenceStr);
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略无效的单号
                }
            }
        }

        // 生成新的序号
        int newSequence = maxSequence + 1;
        return prefix + String.format("%03d", newSequence);
    }

    @Override
    public BigDecimal calculatePrice(Long stringId, Integer isOwnString) {
        // 自带线材（isOwnString == 1）：只收手工费 20 元
        if (isOwnString != null && isOwnString == 1) {
            return HANDLING_FEE;
        }
        // 不自带线材且从系统选择线材：仅线材价格（不加手工费）
        if (stringId != null && (isOwnString == null || isOwnString == 0)) {
            Equipment equipment = equipmentMapper.findById(stringId);
            if (equipment != null && equipment.getPrice() != null) {
                return equipment.getPrice();
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPayment(Long serviceId, String paymentMethod) {
        if (!"BALANCE".equals(paymentMethod)) {
            throw new BusinessException("业务订单仅支持余额支付");
        }

        StringingService service = stringingServiceMapper.findById(serviceId);
        if (service == null) {
            throw new ResourceNotFoundException("穿线服务记录不存在");
        }

        // 权限兜底：只有管理员，且场馆管理者仅限自己场馆
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || service.getVenueId() == null || !currentVenueId.equals(service.getVenueId())) {
                    throw new BusinessException("权限不足，只能处理自己场馆的穿线服务支付");
                }
            } else {
                throw new BusinessException("权限不足，仅管理员可执行此操作");
            }
        }

        // 按支付状态返回明确提示
        Integer payStatus = service.getPaymentStatus();
        if (payStatus != null && payStatus != 0) {
            String msg = (payStatus != null && payStatus == 1)
                ? "该穿线服务已支付，无需重复支付"
                : "该穿线服务已退款，无法再次支付";
            throw new BusinessException(msg);
        }
        if (service.getMemberId() == null) {
            throw new BusinessException("穿线服务未关联会员，无法使用会员支付");
        }
        BigDecimal amount = service.getServicePrice() != null ? service.getServicePrice() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("服务金额无效，无法支付");
        }

        memberConsumeRecordService.createConsumeRecord(
            service.getMemberId(),
            amount,
            "STRINGING",
            serviceId,
            paymentMethod,
            "穿线服务支付：" + service.getServiceNo()
        );

        financeService.createFromBusiness(
            Finance.TYPE_STRINGING,
            serviceId,
            amount,
            Finance.INCOME,
            paymentMethod,
            service.getVenueId(),
            "穿线服务支付：" + service.getServiceNo()
        );

        service.setPaymentMethod(paymentMethod);
        service.setPaymentStatus(1);
        service.setUpdateTime(LocalDateTime.now());
        int updated = stringingServiceMapper.update(service);
        if (updated > 0) {
            try {
                Long userId = service.getUserId();
                if (userId == null && service.getMemberId() != null) {
                    Member m = memberMapper.findById(service.getMemberId());
                    if (m != null && m.getUserId() != null) userId = m.getUserId();
                }
                webSocketPushService.pushOrderStatusToUser(userId, "stringingService", serviceId, 1, "等待穿线", "穿线服务");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StringingServiceServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(Long serviceId) {
        StringingService service = stringingServiceMapper.findById(serviceId);
        if (service == null) {
            throw new ResourceNotFoundException("穿线服务记录不存在");
        }

        // 权限兜底：只有管理员，且场馆管理者仅限自己场馆
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId == null || service.getVenueId() == null || !currentVenueId.equals(service.getVenueId())) {
                    throw new BusinessException("权限不足，只能处理自己场馆的穿线服务退款");
                }
            } else {
                throw new BusinessException("权限不足，仅管理员可执行此操作");
            }
        }

        // 按支付状态返回明确提示
        Integer payStatus = service.getPaymentStatus();
        if (payStatus == null || payStatus != 1) {
            String msg;
            if (payStatus == null || payStatus == 0) {
                msg = "该穿线服务尚未支付，无法退款";
            } else if (payStatus == 2) {
                msg = "该穿线服务已退款，无需重复退款";
            } else {
                msg = "只能对已支付的穿线服务进行退款";
            }
            throw new BusinessException(msg);
        }
        BigDecimal amount = service.getServicePrice() != null ? service.getServicePrice() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("服务金额无效");
        }

        if ("BALANCE".equals(service.getPaymentMethod())) {
            memberConsumeRecordService.createRefundRecord(
                service.getMemberId(),
                amount,
                "STRINGING",
                serviceId,
                service.getPaymentMethod(),
                "穿线服务退款：" + service.getServiceNo()
            );
        }

        financeService.createFromBusiness(
            Finance.TYPE_STRINGING,
            serviceId,
            amount,
            Finance.EXPENSE,
            service.getPaymentMethod(),
            service.getVenueId(),
            "穿线服务退款：" + service.getServiceNo()
        );

        service.setPaymentStatus(2);  // 已退款
        service.setStatus(0);          // 已取消
        service.setUpdateTime(LocalDateTime.now());
        int result = stringingServiceMapper.update(service);
        if (result > 0) {
            try {
                Long userId = service.getUserId();
                if (userId == null && service.getMemberId() != null) {
                    Member m = memberMapper.findById(service.getMemberId());
                    if (m != null && m.getUserId() != null) userId = m.getUserId();
                }
                webSocketPushService.pushOrderStatusToUser(userId, "stringingService", serviceId, 0, "已取消", "穿线服务");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(StringingServiceServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return result;
    }
}
