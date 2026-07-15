package com.badminton.bmp.modules.course.service.impl;

import com.badminton.bmp.common.util.AutoCancelRemarkUtil;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.config.PaymentAutoCancelProperties;
import com.badminton.bmp.modules.course.dto.AttendanceAction;
import com.badminton.bmp.modules.course.dto.AttendanceCommand;
import com.badminton.bmp.modules.course.dto.AttendanceCommandResult;
import com.badminton.bmp.modules.course.cache.CoachStudentRelationCacheInvalidator;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.mapper.CourseBookingMapper;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.course.service.CourseBookingService;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.finance.service.FinanceAuditService;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.websocket.WebSocketPushService;
import com.badminton.bmp.websocket.CoachStudentWebSocketEvent;
import com.badminton.bmp.websocket.CoachStudentWebSocketEventPublisher;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.badminton.bmp.modules.notification.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Clock;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 课程预约业务实现类
 * 处理课程预约相关的业务逻辑
 */
@Service
public class CourseBookingServiceImpl implements CourseBookingService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CourseBookingServiceImpl.class);
    @Autowired
    private CourseBookingMapper courseBookingMapper;
    @Autowired
    private CourseMapper courseMapper;
    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private MemberConsumeRecordService memberConsumeRecordService;
    @Autowired
    private CourtMapper courtMapper;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private FinanceAuditService financeAuditService;
    @Autowired
    private WebSocketPushService webSocketPushService;
    @Autowired
    private CoachStudentWebSocketEventPublisher coachStudentWebSocketEventPublisher;
    @Autowired
    private CoachMapper coachMapper;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private PaymentAutoCancelProperties paymentAutoCancelProperties;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired(required = false)
    private CoachStudentRelationCacheInvalidator coachStudentRelationCacheInvalidator;
    private Clock clock = Clock.systemDefaultZone();

    /**
     * 根据ID查找课程预约记录
     * @param id 预约记录ID
     * @return 预约记录对象
     */
    @Override
    public CourseBooking findById(Long id) {
        CourseBooking booking = courseBookingMapper.findById(id);
        if (booking == null) {
            return null;
        }
        
        // 数据范围过滤：根据角色检查访问权限
        if (SecurityUtils.isPresident()) {
            // PRESIDENT: 可以访问所有数据
            return booking;
        } else if (SecurityUtils.isVenueManager()) {
            // VENUE_MANAGER: 只能访问自己场馆的数据
            Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
            if (currentVenueId != null) {
                // 需要通过课程->场地->场馆查询
                Course course = courseMapper.findById(booking.getCourseId());
                if (course != null && course.getCourtId() != null) {
                    Court court = courtMapper.findById(course.getCourtId());
                    if (court != null && currentVenueId.equals(court.getVenueId())) {
                        return booking;
                    }
                }
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        } else {
            // USER: 只能访问自己的预约
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null && m.getId().equals(booking.getMemberId())) {
                    return booking;
                }
            }
            throw new org.springframework.security.access.AccessDeniedException("权限不足，拒绝访问");
        }
    }

    /**
     * 查找所有课程预约记录（支持条件筛选和分页）
     * @param memberId 会员ID（可选）
     * @param courseId 课程ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配预约单号、会员姓名、课程名称）
     * @param page 页码
     * @param size 每页数量
     * @return 预约记录列表
     */
    @Override
    public List<CourseBooking> findAll(Long memberId, Long courseId, Integer status, String keyword, int page, int size) {
        // 验证分页参数
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 10;
        }
        int offset = (page - 1) * size;
        // 数据范围过滤
        Long venueFilter = null;
        Long memberFilter = memberId;
        if (com.badminton.bmp.common.util.SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：仅能查看自己的课程预约，忽略前端传入的 memberId，防止水平越权
            com.badminton.bmp.modules.system.entity.User current = com.badminton.bmp.common.util.SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) memberFilter = m.getId();
                else memberFilter = null;
            } else {
                memberFilter = null;
            }
        }
        return courseBookingMapper.findAll(venueFilter, memberFilter, courseId, status, keyword, offset, size);
    }

    /**
     * 统计课程预约记录数量（支持条件筛选）
     * @param memberId 会员ID（可选）
     * @param courseId 课程ID（可选）
     * @param status 状态（可选）
     * @param keyword 关键词（匹配预约单号、会员姓名、课程名称）
     * @return 预约记录数量
     */
    @Override
    public int count(Long memberId, Long courseId, Integer status, String keyword) {
        Long venueFilter = null;
        Long memberFilter = memberId;
        if (com.badminton.bmp.common.util.SecurityUtils.isPresident()) {
            venueFilter = null;
        } else if (com.badminton.bmp.common.util.SecurityUtils.isVenueManager()) {
            venueFilter = com.badminton.bmp.common.util.SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：仅统计自己的课程预约，忽略前端传入的 memberId，防止水平越权
            com.badminton.bmp.modules.system.entity.User current = com.badminton.bmp.common.util.SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) memberFilter = m.getId();
                else memberFilter = null;
            } else {
                memberFilter = null;
            }
        }
        return courseBookingMapper.count(venueFilter, memberFilter, courseId, status, keyword);
    }

    @Override
    public List<CourseBooking> findAllForCoach(Long coachId, Long courseId, Integer status, String keyword, int page, int size) {
        if (coachId == null) return java.util.Collections.emptyList();
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        int offset = (page - 1) * size;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        return courseBookingMapper.findAllByCoachId(coachId, courseId, status, keyword, offset, size);
    }

    @Override
    public int countForCoach(Long coachId, Long courseId, Integer status, String keyword) {
        if (coachId == null) return 0;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        return courseBookingMapper.countByCoachId(coachId, courseId, status, keyword);
    }

    @Override
    public CourseBooking findByIdForCoach(Long coachId, Long id) {
        if (coachId == null || id == null) {
            return null;
        }
        return courseBookingMapper.findByIdAndCoachId(coachId, id);
    }

    @Override
    public CourseBooking findLatestActiveByMemberAndCourse(Long memberId, Long courseId) {
        if (memberId == null || courseId == null) {
            return null;
        }
        return courseBookingMapper.findLatestActiveByMemberIdAndCourseId(memberId, courseId);
    }

    @Override
    public Map<Long, CourseBooking> findLatestActiveByMemberAndCourses(Long memberId, List<Long> courseIds) {
        if (memberId == null || courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> distinctIds = courseIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, CourseBooking> latestByCourse = new HashMap<>();
        for (CourseBooking booking : courseBookingMapper.findActiveByMemberAndCourseIds(memberId, distinctIds)) {
            if (booking == null || booking.getCourseId() == null) {
                continue;
            }
            latestByCourse.putIfAbsent(booking.getCourseId(), booking);
        }
        return latestByCourse;
    }

    private void evictCourseCache(Long courseId) {
        if (courseId == null || cacheManager == null) {
            return;
        }
        Cache cache = cacheManager.getCache("course");
        if (cache != null) {
            cache.evict(courseId);
        }
    }

    @Override
    @Transactional
    public int updateStatusForCoach(Long coachId, Long id, Integer status, String remark) {
        if (coachId == null) {
            throw new BusinessException("未绑定教练档案，请联系管理员处理");
        }
        if (id == null) {
            throw new BusinessException("预约记录ID不能为空");
        }
        if (status == null || status != 0) {
            throw new BusinessException("该接口仅允许在课程开始前取消预约");
        }

        CourseBooking booking = courseBookingMapper.findByIdAndCoachId(coachId, id);
        if (booking == null) {
            throw new org.springframework.security.access.AccessDeniedException("预约记录不存在或无权操作");
        }

        Integer oldStatus = booking.getStatus();
        if (oldStatus == null) {
            throw new BusinessException("预约状态异常，无法操作");
        }
        if (oldStatus == 0) {
            return 0;
        }
        if (oldStatus == 4) {
            throw new BusinessException("该预约已完成，不能取消");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = resolveCourseDateTime(booking.getCourseDate(), booking.getCourseStartTime(), "课程开始时间缺失");
        if (!now.isBefore(start)) {
            throw new BusinessException("课程开始后不能取消预约，请使用独立考勤操作");
        }

        String nextRemark = normalizeRemark(remark);
        int result = courseBookingMapper.cancelBeforeStartForCoach(id, coachId, oldStatus, nextRemark, now);
        if (result > 0) {
            if (oldStatus >= 1 && oldStatus <= 3 && courseMapper != null) {
                decrementCourseCurrentStudents(booking.getCourseId());
            }
            evictCourseCache(booking.getCourseId());
            invalidateForBooking(booking);
            return result;
        }
        CourseBooking current = courseBookingMapper.findByIdAndCoachId(coachId, id);
        if (current != null && Integer.valueOf(0).equals(current.getStatus())) {
            return 0;
        }
        throw new BusinessException(409, "预约状态已变化，请刷新后重试");
    }

    @Override
    @Transactional
    public AttendanceCommandResult updateAttendanceForCoach(Long coachId, AttendanceCommand command) {
        if (coachId == null) {
            throw new BusinessException("未绑定教练档案，请联系管理员处理");
        }
        if (command == null || command.getBookingId() == null || command.getAction() == null) {
            throw new BusinessException("预约ID和考勤操作不能为空");
        }

        CourseBooking booking = courseBookingMapper.findByIdAndCoachId(coachId, command.getBookingId());
        if (booking == null) {
            throw new org.springframework.security.access.AccessDeniedException("预约记录不存在或无权操作");
        }
        AttendanceAction action = command.getAction();
        int attendanceStatus = booking.getAttendanceStatus() == null ? 0 : booking.getAttendanceStatus();
        if ((action == AttendanceAction.CHECK_IN && attendanceStatus == 1)
                || (action == AttendanceAction.COMPLETE && attendanceStatus == 2)
                || (action == AttendanceAction.ABSENT && attendanceStatus == 3)) {
            return AttendanceCommandResult.from(booking);
        }
        if (Integer.valueOf(0).equals(booking.getCourseStatus())) {
            throw new BusinessException("课程已取消，不能登记考勤");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = resolveCourseDateTime(booking.getCourseDate(), booking.getCourseStartTime(), "课程开始时间缺失");
        LocalDateTime end = resolveCourseDateTime(booking.getCourseDate(), booking.getCourseEndTime(), "课程结束时间缺失");
        if (now.isBefore(start)) {
            throw new BusinessException("课程开始前不能登记考勤");
        }
        if (now.isAfter(end.plusHours(24))) {
            throw new BusinessException("已超过课程结束后24小时纠错窗口，请联系管理员处理");
        }

        int bookingStatus = booking.getStatus() == null ? -1 : booking.getStatus();
        int targetBookingStatus;
        int targetAttendanceStatus;
        LocalDateTime checkinTime = null;
        LocalDateTime finishTime = null;
        boolean clearActualTimes = false;

        switch (action) {
            case CHECK_IN -> {
                if (attendanceStatus == 1) {
                    return AttendanceCommandResult.from(booking);
                }
                if (attendanceStatus != 0 || (bookingStatus != 2 && bookingStatus != 3)) {
                    throw new BusinessException("当前状态不允许签到");
                }
                targetBookingStatus = 3;
                targetAttendanceStatus = 1;
                checkinTime = now;
            }
            case COMPLETE -> {
                if (attendanceStatus == 2) {
                    return AttendanceCommandResult.from(booking);
                }
                if ((attendanceStatus != 1 && attendanceStatus != 3)
                        || (bookingStatus != 3 && bookingStatus != 4)) {
                    throw new BusinessException("当前状态不允许完成考勤");
                }
                targetBookingStatus = 4;
                targetAttendanceStatus = 2;
                finishTime = now;
            }
            case ABSENT -> {
                if (attendanceStatus == 3) {
                    return AttendanceCommandResult.from(booking);
                }
                if (attendanceStatus < 0 || attendanceStatus > 2
                        || (bookingStatus != 2 && bookingStatus != 3 && bookingStatus != 4)) {
                    throw new BusinessException("当前状态不允许登记缺席");
                }
                targetBookingStatus = bookingStatus;
                targetAttendanceStatus = 3;
                clearActualTimes = true;
            }
            default -> throw new BusinessException("不支持的考勤操作");
        }

        int updated = courseBookingMapper.updateAttendanceWithExpectedState(
                booking.getId(), coachId, bookingStatus, attendanceStatus,
                targetBookingStatus, targetAttendanceStatus, normalizeRemark(command.getRemark()),
                checkinTime, finishTime, clearActualTimes, now);
        CourseBooking current = courseBookingMapper.findByIdAndCoachId(coachId, booking.getId());
        if (current == null) {
            throw new org.springframework.security.access.AccessDeniedException("预约记录不存在或无权操作");
        }
        if (updated == 0 && (!Integer.valueOf(targetBookingStatus).equals(current.getStatus())
                || !Integer.valueOf(targetAttendanceStatus).equals(normalizeAttendanceStatus(current.getAttendanceStatus())))) {
            throw new BusinessException(409, "考勤状态已被其他操作修改，请刷新后重试");
        }
        if (updated > 0 && bookingStatus == 3 && targetBookingStatus == 4) {
            decrementCourseCurrentStudents(booking.getCourseId());
        }
        evictCourseCache(booking.getCourseId());
        invalidateForBooking(booking);
        if (updated > 0) {
            publishCoachStudentEvent(
                    CoachStudentWebSocketEvent.TYPE_ATTENDANCE_CHANGED,
                    current,
                    current.getStatus(),
                    normalizeAttendanceStatus(current.getAttendanceStatus())
            );
        }
        return AttendanceCommandResult.from(current);
    }

    private Integer normalizeAttendanceStatus(Integer status) {
        return status == null ? 0 : status;
    }

    private String normalizeRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String normalized = remark.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private LocalDateTime resolveCourseDateTime(LocalDate date, LocalTime time, String message) {
        if (date == null || time == null) {
            throw new BusinessException(message);
        }
        return LocalDateTime.of(date, time);
    }

    private void invalidateForBooking(CourseBooking booking) {
        if (coachStudentRelationCacheInvalidator != null && booking != null) {
            coachStudentRelationCacheInvalidator.invalidateForBooking(booking.getMemberId());
        }
    }

    /**
     * 添加课程预约记录
     * @param booking 预约记录对象
     * @return 影响行数
     */
    @Override
    @Transactional
    public int add(CourseBooking booking) {
        // 权限校验与默认值：USER 只能为自己创建预约，且可自动补全 memberId/orderAmount
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current == null) {
                throw new RuntimeException("权限不足");
            }
            Member m = memberMapper.findByUserId(current.getId());
            if (m == null) {
                throw new RuntimeException("当前用户未关联会员，无法预约课程");
            }
            if (booking.getMemberId() == null) {
                booking.setMemberId(m.getId());
            } else if (!m.getId().equals(booking.getMemberId())) {
                throw new RuntimeException("权限不足，只能为自己创建预约");
            }
        }
        
        // 验证会员是否存在
        Member member = memberMapper.findById(booking.getMemberId());
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 验证课程是否存在
        Course course = courseMapper.findById(booking.getCourseId());
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        ensureCourseSelectableForBooking(course);
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null && current.getId() != null) {
                com.badminton.bmp.modules.coach.entity.Coach currentCoach = coachMapper.findByUserId(current.getId());
                if (currentCoach != null && currentCoach.getId() != null && currentCoach.getId().equals(course.getCoachId())) {
                    throw new RuntimeException("不能预约自己授课的课程");
                }
            }
        }
        CourseBooking latestActiveBooking = courseBookingMapper.findLatestActiveByMemberIdAndCourseId(
                booking.getMemberId(), booking.getCourseId());
        if (latestActiveBooking != null) {
            throw new RuntimeException("您已预约该课程，请勿重复提交");
        }
        
        // 未传金额时使用课程价格
        if (booking.getOrderAmount() == null || booking.getOrderAmount().signum() <= 0) {
            booking.setOrderAmount(course.getCoursePrice() != null ? course.getCoursePrice() : java.math.BigDecimal.ZERO);
        }
        
        // 验证课程报名人数是否已满（老数据 currentStudents 可能为 null，这里按 0 处理）
        int currentStudents = course.getCurrentStudents() != null ? course.getCurrentStudents() : 0;
        if (currentStudents >= course.getMaxStudents()) {
            throw new RuntimeException("课程报名人数已满");
        }
        
        // 生成预约单号（如果未提供）
        if (booking.getBookingNo() == null || booking.getBookingNo().trim().isEmpty()) {
            booking.setBookingNo(generateBookingNo());
        }
        
        // 验证预约单号是否已存在
        if (courseBookingMapper.existsByBookingNo(booking.getBookingNo(), null)) {
            throw new RuntimeException("预约单号已存在");
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        booking.setCreateTime(now);
        booking.setUpdateTime(now);
        
        // 设置默认值；新增订单强制为未支付，仅点击支付按钮后才扣款
        if (booking.getStatus() == null) {
            booking.setStatus(1);
        }
        booking.setPaymentStatus(0);
        
        // 插入预约记录
        int result = courseBookingMapper.insert(booking);
        
        // 如果插入成功，更新课程的当前报名人数
        if (result > 0) {
            incrementCourseCurrentStudents(booking.getCourseId());
            evictCourseCache(booking.getCourseId());
            invalidateForBooking(booking);
            publishCoachStudentEvent(CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking, null, null);
        }
        
        return result;
    }

    /**
     * 更新课程预约记录
     * @param booking 预约记录对象
     * @return 影响行数
     */
    @Override
    @Transactional
    public int update(CourseBooking booking) {
        // 验证预约记录是否存在
        CourseBooking existing = courseBookingMapper.findById(booking.getId());
        if (existing == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能修改自己场馆的预约
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Course course = courseMapper.findById(existing.getCourseId());
                    if (course != null && course.getCourtId() != null) {
                        Court court = courtMapper.findById(course.getCourtId());
                        if (court == null || !currentVenueId.equals(court.getVenueId())) {
                            throw new RuntimeException("权限不足，只能修改自己场馆的预约");
                        }
                    }
                }
            } else {
                // USER: 只能修改自己的预约
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(existing.getMemberId())) {
                        throw new RuntimeException("权限不足，只能修改自己的预约");
                    }
                } else {
                    throw new RuntimeException("权限不足");
                }
            }
        }
        
        Long originalCourseId = existing.getCourseId();
        Long courseId = booking.getCourseId() != null ? booking.getCourseId() : existing.getCourseId();
        Integer oldStatus = existing.getStatus();
        Integer newStatus = booking.getStatus() != null ? booking.getStatus() : existing.getStatus();
        boolean studentCancellation = isStudentSelfServiceActor()
                && oldStatus != null && oldStatus >= 1 && oldStatus <= 3
                && Integer.valueOf(0).equals(newStatus);
        
        // 如果修改了课程ID，需要处理报名人数的变化
        if (booking.getCourseId() != null && !booking.getCourseId().equals(existing.getCourseId())) {
            Course newCourse = courseMapper.findById(booking.getCourseId());
            if (newCourse == null) {
                throw new RuntimeException("新课程不存在");
            }
            ensureCourseSelectableForBooking(newCourse);

            // 从旧课程减少报名人数（如果旧状态是有效状态）
            if (oldStatus != null && oldStatus >= 1 && oldStatus <= 3) {
                decrementCourseCurrentStudents(existing.getCourseId());
            }

            // 向新课程增加报名人数（需要验证新课程是否还有名额）
            if (newStatus != null && newStatus >= 1 && newStatus <= 3) {
                incrementCourseCurrentStudents(booking.getCourseId(), "新课程报名人数已满");
            }
        } else if (newStatus != null && !newStatus.equals(oldStatus)) {
            // 如果只修改了状态（未修改课程），需要处理报名人数的变化
            Course course = courseMapper.findById(courseId);
            if (course != null) {
                boolean oldStatusValid = oldStatus != null && oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = newStatus >= 1 && newStatus <= 3;
                
                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），减少报名人数
                    decrementCourseCurrentStudents(courseId);
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，增加报名人数
                    incrementCourseCurrentStudents(courseId);
                }
            }
        }
        
        // 设置更新时间
        booking.setUpdateTime(LocalDateTime.now());
        int result = courseBookingMapper.update(booking);
        if (result > 0) {
            evictCourseCache(originalCourseId);
            evictCourseCache(courseId);
            invalidateForBooking(existing);
            if (studentCancellation) {
                publishCoachStudentEvent(CoachStudentWebSocketEvent.TYPE_CANCELLED, existing, null, null);
            }
        }
        return result;
    }

    /**
     * 删除课程预约记录（逻辑删除）
     * @param id 预约记录ID
     * @return 影响行数
     */
    @Override
    @Transactional
    public int deleteById(Long id) {
        // 验证预约记录是否存在
        CourseBooking booking = courseBookingMapper.findById(id);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能删除自己场馆的预约
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Course course = courseMapper.findById(booking.getCourseId());
                    if (course != null && course.getCourtId() != null) {
                        Court court = courtMapper.findById(course.getCourtId());
                        if (court == null || !currentVenueId.equals(court.getVenueId())) {
                            throw new RuntimeException("权限不足，只能删除自己场馆的预约");
                        }
                    }
                }
            } else {
                // USER: 只能删除自己的预约
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(booking.getMemberId())) {
                        throw new RuntimeException("权限不足，只能删除自己的预约");
                    }
                } else {
                    throw new RuntimeException("权限不足");
                }
            }
        }
        
        // 如果状态是有效状态（1-待支付，2-已支付，3-进行中），都需要减少课程的当前报名人数
        if (booking.getStatus() != null && booking.getStatus() >= 1 && booking.getStatus() <= 3) {
            decrementCourseCurrentStudents(booking.getCourseId());
        }
        
        int result = courseBookingMapper.deleteById(id);
        if (result > 0) {
            evictCourseCache(booking.getCourseId());
            invalidateForBooking(booking);
            if (isStudentSelfServiceActor()
                    && booking.getStatus() != null && booking.getStatus() >= 1 && booking.getStatus() <= 3) {
                publishCoachStudentEvent(CoachStudentWebSocketEvent.TYPE_CANCELLED, booking, null, null);
            }
        }
        return result;
    }

    /**
     * 更新课程预约状态
     * @param id 预约记录ID
     * @param status 状态值（0-已取消，1-待支付，2-已支付，3-进行中，4-已完成）
     * @return 影响行数
     */
    @Override
    @Transactional
    public int updateStatus(Long id, Integer status) {
        // 验证预约记录是否存在
        CourseBooking booking = courseBookingMapper.findById(id);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能修改自己场馆的预约状态
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Course course = courseMapper.findById(booking.getCourseId());
                    if (course != null && course.getCourtId() != null) {
                        Court court = courtMapper.findById(course.getCourtId());
                        if (court == null || !currentVenueId.equals(court.getVenueId())) {
                            throw new RuntimeException("权限不足，只能修改自己场馆的预约状态");
                        }
                    }
                }
            } else {
                // USER: 只能修改自己的预约状态（通常只能取消）
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(booking.getMemberId())) {
                        throw new RuntimeException("权限不足，只能修改自己的预约状态");
                    }
                    // USER只能取消预约（status=0），不能设置为其他状态
                    if (status != null && status != 0) {
                        throw new RuntimeException("普通用户只能取消预约");
                    }
                } else {
                    throw new RuntimeException("权限不足");
                }
            }
        }
        
        // 验证状态值是否有效
        if (status == null || status < 0 || status > 4) {
            throw new RuntimeException("无效的状态值，必须是0（已取消）、1（待支付）、2（已支付）、3（进行中）或4（已完成）");
        }
        
        // 如果状态变更，需要处理报名人数的变化
        Integer oldStatus = booking.getStatus();
        if (oldStatus != null && !oldStatus.equals(status)) {
            Course course = courseMapper.findById(booking.getCourseId());
            if (course != null) {
                boolean oldStatusValid = oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = status >= 1 && status <= 3;
                
                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），减少报名人数
                    decrementCourseCurrentStudents(booking.getCourseId());
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，增加报名人数
                    incrementCourseCurrentStudents(booking.getCourseId());
                }
            }
        }
        
        int result;
        if (status == 0 && booking.getPaymentStatus() != null && booking.getPaymentStatus() == 1) {
            booking.setStatus(0);
            booking.setPaymentStatus(3);
            booking.setUpdateTime(LocalDateTime.now());
            result = courseBookingMapper.update(booking);
        } else {
            result = courseBookingMapper.updateStatus(id, status);
        }
        if (result > 0) {
            evictCourseCache(booking.getCourseId());
            invalidateForBooking(booking);
            if (isStudentSelfServiceActor()
                    && oldStatus != null && oldStatus >= 1 && oldStatus <= 3
                    && status == 0) {
                publishCoachStudentEvent(CoachStudentWebSocketEvent.TYPE_CANCELLED, booking, null, null);
            }
        }
        try {
            Long userId = null;
            Member m = memberMapper.findById(booking.getMemberId());
            if (m != null && m.getUserId() != null) userId = m.getUserId();
            String statusText = courseBookingStatusText(status);
            webSocketPushService.pushOrderStatusToUser(userId, "courseBooking", id, status, statusText, "课程预约");
            webSocketPushService.pushDashboardRefresh();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(CourseBookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
        return result;
    }

    private static String courseBookingStatusText(int status) {
        switch (status) {
            case 0: return "已取消";
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "进行中";
            case 4: return "已完成";
            default: return "未知";
        }
    }

    private void publishCoachStudentEvent(
            String type,
            CourseBooking booking,
            Integer bookingStatus,
            Integer attendanceStatus
    ) {
        if (coachStudentWebSocketEventPublisher != null) {
            coachStudentWebSocketEventPublisher.publishAfterCommit(
                    type, booking, bookingStatus, attendanceStatus);
        }
    }

    private boolean isStudentSelfServiceActor() {
        return SecurityUtils.isUser()
                && !SecurityUtils.isPresident()
                && !SecurityUtils.isVenueManager();
    }

    /**
     * 获取课程预约统计信息
     * @return 统计信息（总预约数、各状态数量）
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }
        
        // 获取总预约数（场馆管理员仅本场馆）
        int total = venueFilter != null
            ? courseBookingMapper.countAllFiltered(venueFilter)
            : courseBookingMapper.countAll();
        stats.put("total", total);
        
        // 获取各状态的预约数量
        List<Map<String, Object>> statusCounts = venueFilter != null
            ? courseBookingMapper.countByStatusFiltered(venueFilter)
            : courseBookingMapper.countByStatus();
        
        // 初始化各状态数量
        int cancelled = 0;  // 已取消（0）
        int pending = 0;     // 待支付（1）
        int paid = 0;       // 已支付（2）
        int ongoing = 0;    // 进行中（3）
        int finished = 0;   // 已完成（4）
        
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
                    pending = count;
                    break;
                case 2:
                    paid = count;
                    break;
                case 3:
                    ongoing = count;
                    break;
                case 4:
                    finished = count;
                    break;
            }
        }
        
        stats.put("cancelled", cancelled);
        stats.put("pending", pending);
        stats.put("paid", paid);
        stats.put("ongoing", ongoing);
        stats.put("finished", finished);

        LocalDate today = LocalDate.now();
        LocalDate currentWeekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate startWeek = currentWeekStart.minusWeeks(7);
        LocalDate endWeek = currentWeekStart.plusDays(6);

        List<Map<String, Object>> weeklyRows = courseMapper.sumWeeklyCapacityStats(venueFilter, startWeek, endWeek);
        Map<LocalDate, Map<String, Object>> weeklyRowMap = new HashMap<>();
        if (weeklyRows != null) {
            for (Map<String, Object> row : weeklyRows) {
                Object weekStartObj = row.get("weekStart");
                if (weekStartObj == null) {
                    weekStartObj = row.get("WEEKSTART");
                }
                if (weekStartObj == null) {
                    continue;
                }
                LocalDate weekStartDate = weekStartObj instanceof LocalDate
                        ? (LocalDate) weekStartObj
                        : LocalDate.parse(weekStartObj.toString().substring(0, 10));
                weeklyRowMap.put(weekStartDate, row);
            }
        }

        List<Map<String, Object>> weeklyCapacity = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM/dd");
        for (int i = 0; i < 8; i++) {
            LocalDate weekStartDate = startWeek.plusWeeks(i);
            LocalDate weekEndDate = weekStartDate.plusDays(6);
            Map<String, Object> row = weeklyRowMap.get(weekStartDate);
            int bookedStudents = row != null && row.get("bookedStudents") instanceof Number
                    ? ((Number) row.get("bookedStudents")).intValue() : 0;
            int totalCapacity = row != null && row.get("totalCapacity") instanceof Number
                    ? ((Number) row.get("totalCapacity")).intValue() : 0;
            double rate = totalCapacity > 0 ? (bookedStudents * 100.0) / totalCapacity : 0D;

            Map<String, Object> w = new HashMap<>();
            String weekLabel = labelFormatter.format(weekStartDate) + "-" + labelFormatter.format(weekEndDate);
            w.put("week", weekLabel);
            w.put("label", weekLabel);
            w.put("rate", rate);
            w.put("capacityRate", rate);
            w.put("bookedStudents", bookedStudents);
            w.put("totalCapacity", totalCapacity);
            weeklyCapacity.add(w);
        }
        stats.put("weeklyCapacity", weeklyCapacity);
        
        return stats;
    }

    /**
     * 生成预约单号
     * 格式：CB + yyyyMMdd + 三位序号（如：CB20240101001）
     * @return 预约单号
     */
    @Override
    public synchronized String generateBookingNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CB" + dateStr;
        // 不区分逻辑删除，从全表中查出当前日期前缀下最大的预约单号，避免重复
        String maxNo = courseBookingMapper.findMaxBookingNoByPrefix(prefix);
        int nextSeq = 1;
        if (maxNo != null && maxNo.startsWith(prefix)) {
            try {
                String seq = maxNo.substring(prefix.length());
                nextSeq = Integer.parseInt(seq) + 1;
            } catch (NumberFormatException ignored) {
                // 忽略格式异常，退回到默认 001
            }
        }
        return prefix + String.format("%03d", nextSeq);
    }

    /**
     * 处理支付（含余额扣减、消费记录）
     * @param bookingId 预约ID
     * @param paymentMethod 支付方式
     * @return 处理结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPayment(Long bookingId, String paymentMethod) {
        return processPaymentInternal(bookingId, paymentMethod, null, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processMemberPayment(Long bookingId, String paymentMethod, Long userId) {
        return processPaymentInternal(bookingId, paymentMethod, userId, false);
    }

    @Transactional(rollbackFor = Exception.class)
    protected int processPaymentInternal(Long bookingId, String paymentMethod, Long userId, boolean adminMode) {
        if (!"BALANCE".equals(paymentMethod)) {
            throw new RuntimeException("业务订单仅支持余额支付");
        }

        // 查询预约记录
        CourseBooking booking = courseBookingMapper.findById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }

        if (adminMode) {
            // 权限兜底：只有管理员，且 VM 仅限自己场馆
            if (!SecurityUtils.isPresident()) {
                if (SecurityUtils.isVenueManager()) {
                    Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                    Course course = courseMapper.findById(booking.getCourseId());
                    Court court = (course != null && course.getCourtId() != null) ? courtMapper.findById(course.getCourtId()) : null;
                    if (currentVenueId == null || court == null || !currentVenueId.equals(court.getVenueId())) {
                        throw new RuntimeException("权限不足，只能处理自己场馆的课程预约支付");
                    }
                } else {
                    throw new RuntimeException("权限不足，仅管理员可执行此操作");
                }
            }
        } else {
            if (userId == null) {
                throw new RuntimeException("未登录或Token无效");
            }
            Member currentMember = memberMapper.findByUserId(userId);
            if (currentMember == null) {
                throw new RuntimeException("当前用户未绑定会员信息，无法支付课程预约");
            }
            if (!currentMember.getId().equals(booking.getMemberId())) {
                throw new RuntimeException("权限不足，只能支付自己的课程预约订单");
            }
        }

        // 验证预约状态，按状态返回明确提示
        Integer status = booking.getStatus();
        if (status == null || status != 1) {
            String msg;
            if (status == null) {
                msg = "预约状态异常，无法支付";
            } else if (status == 0) {
                msg = "该课程预约已取消，无法支付";
            } else if (status == 2) {
                msg = "该课程预约已支付，无需重复支付";
            } else if (status == 3) {
                msg = "该课程预约进行中，无法重复支付";
            } else if (status == 4) {
                msg = "该课程预约已完成，无法支付";
            } else {
                msg = "预约状态不正确，只能对待支付状态的预约进行支付";
            }
            throw new RuntimeException(msg);
        }

        LocalDateTime now = LocalDateTime.now();
        if (isPendingPaymentExpired(booking.getCreateTime(), now)) {
            throw new RuntimeException("该课程预约已超出支付时限，系统已自动取消或正在取消，无法支付");
        }
        LocalDateTime expireBefore = resolvePendingPaymentExpireBefore(now);
        int updated = courseBookingMapper.markPaidIfPending(bookingId, paymentMethod, now, expireBefore);
        if (updated <= 0) {
            throw new RuntimeException(resolvePendingPaymentConflictMessage(courseBookingMapper.findById(bookingId), "课程预约"));
        }

        memberConsumeRecordService.createConsumeRecord(
            booking.getMemberId(),
            booking.getOrderAmount(),
            "COURSE",
            bookingId,
            paymentMethod,
            "课程预约支付：" + booking.getBookingNo()
        );

        // 获取场馆ID用于财务记录
        Course courseForFinance = courseMapper.findById(booking.getCourseId());
        Court courtForFinance = (courseForFinance != null && courseForFinance.getCourtId() != null)
            ? courtMapper.findById(courseForFinance.getCourtId()) : null;
        Long venueIdForFinance = courtForFinance != null ? courtForFinance.getVenueId() : null;

        // 创建财务记录（收入）
        financeService.createFromBusiness(
            Finance.TYPE_COURSE,
            bookingId,
            booking.getOrderAmount(),
            Finance.INCOME,
            paymentMethod,
            venueIdForFinance,
            "课程预约支付：" + booking.getBookingNo()
        );
        CourseBooking refreshedBooking = courseBookingMapper.findById(bookingId);
        Integer refreshedPaymentStatus = refreshedBooking != null ? refreshedBooking.getPaymentStatus() : null;
        Integer refreshedStatus = refreshedBooking != null ? refreshedBooking.getStatus() : null;
        if (refreshedPaymentStatus == null || refreshedPaymentStatus != 1 || refreshedStatus == null || refreshedStatus == 1) {
            org.slf4j.LoggerFactory.getLogger(CourseBookingServiceImpl.class).error(
                    "支付后课程预约状态异常, bookingId={}, actualPaymentStatus={}, actualStatus={}",
                    bookingId, refreshedPaymentStatus, refreshedStatus
            );
            throw new RuntimeException("支付成功后课程预约状态异常，请稍后重试");
        }
        if (updated > 0) {
            evictCourseCache(booking.getCourseId());
            try {
                Long notifyUserId = null;
                Member m = memberMapper.findById(booking.getMemberId());
                if (m != null && m.getUserId() != null) notifyUserId = m.getUserId();
                webSocketPushService.pushOrderStatusToUser(notifyUserId, "courseBooking", bookingId, 2, "已支付", "课程预约");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(CourseBookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return updated;
    }

    /**
     * 处理退款（含余额回滚、消费记录冲正）
     * @param bookingId 预约ID
     * @return 处理结果
     */
    private LocalDateTime resolvePendingPaymentExpireBefore(LocalDateTime now) {
        if (paymentAutoCancelProperties == null || !paymentAutoCancelProperties.isEnabled()) {
            return null;
        }
        return now.minus(paymentAutoCancelProperties.getTimeoutDuration());
    }

    private boolean isPendingPaymentExpired(LocalDateTime createTime, LocalDateTime now) {
        LocalDateTime expireBefore = resolvePendingPaymentExpireBefore(now);
        return expireBefore != null && createTime != null && !createTime.isAfter(expireBefore);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(Long bookingId) {
        // 查询预约记录
        CourseBooking booking = courseBookingMapper.findById(bookingId);
        if (booking == null) {
            throw new RuntimeException("预约记录不存在");
        }

        // 权限兜底：只有管理员，且 VM 仅限自己场馆
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                Course course = courseMapper.findById(booking.getCourseId());
                Court court = (course != null && course.getCourtId() != null) ? courtMapper.findById(course.getCourtId()) : null;
                if (currentVenueId == null || court == null || !currentVenueId.equals(court.getVenueId())) {
                    throw new RuntimeException("权限不足，只能处理自己场馆的课程预约退款");
                }
            } else {
                throw new RuntimeException("权限不足，仅管理员可执行此操作");
            }
        }

        // 验证预约状态，按状态返回明确提示
        Integer payStatus = booking.getPaymentStatus();
        if (payStatus == null || (payStatus != 1 && payStatus != 3)) {
            String errMsg;
            if (payStatus == null || payStatus == 0) {
                errMsg = "该课程预约尚未支付，无法退款";
            } else if (payStatus == 2) {
                errMsg = "该课程预约已退款，无需重复退款";
            } else {
                errMsg = "只能对已支付的预约进行退款";
            }
            throw new RuntimeException(errMsg);
        }

        // 调用退款记录服务（只有余额支付才需要）
        if ("BALANCE".equals(booking.getPaymentMethod())) {
            memberConsumeRecordService.createRefundRecord(
                booking.getMemberId(),
                booking.getOrderAmount(),
                "COURSE",
                bookingId,
                booking.getPaymentMethod(),
                "课程预约退款：" + booking.getBookingNo()
            );
        }

        // 获取场馆ID用于财务记录
        Course courseForRefund = courseMapper.findById(booking.getCourseId());
        Court courtForRefund = (courseForRefund != null && courseForRefund.getCourtId() != null)
            ? courtMapper.findById(courseForRefund.getCourtId()) : null;
        Long venueIdForRefund = courtForRefund != null ? courtForRefund.getVenueId() : null;

        // 创建财务记录（支出/退款）
        financeService.createFromBusiness(
            Finance.TYPE_COURSE,
            bookingId,
            booking.getOrderAmount(),
            Finance.EXPENSE,
            booking.getPaymentMethod(),
            venueIdForRefund,
            "课程预约退款：" + booking.getBookingNo()
        );

        // 更新预约状态
        booking.setPaymentStatus(2); // 已退款
        booking.setStatus(0); // 已取消
        booking.setUpdateTime(LocalDateTime.now());
        int result = courseBookingMapper.update(booking);

        // 减少课程的当前报名人数
        decrementCourseCurrentStudents(booking.getCourseId());
        if (result > 0) {
            evictCourseCache(booking.getCourseId());
            try {
                Long userId = null;
                Member m = memberMapper.findById(booking.getMemberId());
                if (m != null && m.getUserId() != null) userId = m.getUserId();
                webSocketPushService.pushOrderStatusToUser(userId, "courseBooking", bookingId, 0, "已取消", "课程预约");
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(CourseBookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return result;
    }

    @Override
    public int autoCancelExpiredUnpaidOrders(LocalDateTime cutoff) {
        if (cutoff == null) {
            return 0;
        }
        List<CourseBooking> bookings = courseBookingMapper.findExpiredUnpaidBookings(cutoff);
        if (bookings == null || bookings.isEmpty()) {
            return 0;
        }
        int cancelled = 0;
        for (CourseBooking booking : bookings) {
            if (booking == null || booking.getId() == null) {
                continue;
            }
            cancelled += runAutoCancelInNewTransaction(() -> autoCancelSingleBooking(booking));
        }
        return cancelled;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateCourseBookingStatusByTime() {
        LocalDateTime current = LocalDateTime.now(clock);
        LocalDate today = current.toLocalDate();
        LocalTime now = current.toLocalTime();
        List<Long> toStart = courseBookingMapper.findBookingIdsToStart(today, now);
        if (toStart != null) {
            for (Long id : toStart) {
                if (courseBookingMapper.startBookingIfPaid(id, current) > 0) {
                    invalidateForBooking(courseBookingMapper.findById(id));
                }
            }
        }
        List<Long> toFinish = courseBookingMapper.findBookingIdsToFinish(today, now);
        if (toFinish != null) {
            for (Long id : toFinish) {
                int updated = courseBookingMapper.finishBookingAndAttendanceIfOngoing(id, current);
                if (updated > 0) {
                    CourseBooking booking = courseBookingMapper.findById(id);
                    if (booking == null) {
                        continue;
                    }
                    decrementCourseCurrentStudents(booking.getCourseId());
                    evictCourseCache(booking.getCourseId());
                    invalidateForBooking(booking);
                }
            }
        }
        if (webSocketPushService != null
                && ((toStart != null && !toStart.isEmpty()) || (toFinish != null && !toFinish.isEmpty()))) {
            try {
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                log.warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
    }

    private int autoCancelSingleBooking(CourseBooking booking) {
        Long bookingId = booking.getId();
        LocalDateTime cancelledAt = LocalDateTime.now();
        String cancelRemark = AutoCancelRemarkUtil.buildAutoCancelRemark(booking.getRemark());
        int updated = courseBookingMapper.cancelExpiredUnpaidBooking(bookingId, cancelledAt, cancelRemark);
        if (updated <= 0) {
            return 0;
        }
        decrementCourseCurrentStudents(booking.getCourseId());
        evictCourseCache(booking.getCourseId());
        publishAutoCancelArtifacts(booking);
        booking.setStatus(0);
        booking.setUpdateTime(cancelledAt);
        booking.setRemark(cancelRemark);
        try {
            Long userId = null;
            Member m = memberMapper.findById(booking.getMemberId());
            if (m != null && m.getUserId() != null) userId = m.getUserId();
            webSocketPushService.pushOrderStatusToUser(userId, "courseBooking", bookingId, 0, "已取消", "课程预约");
            webSocketPushService.pushDashboardRefresh();
        } catch (Exception e) {
            log.warn("WebSocket 推送失败: {}", e.getMessage());
        }
        return updated;
    }

    private int runAutoCancelInNewTransaction(java.util.function.IntSupplier action) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        Integer result = transactionTemplate.execute(status -> action.getAsInt());
        return result == null ? 0 : result;
    }

    private void publishAutoCancelArtifacts(CourseBooking booking) {
        Course course = courseMapper.findById(booking.getCourseId());
        Court court = course != null && course.getCourtId() != null ? courtMapper.findById(course.getCourtId()) : null;
        Long venueId = court != null ? court.getVenueId() : null;
        String businessNo = booking.getBookingNo();
        String content = "课程预约订单 " + businessNo + " 超时未支付，系统已自动取消。";
        try {
            notificationService.publishSystemNotification("课程预约订单已自动取消", content, venueId);
        } catch (Exception e) {
            log.warn("课程预约自动取消通知持久化失败, orderNo={}", businessNo, e);
        }
        financeAuditService.logSystemAutoCancel("课程预约", booking.getId(), businessNo, booking, content);
    }

    private void incrementCourseCurrentStudents(Long courseId) {
        incrementCourseCurrentStudents(courseId, "课程报名人数已满");
    }

    private void incrementCourseCurrentStudents(Long courseId, String fullMessage) {
        adjustCourseCurrentStudents(courseId, 1, fullMessage, false);
    }

    private void decrementCourseCurrentStudents(Long courseId) {
        adjustCourseCurrentStudents(courseId, -1, null, true);
    }

    private void ensureCourseSelectableForBooking(Course course) {
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        Integer status = course.getStatus();
        if (status != null && status == 1) {
            return;
        }
        if (status == null) {
            throw new RuntimeException("该课程状态异常，仅可选择报名中的课程");
        }
        switch (status) {
            case 0:
                throw new RuntimeException("该课程已取消，仅可选择报名中的课程");
            case 2:
                throw new RuntimeException("该课程已进行中，仅可选择报名中的课程");
            case 3:
                throw new RuntimeException("该课程已结束，仅可选择报名中的课程");
            default:
                throw new RuntimeException("仅可预约报名中的课程");
        }
    }

    private void adjustCourseCurrentStudents(Long courseId, int delta, String fullMessage, boolean allowMissingCourse) {
        if (courseId == null || delta == 0) {
            return;
        }
        for (int attempt = 0; attempt < 3; attempt++) {
            Course course = courseMapper.findById(courseId);
            if (course == null) {
                if (allowMissingCourse) {
                    return;
                }
                throw new RuntimeException("课程不存在");
            }
            int current = course.getCurrentStudents() != null ? course.getCurrentStudents() : 0;
            int target = Math.max(0, current + delta);
            if (delta > 0) {
                Integer maxStudents = course.getMaxStudents();
                if (maxStudents != null && target > maxStudents) {
                    throw new RuntimeException(fullMessage != null ? fullMessage : "课程报名人数已满");
                }
            }
            Integer version = course.getVersion() != null ? course.getVersion() : 0;
            int updated = courseMapper.updateCurrentStudentsWithVersion(courseId, target, version);
            if (updated > 0) {
                return;
            }
        }
        throw new RuntimeException("课程报名人数更新失败，请稍后重试");
    }

    private String resolvePendingPaymentConflictMessage(CourseBooking latestBooking, String orderLabel) {
        if (latestBooking == null) {
            return orderLabel + "记录不存在";
        }
        Integer payStatus = latestBooking.getPaymentStatus();
        if (payStatus != null) {
            if (payStatus == 1) {
                return "该" + orderLabel + "已支付，无需重复支付";
            }
            if (payStatus == 2) {
                return "该" + orderLabel + "已退款，无法再次支付";
            }
            if (payStatus == 3) {
                return "该" + orderLabel + "退款申请处理中，无法再次支付";
            }
        }
        if (isPendingPaymentExpired(latestBooking.getCreateTime(), LocalDateTime.now())) {
            return "该" + orderLabel + "已超出支付时限，系统已自动取消或正在取消，无法支付";
        }
        Integer status = latestBooking.getStatus();
        if (status != null && status == 0) {
            return "该" + orderLabel + "已超时取消，无法支付";
        }
        return orderLabel + "状态已变化，请刷新后重试";
    }
}
