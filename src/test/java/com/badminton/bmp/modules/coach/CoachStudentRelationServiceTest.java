package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import com.badminton.bmp.modules.coach.service.impl.CoachStudentRelationServiceImpl;
import com.badminton.bmp.modules.coach.service.impl.CoachStudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

class CoachStudentRelationServiceTest {

    @Test
    void relationRequiresMapperConfirmedBookingWithinFrozenBoundary() {
        CoachStudentMapper mapper = mock(CoachStudentMapper.class);
        CoachStudentRelationServiceImpl service = new CoachStudentRelationServiceImpl();
        ReflectionTestUtils.setField(service, "coachStudentMapper", mapper);
        when(mapper.existsValidCoachStudentRelation(9L, 100L, 180)).thenReturn(1, 0);

        assertTrue(service.canCoachViewStudent(9L, 100L));
        assertFalse(service.canCoachViewStudent(9L, 100L));
        assertFalse(service.canCoachViewStudent(null, 100L));
    }

    @Test
    void everyStudentDetailOperationStopsBeforeQueryWhenRelationIsMissing() {
        CoachStudentMapper mapper = mock(CoachStudentMapper.class);
        CoachStudentRelationService relation = mock(CoachStudentRelationService.class);
        CoachStudentServiceImpl service = new CoachStudentServiceImpl();
        ReflectionTestUtils.setField(service, "coachStudentMapper", mapper);
        ReflectionTestUtils.setField(service, "relationService", relation);
        when(relation.canCoachViewStudent(9L, 100L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getStudentDetail(9L, 100L));
        assertThrows(AccessDeniedException.class, () -> service.getStudentProgress(9L, 100L));
        assertThrows(AccessDeniedException.class,
                () -> service.getStudentSchedule(9L, 100L, null));
        assertThrows(AccessDeniedException.class,
                () -> service.getStudentAttendance(9L, 100L, null));
        assertThrows(AccessDeniedException.class,
                () -> service.getStudentConsumeRecords(9L, 100L, 1, 20));
        verifyNoInteractions(mapper);
    }
}
