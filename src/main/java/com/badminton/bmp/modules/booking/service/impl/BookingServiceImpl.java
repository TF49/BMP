package com.badminton.bmp.modules.booking.service.impl;

import com.badminton.bmp.modules.booking.entity.Booking;
import com.badminton.bmp.modules.booking.mapper.BookingMapper;
import com.badminton.bmp.modules.booking.service.BookingService;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.mapper.CourtMapper;
import com.badminton.bmp.modules.court.service.CourtService;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.member.service.MemberConsumeRecordService;
import com.badminton.bmp.modules.finance.entity.Finance;
import com.badminton.bmp.modules.finance.service.FinanceService;
import com.badminton.bmp.websocket.WebSocketPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.ResourceNotFoundException;
import com.badminton.bmp.common.util.SecurityUtils;
import java.math.BigDecimal;
import java.time.Duration;
import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 场地预约业务实现类
 * 处理场地预约相关的业务逻辑
 */
@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CourtMapper courtMapper;

    @Autowired
    private CourtService courtService;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberConsumeRecordService memberConsumeRecordService;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private WebSocketPushService webSocketPushService;

    @Override
    public Booking findById(Long id) {
        Booking booking = bookingMapper.findById(id);
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
                // 需要通过场地查询场馆ID
                Court court = courtMapper.findById(booking.getCourtId());
                if (court != null && currentVenueId.equals(court.getVenueId())) {
                    return booking;
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

    @Override
    public List<Booking> findAll(Long venueId,
                                 Long memberId,
                                 Long courtId,
                                 Integer status,
                                 String memberKeyword,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 int page,
                                 int size) {
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
        if (SecurityUtils.isPresident()) {
            // 总会长可以查看所有场馆，也可以按前端传入的场馆筛选
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            // 场馆管理员强制限定在自己场馆，前端已做禁用，这里兜底
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：仅能查看自己的预约，忽略前端传入的 memberId，防止水平越权
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                com.badminton.bmp.modules.member.entity.Member m = memberMapper.findByUserId(current.getId());
                if (m != null) {
                    memberFilter = m.getId();
                } else {
                    memberFilter = null;
                }
            } else {
                memberFilter = null;
            }
        }
        return bookingMapper.findAll(venueFilter,
                memberFilter,
                courtId,
                status,
                memberKeyword,
                startDate,
                endDate,
                offset,
                size);
    }

    @Override
    public int count(Long venueId,
                     Long memberId,
                     Long courtId,
                     Integer status,
                     String memberKeyword,
                     LocalDate startDate,
                     LocalDate endDate) {
        Long venueFilter = null;
        Long memberFilter = memberId;
        if (SecurityUtils.isPresident()) {
            venueFilter = venueId;
        } else if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else {
            // 普通用户：仅统计自己的预约，忽略前端传入的 memberId，防止水平越权
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                com.badminton.bmp.modules.member.entity.Member m = memberMapper.findByUserId(current.getId());
                if (m != null) {
                    memberFilter = m.getId();
                } else {
                    memberFilter = null;
                }
            } else {
                memberFilter = null;
            }
        }
        return bookingMapper.count(venueFilter,
                memberFilter,
                courtId,
                status,
                memberKeyword,
                startDate,
                endDate);
    }

    /**
     * 计算时长（小时）
     */
    private int calculateDuration(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null) {
            return 0;
        }
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        // 如果有分钟，向上取整
        if (minutes > 0) {
            hours++;
        }
        return (int) hours;
    }

    /**
     * 计算订单金额
     */
    private BigDecimal calculateOrderAmount(Court court, int duration) {
        if (court == null || court.getPricePerHour() == null) {
            return BigDecimal.ZERO;
        }
        if ("TIME".equals(court.getBillingType())) {
            // 按次计费
            return court.getPricePerHour();
        } else {
            // 按小时计费（默认HOUR）
            return court.getPricePerHour().multiply(BigDecimal.valueOf(duration));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(Booking booking) {
        // 权限校验 / 会员归属处理
        if (!SecurityUtils.isPresident() && !SecurityUtils.isVenueManager()) {
            // USER: 只能为自己创建预约；如果前端未显式传入 memberId，则由当前登录用户自动补全
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m == null) {
                    throw new BusinessException("当前用户未绑定会员信息，无法创建预约");
                }
                if (booking.getMemberId() == null) {
                    booking.setMemberId(m.getId());
                } else if (!m.getId().equals(booking.getMemberId())) {
                    throw new BusinessException("权限不足，只能为自己创建预约");
                }
            } else {
                throw new BusinessException("权限不足");
            }
        }

        // 验证会员是否存在（管理员和普通用户统一校验）
        Member member = memberMapper.findById(booking.getMemberId());
        if (member == null) {
            throw new ResourceNotFoundException("会员不存在");
        }

        // 验证场地是否存在且可预约（维护中 status=0 的场地不可预约）
        Court court = courtMapper.findById(booking.getCourtId());
        if (court == null) {
            throw new ResourceNotFoundException("场地不存在");
        }
        if (court.getStatus() != null && court.getStatus() == 0) {
            throw new BusinessException("该场地正在维护中，暂不可预约");
        }

        // 验证时间逻辑
        if (booking.getStartTime() == null || booking.getEndTime() == null) {
            throw new BusinessException("开始时间和结束时间不能为空");
        }
        if (booking.getEndTime().isBefore(booking.getStartTime()) || 
            booking.getEndTime().equals(booking.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 验证预约日期
        if (booking.getBookingDate() == null) {
            throw new BusinessException("预约日期不能为空");
        }

        // 冲突检测：仅限制同一会员在同一时间段、同一场地重复预约
        List<Booking> memberConflictingBookings = bookingMapper.findMemberConflictingBookings(
            booking.getMemberId(),
            booking.getCourtId(),
            booking.getBookingDate(),
            booking.getStartTime(),
            booking.getEndTime(),
            null
        );
        if (!memberConflictingBookings.isEmpty()) {
            throw new BusinessException("您已在该时间段预约该场地，无需重复预约");
        }

        // 计算时长和订单金额
        int duration = calculateDuration(booking.getStartTime(), booking.getEndTime());
        booking.setDuration(duration);
        BigDecimal orderAmount = calculateOrderAmount(court, duration);
        booking.setOrderAmount(orderAmount);

        // 生成预约单号（如果未提供）
        if (booking.getBookingNo() == null || booking.getBookingNo().trim().isEmpty()) {
            booking.setBookingNo(generateBookingNo());
        }

        // 验证预约单号是否已存在
        if (bookingMapper.existsByBookingNo(booking.getBookingNo(), null)) {
            throw new BusinessException("预约单号已存在");
        }

        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        booking.setCreateTime(now);
        booking.setUpdateTime(now);

        // 新增预约强制为待支付，忽略前端传入的 status/paymentStatus
        booking.setStatus(1); // 待支付
        booking.setPaymentStatus(0); // 未支付

        // 插入预约记录
        int result = bookingMapper.insert(booking);

        // 如果插入成功，更新场地状态为"预约中"（2）
        if (result > 0 && booking.getStatus() >= 1 && booking.getStatus() <= 3) {
            courtService.updateStatus(booking.getCourtId(), 2); // 预约中
            try {
                webSocketPushService.pushTodoUpdate(getOperationTodoCount(), getTodoList());
                webSocketPushService.pushDashboardRefresh();
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(Booking booking) {
        // 验证预约记录是否存在
        Booking existing = bookingMapper.findById(booking.getId());
        if (existing == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能修改自己场馆的预约
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Court court = courtMapper.findById(existing.getCourtId());
                    if (court == null || !currentVenueId.equals(court.getVenueId())) {
                        throw new BusinessException("权限不足，只能修改自己场馆的预约");
                    }
                }
            } else {
                // USER: 只能修改自己的预约
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(existing.getMemberId())) {
                        throw new BusinessException("权限不足，只能修改自己的预约");
                    }
                } else {
                    throw new BusinessException("权限不足");
                }
            }
        }

        Long courtId = booking.getCourtId() != null ? booking.getCourtId() : existing.getCourtId();
        Integer oldStatus = existing.getStatus();
        // 编辑不通过接口变更状态：状态仅由支付/退款/定时任务变更，使用原记录状态
        booking.setStatus(existing.getStatus());
        booking.setPaymentStatus(existing.getPaymentStatus());
        Integer newStatus = existing.getStatus();

        // 如果修改了场地ID或时间，需要重新进行冲突检测（仅限制同一会员重复预约）
        boolean needConflictCheck = false;
        if (booking.getCourtId() != null && !booking.getCourtId().equals(existing.getCourtId())) {
            needConflictCheck = true;
        }
        if (booking.getBookingDate() != null && !booking.getBookingDate().equals(existing.getBookingDate())) {
            needConflictCheck = true;
        }
        if (booking.getStartTime() != null && !booking.getStartTime().equals(existing.getStartTime())) {
            needConflictCheck = true;
        }
        if (booking.getEndTime() != null && !booking.getEndTime().equals(existing.getEndTime())) {
            needConflictCheck = true;
        }

        if (needConflictCheck) {
            Long checkCourtId = booking.getCourtId() != null ? booking.getCourtId() : existing.getCourtId();
            LocalDate checkDate = booking.getBookingDate() != null ? booking.getBookingDate() : existing.getBookingDate();
            LocalTime checkStartTime = booking.getStartTime() != null ? booking.getStartTime() : existing.getStartTime();
            LocalTime checkEndTime = booking.getEndTime() != null ? booking.getEndTime() : existing.getEndTime();
            Long checkMemberId = booking.getMemberId() != null ? booking.getMemberId() : existing.getMemberId();

            List<Booking> memberConflictingBookingsForUpdate = bookingMapper.findMemberConflictingBookings(
                checkMemberId,
                checkCourtId,
                checkDate,
                checkStartTime,
                checkEndTime,
                booking.getId()
            );
            if (!memberConflictingBookingsForUpdate.isEmpty()) {
                throw new BusinessException("该时间段您已有该场地的预约记录，请勿重复预约");
            }
        }

        // 如果修改了场地，需校验新场地可预约（维护中不可预约）
        Court court = courtMapper.findById(courtId);
        if (court != null && court.getStatus() != null && court.getStatus() == 0) {
            throw new BusinessException("该场地正在维护中，暂不可预约");
        }
        if (court != null) {
            LocalTime startTime = booking.getStartTime() != null ? booking.getStartTime() : existing.getStartTime();
            LocalTime endTime = booking.getEndTime() != null ? booking.getEndTime() : existing.getEndTime();
            int duration = calculateDuration(startTime, endTime);
            booking.setDuration(duration);
            BigDecimal orderAmount = calculateOrderAmount(court, duration);
            booking.setOrderAmount(orderAmount);
        }

        // 如果修改了场地ID，需要处理场地状态的变化
        if (booking.getCourtId() != null && !booking.getCourtId().equals(existing.getCourtId())) {
            // 恢复旧场地状态（如果旧状态是有效状态）
            if (oldStatus != null && oldStatus >= 1 && oldStatus <= 3) {
                Court oldCourt = courtMapper.findById(existing.getCourtId());
                if (oldCourt != null) {
                    courtService.updateStatus(existing.getCourtId(), 1); // 空闲
                }
            }
            // 更新新场地状态（如果新状态是有效状态）
            if (newStatus != null && newStatus >= 1 && newStatus <= 3) {
                courtService.updateStatus(booking.getCourtId(), 2); // 预约中
            }
        } else if (newStatus != null && !newStatus.equals(oldStatus)) {
            // 如果只修改了状态（未修改场地），需要处理场地状态的变化
            Court court2 = courtMapper.findById(courtId);
            if (court2 != null) {
                boolean oldStatusValid = oldStatus != null && oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = newStatus >= 1 && newStatus <= 3;

                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），恢复场地状态
                    courtService.updateStatus(courtId, 1); // 空闲
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，更新场地状态
                    courtService.updateStatus(courtId, 2); // 预约中
                } else if (newStatus == 3) {
                    // 进行中
                    courtService.updateStatus(courtId, 3); // 使用中
                } else if (newStatus == 4 || newStatus == 0) {
                    // 已完成或已取消
                    courtService.updateStatus(courtId, 1); // 空闲
                }
            }
        }

        // 设置更新时间
        booking.setUpdateTime(LocalDateTime.now());
        return bookingMapper.update(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        // 验证预约记录是否存在
        Booking booking = bookingMapper.findById(id);
        if (booking == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能删除自己场馆的预约
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Court court = courtMapper.findById(booking.getCourtId());
                    if (court == null || !currentVenueId.equals(court.getVenueId())) {
                        throw new BusinessException("权限不足，只能删除自己场馆的预约");
                    }
                }
            } else {
                // USER: 只能删除自己的预约
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(booking.getMemberId())) {
                        throw new BusinessException("权限不足，只能删除自己的预约");
                    }
                } else {
                    throw new BusinessException("权限不足");
                }
            }
        }

        // 如果状态是有效状态（1-待支付，2-已支付，3-进行中），都需要恢复场地状态
        if (booking.getStatus() != null && booking.getStatus() >= 1 && booking.getStatus() <= 3) {
            Court court = courtMapper.findById(booking.getCourtId());
            if (court != null) {
                courtService.updateStatus(booking.getCourtId(), 1); // 空闲
            }
        }

        return bookingMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStatus(Long id, Integer status) {
        // 验证预约记录是否存在
        Booking booking = bookingMapper.findById(id);
        if (booking == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }
        
        // 所有权/场馆归属校验
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                // VENUE_MANAGER: 只能修改自己场馆的预约状态
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                if (currentVenueId != null) {
                    Court court = courtMapper.findById(booking.getCourtId());
                    if (court == null || !currentVenueId.equals(court.getVenueId())) {
                        throw new BusinessException("权限不足，只能修改自己场馆的预约状态");
                    }
                }
            } else {
                // USER: 只能修改自己的预约状态（通常只能取消）
                com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
                if (current != null) {
                    Member m = memberMapper.findByUserId(current.getId());
                    if (m == null || !m.getId().equals(booking.getMemberId())) {
                        throw new BusinessException("权限不足，只能修改自己的预约状态");
                    }
                    // USER只能取消预约（status=0），不能设置为其他状态
                    if (status != null && status != 0) {
                        throw new BusinessException("普通用户只能取消预约");
                    }
                } else {
                    throw new BusinessException("权限不足");
                }
            }
        }

        // 验证状态值是否有效
        if (status == null || status < 0 || status > 4) {
            throw new BusinessException("无效的状态值，必须是0（已取消）、1（待支付）、2（已支付）、3（进行中）或4（已完成）");
        }

        // 如果状态变更，需要处理场地状态的变化
        Integer oldStatus = booking.getStatus();
        if (oldStatus != null && !oldStatus.equals(status)) {
            Court court = courtMapper.findById(booking.getCourtId());
            if (court != null) {
                boolean oldStatusValid = oldStatus >= 1 && oldStatus <= 3;
                boolean newStatusValid = status >= 1 && status <= 3;

                if (oldStatusValid && !newStatusValid) {
                    // 从有效状态变为无效状态（如已取消），恢复场地状态
                    courtService.updateStatus(booking.getCourtId(), 1); // 空闲
                } else if (!oldStatusValid && newStatusValid) {
                    // 从无效状态变为有效状态，更新场地状态
                    courtService.updateStatus(booking.getCourtId(), 2); // 预约中
                } else if (status == 3) {
                    // 进行中
                    courtService.updateStatus(booking.getCourtId(), 3); // 使用中
                } else if (status == 4 || status == 0) {
                    // 已完成或已取消
                    courtService.updateStatus(booking.getCourtId(), 1); // 空闲
                }
            }
        }

        int result = bookingMapper.updateStatus(id, status);
        try {
            Long userId = null;
            Member member = memberMapper.findById(booking.getMemberId());
            if (member != null && member.getUserId() != null) {
                userId = member.getUserId();
            }
            String statusText = getBookingStatusText(status);
            String title = "场地预约";
            Map<String, Object> todoCount = getOperationTodoCount();
            List<Map<String, Object>> todoList = getTodoList();
            webSocketPushService.onOrderStatusChanged(userId, "booking", id, status, statusText, title, todoCount, todoList);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 数据范围过滤：场馆管理者仅统计自己场馆；普通用户仅统计本人预约
        Long venueFilter = null;
        Long memberFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        } else if (SecurityUtils.isUser() || SecurityUtils.isMember()) {
            com.badminton.bmp.modules.system.entity.User current = SecurityUtils.getCurrentUser();
            if (current != null) {
                Member m = memberMapper.findByUserId(current.getId());
                if (m != null) {
                    memberFilter = m.getId();
                }
            }
        }

        // 获取总预约数（按角色：本人/本场馆/全部）
        int totalCount;
        if (memberFilter != null) {
            totalCount = bookingMapper.count(null, memberFilter, null, null, null, null, null);
        } else if (venueFilter != null) {
            totalCount = bookingMapper.count(venueFilter, null, null, null, null, null, null);
        } else {
            totalCount = bookingMapper.countAll();
        }
        stats.put("total", totalCount);

        // 各状态数量（与上面范围一致）
        int cancelled = 0, pending = 0, paid = 0, ongoing = 0, finished = 0;
        if (memberFilter != null) {
            cancelled = bookingMapper.count(null, memberFilter, null, 0, null, null, null);
            pending = bookingMapper.count(null, memberFilter, null, 1, null, null, null);
            paid = bookingMapper.count(null, memberFilter, null, 2, null, null, null);
            ongoing = bookingMapper.count(null, memberFilter, null, 3, null, null, null);
            finished = bookingMapper.count(null, memberFilter, null, 4, null, null, null);
        } else if (venueFilter != null) {
            cancelled = bookingMapper.count(venueFilter, null, null, 0, null, null, null);
            pending = bookingMapper.count(venueFilter, null, null, 1, null, null, null);
            paid = bookingMapper.count(venueFilter, null, null, 2, null, null, null);
            ongoing = bookingMapper.count(venueFilter, null, null, 3, null, null, null);
            finished = bookingMapper.count(venueFilter, null, null, 4, null, null, null);
        } else {
            List<Map<String, Object>> statusCounts = bookingMapper.countByStatus();
            for (Map<String, Object> item : statusCounts) {
                Object statusObj = item.get("status");
                if (statusObj == null) statusObj = item.get("STATUS");
                Object countObj = item.get("count");
                if (countObj == null) countObj = item.get("COUNT");
                if (statusObj == null || countObj == null) continue;
                int status = ((Number) statusObj).intValue();
                int count = ((Number) countObj).intValue();
                switch (status) {
                    case 0: cancelled = count; break;
                    case 1: pending = count; break;
                    case 2: paid = count; break;
                    case 3: ongoing = count; break;
                    case 4: finished = count; break;
                }
            }
        }

        stats.put("cancelled", cancelled);
        stats.put("pending", pending);
        stats.put("paid", paid);
        stats.put("ongoing", ongoing);
        stats.put("finished", finished);

        // 今日/昨日预订（用户端按本人统计，管理端按场馆或全局）
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        int todayBookings;
        int yesterdayBookings;
        if (memberFilter != null) {
            todayBookings = bookingMapper.count(null, memberFilter, null, null, null, today, today);
            yesterdayBookings = bookingMapper.count(null, memberFilter, null, null, null, yesterday, yesterday);
        } else {
            todayBookings = bookingMapper.countByBookingDate(venueFilter, today);
            yesterdayBookings = bookingMapper.countByBookingDate(venueFilter, yesterday);
        }
        double bookingTrend = 0;
        if (yesterdayBookings > 0) {
            bookingTrend = ((double) (todayBookings - yesterdayBookings) / yesterdayBookings) * 100.0;
        } else if (todayBookings > 0) {
            bookingTrend = 100.0;
        }
        stats.put("todayBookings", todayBookings);
        stats.put("bookingTrend", bookingTrend);

        // 高峰时段（仅管理端有场馆维度；用户端返回 --）
        String peakHours = "--";
        if (memberFilter == null) {
            List<Map<String, Object>> hourRows = bookingMapper.countStartHourForDate(venueFilter, today);
            if (hourRows != null && !hourRows.isEmpty()) {
                Object hourObj = hourRows.get(0).get("hour");
                int hour = 18;
                if (hourObj instanceof Number) {
                    hour = ((Number) hourObj).intValue();
                } else if (hourObj != null) {
                    try { hour = Integer.parseInt(hourObj.toString()); } catch (NumberFormatException ignored) {}
                }
                int endHour = (hour + 2) % 24;
                peakHours = String.format("%02d:00-%02d:00", hour, endHour);
            }
        }
        stats.put("peakHours", peakHours);

        return stats;
    }

    @Override
    public synchronized String generateBookingNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BK" + dateStr;
        // 必须包含已软删除记录：软删除后单号仍占唯一约束，否则会生成重复单号
        List<String> existingNos = bookingMapper.findBookingNosByPrefix(prefix);
        int maxSequence = 0;
        for (String no : existingNos) {
            if (no != null && no.startsWith(prefix)) {
                try {
                    String seq = no.substring(prefix.length());
                    int sequence = Integer.parseInt(seq);
                    if (sequence > maxSequence) {
                        maxSequence = sequence;
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略格式错误的单号
                }
            }
        }
        return prefix + String.format("%03d", maxSequence + 1);
    }

    @Override
    public int countBookingsForTimeRange(Long courtId,
                                         LocalDate bookingDate,
                                         LocalTime startTime,
                                         LocalTime endTime) {
        // 基本参数校验：任何一个为空或时间不合法则直接返回 0，避免无意义查询
        if (courtId == null || bookingDate == null || startTime == null || endTime == null) {
            return 0;
        }
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            return 0;
        }
        return bookingMapper.countBookingsForTimeRange(courtId, bookingDate, startTime, endTime);
    }

    @Override
    public List<CourtBookingUserDTO> getCurrentOccupancy(Long courtId) {
        List<CourtBookingUserDTO> result = new ArrayList<>();
        if (courtId == null) {
            return result;
        }
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        List<Map<String, Object>> rows = bookingMapper.findCurrentOccupancy(courtId, today, nowTime);
        return convertToOccupancyDTOs(rows);
    }

    @Override
    public List<CourtBookingUserDTO> getRangeOccupancy(Long courtId,
                                                       LocalDate bookingDate,
                                                       LocalTime startTime,
                                                       LocalTime endTime) {
        List<CourtBookingUserDTO> result = new ArrayList<>();
        if (courtId == null || bookingDate == null || startTime == null || endTime == null) {
            return result;
        }
        if (!endTime.isAfter(startTime)) {
            return result;
        }
        List<Map<String, Object>> rows = bookingMapper.findOccupancyByTimeRange(courtId, bookingDate, startTime, endTime);
        return convertToOccupancyDTOs(rows);
    }

    /**
     * 将 Mapper 返回的 Map 列表转换为脱敏后的 CourtBookingUserDTO 列表
     */
    private List<CourtBookingUserDTO> convertToOccupancyDTOs(List<Map<String, Object>> rows) {
        List<CourtBookingUserDTO> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            CourtBookingUserDTO dto = new CourtBookingUserDTO();

            Object bookingIdObj = row.get("booking_id");
            if (bookingIdObj != null) {
                dto.setBookingId(toLong(bookingIdObj));
            }

            String memberName = (String) row.get("member_name");
            dto.setMemberName(maskName(memberName));

            dto.setMemberType((String) row.get("member_type"));

            Object memberLevelObj = row.get("member_level");
            if (memberLevelObj != null) {
                dto.setMemberLevel(toInt(memberLevelObj));
            }

            Object startTimeObj = row.get("start_time");
            dto.setStartTime(formatTime(startTimeObj));

            Object endTimeObj = row.get("end_time");
            dto.setEndTime(formatTime(endTimeObj));

            Object statusObj = row.get("status");
            if (statusObj != null) {
                int status = toInt(statusObj);
                dto.setStatus(status);
                dto.setStatusText(getBookingStatusText(status));
            }

            result.add(dto);
        }
        return result;
    }

    /**
     * 安全转换为 int，兼容 Number 与 Boolean
     */
    private int toInt(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj ? 1 : 0;
        }
        return 0;
    }

    /**
     * 安全转换为 long，兼容 Number 与 Boolean
     */
    private long toLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj ? 1L : 0L;
        }
        return 0L;
    }

    /**
     * 姓名脱敏处理，与 CourtServiceImpl 保持一致
     */
    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "***";
        }
        int length = name.length();
        if (length == 1) {
            return name;
        } else if (length == 2) {
            return name.charAt(0) + "*";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(name.charAt(0));
            for (int i = 1; i < length - 1; i++) {
                sb.append("*");
            }
            sb.append(name.charAt(length - 1));
            return sb.toString();
        }
    }

    /**
     * 将时间对象格式化为 HH:mm
     */
    private String formatTime(Object timeObj) {
        if (timeObj == null) {
            return "";
        }
        if (timeObj instanceof LocalTime) {
            LocalTime time = (LocalTime) timeObj;
            return String.format("%02d:%02d", time.getHour(), time.getMinute());
        } else if (timeObj instanceof Time) {
            Time time = (Time) timeObj;
            String s = time.toString();
            return s.length() >= 5 ? s.substring(0, 5) : s;
        } else if (timeObj instanceof String) {
            String s = (String) timeObj;
            return s.length() >= 5 ? s.substring(0, 5) : s;
        }
        return timeObj.toString();
    }

    /**
     * 预约状态文本
     */
    private String getBookingStatusText(int status) {
        switch (status) {
            case 0: return "已取消";
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "进行中";
            case 4: return "已完成";
            default: return "未知";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processPayment(Long bookingId, String paymentMethod) {
        // 查询预约记录
        Booking booking = bookingMapper.findById(bookingId);
        if (booking == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }

        // 权限兜底：只有管理员，且 VM 仅限自己场馆
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                Court court = courtMapper.findById(booking.getCourtId());
                if (currentVenueId == null || court == null || !currentVenueId.equals(court.getVenueId())) {
                    throw new BusinessException("权限不足，只能处理自己场馆的预约支付");
                }
            } else {
                throw new BusinessException("权限不足，仅管理员可执行此操作");
            }
        }

        // 验证预约状态，按状态返回明确提示
        Integer status = booking.getStatus();
        if (status == null || status != 1) {
            String msg;
            if (status == null) {
                msg = "预约状态异常，无法支付";
            } else if (status == 0) {
                msg = "该场地预约已取消，无法支付";
            } else if (status == 2) {
                msg = "该场地预约已支付，无需重复支付";
            } else if (status == 3) {
                msg = "该场地预约进行中，无法重复支付";
            } else if (status == 4) {
                msg = "该场地预约已完成，无法支付";
            } else {
                msg = "预约状态不正确，只能对待支付状态的预约进行支付";
            }
            throw new BusinessException(msg);
        }

        // 如果使用余额支付，调用消费记录服务
        if ("BALANCE".equals(paymentMethod)) {
            memberConsumeRecordService.createConsumeRecord(
                booking.getMemberId(),
                booking.getOrderAmount(),
                "BOOKING",
                bookingId,
                paymentMethod,
                "场地预约支付：" + booking.getBookingNo()
            );
        }

        // 获取场馆ID用于财务记录
        Court court = courtMapper.findById(booking.getCourtId());
        Long venueId = court != null ? court.getVenueId() : null;

        // 创建财务记录（收入）
        financeService.createFromBusiness(
            Finance.TYPE_BOOKING,
            bookingId,
            booking.getOrderAmount(),
            Finance.INCOME,
            paymentMethod,
            venueId,
            "场地预约支付：" + booking.getBookingNo()
        );

        // 更新预约支付状态和状态：待支付完成 -> 进行中，并同步场地为使用中
        booking.setPaymentMethod(paymentMethod);
        booking.setPaymentStatus(1); // 已支付
        booking.setStatus(3); // 进行中
        booking.setUpdateTime(LocalDateTime.now());
        int updated = bookingMapper.update(booking);
        if (updated > 0) {
            courtService.updateStatus(booking.getCourtId(), 3); // 使用中
            try {
                Long userId = null;
                Member m = memberMapper.findById(booking.getMemberId());
                if (m != null && m.getUserId() != null) userId = m.getUserId();
                webSocketPushService.onOrderStatusChanged(userId, "booking", bookingId, 3, "进行中", "场地预约", getOperationTodoCount(), getTodoList());
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(Long bookingId) {
        // 查询预约记录
        Booking booking = bookingMapper.findById(bookingId);
        if (booking == null) {
            throw new ResourceNotFoundException("预约记录不存在");
        }

        // 权限兜底：只有管理员，且 VM 仅限自己场馆
        if (!SecurityUtils.isPresident()) {
            if (SecurityUtils.isVenueManager()) {
                Long currentVenueId = SecurityUtils.getCurrentUserVenueId();
                Court court = courtMapper.findById(booking.getCourtId());
                if (currentVenueId == null || court == null || !currentVenueId.equals(court.getVenueId())) {
                    throw new BusinessException("权限不足，只能处理自己场馆的预约退款");
                }
            } else {
                throw new BusinessException("权限不足，仅管理员可执行此操作");
            }
        }

        // 验证预约状态，按状态返回明确提示（null 视为未支付，避免 NPE）
        Integer payStatus = booking.getPaymentStatus();
        if (payStatus == null || payStatus != 1) {
            String msg;
            if (payStatus == null || payStatus == 0) {
                msg = "该场地预约尚未支付，无法退款";
            } else if (payStatus == 2) {
                msg = "该场地预约已退款，无需重复退款";
            } else {
                msg = "只能对已支付的预约进行退款";
            }
            throw new BusinessException(msg);
        }

        // 调用退款记录服务（只有余额支付才需要）
        if ("BALANCE".equals(booking.getPaymentMethod())) {
            memberConsumeRecordService.createRefundRecord(
                booking.getMemberId(),
                booking.getOrderAmount(),
                "BOOKING",
                bookingId,
                booking.getPaymentMethod(),
                "场地预约退款：" + booking.getBookingNo()
            );
        }

        // 获取场馆ID用于财务记录
        Court courtForRefund = courtMapper.findById(booking.getCourtId());
        Long venueIdForRefund = courtForRefund != null ? courtForRefund.getVenueId() : null;

        // 创建财务记录（支出/退款）
        financeService.createFromBusiness(
            Finance.TYPE_BOOKING,
            bookingId,
            booking.getOrderAmount(),
            Finance.EXPENSE,
            booking.getPaymentMethod(),
            venueIdForRefund,
            "场地预约退款：" + booking.getBookingNo()
        );

        // 更新预约状态
        booking.setPaymentStatus(2); // 已退款
        booking.setStatus(0); // 已取消
        booking.setUpdateTime(LocalDateTime.now());
        int result = bookingMapper.update(booking);

        // 恢复场地状态
        Court court = courtMapper.findById(booking.getCourtId());
        if (court != null) {
            courtService.updateStatus(booking.getCourtId(), 1); // 空闲
        }
        try {
            Long userId = null;
            Member m = memberMapper.findById(booking.getMemberId());
            if (m != null && m.getUserId() != null) userId = m.getUserId();
            webSocketPushService.onOrderStatusChanged(userId, "booking", bookingId, 0, "已取消", "场地预约", getOperationTodoCount(), getTodoList());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOverdueInProgressBookings() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<Booking> overdue = bookingMapper.findOverdueInProgressBookings(today, now);
        if (overdue == null || overdue.isEmpty()) {
            return;
        }
        Set<Long> courtIds = new HashSet<>();
        for (Booking b : overdue) {
            bookingMapper.updateStatus(b.getId(), 4); // 已完成
            if (b.getCourtId() != null) {
                courtIds.add(b.getCourtId());
            }
        }
        for (Long courtId : courtIds) {
            courtService.recomputeCourtStatus(courtId);
        }
        try {
            webSocketPushService.pushTodoUpdate(getOperationTodoCount(), getTodoList());
            webSocketPushService.pushDashboardRefresh();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class).warn("WebSocket 推送失败: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOperationTodoCount() {
        Map<String, Object> stats = getStatistics();
        Map<String, Object> todo = new HashMap<>();
        todo.put("pendingBookings", stats.get("pending") != null ? stats.get("pending") : 0);
        todo.put("pending", stats.get("pending") != null ? stats.get("pending") : 0);
        todo.put("unpaidOrders", stats.get("unpaidOrders") != null ? stats.get("unpaidOrders") : 0);
        todo.put("unpaid", 0);
        todo.put("pendingIssues", stats.get("pendingIssues") != null ? stats.get("pendingIssues") : 0);
        todo.put("issues", 0);
        return todo;
    }

    @Override
    public List<Map<String, Object>> getTodoList() {
        Long venueFilter = null;
        if (SecurityUtils.isVenueManager()) {
            venueFilter = SecurityUtils.getCurrentUserVenueId();
        }
        List<Booking> pending = findAll(venueFilter, null, null, 1, null, null, null, 1, 30);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Booking b : pending) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "booking");
            item.put("id", b.getId());
            item.put("bookingNo", b.getBookingNo());
            item.put("title", b.getBookingNo() != null ? b.getBookingNo() : "场地预约 " + b.getId());
            list.add(item);
        }
        return list;
    }
}
