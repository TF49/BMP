package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badminton.bmp.common.exception.ServiceUnavailableException;
import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import com.badminton.bmp.modules.coach.service.impl.CoachStudentServiceImpl;
import com.badminton.bmp.modules.coach.support.CoachStudentLastSuccessCache;
import com.badminton.bmp.modules.coach.vo.CoachStudentProgressVO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CoachStudentFallbackTest {
    private CoachStudentMapper mapper;
    private CoachStudentRelationService relation;
    private CoachStudentLastSuccessCache fallback;
    private CoachStudentServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(CoachStudentMapper.class);
        relation = mock(CoachStudentRelationService.class);
        fallback = mock(CoachStudentLastSuccessCache.class);
        service = new CoachStudentServiceImpl();
        ReflectionTestUtils.setField(service, "coachStudentMapper", mapper);
        ReflectionTestUtils.setField(service, "relationService", relation);
        ReflectionTestUtils.setField(service, "lastSuccessCache", fallback);
        when(relation.canCoachViewStudent(9L, 100L)).thenReturn(true);
    }

    @Test
    void databaseFailureReturnsLastSuccessfulProgressWhenPresent() {
        CoachStudentProgressVO cached = new CoachStudentProgressVO();
        cached.setCourseName("基础训练");
        when(mapper.findProgress(9L, 100L)).thenThrow(new RuntimeException("timeout"));
        when(fallback.get("progress:9:100")).thenReturn(List.of(cached));

        assertEquals("基础训练", service.getStudentProgress(9L, 100L).get(0).getCourseName());
    }

    @Test
    void databaseFailureWithoutCacheReturns503WithTraceId() {
        when(mapper.findProgress(9L, 100L)).thenThrow(new RuntimeException("timeout"));
        when(fallback.get("progress:9:100")).thenReturn(null);

        ServiceUnavailableException error = assertThrows(ServiceUnavailableException.class,
                () -> service.getStudentProgress(9L, 100L));

        assertEquals(503, error.getCode());
        assertEquals(32, error.getTraceId().length());
    }
}
