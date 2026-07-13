package com.badminton.bmp.modules.course.dto;

import com.badminton.bmp.modules.course.entity.CourseBooking;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceCommandResult {
    private Long id;
    private Integer bookingStatus;
    private Integer attendanceStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualCheckinTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualFinishTime;

    public static AttendanceCommandResult from(CourseBooking booking) {
        AttendanceCommandResult result = new AttendanceCommandResult();
        result.setId(booking.getId());
        result.setBookingStatus(booking.getStatus());
        result.setAttendanceStatus(booking.getAttendanceStatus());
        result.setActualCheckinTime(booking.getActualCheckinTime());
        result.setActualFinishTime(booking.getActualFinishTime());
        return result;
    }
}
