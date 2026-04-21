package com.badminton.bmp.modules.course.service.impl;

import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.mapper.CourseBookingMapper;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.course.service.CourseBookingService;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.websocket.WebSocketPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程预约业务实现类
 * 处理课程预约相关的业务逻辑
 */
@Service
public class CourseBookingServiceImpl implements CourseBookingService {
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
    private WebSocketPushService webSocketPushService;

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
    public List<CourseBooking> findAllForCoach(Long coachId, Integer status, String keyword, int page, int size) {
        if (coachId == null) return java.util.Collections.emptyList();
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 10;
        int offset = (page - 1) * size;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        return courseBookingMapper.findAllByCoachId(coachId, status, keyword, offset, size);
    }

    @Override
    public int countForCoach(Long coachId, Integer status, String keyword) {
        if (coachId == null) return 0;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;
        return courseBookingMapper.countByCoachId(coachId, status, keyword);
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
            int newCurrent = currentStudents + 1;
            courseMapper.updateCurrentStudents(booking.getCourseId(), newCurrent);
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
        
        Long courseId = booking.getCourseId() != null ? booking.getCourseId() : existing.getCourseId();
        Integer oldStatus = existing.getStatus();
        Integer newStatus = booking.getStatus() != null ? booking.getStatus() : existing.getStatus();
        
        // 如果修改了课程ID，需要处理报名人数的变化
        if (booking.getCourseId() != null && !booking.getCourseId().equals(existing.getCourseId())) {
            // 从旧课程减少报名人数（如果旧状态是有效状态）
            if (oldStatus != null && oldStatus >= 1 && oldStatus <= 3) {
                Course oldCourse = courseMapper.findById(existing.getCourseId());
                if (oldCourse != null) {
                    int oldCurrent = oldCourse.getCurrentStudents() != null ? oldCourse.getCurrentStudents() : 0;
                    courseMapper.updateCurrentStudents(existing.getCourseId(),
                        Math.max(0, oldCurrent - 1));
                }
            }
            
            // 向新课程增加报名人数（需要验证新课程是否还有名额）
            Course newCourse = courseMapper.findById(booking.getCourseId());
            if (newCourse == null) {
                throw new RuntimeException("新课程不存在");
            }
            if (newStatus != null && newStatus >= 1 && newStatus <= 3) {
                int newCurrent = newCourse.getCurrentStudents() != null ? newCourse.getCurrentStudents() : 0;
                if (newCurrent >= newCourse.getMaxStudents()) {
                    throw new RuntimeException("新课程报名人数已满");
                }
                courseMapper.updateCurrentStudents(booking.getCourseId(),
                    newCurrent + 1);
            }
        } else if (newStatus != null && !newStatus.equals(oldStatus)) {
            // 如果只修改了状态（未修改课程），需要处理报名人数的变化
            Course course = courseMapper.findById(courseId);
            if (course != null) {
                int current = course.getCurrentStudents() != null ? course.getCurrentStudents() : 0;
                boolean oldStatusValid = oldStatus != null && oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = newStatus >= 1 && newStatus <= 3;
                
                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），减少报名人数
                    courseMapper.updateCurrentStudents(courseId,
                        Math.max(0, current - 1));
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，增加报名人数
                    if (current >= course.getMaxStudents()) {
                        throw new RuntimeException("课程报名人数已满");
                    }
                    courseMapper.updateCurrentStudents(courseId,
                        current + 1);
                }
            }
        }
        
        // 设置更新时间
        booking.setUpdateTime(LocalDateTime.now());
        return courseBookingMapper.update(booking);
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
            Course course = courseMapper.findById(booking.getCourseId());
            if (course != null) {
                int current = course.getCurrentStudents() != null ? course.getCurrentStudents() : 0;
                courseMapper.updateCurrentStudents(booking.getCourseId(),
                    Math.max(0, current - 1));
            }
        }
        
        return courseBookingMapper.deleteById(id);
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
                int current = course.getCurrentStudents() != null ? course.getCurrentStudents() : 0;
                boolean oldStatusValid = oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = status >= 1 && status <= 3;
                
                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），减少报名人数
                    courseMapper.updateCurrentStudents(booking.getCourseId(),
                        Math.max(0, current - 1));
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，增加报名人数
                    if (current >= course.getMaxStudents()) {
                        throw new RuntimeException("课程报名人数已满");
                    }
                    courseMapper.updateCurrentStudents(booking.getCourseId(),
                        current + 1);
                }
            }
        }
        
        int result = courseBookingMapper.updateStatus(id, status);
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

        // Dashboard 课程满班率趋势：近8周（占位，后续可接入按周汇总的满班率）
        java.util.List<Map<String, Object>> weeklyCapacity = new java.util.ArrayList<>();
        for (int i = 7; i >= 0; i--) {
            Map<String, Object> w = new java.util.HashMap<>();
            w.put("week", "第" + (8 - i) + "周");
            w.put("label", "第" + (8 - i) + "周");
            w.put("rate", 0);
            w.put("capacityRate", 0);
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

        // 更新预约支付状态和状态
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentStatus(1); // 已支付
        booking.setStatus(2); // 已支付
        booking.setUpdateTime(LocalDateTime.now());
        int updated = courseBookingMapper.update(booking);
        if (updated > 0) {
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
        if (payStatus == null || payStatus != 1) {
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
        Course course = courseMapper.findById(booking.getCourseId());
        if (course != null) {
            int current = course.getCurrentStudents() != null ? course.getCurrentStudents() : 0;
            courseMapper.updateCurrentStudents(booking.getCourseId(),
                Math.max(0, current - 1));
        }
        if (result > 0) {
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
    @Transactional(rollbackFor = Exception.class)
    public void autoUpdateCourseBookingStatusByTime() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Long> toStart = courseBookingMapper.findBookingIdsToStart(today, now);
        if (toStart != null) {
            for (Long id : toStart) {
                courseBookingMapper.updateStatus(id, 3); // 进行中
            }
        }
        List<Long> toFinish = courseBookingMapper.findBookingIdsToFinish(today, now);
        if (toFinish != null) {
            for (Long id : toFinish) {
                courseBookingMapper.updateStatus(id, 4); // 已完成
            }
        }
        if ((toStart != null && !toStart.isEmpty()) || (toFinish != null && !toFinish.isEmpty())) {
            try {
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(CourseBookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
    }
}
