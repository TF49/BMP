package com.badminton.bmp.modules.coach.service;

public interface CoachStudentRelationService {
    boolean canCoachViewStudent(Long coachId, Long memberId);
}
