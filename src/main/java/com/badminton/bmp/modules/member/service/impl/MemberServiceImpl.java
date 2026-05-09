package com.badminton.bmp.modules.member.service.impl;

import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.entity.MemberConsumeRecord;
import com.badminton.bmp.modules.member.mapper.MemberConsumeRecordMapper;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberService;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.course.mapper.CourseBookingMapper;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.mapper.UserMapper;
import com.badminton.bmp.common.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会员服务实现类
 */
@Service
public class MemberServiceImpl implements MemberService {
    private static final int ACTIVE_MEMBER_WINDOW_DAYS = 30;
    private static final int HIGH_FREQUENCY_ACTIVITY_THRESHOLD = 4;
    private static final String SOURCE_MANUAL = "后台录入";
    private static final String SOURCE_APP = "用户端注册";
    private static final String SOURCE_SYSTEM = "系统补档";

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberConsumeRecordMapper memberConsumeRecordMapper;

    @Autowired
    private CourseBookingMapper courseBookingMapper;

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private UserMapper userMapper;

    private void ensurePresidentAnalyticsAccess() {
        if (!SecurityUtils.isPresident()) {
            throw new org.springframework.security.access.AccessDeniedException("权限不足，仅会长可访问会员统计数据");
        }
    }

    @Override
    public Member findById(Long id) {
        return findById(id, null);
    }

    @Override
    public Member findById(Long id, Long venueId) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            return null;
        }

        if (SecurityUtils.isPresident()) {
            return member;
        } else if (SecurityUtils.isVenueManager()) {
            return member;
        } else if (SecurityUtils.getCurrentUserRoles().stream().anyMatch(r -> "COACH".equalsIgnoreCase(r))) {
            Long coachVenueId = venueId != null ? venueId : resolveCurrentCoachVenueId();
            if (coachVenueId != null && isMemberVisibleForVenue(member.getId(), coachVenueId)) {
                return member;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            User current = SecurityUtils.getCurrentUser();
            if (current != null && current.getId().equals(member.getUserId())) {
                return member;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    @Override
    public Member findByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        Member existing = memberMapper.findByUserId(userId);
        if (existing != null) {
            return existing;
        }
        User user = userMapper.findById(userId);
        if (!isUserSideAccount(user)) {
            return null;
        }
        return ensureMemberArchiveForUser(user);
    }

    /**
     * 为系统用户创建默认会员记录（NORMAL，余额0），已存在则直接返回
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member createDefaultMemberForUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("用户信息不完整，无法创建会员");
        }
        // 已存在则直接返回；若存在逻辑删除档案则恢复
        Member existing = memberMapper.findAnyByUserId(user.getId());
        if (existing != null) {
            if (existing.getDelFlag() != null && existing.getDelFlag() == 1) {
                memberMapper.restoreById(existing.getId(), LocalDateTime.now());
                return memberMapper.findById(existing.getId());
            }
            return existing;
        }

        Member member = new Member();
        member.setUserId(user.getId());
        // 姓名暂无字段，沿用用户名
        member.setMemberName(user.getUsername());
        member.setPhone(user.getPhone());
        member.setIdCard(user.getIdCard());
        member.setMemberType("NORMAL");
        member.setMemberLevel(null);
        member.setRegisterTime(LocalDateTime.now());
        member.setStatus(1);
        member.setTotalConsumption(BigDecimal.ZERO);
        member.setBalance(BigDecimal.ZERO);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        member.setDelFlag(0);

        memberMapper.insert(member);
        return member;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void reconcileUserSideMemberArchives() {
        java.util.List<User> users = userMapper.findAllUserSideAccounts();
        if (users == null || users.isEmpty()) {
            return;
        }
        for (User user : users) {
            if (!isUserSideAccount(user)) {
                continue;
            }
            ensureMemberArchiveForUser(user);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    protected Member ensureMemberArchiveForUser(User user) {
        if (!isUserSideAccount(user)) {
            return null;
        }
        Member existing = memberMapper.findAnyByUserId(user.getId());
        if (existing != null) {
            if (existing.getDelFlag() != null && existing.getDelFlag() == 1) {
                memberMapper.restoreById(existing.getId(), LocalDateTime.now());
                return memberMapper.findById(existing.getId());
            }
            return existing;
        }

        Member rebound = tryBindExistingArchive(user);
        if (rebound != null) {
            return rebound;
        }

        return createDefaultMemberForUser(user);
    }

    private Member tryBindExistingArchive(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }

        Member candidate = null;
        if (hasText(user.getIdCard())) {
            java.util.List<Member> byIdCard = memberMapper.findUnboundByIdCard(user.getIdCard().trim());
            if (byIdCard != null && byIdCard.size() == 1) {
                candidate = byIdCard.get(0);
            }
        }

        if (candidate == null && hasText(user.getPhone()) && hasText(user.getUsername())) {
            java.util.List<Member> byPhoneAndName = memberMapper.findUnboundByPhoneAndName(user.getPhone().trim(), user.getUsername().trim());
            if (byPhoneAndName != null && byPhoneAndName.size() == 1) {
                candidate = byPhoneAndName.get(0);
            }
        }

        if (candidate == null) {
            return null;
        }

        memberMapper.bindUser(candidate.getId(), user.getId(), LocalDateTime.now());
        return memberMapper.findByUserId(user.getId());
    }

    private boolean isUserSideAccount(User user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String role = user.getRole().trim();
        return "USER".equalsIgnoreCase(role) || "MEMBER".equalsIgnoreCase(role);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public List<Member> findByConditions(String memberName, String phone, Long memberId, String memberType, Integer status, int page, int size) {
        reconcileUserSideMemberArchives();
        int p = Math.max(page, 1);
        int s = (size <= 0 || size > 100) ? 10 : size;
        int offset = (p - 1) * s;
        
        // 数据范围过滤：根据角色调整查询参数
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            // USER: 只能查询自己的会员记录
            User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) {
                    // 强制设置为当前用户的memberId
                    memberId = m.getId();
                } else {
                    // 用户没有会员记录，返回空列表
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }
        }
        // PRESIDENT 和 VENUE_MANAGER: 使用传入参数（全量或按条件）
        
        return memberMapper.findByConditions(memberName, phone, memberId, memberType, status, offset, s);
    }

    @Override
    public int countByConditions(String memberName, String phone, Long memberId, String memberType, Integer status) {
        reconcileUserSideMemberArchives();
        // 数据范围过滤：根据角色调整查询参数
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            // USER: 只能统计自己的会员记录
            User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) {
                    // 强制设置为当前用户的memberId
                    memberId = m.getId();
                } else {
                    // 用户没有会员记录，返回0
                    return 0;
                }
            } else {
                return 0;
            }
        }
        // PRESIDENT 和 VENUE_MANAGER: 使用传入参数（全量或按条件）
        
        return memberMapper.countByConditions(memberName, phone, memberId, memberType, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Member add(Member member) {
        // Service兜底：只有管理员可以新增会员
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new BusinessException("权限不足，仅管理员可执行此操作");
        }
        if (member == null) {
            throw new IllegalArgumentException("会员信息不能为空");
        }
        if (member.getUserId() == null) {
            throw new BusinessException("新增会员必须绑定一个用户账号");
        }
        User boundUser = userMapper.findById(member.getUserId());
        if (!isUserSideAccount(boundUser)) {
            throw new BusinessException("只能绑定用户端账号（USER/MEMBER）");
        }
        Member existingByUser = memberMapper.findAnyByUserId(member.getUserId());
        if (existingByUser != null) {
            throw new BusinessException("该用户账号已存在会员档案，不能重复创建");
        }
        if (member.getMemberType() == null || member.getMemberType().trim().isEmpty()) {
            member.setMemberType("NORMAL");
        }
        if (!"MEMBER".equals(member.getMemberType())) {
            member.setMemberLevel(null);
        } else if (member.getMemberLevel() == null) {
            member.setMemberLevel(1);
        }
        if (member.getStatus() == null) {
            member.setStatus(1);
        }
        if (!hasText(member.getMemberName())) {
            member.setMemberName(boundUser.getUsername());
        }
        if (!hasText(member.getPhone())) {
            member.setPhone(boundUser.getPhone());
        }
        if (!hasText(member.getIdCard())) {
            member.setIdCard(boundUser.getIdCard());
        }
        if (member.getRegisterTime() == null) {
            member.setRegisterTime(LocalDateTime.now());
        }
        if (member.getTotalConsumption() == null) {
            member.setTotalConsumption(BigDecimal.ZERO);
        }
        if (member.getBalance() == null) {
            member.setBalance(BigDecimal.ZERO);
        }
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        member.setDelFlag(0);
        memberMapper.insert(member);
        return member;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(Member member) {
        if (member == null || member.getId() == null) {
            throw new IllegalArgumentException("会员ID不能为空");
        }

        Member existing = memberMapper.findById(member.getId());
        if (existing == null || (existing.getDelFlag() != null && existing.getDelFlag() == 1)) {
            throw new BusinessException("会员不存在");
        }
        if (existing.getUserId() == null) {
            throw new BusinessException("当前会员档案缺少绑定用户账号，请先修复数据");
        }
        if (member.getUserId() == null) {
            member.setUserId(existing.getUserId());
        } else if (!existing.getUserId().equals(member.getUserId())) {
            throw new BusinessException("会员关联用户账号不允许修改");
        }

        // Service兜底：USER 只能更新自己的会员记录
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            User current = SecurityUtils.getCurrentUser();
            if (current == null) {
                throw new BusinessException("权限不足");
            }
            Member self = memberMapper.findByUserId(current.getId());
            if (self == null || !self.getId().equals(member.getId())) {
                throw new BusinessException("权限不足，只能修改自己的会员信息");
            }
        }

        // 若类型为普通用户，清空等级
        if (!"MEMBER".equals(member.getMemberType())) {
            member.setMemberLevel(null);
        }
        member.setUpdateTime(LocalDateTime.now());

        // 乐观锁：更新时version+1
        int result = memberMapper.update(member);
        if (result == 0) {
            throw new BusinessException("更新失败，数据可能已被其他操作修改，请刷新后重试");
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        // Service兜底：删除属于高危操作，这里要求必须是管理员
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            throw new BusinessException("权限不足，仅管理员可执行此操作");
        }
        Member existing = memberMapper.findById(id);
        if (existing == null) {
            throw new BusinessException("会员不存在");
        }
        if (existing.getUserId() != null) {
            throw new BusinessException("该会员已绑定用户账号，不能直接删除。请改为冻结会员或禁用对应用户账号");
        }
        return memberMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getStatistics() {
        reconcileUserSideMemberArchives();
        ensurePresidentAnalyticsAccess();
        Map<String, Object> map = new HashMap<>();
        int total = memberMapper.countAllAnalyticsMembers();
        int normal = memberMapper.countByStatus(1);
        int frozen = memberMapper.countByStatus(0);
        int expired = memberMapper.countByStatus(2);
        List<Map<String, Object>> typeLevel = memberMapper.countByTypeAndLevel();
        map.put("total", total);
        map.put("normal", normal);
        map.put("frozen", frozen);
        map.put("expired", expired);
        map.put("typeLevel", typeLevel);

        // 今日新增/增长率（按 register_time）
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate yesterday = today.minusDays(1);
        int newToday = memberMapper.countRegisteredOnDate(today);
        int newYesterday = memberMapper.countRegisteredOnDate(yesterday);
        double growthRate = 0;
        if (newYesterday > 0) {
            growthRate = ((double) (newToday - newYesterday) / newYesterday) * 100.0;
        } else if (newToday > 0) {
            growthRate = 100.0;
        }
        map.put("newToday", newToday);
        map.put("growthRate", growthRate);

        return map;
    }

    @Override
    public List<Map<String, Object>> getDistribution() {
        reconcileUserSideMemberArchives();
        ensurePresidentAnalyticsAccess();
        List<Map<String, Object>> result = new ArrayList<>();

        // 会员分布：按会员等级 member_level 统计（Lv.1~Lv.5；若存在 level=0/空，则显示 Lv.0）
        int[] levelCounts = new int[6];
        List<Map<String, Object>> typeLevel = memberMapper.countByTypeAndLevel();
        if (typeLevel != null) {
            for (Map<String, Object> row : typeLevel) {
                if (row == null) continue;
                Object typeObj = row.get("memberType");
                if (typeObj == null) typeObj = row.get("MEMBERTYPE");
                String type = typeObj != null ? String.valueOf(typeObj) : null;
                if (!"MEMBER".equalsIgnoreCase(type)) continue;

                Object levelObj = row.get("memberLevel");
                if (levelObj == null) levelObj = row.get("MEMBERLEVEL");
                int level = 0;
                if (levelObj instanceof Number) {
                    level = ((Number) levelObj).intValue();
                } else if (levelObj != null) {
                    try { level = Integer.parseInt(levelObj.toString()); } catch (NumberFormatException ignored) {}
                }
                if (level < 0) level = 0;
                if (level > 5) level = 5;

                Object cntObj = row.get("cnt");
                if (cntObj == null) cntObj = row.get("CNT");
                int cnt = 0;
                if (cntObj instanceof Number) {
                    cnt = ((Number) cntObj).intValue();
                } else if (cntObj != null) {
                    try { cnt = Integer.parseInt(cntObj.toString()); } catch (NumberFormatException ignored) {}
                }

                levelCounts[level] += cnt;
            }
        }

        for (int level = 1; level <= 5; level++) {
            result.add(mapItem("Lv." + level, levelCounts[level]));
        }
        if (levelCounts[0] > 0) {
            result.add(mapItem("Lv.0", levelCounts[0]));
        }

        return result;
    }

    private Map<String, Object> mapItem(String name, int value) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("value", value);
        return m;
    }

    @Override
    public List<Map<String, Object>> getFunnel() {
        reconcileUserSideMemberArchives();
        ensurePresidentAnalyticsAccess();
        List<Map<String, Object>> result = new ArrayList<>();
        int total = memberMapper.countAllAnalyticsMembers();
        LocalDateTime since = LocalDateTime.now().minusDays(ACTIVE_MEMBER_WINDOW_DAYS);
        List<Map<String, Object>> activityRows = memberMapper.countMemberActivitiesSince(since);
        int activeMembers = 0;
        int highFrequencyMembers = 0;
        int vipMembers = 0;
        if (activityRows != null) {
            for (Map<String, Object> row : activityRows) {
                int activityCount = parseIntValue(row.get("activityCount"));
                if (activityCount > 0) {
                    activeMembers++;
                }
                if (activityCount >= HIGH_FREQUENCY_ACTIVITY_THRESHOLD) {
                    highFrequencyMembers++;
                    if (parseIntValue(row.get("memberLevel")) >= 3) {
                        vipMembers++;
                    }
                }
            }
        }
        result.add(mapFunnelItem("注册会员", total, total > 0 ? 100.0 : 0));
        result.add(mapFunnelItem("活跃会员", activeMembers, total > 0 ? (activeMembers * 100.0 / total) : 0));
        result.add(mapFunnelItem("高频会员", highFrequencyMembers, activeMembers > 0 ? (highFrequencyMembers * 100.0 / activeMembers) : 0));
        result.add(mapFunnelItem("VIP会员", vipMembers, highFrequencyMembers > 0 ? (vipMembers * 100.0 / highFrequencyMembers) : 0));
        return result;
    }

    private Map<String, Object> mapFunnelItem(String name, int value, double rate) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("value", value);
        m.put("rate", Math.round(rate * 10) / 10.0);
        return m;
    }

    @Override
    public Map<String, Object> getConsumeRecords(Long memberId, int page, int size) {
        int p = Math.max(page, 1);
        int s = (size <= 0 || size > 100) ? 10 : size;
        int offset = (p - 1) * s;

        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            if (SecurityUtils.getCurrentUserRoles().stream().anyMatch(r -> "COACH".equalsIgnoreCase(r))) {
                Long coachVenueId = resolveCurrentCoachVenueId();
                if (coachVenueId == null || !isMemberVisibleForVenue(memberId, coachVenueId)) {
                    throw new BusinessException("权限不足，只能查看本场馆学员的消费记录");
                }
                List<MemberConsumeRecord> list = memberConsumeRecordMapper.findByMemberIdAndVenueId(memberId, coachVenueId, offset, s);
                int total = memberConsumeRecordMapper.countByMemberIdAndVenueId(memberId, coachVenueId);
                return buildConsumeRecordResult(list, total);
            } else {
                User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(memberId)) {
                        throw new BusinessException("权限不足，只能查看自己的消费记录");
                    }
                } else {
                    throw new BusinessException("权限不足");
                }
            }
        }

        List<MemberConsumeRecord> list = memberConsumeRecordMapper.findByMemberId(memberId, offset, s);
        int total = memberConsumeRecordMapper.countByMemberId(memberId);
        return buildConsumeRecordResult(list, total);
    }

    private Long resolveCurrentCoachVenueId() {
        User current = SecurityUtils.getCurrentUser();
        if (current == null || current.getId() == null) {
            return null;
        }
        com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findByUserId(current.getId());
        return coach != null ? coach.getVenueId() : null;
    }

    private boolean isMemberVisibleForVenue(Long memberId, Long venueId) {
        if (memberId == null || venueId == null) {
            return false;
        }
        if (courseBookingMapper.countByMemberIdAndVenueId(memberId, venueId) > 0) {
            return true;
        }
        return memberConsumeRecordMapper.countByMemberIdAndVenueId(memberId, venueId) > 0;
    }

    private Map<String, Object> buildConsumeRecordResult(List<MemberConsumeRecord> list, int total) {
        List<Map<String, Object>> recordsWithType = new ArrayList<>();
        for (MemberConsumeRecord record : list) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("memberId", record.getMemberId());
            recordMap.put("businessType", record.getBusinessType());
            recordMap.put("businessId", record.getBusinessId());
            recordMap.put("description", record.getDescription());
            recordMap.put("amount", record.getAmount());
            recordMap.put("paymentMethod", record.getPaymentMethod());
            recordMap.put("paymentStatus", record.getPaymentStatus());
            recordMap.put("beforeBalance", record.getBeforeBalance());
            recordMap.put("afterBalance", record.getAfterBalance());
            recordMap.put("remark", record.getRemark());
            recordMap.put("createTime", record.getCreateTime());
            recordMap.put("incomeExpenseType",
                    record.getAmount() != null && record.getAmount().compareTo(BigDecimal.ZERO) > 0 ? 0 : 1);
            recordsWithType.add(recordMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", recordsWithType);
        result.put("total", total);
        return result;
    }

    @Override
    public List<Map<String, Object>> getExpiringMembers(int days) {
        reconcileUserSideMemberArchives();
        ensurePresidentAnalyticsAccess();
        int d = days <= 0 ? 30 : Math.min(days, 365);
        List<Map<String, Object>> rows = memberMapper.findExpiringWithinDays(d);
        if (rows == null) return new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new HashMap<>();
            Object idObj = row.get("id");
            item.put("memberId", idObj);
            item.put("id", idObj);
            Object nameObj = row.get("member_name") != null ? row.get("member_name") : row.get("memberName");
            item.put("memberName", nameObj);
            item.put("name", nameObj);
            Object levelObj = row.get("member_level") != null ? row.get("member_level") : row.get("memberLevel");
            int level = levelObj instanceof Number ? ((Number) levelObj).intValue() : 0;
            item.put("memberLevel", level >= 3 ? "VIP" : level == 2 ? "金卡" : level == 1 ? "银卡" : "普通会员");
            item.put("level", item.get("memberLevel"));
            Object expObj = row.get("expire_time") != null ? row.get("expire_time") : row.get("expireTime");
            if (expObj != null) {
                LocalDateTime exp = expObj instanceof LocalDateTime ? (LocalDateTime) expObj
                    : java.time.LocalDateTime.parse(expObj.toString().replace(" ", "T"));
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(now.toLocalDate(), exp.toLocalDate());
                item.put("daysLeft", (int) Math.max(0, daysLeft));
                item.put("remainingDays", (int) Math.max(0, daysLeft));
                item.put("expireDate", exp.toLocalDate().toString());
                item.put("expiryDate", exp.toLocalDate().toString());
            } else {
                item.put("daysLeft", 0);
                item.put("remainingDays", 0);
                item.put("expireDate", "");
                item.put("expiryDate", "");
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getSource() {
        reconcileUserSideMemberArchives();
        ensurePresidentAnalyticsAccess();
        List<Map<String, Object>> rows = memberMapper.findSourceAnalyticsRows();
        Map<String, Integer> byType = new HashMap<>();
        byType.put(SOURCE_MANUAL, 0);
        byType.put(SOURCE_APP, 0);
        byType.put(SOURCE_SYSTEM, 0);
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                String source = classifyMemberSource(row);
                byType.merge(source, 1, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String source : new String[]{SOURCE_MANUAL, SOURCE_APP, SOURCE_SYSTEM}) {
            Map<String, Object> item = new HashMap<>();
            int count = byType.getOrDefault(source, 0);
            item.put("source", source);
            item.put("name", source);
            item.put("count", count);
            item.put("value", count);
            result.add(item);
        }
        return result;
    }

    private int parseIntValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private String classifyMemberSource(Map<String, Object> row) {
        if (row == null) {
            return SOURCE_MANUAL;
        }
        Object userId = row.get("user_id");
        if (userId == null) {
            return SOURCE_MANUAL;
        }

        String memberName = stringValue(row.get("member_name"));
        String username = stringValue(row.get("username"));
        String memberType = stringValue(row.get("member_type"));
        LocalDateTime registerTime = toLocalDateTime(row.get("register_time"));
        LocalDateTime userCreateTime = toLocalDateTime(row.get("user_create_time"));
        BigDecimal totalConsumption = toBigDecimal(row.get("total_consumption"));
        BigDecimal totalRecharge = toBigDecimal(row.get("total_recharge"));
        BigDecimal balance = toBigDecimal(row.get("balance"));

        // 当前库没有独立 source 字段，这里按已存在业务行为做最小兼容分类：
        // 1) 未绑定账号 -> 后台录入
        // 2) 由用户端账号自动补齐的默认会员档案 -> 系统补档
        // 3) 其余绑定用户端账号的档案 -> 用户端注册
        boolean looksLikeAutoArchive =
                "NORMAL".equalsIgnoreCase(memberType)
                        && memberName != null
                        && memberName.equals(username)
                        && totalConsumption.compareTo(BigDecimal.ZERO) == 0
                        && totalRecharge.compareTo(BigDecimal.ZERO) == 0
                        && balance.compareTo(BigDecimal.ZERO) == 0
                        && registerTime != null
                        && userCreateTime != null
                        && Math.abs(Duration.between(userCreateTime, registerTime).toMinutes()) <= 10;
        return looksLikeAutoArchive ? SOURCE_SYSTEM : SOURCE_APP;
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.replace(" ", "T"));
        } catch (Exception ignored) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return BigDecimal.ZERO;
        }
    }
}
