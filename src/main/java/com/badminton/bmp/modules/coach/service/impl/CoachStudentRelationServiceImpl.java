package com.badminton.bmp.modules.coach.service.impl;

import com.badminton.bmp.modules.coach.mapper.CoachStudentRelationMapper;
import com.badminton.bmp.modules.coach.service.CoachStudentRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoachStudentRelationServiceImpl implements CoachStudentRelationService {
    @Autowired
    private CoachStudentRelationMapper relationMapper;

    @Override
    public boolean canCoachViewStudent(Long coachId, Long memberId) {
        if (coachId == null || memberId == null || coachId <= 0 || memberId <= 0) {
            return false;
        }
        return relationMapper.existsActive(coachId, memberId) > 0;
    }
}
