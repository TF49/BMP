package com.badminton.bmp.modules.coach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.modules.coach.dto.CoachStudentListQuery;
import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.service.impl.CoachStudentServiceImpl;
import com.badminton.bmp.modules.coach.util.CoachStudentPrivacyUtils;
import com.badminton.bmp.modules.coach.vo.CoachStudentDetailVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentListItemVO;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CoachStudentPrivacyAndValidationTest {

    @Test
    void phoneAndIdentityAreMinimizedByContract() {
        assertEquals("138****1001", CoachStudentPrivacyUtils.maskPhone("13812341001"));
        assertEquals("95123456", CoachStudentPrivacyUtils.maskPhone("95123456"));
        assertEquals("未填写", CoachStudentPrivacyUtils.maskPhone(null));
        assertEquals("1001", CoachStudentPrivacyUtils.idCardTail("110101199001011001"));

        assertFalse(Arrays.stream(CoachStudentListItemVO.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("phone")));
        assertFalse(Arrays.stream(CoachStudentDetailVO.class.getDeclaredFields())
                .anyMatch(field -> field.getName().equals("balance")
                        || field.getName().equals("totalRecharge")
                        || field.getName().equals("idCard")));
    }

    @Test
    void invalidPagingKeywordAndSortAreRejectedBeforeMapper() {
        CoachStudentMapper mapper = mock(CoachStudentMapper.class);
        CoachStudentServiceImpl service = new CoachStudentServiceImpl();
        ReflectionTestUtils.setField(service, "coachStudentMapper", mapper);

        CoachStudentListQuery query = new CoachStudentListQuery();
        query.setPage(0);
        assertThrows(BusinessException.class, () -> service.listStudents(9L, query));

        query.setPage(1);
        query.setSize(101);
        assertThrows(BusinessException.class, () -> service.listStudents(9L, query));

        query.setSize(20);
        query.setKeyword("x".repeat(51));
        assertThrows(BusinessException.class, () -> service.listStudents(9L, query));

        query.setKeyword(null);
        query.setOrderBy("drop table");
        assertThrows(BusinessException.class, () -> service.listStudents(9L, query));
        verifyNoInteractions(mapper);
    }

    @Test
    void listSummaryUsesGlobalRiskCountInsteadOfCurrentPageCount() {
        CoachStudentMapper mapper = mock(CoachStudentMapper.class);
        CoachStudentServiceImpl service = new CoachStudentServiceImpl();
        ReflectionTestUtils.setField(service, "coachStudentMapper", mapper);
        CoachStudentListQuery query = new CoachStudentListQuery();
        when(mapper.findStudents(9L, query, 180, 0)).thenReturn(List.of());
        when(mapper.countStudents(9L, query, 180)).thenReturn(28);
        when(mapper.summarizeStudents(9L, 180)).thenReturn(Map.of(
                "totalStudents", 28, "averageAttendanceRate", new java.math.BigDecimal("76.50")));
        when(mapper.countRiskStudents(9L, 180)).thenReturn(6);

        var response = service.listStudents(9L, query);
        assertEquals(6, response.getRiskStudents());
        assertEquals(new java.math.BigDecimal("76.50"), response.getAverageAttendanceRate());
        verify(mapper).countRiskStudents(9L, 180);
    }
}
