package com.badminton.bmp.modules.course.service.impl;

import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.course.cache.CourseEntityCache;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.course.mapper.CourseBookingMapper;
import com.badminton.bmp.modules.course.service.CourseService;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.system.entity.User;

/**
 * 课程业务实现类
 * 处理课程相关的业务逻辑
 */
@Service
public class CourseServiceImpl implements CourseService {
    private static final String FORBIDDEN_SCOPE_MESSAGE = "权限不足：只能操作自己场馆的数据";

    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private CourseBookingMapper courseBookingMapper;
    @Autowired
    private CourseEntityCache courseEntityCache;
    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private CourtMapper courtMapper;

    @Override
    public Course findById(Long id) {
        Course course = courseEntityCache.getById(id);
        if (course == null) {
            return null;
        }

        // 数据范围过滤：根据角色检查访问权限
        if (SecurityUtils.isPresident()) {
            return course;
        } else if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            // 检查教练所属场馆或场地所属场馆是否与当前场馆一致
            if (vId != null) {
                if (course.getCoachId() != null) {
                    com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findById(course.getCoachId());
                    if (coach != null && vId.equals(coach.getVenueId())) {
                        return course;
                    }
                }
                if (course.getCourtId() != null) {
                    com.badminton.bmp.modules.court.entity.Court court = courtMapper.findById(course.getCourtId());
                    if (court != null && vId.equals(court.getVenueId())) {
                        return course;
                    }
                }
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else if (SecurityUtils.getCurrentUserRoles().stream().anyMatch(r -> "COACH".equalsIgnoreCase(r))) {
            Long coachVenueId = resolveCurrentCoachVenueId();
            if (coachVenueId != null) {
                if (course.getCoachId() != null) {
                    com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findById(course.getCoachId());
                    if (coach != null && coachVenueId.equals(coach.getVenueId())) {
                        return course;
                    }
                }
                if (course.getCourtId() != null) {
                    com.badminton.bmp.modules.court.entity.Court court = courtMapper.findById(course.getCourtId());
                    if (court != null && coachVenueId.equals(court.getVenueId())) {
                        return course;
                    }
                }
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            return course;
        }
    }

    private Long resolveCurrentCoachVenueId() {
        User current = SecurityUtils.getCurrentUser();
        if (current == null || current.getId() == null) {
            return null;
        }
        com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findByUserId(current.getId());
        return coach != null ? coach.getVenueId() : null;
    }

    /**
     * 查找所有课程（支持条件筛选和分页）
     * @param coachId 教练ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配课程名称）
     * @param page 页码
     * @param size 每页数量
     * @return 课程列表
     */
    @Override
    public List<Course> findAll(Long coachId, Long courtId, Integer status, String keyword, String startTime, String endTime, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int offset = (page - 1) * size;
        Long venueFilter = null;
        if (SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }
        return courseMapper.findAll(venueFilter, coachId, courtId, status, keyword, startTime, endTime, offset, size);
    }

    /**
     * 统计课程数量（支持条件筛选）
     */
    @Override
    public int count(Long coachId, Long courtId, Integer status, String keyword, String startTime, String endTime) {
        Long venueFilter = null;
        if (SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }
        return courseMapper.count(venueFilter, coachId, courtId, status, keyword, startTime, endTime);
    }

    /**
     * 添加课程
     * @param course 课程对象
     * @return 影响行数
     */
    @Override
    @Transactional
    public int add(Course course) {
        com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findById(course.getCoachId());
        if (coach == null) {
            throw new ResourceNotFoundException("教练不存在");
        }

        com.badminton.bmp.modules.court.entity.Court court = null;
        if (course.getCourtId() != null) {
            court = courtMapper.findById(course.getCourtId());
            if (court == null) {
                throw new ResourceNotFoundException("场地不存在");
            }
        }

        Long coachVenueId = coach.getVenueId();
        Long courtVenueId = court != null ? court.getVenueId() : null;
        if (coachVenueId == null) {
            throw new BusinessException("所选教练未绑定场馆");
        }
        if (court != null && courtVenueId != null && !coachVenueId.equals(courtVenueId)) {
            throw new BusinessException("所选教练/场地不属于同一场馆");
        }

        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法新增课程");
            }
            if (!currentVenueId.equals(coachVenueId) || (courtVenueId != null && !currentVenueId.equals(courtVenueId))) {
                throw new BusinessException(FORBIDDEN_SCOPE_MESSAGE);
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以新增课程");
        }

        // 验证时间逻辑：结束时间应该晚于开始时间
        if (course.getStartTime() != null && course.getEndTime() != null) {
            if (course.getEndTime().isBefore(course.getStartTime()) || course.getEndTime().equals(course.getStartTime())) {
                throw new BusinessException("结束时间必须晚于开始时间");
            }
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        course.setCreateTime(now);
        course.setUpdateTime(now);
        
        // 设置默认值
        if (course.getStatus() == null) {
            course.setStatus(1);
        }
        if (course.getCurrentStudents() == null) {
            course.setCurrentStudents(0);
        }
        if (course.getMaxStudents() == null) {
            course.setMaxStudents(10);
        }
        
        return courseMapper.insert(course);
    }

    /**
     * 更新课程信息
     * @param course 课程对象
     * @return 影响行数
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "course", key = "#course.id")
    public int update(Course course) {
        // 验证课程是否存在
        Course existing = courseEntityCache.getById(course.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("课程不存在");
        }

        // 权限校验：VENUE_MANAGER只能更新自己场馆的课程
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新课程");
            }
            // 通过教练或场地判断课程归属
            boolean hasPermission = false;
            if (existing.getCoachId() != null) {
                com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findById(existing.getCoachId());
                if (coach != null && vId.equals(coach.getVenueId())) {
                    hasPermission = true;
                }
            }
            if (!hasPermission && existing.getCourtId() != null) {
                com.badminton.bmp.modules.court.entity.Court court = courtMapper.findById(existing.getCourtId());
                if (court != null && vId.equals(court.getVenueId())) {
                    hasPermission = true;
                }
            }
            if (!hasPermission) {
                throw new BusinessException(FORBIDDEN_SCOPE_MESSAGE);
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以更新课程");
        }

        com.badminton.bmp.modules.coach.entity.Coach effectiveCoach =
                coachMapper.findById(course.getCoachId() != null ? course.getCoachId() : existing.getCoachId());
        if (effectiveCoach == null) {
            throw new ResourceNotFoundException("教练不存在");
        }

        com.badminton.bmp.modules.court.entity.Court effectiveCourt = null;
        Long effectiveCourtId = course.getCourtId() != null ? course.getCourtId() : existing.getCourtId();
        if (effectiveCourtId != null) {
            effectiveCourt = courtMapper.findById(effectiveCourtId);
            if (effectiveCourt == null) {
                throw new ResourceNotFoundException("场地不存在");
            }
        }

        Long effectiveCoachVenueId = effectiveCoach.getVenueId();
        Long effectiveCourtVenueId = effectiveCourt != null ? effectiveCourt.getVenueId() : null;
        if (effectiveCoachVenueId == null) {
            throw new BusinessException("所选教练未绑定场馆");
        }
        if (effectiveCourt != null && effectiveCourtVenueId != null && !effectiveCoachVenueId.equals(effectiveCourtVenueId)) {
            throw new BusinessException("所选教练/场地不属于同一场馆");
        }
        if (SecurityUtils.isVenueManager()) {
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法更新课程");
            }
            if (!currentVenueId.equals(effectiveCoachVenueId)
                    || (effectiveCourtVenueId != null && !currentVenueId.equals(effectiveCourtVenueId))) {
                throw new BusinessException(FORBIDDEN_SCOPE_MESSAGE);
            }
        }

        // 验证时间逻辑：结束时间应该晚于开始时间
        java.time.LocalTime startTime = course.getStartTime() != null ? course.getStartTime() : existing.getStartTime();
        java.time.LocalTime endTime = course.getEndTime() != null ? course.getEndTime() : existing.getEndTime();
        if (startTime != null && endTime != null) {
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                throw new BusinessException("结束时间必须晚于开始时间");
            }
        }
        
        // 如果修改了maxStudents，确保currentStudents不超过maxStudents
        Integer maxStudents = course.getMaxStudents() != null ? course.getMaxStudents() : existing.getMaxStudents();
        Integer currentStudents = existing.getCurrentStudents() != null ? existing.getCurrentStudents() : 0;
        if (currentStudents > maxStudents) {
            throw new BusinessException("当前报名人数不能超过最大学员数");
        }
        // 编辑时不允许覆盖“当前报名人数”，仅通过预约/取消/退款等操作由 updateCurrentStudents 更新
        course.setCurrentStudents(existing.getCurrentStudents());
        
        // 若修改了状态，校验状态流转是否允许（与 updateStatus 规则一致）
        Integer newStatus = course.getStatus() != null ? course.getStatus() : existing.getStatus();
        Integer oldStatus = existing.getStatus();
        if (newStatus != null && !newStatus.equals(oldStatus)) {
            if (oldStatus != null) {
                if (oldStatus == 0) {
                    throw new BusinessException("已取消的课程不能修改状态");
                }
                if (oldStatus == 3) {
                    throw new BusinessException("已结束的课程不能修改状态");
                }
                if (oldStatus == 1 && newStatus != 2 && newStatus != 0) {
                    throw new BusinessException("报名中的课程只能改为「进行中」或「已取消」");
                }
                if (oldStatus == 2 && newStatus != 3 && newStatus != 0) {
                    throw new BusinessException("进行中的课程只能改为「已结束」或「已取消」");
                }
            }
        }
        
        // 设置更新时间
        course.setUpdateTime(LocalDateTime.now());
        return courseMapper.update(course);
    }

    /**
     * 删除课程（逻辑删除）
     * @param id 课程ID
     * @return 影响行数
     */
    @Override
    @CacheEvict(cacheNames = "course", key = "#id")
    public int deleteById(Long id) {
        // 验证课程是否存在
        Course course = courseEntityCache.getById(id);
        if (course == null) {
            throw new ResourceNotFoundException("课程不存在");
        }

        // 权限校验：VENUE_MANAGER只能删除自己场馆的课程
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法删除课程");
            }
            // 通过教练或场地判断课程归属
            boolean hasPermission = false;
            if (course.getCoachId() != null) {
                com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findById(course.getCoachId());
                if (coach != null && vId.equals(coach.getVenueId())) {
                    hasPermission = true;
                }
            }
            if (!hasPermission && course.getCourtId() != null) {
                com.badminton.bmp.modules.court.entity.Court court = courtMapper.findById(course.getCourtId());
                if (court != null && vId.equals(court.getVenueId())) {
                    hasPermission = true;
                }
            }
            if (!hasPermission) {
                throw new RuntimeException(FORBIDDEN_SCOPE_MESSAGE);
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以删除课程");
        }

        return courseMapper.deleteById(id);
    }

    /**
     * 更新课程状态（仅允许合理流转：报名中→进行中/已取消，进行中→已结束/已取消，已取消/已结束不可再改）
     * @param id 课程ID
     * @param status 状态值（0-已取消，1-报名中，2-进行中，3-已结束）
     * @return 影响行数
     */
    @Override
    public int updateStatus(Long id, Integer status) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new ResourceNotFoundException("课程不存在");
        }
        if (SecurityUtils.isVenueManager()) {
            Long vId = SecurityUtils.getCurrentUserVenueId();
            if (vId == null) {
                throw new BusinessException("当前账号未绑定场馆，无法修改课程状态");
            }
            boolean hasPermission = false;
            if (course.getCoachId() != null) {
                com.badminton.bmp.modules.coach.entity.Coach coach = coachMapper.findById(course.getCoachId());
                if (coach != null && vId.equals(coach.getVenueId())) {
                    hasPermission = true;
                }
            }
            if (!hasPermission && course.getCourtId() != null) {
                com.badminton.bmp.modules.court.entity.Court court = courtMapper.findById(course.getCourtId());
                if (court != null && vId.equals(court.getVenueId())) {
                    hasPermission = true;
                }
            }
            if (!hasPermission) {
                throw new BusinessException(FORBIDDEN_SCOPE_MESSAGE);
            }
        } else if (!SecurityUtils.isPresident()) {
            throw new BusinessException("权限不足：只有会长和场馆管理员可以修改课程状态");
        }
        if (status == null || status < 0 || status > 3) {
            throw new BusinessException("无效的状态值，必须是0（已取消）、1（报名中）、2（进行中）或3（已结束）");
        }
        Integer current = course.getStatus();
        if (current != null) {
            if (current == 0) {
                throw new RuntimeException("已取消的课程不能修改状态");
            }
            if (current == 3) {
                throw new RuntimeException("已结束的课程不能修改状态");
            }
            if (current == 1 && (status != 2 && status != 0)) {
                throw new BusinessException("报名中的课程只能改为「开始上课」或「取消」");
            }
            if (current == 2 && (status != 3 && status != 0)) {
                throw new BusinessException("进行中的课程只能改为「结束」或「取消」");
            }
        }
        return courseMapper.updateStatus(id, status);
    }

    /**
     * 获取课程统计信息
     * @return 统计信息（总课程数、各状态数量）
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }
        // 场馆管理员只统计本场馆课程
        int totalCount = venueFilter != null ? courseMapper.countAllFiltered(venueFilter) : courseMapper.countAll();
        List<Map<String, Object>> statusCounts = venueFilter != null ? courseMapper.countByStatusFiltered(venueFilter) : courseMapper.countByStatus();
        stats.put("total", totalCount);
        
        // 初始化各状态数量
        int cancelled = 0;  // 已取消（0）
        int enrolling = 0;  // 报名中（1）
        int ongoing = 0;    // 进行中（2）
        int finished = 0;   // 已结束（3）
        
        for (Map<String, Object> item : statusCounts) {
            Object statusObj = item.get("status");
            if (statusObj == null) statusObj = item.get("STATUS");
            Object countObj = item.get("count");
            if (countObj == null) countObj = item.get("COUNT");
            if (statusObj == null || countObj == null) continue;
            int status = ((Number) statusObj).intValue();
            int count = ((Number) countObj).intValue();
            switch (status) {
                case 0:
                    cancelled = count;
                    break;
                case 1:
                    enrolling = count;
                    break;
                case 2:
                    ongoing = count;
                    break;
                case 3:
                    finished = count;
                    break;
            }
        }
        
        stats.put("cancelled", cancelled);
        stats.put("enrolling", enrolling);
        stats.put("ongoing", ongoing);
        stats.put("finished", finished);

        // Dashboard 课程热度：报名人数与课程管理一致（按课程名称汇总 current_students），出勤率来自预约表
        List<Map<String, Object>> hotByName = courseMapper.sumCurrentStudentsByCourseName(venueFilter, 10);
        List<Map<String, Object>> attendanceByName = courseBookingMapper.attendanceCountByCourseName(venueFilter);
        java.util.Map<String, Integer> attendanceMap = new HashMap<>();
        if (attendanceByName != null) {
            for (Map<String, Object> row : attendanceByName) {
                Object name = row.get("courseName");
                Object cnt = row.get("attendanceCount");
                if (name != null && cnt instanceof Number) {
                    attendanceMap.put(name.toString(), ((Number) cnt).intValue());
                }
            }
        }
        List<Map<String, Object>> hotCourses = new java.util.ArrayList<>();
        if (hotByName != null) {
            for (Map<String, Object> row : hotByName) {
                Object courseNameObj = row.get("courseName");
                Object signupCountObj = row.get("signupCount");
                if (courseNameObj == null) continue;
                int signupCount = signupCountObj instanceof Number ? ((Number) signupCountObj).intValue() : 0;
                int attendanceCount = attendanceMap.getOrDefault(courseNameObj.toString(), 0);
                double attendanceRate = signupCount > 0 ? (double) attendanceCount / signupCount : 0.0;
                Map<String, Object> item = new HashMap<>();
                item.put("courseName", courseNameObj.toString());
                item.put("name", courseNameObj.toString());
                item.put("signupCount", signupCount);
                item.put("bookingCount", signupCount);
                item.put("attendanceRate", attendanceRate);
                hotCourses.add(item);
            }
        }
        stats.put("hotCourses", hotCourses);
        
        return stats;
    }

    /**
     * 更新课程当前报名人数（使用乐观锁）
     * @param id 课程ID
     * @param currentStudents 当前报名人数
     * @return 影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCurrentStudents(Long id, Integer currentStudents) {
        // 查询课程信息获取版本号
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new ResourceNotFoundException("课程不存在");
        }

        // 验证报名人数不超过最大学员数
        if (currentStudents > course.getMaxStudents()) {
            throw new BusinessException("报名人数不能超过最大学员数");
        }

        // 验证报名人数不为负数
        if (currentStudents < 0) {
            throw new BusinessException("报名人数不能为负数");
        }

        // 使用乐观锁更新
        Integer version = course.getVersion() != null ? course.getVersion() : 0;
        int result = courseMapper.updateCurrentStudentsWithVersion(id, currentStudents, version);
        if (result == 0) {
            throw new BusinessException("报名人数更新失败，数据已被其他操作修改，请重试");
        }
        return result;
    }

    @Override
    public void autoUpdateCourseStatusByTime() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        courseMapper.batchUpdateEnrollingToOngoing(today, now);
        courseMapper.batchUpdateOngoingToFinished(today, now);
    }
}
