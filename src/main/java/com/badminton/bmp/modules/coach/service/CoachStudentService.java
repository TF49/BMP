package com.badminton.bmp.modules.coach.service;

import com.badminton.bmp.modules.coach.dto.CoachStudentListQuery;
import com.badminton.bmp.modules.coach.dto.CoachStudentScheduleQuery;
import com.badminton.bmp.modules.coach.vo.CoachStudentAttendanceVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentConsumeRecordVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentDetailVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentListResponseVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentPageVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentProgressVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentScheduleVO;
import java.util.List;

public interface CoachStudentService {
    CoachStudentListResponseVO listStudents(Long coachId, CoachStudentListQuery query);
    CoachStudentDetailVO getStudentDetail(Long coachId, Long memberId);
    List<CoachStudentProgressVO> getStudentProgress(Long coachId, Long memberId);
    CoachStudentPageVO<CoachStudentScheduleVO> getStudentSchedule(Long coachId, Long memberId,
                                                                  CoachStudentScheduleQuery query);
    CoachStudentPageVO<CoachStudentAttendanceVO> getStudentAttendance(Long coachId, Long memberId,
                                                                      CoachStudentScheduleQuery query);
    CoachStudentPageVO<CoachStudentConsumeRecordVO> getStudentConsumeRecords(Long coachId, Long memberId,
                                                                             int page, int size);
}
