package com.badminton.bmp.modules.coach.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.common.exception.BusinessException;
import com.badminton.bmp.common.util.SecurityUtils;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.coach.mapper.AdminCoachStudentMapper;
import com.badminton.bmp.modules.coach.mapper.CoachMapper;
import com.badminton.bmp.modules.coach.mapper.CoachStudentRelationMapper;
import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.coach.vo.AdminCoachStudentVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentAttendanceVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentConsumeRecordVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentScheduleVO;
import com.badminton.bmp.modules.coach.vo.CoachStudentPageVO;
import com.badminton.bmp.modules.member.mapper.MemberMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端教练学员关系 Controller
 * 权限：PRESIDENT（全量）/ VENUE_MANAGER（仅当前场馆）
 */
@Tag(name = "管理端-学员管理", description = "会长/场馆管理者查看和管理教练与学员的绑定关系")
@RestController
@RequestMapping("/api/admin/coach-students")
@PreAuthorize("hasAnyRole('PRESIDENT', 'VENUE_MANAGER')")
public class AdminCoachStudentController extends BaseController {

    @Autowired
    private AdminCoachStudentMapper adminCoachStudentMapper;

    @Autowired
    private CoachMapper coachMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private CoachStudentRelationMapper relationMapper;

    // ================================================================
    // 列表查询
    // ================================================================

    @Operation(summary = "分页查询教练学员关系列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long coachId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 场馆管理者强制限定只能看自己场馆的数据
        Long effectiveVenueId = resolveVenueId(venueId);

        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        int offset = (page - 1) * size;
        List<AdminCoachStudentVO> list = adminCoachStudentMapper.findList(
                effectiveVenueId, coachId, keyword, offset, size);
        int total = adminCoachStudentMapper.countList(effectiveVenueId, coachId, keyword);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", (total + size - 1) / size);
        return success(result);
    }

    // ================================================================
    // 绑定 / 解绑
    // ================================================================

    @Operation(summary = "手动绑定学员到教练")
    @PostMapping("/bind")
    public Result<String> bind(@RequestBody Map<String, Long> body) {
        if (body == null) {
            return badRequest("请求体不能为空");
        }
        Long coachId = body.get("coachId");
        Long memberId = body.get("memberId");
        if (coachId == null || memberId == null || coachId <= 0 || memberId <= 0) {
            return badRequest("coachId 和 memberId 必须为正数");
        }

        // 权限校验：VENUE_MANAGER 只能操作本场馆的教练
        validateCoachVenueAccess(coachId);

        if (memberMapper.findById(memberId) == null) {
            throw new BusinessException("学员不存在");
        }
        if (relationMapper.existsActive(coachId, memberId) > 0) {
            throw new BusinessException("该学员已与此教练绑定，无需重复绑定");
        }
        relationMapper.activate(coachId, memberId, "MANUAL");
        return success("绑定成功");
    }

    @Operation(summary = "解绑（逻辑删除）教练学员关系")
    @DeleteMapping("/{id}")
    public Result<String> unbind(@PathVariable Long id) {
        if (id == null || id <= 0) return badRequest("关系ID不合法");

        Long coachId = relationMapper.findCoachIdByRelationId(id);
        if (coachId == null) {
            throw new BusinessException("关系不存在或已解绑");
        }
        validateCoachVenueAccess(coachId);
        int affected = relationMapper.deactivate(id);
        if (affected <= 0) {
            throw new BusinessException("关系不存在或已解绑");
        }
        return success("解绑成功");
    }

    // ================================================================
    // 学员详情 - 课表 / 考勤 / 消费
    // ================================================================

    @Operation(summary = "查询学员在指定教练下的课程排课列表")
    @GetMapping("/detail/schedule")
    public Result<CoachStudentPageVO<CoachStudentScheduleVO>> detailSchedule(
            @RequestParam Long coachId,
            @RequestParam Long memberId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        validateCoachVenueAccess(coachId);
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end   = endDate   != null ? LocalDate.parse(endDate)   : null;
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;

        int offset = (page - 1) * size;
        List<CoachStudentScheduleVO> rows = adminCoachStudentMapper.findSchedule(
                coachId, memberId, start, end, status, offset, size);
        int total = adminCoachStudentMapper.countSchedule(coachId, memberId, start, end, status);
        return success(new CoachStudentPageVO<>(rows, total, page, size, (total + size - 1) / size));
    }

    @Operation(summary = "查询学员在指定教练下的考勤历史")
    @GetMapping("/detail/attendance")
    public Result<CoachStudentPageVO<CoachStudentAttendanceVO>> detailAttendance(
            @RequestParam Long coachId,
            @RequestParam Long memberId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer attendanceStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        validateCoachVenueAccess(coachId);
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end   = endDate   != null ? LocalDate.parse(endDate)   : null;
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;

        int offset = (page - 1) * size;
        List<CoachStudentAttendanceVO> rows = adminCoachStudentMapper.findAttendance(
                coachId, memberId, start, end, attendanceStatus, offset, size);
        int total = adminCoachStudentMapper.countAttendance(coachId, memberId, start, end, attendanceStatus);
        return success(new CoachStudentPageVO<>(rows, total, page, size, (total + size - 1) / size));
    }

    @Operation(summary = "查询学员在指定教练下的消费明细")
    @GetMapping("/detail/consume-records")
    public Result<CoachStudentPageVO<CoachStudentConsumeRecordVO>> detailConsumeRecords(
            @RequestParam Long coachId,
            @RequestParam Long memberId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        validateCoachVenueAccess(coachId);
        if (page < 1) page = 1;
        if (size < 1 || size > 100) size = 20;

        int offset = (page - 1) * size;
        List<CoachStudentConsumeRecordVO> rows = adminCoachStudentMapper.findConsumeRecords(
                coachId, memberId, offset, size);
        int total = adminCoachStudentMapper.countConsumeRecords(coachId, memberId);
        return success(new CoachStudentPageVO<>(rows, total, page, size, (total + size - 1) / size));
    }

    // ================================================================
    // 私有辅助方法
    // ================================================================

    /**
     * 解析有效场馆 ID：VENUE_MANAGER 强制使用自己的场馆 ID，PRESIDENT 使用前端传入的
     */
    private Long resolveVenueId(Long requestVenueId) {
        if (SecurityUtils.isVenueManager()) {
            Long venueId = SecurityUtils.getCurrentUserVenueId();
            if (venueId == null) {
                throw new BusinessException("当前管理者未绑定场馆，无法查询学员关系");
            }
            return venueId;
        }
        return requestVenueId; // PRESIDENT 可查全部或按传入的 venueId 过滤
    }

    /**
     * 校验 VENUE_MANAGER 只能操作本场馆的教练
     */
    private void validateCoachVenueAccess(Long coachId) {
        Coach coach = coachMapper.findById(coachId);
        if (coach == null) throw new BusinessException("教练不存在");
        if (SecurityUtils.isPresident()) return;
        Long myVenueId = SecurityUtils.getCurrentUserVenueId();
        if (myVenueId == null) throw new BusinessException("当前管理者未绑定场馆，无法操作");
        if (!myVenueId.equals(coach.getVenueId())) {
            throw new BusinessException("无权操作非本场馆的教练数据");
        }
    }
}
