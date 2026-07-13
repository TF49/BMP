package com.badminton.bmp.modules.course.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.modules.coach.service.CoachService;
import com.badminton.bmp.modules.course.dto.AttendanceAction;
import com.badminton.bmp.modules.course.dto.AttendanceCommand;
import com.badminton.bmp.modules.course.service.CourseBookingService;
import com.badminton.bmp.modules.course.support.CoachAttendanceRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

class CourseBookingControllerAttendanceTest {

    private CourseBookingController controller;
    private CourseBookingService service;
    private CoachService coachService;
    private CoachAttendanceRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        controller = new CourseBookingController();
        service = mock(CourseBookingService.class);
        coachService = mock(CoachService.class);
        rateLimiter = mock(CoachAttendanceRateLimiter.class);
        ReflectionTestUtils.setField(controller, "courseBookingService", service);
        ReflectionTestUtils.setField(controller, "coachService", coachService);
        ReflectionTestUtils.setField(controller, "coachAttendanceRateLimiter", rateLimiter);
    }

    @Test
    void attendanceCommandUsesCoachIdFromLoginStateAndDoesNotSwallowAccessDenied() {
        AttendanceCommand command = command(AttendanceAction.CHECK_IN);
        when(coachService.getCurrentCoachIdOrNull()).thenReturn(9L);
        when(rateLimiter.tryAcquire(9L)).thenReturn(true);
        when(service.updateAttendanceForCoach(9L, command))
                .thenThrow(new AccessDeniedException("forbidden"));

        assertThrows(AccessDeniedException.class, () -> controller.updateAttendanceForCoach(command));

        verify(service).updateAttendanceForCoach(9L, command);
    }

    @Test
    void sixthAttendanceCommandWithinOneSecondIsRejectedWith429() {
        AttendanceCommand command = command(AttendanceAction.ABSENT);
        when(coachService.getCurrentCoachIdOrNull()).thenReturn(9L);
        when(rateLimiter.tryAcquire(9L)).thenReturn(false);

        BusinessException error = assertThrows(BusinessException.class,
                () -> controller.updateAttendanceForCoach(command));

        assertEquals(429, error.getCode());
        verifyNoInteractions(service);
    }

    @Test
    void legacyCoachStatusEndpointRejectsNonCancelStatusBeforeServiceCall() {
        CourseBookingController.CoachBookingStatusRequest request =
                new CourseBookingController.CoachBookingStatusRequest();
        request.setId(100L);
        request.setStatus(3);
        when(coachService.getCurrentCoachIdOrNull()).thenReturn(9L);

        assertThrows(BusinessException.class, () -> controller.updateBookingStatusForCoach(request));
        verifyNoInteractions(service);
    }

    private static AttendanceCommand command(AttendanceAction action) {
        AttendanceCommand command = new AttendanceCommand();
        command.setBookingId(100L);
        command.setAction(action);
        return command;
    }
}
