package com.badminton.bmp.websocket;

import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 解析教练登录账号，并确保学员事件只在业务事务提交成功后推送。
 */
@Component
public class CoachStudentWebSocketEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CoachStudentWebSocketEventPublisher.class);

    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final WebSocketPushService webSocketPushService;
    private final Executor taskExecutor;
    private Clock clock = Clock.systemDefaultZone();

    public CoachStudentWebSocketEventPublisher(
            CourseMapper courseMapper,
            CoachMapper coachMapper,
            MemberMapper memberMapper,
            WebSocketPushService webSocketPushService,
            @Qualifier("taskExecutor") Executor taskExecutor
    ) {
        this.courseMapper = courseMapper;
        this.coachMapper = coachMapper;
        this.memberMapper = memberMapper;
        this.webSocketPushService = webSocketPushService;
        this.taskExecutor = taskExecutor;
    }

    public void publishAfterCommit(
            String type,
            CourseBooking booking,
            Integer bookingStatus,
            Integer attendanceStatus
    ) {
        ResolvedEvent resolved;
        try {
            resolved = resolve(type, booking, bookingStatus, attendanceStatus);
        } catch (Exception exception) {
            log.warn("跳过教练学员 WebSocket 事件：关联信息解析失败, type={}, bookingId={}: {}",
                    type, booking != null ? booking.getId() : null, exception.getMessage());
            return;
        }
        if (resolved == null) {
            return;
        }
        Runnable dispatch = () -> dispatchSafely(resolved.userId(), resolved.event());
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                log.warn("跳过教练学员 WebSocket 事件：事务同步器不可用, bookingId={}, type={}",
                        resolved.event().getBookingId(), resolved.event().getType());
                return;
            }
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch.run();
                }
            });
            return;
        }
        dispatch.run();
    }

    private ResolvedEvent resolve(
            String type,
            CourseBooking booking,
            Integer bookingStatus,
            Integer attendanceStatus
    ) {
        if (booking == null || booking.getId() == null || booking.getCourseId() == null
                || booking.getMemberId() == null || !isSupportedType(type)) {
            log.warn("跳过教练学员 WebSocket 事件：预约或事件类型不完整, type={}, bookingId={}",
                    type, booking != null ? booking.getId() : null);
            return null;
        }
        Course course = courseMapper.findById(booking.getCourseId());
        if (course == null || course.getCoachId() == null) {
            log.warn("跳过教练学员 WebSocket 事件：课程或教练关联不存在, bookingId={}, courseId={}",
                    booking.getId(), booking.getCourseId());
            return null;
        }
        Coach coach = coachMapper.findById(course.getCoachId());
        if (coach == null || coach.getUserId() == null) {
            log.warn("跳过教练学员 WebSocket 事件：教练未绑定登录账号, bookingId={}, coachId={}",
                    booking.getId(), course.getCoachId());
            return null;
        }
        Member member = memberMapper.findById(booking.getMemberId());
        String memberName = member != null ? member.getMemberName() : booking.getMemberName();
        String courseName = course.getCourseName() != null ? course.getCourseName() : booking.getCourseName();
        String priority = CoachStudentWebSocketEvent.TYPE_CANCELLED.equals(type)
                ? CoachStudentWebSocketEvent.PRIORITY_HIGH
                : CoachStudentWebSocketEvent.PRIORITY_NORMAL;
        CoachStudentWebSocketEvent event = new CoachStudentWebSocketEvent(
                type,
                priority,
                course.getCoachId(),
                booking.getMemberId(),
                booking.getId(),
                booking.getCourseId(),
                courseName,
                memberName,
                LocalDateTime.now(clock),
                bookingStatus,
                attendanceStatus
        );
        return new ResolvedEvent(coach.getUserId(), event);
    }

    private boolean isSupportedType(String type) {
        return CoachStudentWebSocketEvent.TYPE_NEW_BOOKING.equals(type)
                || CoachStudentWebSocketEvent.TYPE_CANCELLED.equals(type)
                || CoachStudentWebSocketEvent.TYPE_ATTENDANCE_CHANGED.equals(type);
    }

    private void sendSafely(Long userId, CoachStudentWebSocketEvent event) {
        try {
            webSocketPushService.sendToUser(userId, WebSocketPushService.QUEUE_NOTIFICATIONS, event);
        } catch (Exception exception) {
            log.warn("教练学员 WebSocket 推送失败, userId={}, bookingId={}, type={}: {}",
                    userId, event.getBookingId(), event.getType(), exception.getMessage());
        }
    }

    private void dispatchSafely(Long userId, CoachStudentWebSocketEvent event) {
        try {
            taskExecutor.execute(() -> sendSafely(userId, event));
        } catch (Exception exception) {
            log.warn("教练学员 WebSocket 异步任务提交失败, userId={}, bookingId={}, type={}: {}",
                    userId, event.getBookingId(), event.getType(), exception.getMessage());
        }
    }

    private record ResolvedEvent(Long userId, CoachStudentWebSocketEvent event) {
    }
}
