package com.badminton.bmp.modules.venue.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class VenueSchedule {
    private Long id;

    /**
     * 由路径参数中的 venueId 注入，前端保存营业时间时无需在请求体中重复传递。
     */
    private Long venueId;

    private String scheduleType;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer isActive;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
