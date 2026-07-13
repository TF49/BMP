package com.badminton.bmp.modules.coach.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.exception.RateLimitException;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.coach.dto.CoachStudentListQuery;
import com.badminton.bmp.modules.coach.dto.CoachStudentScheduleQuery;
import com.badminton.bmp.modules.coach.service.CoachService;
import com.badminton.bmp.modules.coach.service.CoachStudentService;
import com.badminton.bmp.modules.coach.support.CoachStudentQueryRateLimiter;
import com.badminton.bmp.modules.coach.vo.CoachStudentAttendanceVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentConsumeRecordVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentDetailVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentListResponseVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentPageVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentProgressVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "教练学员查看", description = "教练只读查看与本人课程有关的学员")
@RestController
@RequestMapping("/api/coach/students")
@PreAuthorize("hasRole('COACH')")
public class CoachStudentController extends BaseController {
    @Autowired
    private CoachStudentService coachStudentService;
    @Autowired
    private CoachService coachService;
    @Autowired
    private CoachStudentQueryRateLimiter rateLimiter;

    @Operation(summary = "我的学员列表")
    @GetMapping
    public Result<CoachStudentListResponseVO> listStudents(@ModelAttribute CoachStudentListQuery query) {
        Long coachId = currentCoachId();
        if (!rateLimiter.tryAcquireList(coachId)) {
            throw new RateLimitException("查询过于频繁，请稍后重试");
        }
        return success(coachStudentService.listStudents(coachId, query));
    }

    @Operation(summary = "学员详情")
    @GetMapping("/{id}")
    public Result<CoachStudentDetailVO> getStudentDetail(@PathVariable("id") Long id) {
        Long coachId = currentCoachId();
        acquireDetail(coachId, id);
        return success(coachStudentService.getStudentDetail(coachId, id));
    }

    @Operation(summary = "学员训练进度")
    @GetMapping("/{id}/progress")
    public Result<List<CoachStudentProgressVO>> getStudentProgress(@PathVariable("id") Long id) {
        Long coachId = currentCoachId();
        acquireDetail(coachId, id);
        return success(coachStudentService.getStudentProgress(coachId, id));
    }

    @Operation(summary = "学员课程安排")
    @GetMapping("/{id}/schedule")
    public Result<CoachStudentPageVO<CoachStudentScheduleVO>> getStudentSchedule(
            @PathVariable("id") Long id, @ModelAttribute CoachStudentScheduleQuery query) {
        Long coachId = currentCoachId();
        acquireDetail(coachId, id);
        return success(coachStudentService.getStudentSchedule(coachId, id, query));
    }

    @Operation(summary = "学员出勤记录")
    @GetMapping("/{id}/attendance")
    public Result<CoachStudentPageVO<CoachStudentAttendanceVO>> getStudentAttendance(
            @PathVariable("id") Long id, @ModelAttribute CoachStudentScheduleQuery query) {
        Long coachId = currentCoachId();
        acquireDetail(coachId, id);
        return success(coachStudentService.getStudentAttendance(coachId, id, query));
    }

    @Operation(summary = "当前教练课程消费记录")
    @GetMapping("/{id}/consume-records")
    public Result<CoachStudentPageVO<CoachStudentConsumeRecordVO>> getStudentConsumeRecords(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Long coachId = currentCoachId();
        acquireDetail(coachId, id);
        return success(coachStudentService.getStudentConsumeRecords(coachId, id, page, size));
    }

    private Long currentCoachId() {
        Long coachId = coachService.getCurrentCoachIdOrNull();
        if (coachId == null) throw new BusinessException("未绑定教练档案，请联系管理员处理");
        return coachId;
    }

    private void acquireDetail(Long coachId, Long memberId) {
        if (!rateLimiter.tryAcquireDetail(coachId, memberId)) {
            throw new RateLimitException("查询过于频繁，请稍后重试");
        }
    }
}
