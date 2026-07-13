package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.modules.coach.controller.CoachStudentController;
import com.badminton.bmp.modules.coach.dto.CoachStudentListQuery;
import com.badminton.bmp.modules.coach.service.CoachService;
import com.badminton.bmp.modules.coach.service.CoachStudentService;
import com.badminton.bmp.modules.coach.support.CoachStudentQueryRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

class CoachStudentControllerTest {
    private CoachStudentController controller;
    private CoachStudentService studentService;
    private CoachService coachService;
    private CoachStudentQueryRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        controller = new CoachStudentController();
        studentService = mock(CoachStudentService.class);
        coachService = mock(CoachService.class);
        rateLimiter = mock(CoachStudentQueryRateLimiter.class);
        ReflectionTestUtils.setField(controller, "coachStudentService", studentService);
        ReflectionTestUtils.setField(controller, "coachService", coachService);
        ReflectionTestUtils.setField(controller, "rateLimiter", rateLimiter);
        when(coachService.getCurrentCoachIdOrNull()).thenReturn(9L);
    }

    @Test
    void listUsesCurrentCoachAndFivePerSecondLimiter() {
        CoachStudentListQuery query = new CoachStudentListQuery();
        when(rateLimiter.tryAcquireList(9L)).thenReturn(true);

        controller.listStudents(query);

        verify(studentService).listStudents(9L, query);
    }

    @Test
    void listRateLimitRejectsBeforeServiceWith429() {
        when(rateLimiter.tryAcquireList(9L)).thenReturn(false);

        BusinessException error = assertThrows(BusinessException.class,
                () -> controller.listStudents(new CoachStudentListQuery()));

        assertEquals(429, error.getCode());
        verifyNoInteractions(studentService);
    }

    @Test
    void detailAccessDeniedIsNotConvertedToNormalResult() {
        when(rateLimiter.tryAcquireDetail(9L, 100L)).thenReturn(true);
        when(studentService.getStudentDetail(9L, 100L))
                .thenThrow(new AccessDeniedException("forbidden"));

        assertThrows(AccessDeniedException.class, () -> controller.getStudentDetail(100L));
    }
}
