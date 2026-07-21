package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.mapper.CoachStudentRelationMapper;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import com.badminton.bmp.modules.coach.service.impl.CoachStudentRelationServiceImpl;
import com.badminton.bmp.modules.coach.service.impl.CoachStudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

class CoachStudentRelationServiceTest {

    @Test
    void queryCachesCannotBypassRelationshipAuthorization() throws Exception {
        assertFalse(CoachStudentRelationServiceImpl.class
                .getMethod("canCoachViewStudent", Long.class, Long.class)
                .isAnnotationPresent(Cacheable.class));
        assertFalse(CoachStudentServiceImpl.class
                .getMethod("getStudentProgress", Long.class, Long.class)
                .isAnnotationPresent(Cacheable.class));
        assertFalse(CoachStudentServiceImpl.class
                .getMethod("getStudentAttendance", Long.class, Long.class,
                        com.badminton.bmp.modules.coach.dto.CoachStudentScheduleQuery.class)
                .isAnnotationPresent(Cacheable.class));
    }

    @Test
    void activeRelationshipAuthorizesStudentAccess() {
        CoachStudentRelationMapper mapper = mock(CoachStudentRelationMapper.class);
        CoachStudentRelationServiceImpl service = relationService(mapper);
        when(mapper.existsActive(9L, 100L)).thenReturn(1);

        assertTrue(service.canCoachViewStudent(9L, 100L));

        verify(mapper).existsActive(9L, 100L);
    }

    @Test
    void deletedRelationshipDoesNotAuthorizeStudentAccess() {
        CoachStudentRelationMapper mapper = mock(CoachStudentRelationMapper.class);
        CoachStudentRelationServiceImpl service = relationService(mapper);
        when(mapper.existsActive(9L, 100L)).thenReturn(0);

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

    private CoachStudentRelationServiceImpl relationService(CoachStudentRelationMapper mapper) {
        CoachStudentRelationServiceImpl service = new CoachStudentRelationServiceImpl();
        ReflectionTestUtils.setField(service, "relationMapper", mapper);
        return service;
    }
}
