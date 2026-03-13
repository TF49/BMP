package com.badminton.bmp.modules.venue.service.impl;

import com.badminton.bmp.modules.venue.entity.VenueStatusLog;
import com.badminton.bmp.modules.venue.mapper.VenueStatusLogMapper;
import com.badminton.bmp.modules.venue.service.VenueStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场馆状态变更日志业务实现类
 */
@Service
public class VenueStatusLogServiceImpl implements VenueStatusLogService {

    @Autowired
    private VenueStatusLogMapper venueStatusLogMapper;

    @Override
    public List<VenueStatusLog> findByVenueId(Long venueId) {
        return venueStatusLogMapper.findByVenueId(venueId);
    }

    @Override
    public int logStatusChange(Long venueId, Integer oldStatus, Integer newStatus, 
                               Long operatorId, String operatorName, String changeReason) {
        VenueStatusLog log = new VenueStatusLog();
        log.setVenueId(venueId);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setChangeReason(changeReason);
        log.setCreateTime(LocalDateTime.now());
        return venueStatusLogMapper.insert(log);
    }
}
