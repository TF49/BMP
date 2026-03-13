package com.badminton.bmp.modules.venue.service.impl;

import com.badminton.bmp.modules.venue.entity.VenueSchedule;
import com.badminton.bmp.modules.venue.mapper.VenueScheduleMapper;
import com.badminton.bmp.modules.venue.service.VenueScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 场馆营业时间业务实现类
 */
@Service
public class VenueScheduleServiceImpl implements VenueScheduleService {

    @Autowired
    private VenueScheduleMapper venueScheduleMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public List<VenueSchedule> findByVenueId(Long venueId) {
        return venueScheduleMapper.findByVenueId(venueId);
    }

    @Override
    public VenueSchedule findByVenueIdAndType(Long venueId, String scheduleType) {
        return venueScheduleMapper.findByVenueIdAndType(venueId, scheduleType);
    }

    @Override
    public int add(VenueSchedule schedule) {
        if (schedule.getCreateTime() == null) {
            schedule.setCreateTime(LocalDateTime.now());
        }
        if (schedule.getUpdateTime() == null) {
            schedule.setUpdateTime(LocalDateTime.now());
        }
        if (schedule.getIsActive() == null) {
            schedule.setIsActive(1);
        }
        return venueScheduleMapper.insert(schedule);
    }

    @Override
    public int update(VenueSchedule schedule) {
        schedule.setUpdateTime(LocalDateTime.now());
        return venueScheduleMapper.update(schedule);
    }

    @Override
    public int deleteById(Long id) {
        return venueScheduleMapper.deleteById(id);
    }

    @Override
    public int deleteByVenueId(Long venueId) {
        return venueScheduleMapper.deleteByVenueId(venueId);
    }

    @Override
    public int batchSave(Long venueId, List<VenueSchedule> schedules) {
        // 先删除该场馆的所有时间表
        deleteByVenueId(venueId);

        // 再添加新的时间表
        int count = 0;
        for (VenueSchedule schedule : schedules) {
            schedule.setVenueId(venueId);
            if (add(schedule) > 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean validateBusinessHours(String startTime, String endTime) {
        if (startTime == null || endTime == null || startTime.trim().isEmpty() || endTime.trim().isEmpty()) {
            return false;
        }

        try {
            LocalTime start = LocalTime.parse(startTime.trim(), TIME_FORMATTER);
            LocalTime end = LocalTime.parse(endTime.trim(), TIME_FORMATTER);

            // 开始时间必须小于结束时间（不允许跨夜）
            return start.isBefore(end);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public VenueSchedule findById(Long id) {
        return venueScheduleMapper.findById(id);
    }
}
