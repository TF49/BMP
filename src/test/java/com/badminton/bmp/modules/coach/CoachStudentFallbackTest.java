package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badminton.bmp.common.exception.ServiceUnavailableException;
import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.dto.CoachStudentListQuery;
import com.badminton.bmp.modules.coach.dto.CoachStudentScheduleQuery;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import com.badminton.bmp.modules.coach.service.impl.CoachStudentServiceImpl;
import com.badminton.bmp.modules.coach.support.CoachStudentLastSuccessCache;
import com.badminton.bmp.modules.coach.vo.CoachStudentConsumeRecordVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentDetailVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentListResponseVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentPageVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentProgressVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentScheduleVO;
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

    @Test
    void listDetailScheduleAndConsumeQueriesUseTheSameLastSuccessFallback() {
        CoachStudentListQuery listQuery = new CoachStudentListQuery();
        CoachStudentListResponseVO cachedList = new CoachStudentListResponseVO();
        when(mapper.findStudents(9L, listQuery, 180, 70, 14, 0))
                .thenThrow(new RuntimeException("timeout"));
        when(fallback.get("list:9:" + listQuery)).thenReturn(cachedList);
        assertEquals(cachedList, service.listStudents(9L, listQuery));

        CoachStudentDetailVO cachedDetail = new CoachStudentDetailVO();
        cachedDetail.setId(100L);
        when(mapper.findStudentDetail(9L, 100L)).thenThrow(new RuntimeException("timeout"));
        when(fallback.get("detail:9:100")).thenReturn(cachedDetail);
        assertEquals(cachedDetail, service.getStudentDetail(9L, 100L));

        CoachStudentScheduleQuery scheduleQuery = new CoachStudentScheduleQuery();
        CoachStudentPageVO<CoachStudentScheduleVO> cachedSchedule =
                new CoachStudentPageVO<>(List.of(), 0, 1, 20, 0);
        when(mapper.findSchedule(9L, 100L, scheduleQuery, 0)).thenThrow(new RuntimeException("timeout"));
        when(fallback.get("schedule:9:100:" + scheduleQuery)).thenReturn(cachedSchedule);
        assertEquals(cachedSchedule, service.getStudentSchedule(9L, 100L, scheduleQuery));

        CoachStudentPageVO<CoachStudentConsumeRecordVO> cachedConsume =
                new CoachStudentPageVO<>(List.of(), 0, 1, 20, 0);
        when(mapper.findConsumeRecordsForCoach(9L, 100L, 0, 20))
                .thenThrow(new RuntimeException("timeout"));
        when(fallback.get("consume:9:100:1:20")).thenReturn(cachedConsume);
        assertEquals(cachedConsume, service.getStudentConsumeRecords(9L, 100L, 1, 20));
    }

    @Test
    void everyQueryReturns503WithoutAStaleValue() {
        CoachStudentListQuery listQuery = new CoachStudentListQuery();
        when(mapper.findStudents(9L, listQuery, 180, 70, 14, 0))
                .thenThrow(new RuntimeException("timeout"));
        assertThrows(ServiceUnavailableException.class, () -> service.listStudents(9L, listQuery));

        when(mapper.findStudentDetail(9L, 100L)).thenThrow(new RuntimeException("timeout"));
        assertThrows(ServiceUnavailableException.class, () -> service.getStudentDetail(9L, 100L));

        CoachStudentScheduleQuery scheduleQuery = new CoachStudentScheduleQuery();
        when(mapper.findSchedule(9L, 100L, scheduleQuery, 0)).thenThrow(new RuntimeException("timeout"));
        assertThrows(ServiceUnavailableException.class,
                () -> service.getStudentSchedule(9L, 100L, scheduleQuery));

        when(mapper.findConsumeRecordsForCoach(9L, 100L, 0, 20))
                .thenThrow(new RuntimeException("timeout"));
        assertThrows(ServiceUnavailableException.class,
                () -> service.getStudentConsumeRecords(9L, 100L, 1, 20));
    }
}
