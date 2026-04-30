package com.badminton.bmp.modules.course.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.coach.service.CoachService;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.service.CourseService;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.service.CourtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "课程管理模块", description = "课程 CRUD、统计")
@RestController
@RequestMapping("/api/course")
public class CourseController extends BaseController {
    private static final String COACH_UNBOUND_MESSAGE = "未绑定教练档案，请联系管理员在教练管理中关联账号";

    @Autowired
    private CourseService courseService;
    @Autowired
    private CoachService coachService;
    @Autowired
    private CourtService courtService;

    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    @Operation(summary = "我的课程", description = "COACH 角色：仅返回当前教练的课程列表，支持时间范围与分页")
    @GetMapping("/my")
    @PreAuthorize("hasRole('COACH')")
    public Result<Object> getMyCourses(
            @RequestParam(value = "courtId", required = false) Long courtId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            Long coachId = coachService.getCurrentCoachIdOrNull();
            if (coachId == null) {
                return error(COACH_UNBOUND_MESSAGE);
            }
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            if (keyword != null && keyword.trim().isEmpty()) keyword = null;
            if (startTime != null && startTime.trim().isEmpty()) startTime = null;
            if (endTime != null && endTime.trim().isEmpty()) endTime = null;
            List<Course> courses = courseService.findAll(coachId, courtId, status, keyword, startTime, endTime, page, size);
            int total = courseService.count(coachId, courtId, status, keyword, startTime, endTime);
            Map<String, Object> result = new HashMap<>();
            result.put("data", courses);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (Exception e) {
            return error("获取我的课程时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "课程列表", description = "支持教练/场地/状态/关键词/时间筛选与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllCourses(
            @RequestParam(value = "coachId", required = false) Long coachId,
            @RequestParam(value = "courtId", required = false) Long courtId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            if (keyword != null && keyword.trim().isEmpty()) keyword = null;
            if (startTime != null && startTime.trim().isEmpty()) startTime = null;
            if (endTime != null && endTime.trim().isEmpty()) endTime = null;
            List<Course> courses = courseService.findAll(coachId, courtId, status, keyword, startTime, endTime, page, size);
            int total = courseService.count(coachId, courtId, status, keyword, startTime, endTime);
            Map<String, Object> result = new HashMap<>();
            result.put("data", courses);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取课程列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "课程详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Course> getCourseInfo(@PathVariable("id") Long id) {
        try {
            Course course = courseService.findById(id);
            return course != null ? success(course) : error("课程不存在");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取课程信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 添加课程（需要ADMIN权限）
     * @param course 课程对象
     * @return 添加结果
     */
    @Operation(summary = "添加课程")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> addCourse(@Valid @RequestBody Course course) {
        try {
            // 权限验证：仅管理员可添加课程
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 添加课程
            int result = courseService.add(course);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", course.getId());
                return success(data);
            } else {
                return error("添加课程失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加课程时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新课程信息（需要ADMIN权限）
     * @param course 课程对象
     * @return 更新结果
     */
    @Operation(summary = "更新课程")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateCourse(@Valid @RequestBody Course course) {
        try {
            // 权限验证：仅管理员可更新课程
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (course.getId() == null) {
                return error("课程ID不能为空");
            }

            // 更新课程信息
            int result = courseService.update(course);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("课程信息更新失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新课程信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除课程（需要ADMIN权限）
     * @param id 课程ID
     * @return 删除结果
     */
    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteCourse(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除课程
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除课程
            int result = courseService.deleteById(id);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("删除课程失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除课程时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新课程状态（需要ADMIN权限）
     * @param id 课程ID
     * @param status 状态值（0-已取消，1-报名中，2-进行中，3-已结束）
     * @return 更新结果
     */
    @Operation(summary = "更新课程状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateCourseStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {
            // 权限验证：仅管理员可更新状态
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (id == null) {
                return error("课程ID不能为空");
            }
            
            // 验证状态值是否有效
            if (status == null || status < 0 || status > 3) {
                return error("状态值无效，必须是0（已取消）、1（报名中）、2（进行中）或3（已结束）");
            }
            
            // 更新课程状态
            int result = courseService.updateStatus(id, status);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("更新课程状态失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新课程状态时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "课程统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCourseStatistics() {
        try {
            return success(courseService.getStatistics());
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取教练下拉列表（供选择教练使用）
     * @return 教练列表（ID、姓名、状态；会长查全部，场馆管理员仅本场馆；含正常与停用便于按教练筛选课程）
     */
    @Operation(summary = "教练下拉列表")
    @GetMapping("/coaches")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCoachList() {
        try {
            List<Coach> coaches = coachService.findAll(null, null, null, null, 1, 500);
            List<Map<String, Object>> coachList = coaches.stream()
                    .map(coach -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", coach.getId());
                        item.put("coachName", coach.getCoachName());
                        item.put("status", coach.getStatus());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(coachList);
        } catch (Exception e) {
            return error("获取教练列表时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取场地下拉列表（供选择场地使用）
     * @return 场地列表（ID、名称、编码、状态；会长查全部，场馆管理员仅本场馆；含正常与停用便于按场地筛选课程）
     */
    @Operation(summary = "场地下拉列表")
    @GetMapping("/courts")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCourtList() {
        try {
            List<Court> courts = courtService.findAll(null, null, null, 1, 500);
            List<Map<String, Object>> courtList = courts.stream()
                    .map(court -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", court.getId());
                        item.put("courtName", court.getCourtName());
                        item.put("courtCode", court.getCourtCode());
                        item.put("status", court.getStatus());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(courtList);
        } catch (Exception e) {
            return error("获取场地列表时发生错误：" + e.getMessage());
        }
    }
}
