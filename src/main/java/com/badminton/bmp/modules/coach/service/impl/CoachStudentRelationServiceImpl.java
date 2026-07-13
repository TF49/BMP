package com.badminton.bmp.modules.coach.service.impl;

import com.badminton.bmp.modules.coach.mapper.CoachStudentMapper;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CoachStudentRelationServiceImpl implements CoachStudentRelationService {
    private static final int RELATION_RETENTION_DAYS = 180;

    @Autowired
    private CoachStudentMapper coachStudentMapper;

    @Override
    @Cacheable(cacheNames = "coachStudentRelation",
            key = "'coachStudentRelation::' + #coachId + ':' + #memberId")
    public boolean canCoachViewStudent(Long coachId, Long memberId) {
        if (coachId == null || memberId == null || coachId <= 0 || memberId <= 0) {
            return false;
        }
        return coachStudentMapper.existsValidCoachStudentRelation(
                coachId, memberId, RELATION_RETENTION_DAYS) > 0;
    }
}
