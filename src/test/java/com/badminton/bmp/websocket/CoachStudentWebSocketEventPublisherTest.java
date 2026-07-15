package com.badminton.bmp.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

class CoachStudentWebSocketEventPublisherTest {

    private CourseMapper courseMapper;
    private CoachMapper coachMapper;
    private MemberMapper memberMapper;
    private WebSocketPushService pushService;
    private CoachStudentWebSocketEventPublisher publisher;

    @BeforeEach
    void setUp() {
        courseMapper = mock(CourseMapper.class);
        coachMapper = mock(CoachMapper.class);
        memberMapper = mock(MemberMapper.class);
        pushService = mock(WebSocketPushService.class);
        publisher = new CoachStudentWebSocketEventPublisher(
                courseMapper, coachMapper, memberMapper, pushService, Runnable::run);
        ReflectionTestUtils.setField(publisher, "clock",
                Clock.fixed(Instant.parse("2026-07-15T04:00:00Z"), ZoneId.of("Asia/Shanghai")));

        Course course = new Course();
        course.setId(20L);
        course.setCoachId(30L);
        course.setCourseName("进阶训练");
        Coach coach = new Coach();
        coach.setId(30L);
        coach.setUserId(40L);
        Member member = new Member();
        member.setId(50L);
        member.setMemberName("张学员");
        when(courseMapper.findById(20L)).thenReturn(course);
        when(coachMapper.findById(30L)).thenReturn(coach);
        when(memberMapper.findById(50L)).thenReturn(member);
    }

    @AfterEach
    void cleanTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void sendsResolvedPrivacySafePayloadOnlyAfterCommit() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();

        publisher.publishAfterCommit(
                CoachStudentWebSocketEvent.TYPE_ATTENDANCE_CHANGED, booking(), 4, 2);

        verify(pushService, never()).sendToUser(any(), any(), any());

        TransactionSynchronizationUtils.triggerAfterCommit();

        var captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(pushService).sendToUser(eq(40L), eq(WebSocketPushService.QUEUE_NOTIFICATIONS), captor.capture());
        CoachStudentWebSocketEvent event = (CoachStudentWebSocketEvent) captor.getValue();
        assertEquals(CoachStudentWebSocketEvent.TYPE_ATTENDANCE_CHANGED, event.getType());
        assertEquals(CoachStudentWebSocketEvent.PRIORITY_NORMAL, event.getPriority());
        assertEquals(30L, event.getCoachId());
        assertEquals(50L, event.getMemberId());
        assertEquals(10L, event.getBookingId());
        assertEquals(20L, event.getCourseId());
        assertEquals("进阶训练", event.getCourseName());
        assertEquals("张学员", event.getMemberName());
        assertEquals(4, event.getBookingStatus());
        assertEquals(2, event.getAttendanceStatus());
        assertNotNull(event.getOccurredAt());

        Map<?, ?> json = new ObjectMapper().findAndRegisterModules().convertValue(event, Map.class);
        assertFalse(json.containsKey("phone"));
        assertFalse(json.containsKey("idCard"));
        assertFalse(json.containsKey("balance"));
        assertFalse(json.containsKey("totalRecharge"));
    }

    @Test
    void cancellationUsesHighPriorityAndSendsImmediatelyWithoutTransaction() {
        publisher.publishAfterCommit(CoachStudentWebSocketEvent.TYPE_CANCELLED, booking(), null, null);

        var captor = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(pushService).sendToUser(eq(40L), eq(WebSocketPushService.QUEUE_NOTIFICATIONS), captor.capture());
        CoachStudentWebSocketEvent event = (CoachStudentWebSocketEvent) captor.getValue();
        assertEquals(CoachStudentWebSocketEvent.PRIORITY_HIGH, event.getPriority());
    }

    @Test
    void dispatchesThroughExecutorInsteadOfSendingOnBusinessThread() {
        Executor executor = mock(Executor.class);
        publisher = new CoachStudentWebSocketEventPublisher(
                courseMapper, coachMapper, memberMapper, pushService, executor);

        publisher.publishAfterCommit(CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking(), null, null);

        var runnable = org.mockito.ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnable.capture());
        verify(pushService, never()).sendToUser(any(), any(), any());
        runnable.getValue().run();
        verify(pushService).sendToUser(eq(40L), eq(WebSocketPushService.QUEUE_NOTIFICATIONS), any());
    }

    @Test
    void activeTransactionWithoutSynchronizationNeverSendsEarly() {
        TransactionSynchronizationManager.setActualTransactionActive(true);

        publisher.publishAfterCommit(CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking(), null, null);

        verify(pushService, never()).sendToUser(any(), any(), any());
    }

    @Test
    void relationLookupFailureDoesNotEscapeToBusinessCaller() {
        when(courseMapper.findById(20L)).thenThrow(new IllegalStateException("lookup failed"));

        publisher.publishAfterCommit(CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking(), null, null);

        verify(pushService, never()).sendToUser(any(), any(), any());
    }

    @Test
    void rejectedAsyncDispatchDoesNotEscapeToBusinessCaller() {
        Executor rejectingExecutor = command -> {
            throw new IllegalStateException("queue full");
        };
        publisher = new CoachStudentWebSocketEventPublisher(
                courseMapper, coachMapper, memberMapper, pushService, rejectingExecutor);

        publisher.publishAfterCommit(CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking(), null, null);

        verify(pushService, never()).sendToUser(any(), any(), any());
    }

    @Test
    void missingCoachAccountSkipsPush() {
        when(coachMapper.findById(30L)).thenReturn(null);

        publisher.publishAfterCommit(CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking(), null, null);

        verify(pushService, never()).sendToUser(any(), any(), any());
    }

    @Test
    void pushFailureDoesNotEscapeToBusinessCaller() {
        org.mockito.Mockito.doThrow(new IllegalStateException("socket down"))
                .when(pushService).sendToUser(any(), any(), any());

        publisher.publishAfterCommit(CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking(), null, null);
    }

    private CourseBooking booking() {
        CourseBooking booking = new CourseBooking();
        booking.setId(10L);
        booking.setCourseId(20L);
        booking.setMemberId(50L);
        return booking;
    }
}
