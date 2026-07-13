package com.badminton.bmp.modules.course.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttendanceCommand {
    @NotNull(message = "预约ID不能为空")
    @JsonProperty("id")
    private Long bookingId;

    @NotNull(message = "考勤操作不能为空")
    private AttendanceAction action;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
