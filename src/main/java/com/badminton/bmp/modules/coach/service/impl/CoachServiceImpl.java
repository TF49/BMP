package com.badminton.bmp.modules.coach.service.impl;

import com.badminton.bmp.modules.coach.cache.CoachEntityCache;
import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.coach.service.CoachService;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.mapper.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.mapper.VenueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;

/**
 * 教练业务实现类
 */
@Service
public class CoachServiceImpl implements CoachService {

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private CoachEntityCache coachEntityCache;

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Coach findById(Long id) {
        Coach coach = getCoachById(id);
        if (coach == null) {
            return null;
        }

        // 数据范围过滤
        if (SecurityUtils.isPresident()) {
            return coach;
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId != null && vId.equals(coach.getVenueId())) {
                return coach;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // 普通用户：仅查看状态为正常的教练（status == 1）
            if (coach.getStatus() != null && coach.getStatus() == 1) {
                return coach;
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    /** 优先走缓存，若缓存不可用（如未配置 coach 缓存名）则直接查库，避免 "Cannot find cache named 'coach'" 导致接口报错 */
    private Coach getCoachById(Long id) {
        if (id == null) return null;
        try {
            Coach coach = coachEntityCache.getById(id);
            if (coach != null) return coach;
        } catch (Exception e) {
            // 缓存未配置或不可用时（如 Redis 未启用且未注册 coach 缓存）回退到直接查库
        }
        return coachMapper.findById(id);
    }

    @Override
    public Coach findByUserId(Long userId) {
        if (userId == null) return null;
        return coachMapper.findByUserId(userId);
    }

    @Override
    public Long getCurrentCoachIdOrNull() {
        User user = SecurityUtils.getCurrentUser();
        if (user == null || user.getId() == null) return null;
        Set<String> roles = SecurityUtils.getCurrentUserRoles();
        if (roles == null || !roles.stream().anyMatch(r -> "COACH".equalsIgnoreCase(r))) return null;
        Coach coach = coachMapper.findByUserId(user.getId());
        return coach != null ? coach.getId() : null;
    }

    @Override
    @Transactional
    public int updateSelfProfile(Long coachId, String coachName, String phone, String specialty, String experience, String avatar) {
        Long currentId = getCurrentCoachIdOrNull();
        if (currentId == null || !currentId.equals(coachId)) {
            throw new BusinessException("只能更新本人的教练档案");
        }
        Coach existing = coachMapper.findById(coachId);
        if (existing == null) throw new ResourceNotFoundException("教练不存在");
        String name = coachName != null ? coachName : existing.getCoachName();
        String ph = phone != null ? phone : existing.getPhone();
        String spec = specialty != null ? specialty : existing.getSpecialty();
        String exp = experience != null ? experience : existing.getExperience();
        String av = avatar != null ? avatar : existing.getAvatar();
        return coachMapper.updateSelfProfile(coachId, name, ph, spec, exp, av);
    }

    @Override
    public List<User> getUnboundCoachUsers() {
        List<User> coaches = userMapper.findAllByRole("COACH");
        if (coaches == null || coaches.isEmpty()) return coaches;
        List<Long> boundIds = coachMapper.findBoundUserIds();
        if (boundIds == null || boundIds.isEmpty()) return coaches;
        Set<Long> boundSet = boundIds.stream().collect(Collectors.toSet());
        return coaches.stream().filter(u -> u.getId() != null && !boundSet.contains(u.getId())).collect(Collectors.toList());
    }

    @Override
    public List<Coach> findAll(Long venueId, Integer status, String keyword, Integer gender, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 1000) {
            size = 1000;
        }
        int offset = (page - 1) * size;
        // 数据范围过滤：根据当前用户角色调整查询参数
        Long venueFilter = venueId;
        if (SecurityUtils.isPresident()) {
            // 协会会长：使用传入的 venueId（可能为 null）
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理者：只能查询自己场馆的数据
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：不按场馆过滤，但只查询可用教练
            venueFilter = null;
        }
        return coachMapper.findAll(venueFilter, status, keyword, gender, offset, size);
    }

    @Override
    public int count(Long venueId, Integer status, String keyword, Integer gender) {
        // 数据范围过滤：根据当前用户角色调整查询参数
        Long venueFilter = venueId;
        if (SecurityUtils.isPresident()) {
            // 协会会长：使用传入的 venueId（可能为 null）
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理者：只能查询自己场馆的数据
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：不按场馆过滤，但只查询可用教练
            venueFilter = null;
        }
        return coachMapper.count(venueFilter, status, keyword, gender);
    }

    @Override
    @Transactional
    public int add(Coach coach) {
        // 验证场馆ID必填（会长必须指定场馆，场馆管理者自动使用自己的场馆）
        if (SecurityUtils.isPresident()) {
            if (coach.getVenueId() == null) {
                throw new BusinessException("请选择所属场馆");
            }
            // 验证场馆是否存在
            Venue venue = venueMapper.findById(coach.getVenueId());
            if (venue == null) {
                throw new ResourceNotFoundException("所选场馆不存在");
            }
        } else if (SecurityUtils.isVenueManager()) {
            // 数据范围：场馆管理者只能在自己场馆下新增
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法新增教练");
            }
            coach.setVenueId(vId);
        } else {
            throw new BusinessException("权限不足，只有管理员可以添加教练");
        }

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        coach.setCreateTime(now);
        coach.setUpdateTime(now);

        // 设置默认状态为正常（1）
        if (coach.getStatus() == null) {
            coach.setStatus(1);
        }

        // 设置默认评分为0
        if (coach.getRating() == null) {
            coach.setRating(BigDecimal.ZERO);
        }

        // 设置默认累计学员数为0
        if (coach.getTotalStudents() == null) {
            coach.setTotalStudents(0);
        }

        // 关联账号：若传入 userId，校验用户为 COACH 且未被其他教练绑定
        if (coach.getUserId() != null) {
            User u = userMapper.findById(coach.getUserId());
            if (u == null) throw new ResourceNotFoundException("关联用户不存在");
            if (!"COACH".equalsIgnoreCase(u.getRole())) throw new BusinessException("只能关联角色为教练的用户");
            List<Long> bound = coachMapper.findBoundUserIds();
            if (bound != null && bound.contains(coach.getUserId())) throw new BusinessException("该用户已绑定其他教练档案");
        }

        // 验证评分范围（0-5）
        if (coach.getRating() != null) {
            if (coach.getRating().compareTo(BigDecimal.ZERO) < 0 || 
                coach.getRating().compareTo(new BigDecimal("5")) > 0) {
                throw new BusinessException("评分必须在0-5之间");
            }
        }

        return coachMapper.insert(coach);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "coach", key = "#coach.id")
    public int update(Coach coach) {
        // 查询原有教练信息
        Coach existingCoach = getCoachById(coach.getId());
        if (existingCoach == null) {
            throw new ResourceNotFoundException("教练不存在");
        }

        // 数据范围：场馆管理者只能更新自己场馆的教练
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新教练");
            }
            if (existingCoach.getVenueId() == null || !vId.equals(existingCoach.getVenueId())) {
                throw new BusinessException("权限不足：只能操作自己场馆的教练");
            }
            // 强制固定为自己的场馆，避免前端传入其他venueId越权
            coach.setVenueId(vId);
        } else if (SecurityUtils.isPresident()) {
            // 会长允许更新venueId（如果未传则保留原值）
            if (coach.getVenueId() == null) {
                coach.setVenueId(existingCoach.getVenueId());
            } else if (!coach.getVenueId().equals(existingCoach.getVenueId())) {
                // 如果修改了场馆，验证新场馆是否存在
                Venue venue = venueMapper.findById(coach.getVenueId());
                if (venue == null) {
                    throw new ResourceNotFoundException("所选场馆不存在");
                }
            }
        } else {
            throw new BusinessException("权限不足");
        }

        // 保留原有值（如果未传入）
        if (coach.getCoachName() == null) {
            coach.setCoachName(existingCoach.getCoachName());
        }
        if (coach.getGender() == null) {
            coach.setGender(existingCoach.getGender());
        }
        if (coach.getPhone() == null) {
            coach.setPhone(existingCoach.getPhone());
        }
        if (coach.getIdCard() == null) {
            coach.setIdCard(existingCoach.getIdCard());
        }
        if (coach.getSpecialty() == null) {
            coach.setSpecialty(existingCoach.getSpecialty());
        }
        if (coach.getExperience() == null) {
            coach.setExperience(existingCoach.getExperience());
        }
        if (coach.getHourlyPrice() == null) {
            coach.setHourlyPrice(existingCoach.getHourlyPrice());
        }
        if (coach.getRating() == null) {
            coach.setRating(existingCoach.getRating());
        }
        if (coach.getTotalStudents() == null) {
            coach.setTotalStudents(existingCoach.getTotalStudents());
        }
        if (coach.getStatus() == null) {
            coach.setStatus(existingCoach.getStatus());
        }
        if (coach.getAvatar() == null) {
            coach.setAvatar(existingCoach.getAvatar());
        }
        if (coach.getVenueId() == null) {
            coach.setVenueId(existingCoach.getVenueId());
        }
        // 关联账号：null 表示解绑；非 null 时校验新用户为 COACH 且未被其他教练绑定（或绑定的是当前教练）
        if (coach.getUserId() != null) {
            Long newUserId = coach.getUserId();
            User u = userMapper.findById(newUserId);
            if (u == null) throw new ResourceNotFoundException("关联用户不存在");
            if (!"COACH".equalsIgnoreCase(u.getRole())) throw new BusinessException("只能关联角色为教练的用户");
            Coach other = coachMapper.findByUserId(newUserId);
            if (other != null && !other.getId().equals(coach.getId())) throw new BusinessException("该用户已绑定其他教练档案");
        }
        // coach.getUserId() == null 时保持 null，即解绑

        // 验证评分范围（0-5）
        if (coach.getRating() != null) {
            if (coach.getRating().compareTo(BigDecimal.ZERO) < 0 || 
                coach.getRating().compareTo(new BigDecimal("5")) > 0) {
                throw new BusinessException("评分必须在0-5之间");
            }
        }

        // 设置更新时间
        coach.setUpdateTime(LocalDateTime.now());

        return coachMapper.update(coach);
    }

    @Override
    @CacheEvict(cacheNames = "coach", key = "#id")
    public int deleteById(Long id) {
        // 验证教练是否存在
        Coach coach = getCoachById(id);
        if (coach == null) {
            throw new ResourceNotFoundException("教练不存在");
        }

        // 数据范围：场馆管理者只能删除自己场馆的教练
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法删除教练");
            }
            if (coach.getVenueId() == null || !vId.equals(coach.getVenueId())) {
                throw new BusinessException("权限不足：只能操作自己场馆的教练");
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足");
        }

        // 检查是否有未完成的课程
        List<Integer> activeStatuses = Arrays.asList(1, 2); // 报名中、进行中
        int activeCourseCount = courseMapper.countByCoachIdAndStatusIn(id, activeStatuses);
        if (activeCourseCount > 0) {
            throw new BusinessException(
                String.format("该教练存在%d条未完成的课程，无法删除。请先处理这些课程后再试。", activeCourseCount)
            );
        }

        return coachMapper.deleteById(id);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        // 验证教练是否存在
        Coach coach = coachMapper.findById(id);
        if (coach == null) {
            throw new ResourceNotFoundException("教练不存在");
        }

        // 数据范围：场馆管理者只能更新自己场馆的教练状态
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新教练状态");
            }
            if (coach.getVenueId() == null || !vId.equals(coach.getVenueId())) {
                throw new BusinessException("权限不足：只能操作自己场馆的教练");
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足");
        }

        // 验证状态值是否有效
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("请选择有效的状态：正常或停用");
        }

        return coachMapper.updateStatus(id, status);
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 数据范围：场馆管理员只统计本场馆
        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }

        // 获取总教练数
        int total = coachMapper.countAll(venueFilter);
        statistics.put("total", total);

        // 获取各状态的教练数量
        List<Map<String, Object>> statusCounts = coachMapper.countByStatus(venueFilter);

        // 初始化各状态数量
        int disabled = 0;  // 停用（0）
        int normal = 0;    // 正常（1）

        for (Map<String, Object> item : statusCounts) {
            Object statusObj = item.get("status");
            if (statusObj == null) statusObj = item.get("STATUS");
            Object countObj = item.get("count");
            if (countObj == null) countObj = item.get("COUNT");

            if (statusObj != null && countObj != null) {
                int statusValue = ((Number) statusObj).intValue();
                int countValue = ((Number) countObj).intValue();

                switch (statusValue) {
                    case 0:
                        disabled = countValue;
                        break;
                    case 1:
                        normal = countValue;
                        break;
                }
            }
        }

        statistics.put("disabled", disabled);
        statistics.put("normal", normal);

        return statistics;
    }
}
