package com.badminton.bmp.websocket;

import java.time.LocalDateTime;

/**
 * 教练端学员相关实时事件。载荷只包含页面刷新和提示所需的最小字段。
 */
public final class CoachStudentWebSocketEvent {

    public static final String TYPE_NEW_BOOKING = "COACH_STUDENT_NEW_BOOKING";
    public static final String TYPE_CANCELLED = "COACH_STUDENT_CANCELLED";
    public static final String TYPE_ATTENDANCE_CHANGED = "COACH_STUDENT_ATTENDANCE_CHANGED";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";

    private final String type;
    private final String priority;
    private final Long coachId;
    private final Long memberId;
    private final Long bookingId;
    private final Long courseId;
    private final String courseName;
    private final String memberName;
    private final LocalDateTime occurredAt;
    private final Integer bookingStatus;
    private final Integer attendanceStatus;

    public CoachStudentWebSocketEvent(
            String type,
            String priority,
            Long coachId,
            Long memberId,
            Long bookingId,
            Long courseId,
            String courseName,
            String memberName,
            LocalDateTime occurredAt,
            Integer bookingStatus,
            Integer attendanceStatus
    ) {
        this.type = type;
        this.priority = priority;
        this.coachId = coachId;
        this.memberId = memberId;
        this.bookingId = bookingId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.memberName = memberName;
        this.occurredAt = occurredAt;
        this.bookingStatus = bookingStatus;
        this.attendanceStatus = attendanceStatus;
    }

    public String getType() {
        return type;
    }

    public String getPriority() {
        return priority;
    }

    public Long getCoachId() {
        return coachId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getMemberName() {
        return memberName;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Integer getBookingStatus() {
        return bookingStatus;
    }

    public Integer getAttendanceStatus() {
        return attendanceStatus;
    }
}
