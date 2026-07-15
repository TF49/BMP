package com.badminton.bmp.modules.course.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.mapper.CourseBookingMapper;
import com.badminton.bmp.modules.course.mapper.CourseMapper;
import com.badminton.bmp.modules.member.entity.Member;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.service.UserService;
import com.badminton.bmp.websocket.CoachStudentWebSocketEvent;
import com.badminton.bmp.websocket.CoachStudentWebSocketEventPublisher;
import com.badminton.bmp.websocket.WebSocketPushService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class CourseBookingCoachStudentWebSocketTest {

    private CourseBookingMapper bookingMapper;
    private CourseMapper courseMapper;
    private MemberMapper memberMapper;
    private CoachStudentWebSocketEventPublisher eventPublisher;
    private CourseBookingServiceImpl service;
    private Member member;
    private Course course;

    @BeforeEach
    void setUp() {
        bookingMapper = mock(CourseBookingMapper.class);
        courseMapper = mock(CourseMapper.class);
        memberMapper = mock(MemberMapper.class);
        eventPublisher = mock(CoachStudentWebSocketEventPublisher.class);
        service = new CourseBookingServiceImpl();
        ReflectionTestUtils.setField(service, "courseBookingMapper", bookingMapper);
        ReflectionTestUtils.setField(service, "courseMapper", courseMapper);
        ReflectionTestUtils.setField(service, "memberMapper", memberMapper);
        ReflectionTestUtils.setField(service, "coachMapper", mock(CoachMapper.class));
        ReflectionTestUtils.setField(service, "webSocketPushService", mock(WebSocketPushService.class));
        ReflectionTestUtils.setField(service, "coachStudentWebSocketEventPublisher", eventPublisher);

        member = new Member();
        member.setId(50L);
        member.setUserId(60L);
        member.setMemberName("张学员");
        course = new Course();
        course.setId(20L);
        course.setCoachId(30L);
        course.setCourseName("进阶训练");
        course.setCoursePrice(new BigDecimal("100.00"));
        course.setCurrentStudents(1);
        course.setMaxStudents(10);
        course.setVersion(2);
        course.setStatus(1);
        course.setCourseDate(LocalDate.now().plusDays(1));
        course.setStartTime(LocalTime.of(10, 0));
        course.setEndTime(LocalTime.of(11, 0));
        when(memberMapper.findById(50L)).thenReturn(member);
        when(courseMapper.findById(20L)).thenReturn(course);
        when(courseMapper.updateCurrentStudentsWithVersion(any(), any(), any())).thenReturn(1);
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void successfulBookingPublishesNewBookingEvent() {
        authenticate("admin", "PRESIDENT", null);
        CourseBooking booking = booking(1);
        when(bookingMapper.insert(booking)).thenReturn(1);

        assertEquals(1, service.add(booking));

        verify(eventPublisher).publishAfterCommit(
                CoachStudentWebSocketEvent.TYPE_NEW_BOOKING, booking, null, null);
    }

    @Test
    void failedBookingInsertDoesNotPublish() {
        authenticate("admin", "PRESIDENT", null);
        CourseBooking booking = booking(1);
        when(bookingMapper.insert(booking)).thenReturn(0);

        assertEquals(0, service.add(booking));

        verify(eventPublisher, never()).publishAfterCommit(any(), any(), any(), any());
    }

    @Test
    void memberCancellationPublishesCancelledEvent() {
        authenticate("member-user", "MEMBER", currentUser());
        CourseBooking booking = booking(2);
        when(bookingMapper.findById(10L)).thenReturn(booking);
        when(bookingMapper.updateStatus(10L, 0)).thenReturn(1);

        assertEquals(1, service.updateStatus(10L, 0));

        verify(eventPublisher).publishAfterCommit(
                CoachStudentWebSocketEvent.TYPE_CANCELLED, booking, null, null);
    }

    @Test
    void memberDeletingActiveBookingPublishesCancelledEvent() {
        authenticate("member-user", "USER", currentUser());
        CourseBooking booking = booking(1);
        when(bookingMapper.findById(10L)).thenReturn(booking);
        when(bookingMapper.deleteById(10L)).thenReturn(1);

        assertEquals(1, service.deleteById(10L));

        verify(eventPublisher).publishAfterCommit(
                CoachStudentWebSocketEvent.TYPE_CANCELLED, booking, null, null);
    }

    @Test
    void coachCancellationDoesNotPretendStudentCancelled() {
        CourseBooking booking = booking(2);
        booking.setCourseDate(LocalDate.now().plusDays(1));
        booking.setCourseStartTime(LocalTime.of(10, 0));
        booking.setCourseEndTime(LocalTime.of(11, 0));
        when(bookingMapper.findByIdAndCoachId(30L, 10L)).thenReturn(booking);
        when(bookingMapper.cancelBeforeStartForCoach(eq(10L), eq(30L), eq(2), any(), any())).thenReturn(1);

        assertEquals(1, service.updateStatusForCoach(30L, 10L, 0, "停课"));

        verify(eventPublisher, never()).publishAfterCommit(any(), any(), any(), any());
    }

    @Test
    void venueManagerWithMemberRoleDoesNotPretendStudentCancelled() {
        authenticate("hybrid-manager", currentUser(), "VENUE_MANAGER", "MEMBER");
        CourseBooking booking = booking(2);
        when(bookingMapper.findById(10L)).thenReturn(booking);
        when(bookingMapper.updateStatus(10L, 0)).thenReturn(1);

        assertEquals(1, service.updateStatus(10L, 0));

        verify(eventPublisher, never()).publishAfterCommit(any(), any(), any(), any());
    }

    private CourseBooking booking(int status) {
        CourseBooking booking = new CourseBooking();
        booking.setId(10L);
        booking.setMemberId(50L);
        booking.setCourseId(20L);
        booking.setOrderAmount(new BigDecimal("100.00"));
        booking.setStatus(status);
        booking.setPaymentStatus(0);
        return booking;
    }

    private User currentUser() {
        User user = new User();
        user.setId(60L);
        user.setUsername("member-user");
        return user;
    }

    private void authenticate(String username, String role, User currentUser) {
        authenticate(username, currentUser, role);
    }

    private void authenticate(String username, User currentUser, String... roles) {
        var authentication = new UsernamePasswordAuthenticationToken(
                username,
                "n/a",
                java.util.Arrays.stream(roles)
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (currentUser != null) {
            UserService userService = mock(UserService.class);
            when(userService.findByUsername(username)).thenReturn(currentUser);
            ReflectionTestUtils.setField(SecurityUtils.class, "staticUserService", userService);
            when(memberMapper.findByUserId(currentUser.getId())).thenReturn(member);
        }
    }
}
