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
import com.badminton.bmp.common.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberConsumeRecordMapper memberConsumeRecordMapper;

    @Autowired
    private CourseBookingMapper courseBookingMapper;

    @Autowired
    private CoachMapper coachMapper;

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
        return memberMapper.findByUserId(userId);
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
        // 已存在则直接返回
        Member existing = memberMapper.findByUserId(user.getId());
        if (existing != null) {
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

    @Override
    public List<Member> findByConditions(String memberName, String phone, Long memberId, String memberType, Integer status, int page, int size) {
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
        return memberMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getStatistics() {
        ensurePresidentAnalyticsAccess();
        Map<String, Object> map = new HashMap<>();
        int total = memberMapper.countAll();
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
        ensurePresidentAnalyticsAccess();
        List<Map<String, Object>> result = new ArrayList<>();
        int total = memberMapper.countAll();
        int normal = memberMapper.countByStatus(1);
        int vip = 0, gold = 0, silver = 0;
        List<Map<String, Object>> typeLevel = memberMapper.countByTypeAndLevel();
        if (typeLevel != null) {
            for (Map<String, Object> row : typeLevel) {
                if (row == null) continue;
                Object typeObj = row.get("memberType");
                if (typeObj == null) typeObj = row.get("MEMBERTYPE");
                if (!"MEMBER".equalsIgnoreCase(String.valueOf(typeObj))) continue;
                Object levelObj = row.get("memberLevel");
                if (levelObj == null) levelObj = row.get("MEMBERLEVEL");
                int level = 0;
                if (levelObj instanceof Number) level = ((Number) levelObj).intValue();
                else if (levelObj != null) {
                    try { level = Integer.parseInt(levelObj.toString()); } catch (NumberFormatException ignored) {}
                }
                Object cntObj = row.get("cnt");
                if (cntObj == null) cntObj = row.get("CNT");
                int cnt = 0;
                if (cntObj instanceof Number) cnt = ((Number) cntObj).intValue();
                else if (cntObj != null) {
                    try { cnt = Integer.parseInt(cntObj.toString()); } catch (NumberFormatException ignored) {}
                }
                if (level >= 3) vip += cnt;
                else if (level == 2) gold += cnt;
                else if (level == 1) silver += cnt;
            }
        }
        int highFreq = gold + silver + vip;
        result.add(mapFunnelItem("注册会员", total, total > 0 ? 100.0 : 0));
        result.add(mapFunnelItem("活跃会员", normal, total > 0 ? (normal * 100.0 / total) : 0));
        result.add(mapFunnelItem("高频会员", highFreq, normal > 0 ? (highFreq * 100.0 / normal) : 0));
        result.add(mapFunnelItem("VIP会员", vip, highFreq > 0 ? (vip * 100.0 / highFreq) : 0));
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
        ensurePresidentAnalyticsAccess();
        List<Map<String, Object>> typeLevel = memberMapper.countByTypeAndLevel();
        Map<String, Integer> byType = new HashMap<>();
        if (typeLevel != null) {
            for (Map<String, Object> row : typeLevel) {
                if (row == null) continue;
                Object typeObj = row.get("memberType");
                if (typeObj == null) typeObj = row.get("MEMBERTYPE");
                String type = typeObj != null ? String.valueOf(typeObj) : "NORMAL";
                Object cntObj = row.get("cnt");
                if (cntObj == null) cntObj = row.get("CNT");
                int cnt = 0;
                if (cntObj instanceof Number) cnt = ((Number) cntObj).intValue();
                else if (cntObj != null) {
                    try { cnt = Integer.parseInt(cntObj.toString()); } catch (NumberFormatException ignored) {}
                }
                byType.merge("MEMBER".equalsIgnoreCase(type) ? "会员" : "普通用户", cnt, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : byType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("source", e.getKey());
            item.put("name", e.getKey());
            item.put("count", e.getValue());
            item.put("value", e.getValue());
            result.add(item);
        }
        return result;
    }
}
