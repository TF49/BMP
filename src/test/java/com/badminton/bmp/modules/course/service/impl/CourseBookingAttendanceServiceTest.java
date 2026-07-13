package com.badminton.bmp.modules.course.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.modules.course.dto.AttendanceAction;
import com.badminton.bmp.modules.course.dto.AttendanceCommand;
import com.badminton.bmp.modules.course.dto.AttendanceCommandResult;
import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.badminton.bmp.modules.course.mapper.CourseBookingMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CourseBookingAttendanceServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final LocalDate COURSE_DATE = LocalDate.of(2026, 7, 13);
    private CourseBookingMapper mapper;
    private CourseBookingServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = mock(CourseBookingMapper.class);
        service = new CourseBookingServiceImpl();
        ReflectionTestUtils.setField(service, "courseBookingMapper", mapper);
        setNow(LocalDateTime.of(2026, 7, 13, 10, 30));
    }

    @Test
    void checkInMovesAttendanceZeroToOneAndPaidBookingToOngoing() {
        CourseBooking before = booking(2, 0);
        CourseBooking after = booking(3, 1);
        after.setActualCheckinTime(LocalDateTime.of(2026, 7, 13, 10, 30));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(before, after);
        when(mapper.updateAttendanceWithExpectedState(
                eq(100L), eq(2), eq(0), eq(3), eq(1), any(), any(), eq(null), eq(false)))
                .thenReturn(1);

        AttendanceCommandResult result = service.updateAttendanceForCoach(9L, command(AttendanceAction.CHECK_IN));

        assertEquals(1, result.getAttendanceStatus());
        assertEquals(3, result.getBookingStatus());
        assertEquals(after.getActualCheckinTime(), result.getActualCheckinTime());
    }

    @Test
    void repeatedCheckInReturnsStableResultWithoutAnotherWrite() {
        CourseBooking checkedIn = booking(3, 1);
        checkedIn.setActualCheckinTime(LocalDateTime.of(2026, 7, 13, 10, 5));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(checkedIn);

        AttendanceCommandResult result = service.updateAttendanceForCoach(9L, command(AttendanceAction.CHECK_IN));

        assertEquals(1, result.getAttendanceStatus());
        assertEquals(checkedIn.getActualCheckinTime(), result.getActualCheckinTime());
        verify(mapper, never()).updateAttendanceWithExpectedState(
                anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any(), anyBoolean());
    }

    @Test
    void completeMovesCheckedInAttendanceToCompletedAndCanFinishOngoingBooking() {
        CourseBooking before = booking(3, 1);
        before.setActualCheckinTime(LocalDateTime.of(2026, 7, 13, 10, 1));
        CourseBooking after = booking(4, 2);
        after.setActualCheckinTime(before.getActualCheckinTime());
        after.setActualFinishTime(LocalDateTime.of(2026, 7, 13, 10, 30));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(before, after);
        when(mapper.updateAttendanceWithExpectedState(
                eq(100L), eq(3), eq(1), eq(4), eq(2), any(), eq(null), any(), eq(false)))
                .thenReturn(1);

        AttendanceCommandResult result = service.updateAttendanceForCoach(9L, command(AttendanceAction.COMPLETE));

        assertEquals(2, result.getAttendanceStatus());
        assertEquals(4, result.getBookingStatus());
        assertEquals(after.getActualFinishTime(), result.getActualFinishTime());
    }

    @Test
    void completeCanCorrectAbsenceAtExactlyEndPlusTwentyFourHours() {
        setNow(LocalDateTime.of(2026, 7, 14, 11, 0));
        CourseBooking before = booking(4, 3);
        CourseBooking after = booking(4, 2);
        after.setActualFinishTime(LocalDateTime.of(2026, 7, 14, 11, 0));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(before, after);
        when(mapper.updateAttendanceWithExpectedState(
                eq(100L), eq(4), eq(3), eq(4), eq(2), any(), eq(null), any(), eq(false)))
                .thenReturn(1);

        AttendanceCommandResult result = service.updateAttendanceForCoach(9L, command(AttendanceAction.COMPLETE));

        assertEquals(2, result.getAttendanceStatus());
    }

    @Test
    void absentCanCorrectCompletionAndClearsBothActualTimesWithoutChangingBookingStatus() {
        CourseBooking before = booking(4, 2);
        before.setActualCheckinTime(LocalDateTime.of(2026, 7, 13, 10, 1));
        before.setActualFinishTime(LocalDateTime.of(2026, 7, 13, 10, 59));
        CourseBooking after = booking(4, 3);
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(before, after);
        when(mapper.updateAttendanceWithExpectedState(
                eq(100L), eq(4), eq(2), eq(4), eq(3), any(), eq(null), eq(null), eq(true)))
                .thenReturn(1);

        AttendanceCommandResult result = service.updateAttendanceForCoach(9L, command(AttendanceAction.ABSENT));

        assertEquals(3, result.getAttendanceStatus());
        assertEquals(4, result.getBookingStatus());
        assertNull(result.getActualCheckinTime());
        assertNull(result.getActualFinishTime());
    }

    @Test
    void attendanceCommandsAreRejectedBeforeCourseStart() {
        setNow(LocalDateTime.of(2026, 7, 13, 9, 59, 59));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(booking(2, 0));

        assertThrows(BusinessException.class,
                () -> service.updateAttendanceForCoach(9L, command(AttendanceAction.CHECK_IN)));
        verify(mapper, never()).updateAttendanceWithExpectedState(
                anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any(), anyBoolean());
    }

    @Test
    void completionAndAbsenceCorrectionsAreRejectedAfterEndPlusTwentyFourHours() {
        setNow(LocalDateTime.of(2026, 7, 14, 11, 0, 0, 1));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(booking(4, 3), booking(4, 2));

        assertThrows(BusinessException.class,
                () -> service.updateAttendanceForCoach(9L, command(AttendanceAction.COMPLETE)));
        assertThrows(BusinessException.class,
                () -> service.updateAttendanceForCoach(9L, command(AttendanceAction.ABSENT)));
    }

    @Test
    void repeatedTerminalAttendanceCommandsRemainStableAfterCorrectionWindow() {
        setNow(LocalDateTime.of(2026, 7, 15, 11, 0));
        CourseBooking completed = booking(4, 2);
        CourseBooking absent = booking(4, 3);
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(completed, absent);

        assertEquals(2, service.updateAttendanceForCoach(9L, command(AttendanceAction.COMPLETE))
                .getAttendanceStatus());
        assertEquals(3, service.updateAttendanceForCoach(9L, command(AttendanceAction.ABSENT))
                .getAttendanceStatus());
        verify(mapper, never()).updateAttendanceWithExpectedState(
                anyLong(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any(), any(), anyBoolean());
    }

    @Test
    void losingConditionalUpdateReturnsWinningStableState() {
        CourseBooking before = booking(3, 1);
        CourseBooking winner = booking(4, 2);
        winner.setActualFinishTime(LocalDateTime.of(2026, 7, 13, 10, 30));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(before, winner);
        when(mapper.updateAttendanceWithExpectedState(
                eq(100L), eq(3), eq(1), eq(4), eq(2), any(), eq(null), any(), eq(false)))
                .thenReturn(0);

        AttendanceCommandResult result = service.updateAttendanceForCoach(9L, command(AttendanceAction.COMPLETE));

        assertEquals(2, result.getAttendanceStatus());
        assertEquals(4, result.getBookingStatus());
    }

    @Test
    void losingConditionalUpdateToDifferentStateReportsConflict() {
        CourseBooking before = booking(3, 1);
        CourseBooking winner = booking(3, 3);
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(before, winner);
        when(mapper.updateAttendanceWithExpectedState(
                eq(100L), eq(3), eq(1), eq(4), eq(2), any(), eq(null), any(), eq(false)))
                .thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.updateAttendanceForCoach(9L, command(AttendanceAction.COMPLETE)));

        assertEquals(409, error.getCode());
    }

    @Test
    void coachStatusEndpointOnlyCancelsBeforeStartAndUsesOldStatusCondition() {
        setNow(LocalDateTime.of(2026, 7, 13, 9, 0));
        CourseBooking before = booking(2, 0);
        CourseBooking cancelled = booking(0, 0);
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(before, cancelled);
        when(mapper.cancelBeforeStartForCoach(eq(100L), eq(9L), eq(2), any(), any())).thenReturn(1);

        assertEquals(1, service.updateStatusForCoach(9L, 100L, 0, "请假"));
        verify(mapper).cancelBeforeStartForCoach(eq(100L), eq(9L), eq(2), any(), any());
    }

    @Test
    void coachCannotCancelAtOrAfterCourseStart() {
        setNow(LocalDateTime.of(2026, 7, 13, 10, 0));
        when(mapper.findByIdAndCoachId(9L, 100L)).thenReturn(booking(2, 0));

        assertThrows(BusinessException.class,
                () -> service.updateStatusForCoach(9L, 100L, 0, null));
        verify(mapper, never()).cancelBeforeStartForCoach(anyLong(), anyLong(), anyInt(), any(), any());
    }

    @Test
    void automaticStartAndFinishUseConditionalMapperUpdates() {
        when(mapper.findBookingIdsToStart(COURSE_DATE, LocalTime.of(10, 30))).thenReturn(List.of(1L));
        when(mapper.findBookingIdsToFinish(COURSE_DATE, LocalTime.of(10, 30))).thenReturn(List.of(2L));
        when(mapper.startBookingIfPaid(1L)).thenReturn(1);
        when(mapper.finishBookingAndAttendanceIfOngoing(eq(2L), any(LocalDateTime.class))).thenReturn(0);

        service.autoUpdateCourseBookingStatusByTime();

        verify(mapper).startBookingIfPaid(1L);
        verify(mapper).finishBookingAndAttendanceIfOngoing(eq(2L), any(LocalDateTime.class));
        verify(mapper, never()).updateStatus(anyLong(), anyInt());
    }

    private AttendanceCommand command(AttendanceAction action) {
        AttendanceCommand command = new AttendanceCommand();
        command.setBookingId(100L);
        command.setAction(action);
        command.setRemark("  训练记录  ");
        return command;
    }

    private CourseBooking booking(int bookingStatus, int attendanceStatus) {
        CourseBooking booking = new CourseBooking();
        booking.setId(100L);
        booking.setCourseId(200L);
        booking.setMemberId(300L);
        booking.setStatus(bookingStatus);
        booking.setAttendanceStatus(attendanceStatus);
        booking.setCourseStatus(2);
        booking.setCourseDate(COURSE_DATE);
        booking.setCourseStartTime(LocalTime.of(10, 0));
        booking.setCourseEndTime(LocalTime.of(11, 0));
        return booking;
    }

    private void setNow(LocalDateTime now) {
        Instant instant = now.atZone(ZONE).toInstant();
        ReflectionTestUtils.setField(service, "clock", Clock.fixed(instant, ZONE));
    }
}
