package com.badminton.bmp.modules.coach.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.coach.entity.Coach;
import com.badminton.bmp.modules.coach.service.CoachService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "教练管理模块", description = "教练 CRUD、统计")
@RestController
@RequestMapping("/api/coach")
public class CoachController extends BaseController {

    @Autowired
    private CoachService coachService;

    @Autowired
    private VenueService venueService;

    /**
     * 检查当前用户是否为管理员
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    @Operation(summary = "教练列表", description = "支持场馆/状态/关键词搜索与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllCoaches(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "coachName", required = false) String coachName,
            @RequestParam(value = "gender", required = false) Integer gender,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            // 兼容前端传入的 coachName 作为 keyword
            if ((keyword == null || keyword.trim().isEmpty()) && coachName != null && !coachName.trim().isEmpty()) {
                keyword = coachName.trim();
            } else if (keyword != null && keyword.trim().isEmpty()) {
                keyword = null;
            } else if (keyword != null) {
                keyword = keyword.trim();
            }

            // 验证分页参数
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 100) {
                size = 10;
            }

            // 调用查询方法
            List<Coach> coaches = coachService.findAll(venueId, status, keyword, gender, page, size);
            // 统计符合条件的教练总数
            int total = coachService.count(venueId, status, keyword, gender);

            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", coaches);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取教练列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "当前教练信息", description = "COACH 角色且已绑定教练档案时返回本人教练详情；未绑定时返回错误")
    @GetMapping("/me")
    @PreAuthorize("hasRole('COACH')")
    public Result<Coach> getCurrentCoach() {
        try {
            Long coachId = coachService.getCurrentCoachIdOrNull();
            if (coachId == null) {
                return error("未绑定教练档案，请联系管理员在教练管理中关联账号");
            }
            Coach coach = coachService.findById(coachId);
            return coach != null ? success(coach) : error("教练不存在");
        } catch (Exception e) {
            return error("获取当前教练信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "教练本人更新档案", description = "仅可更新姓名、电话、专长、经验、头像")
    @PutMapping("/me")
    @PreAuthorize("hasRole('COACH')")
    public Result<Object> updateCurrentCoach(@RequestBody Map<String, Object> body) {
        try {
            Long coachId = coachService.getCurrentCoachIdOrNull();
            if (coachId == null) {
                return error("未绑定教练档案，请联系管理员在教练管理中关联账号");
            }
            String coachName = body != null && body.containsKey("coachName") ? (String) body.get("coachName") : null;
            String phone = body != null && body.containsKey("phone") ? (String) body.get("phone") : null;
            String specialty = body != null && body.containsKey("specialty") ? (String) body.get("specialty") : null;
            String experience = body != null && body.containsKey("experience") ? (String) body.get("experience") : null;
            String avatar = body != null && body.containsKey("avatar") ? (String) body.get("avatar") : null;
            int n = coachService.updateSelfProfile(coachId, coachName, phone, specialty, experience, avatar);
            return n > 0 ? success(null) : error("更新失败");
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新教练档案时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "未绑定教练档案的 COACH 用户列表", description = "供管理端“关联账号”下拉使用，需会长或场馆管理员权限")
    @GetMapping("/unbound-users")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getUnboundCoachUsers() {
        try {
            List<com.badminton.bmp.modules.system.entity.User> users = coachService.getUnboundCoachUsers();
            List<Map<String, Object>> list = users == null ? java.util.Collections.emptyList() : users.stream()
                    .map(u -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", u.getId());
                        m.put("username", u.getUsername());
                        m.put("role", u.getRole());
                        m.put("status", u.getStatus());
                        return m;
                    })
                    .collect(Collectors.toList());
            return success(list);
        } catch (Exception e) {
            return error("获取未绑定教练用户列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "教练详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Coach> getCoachInfo(@PathVariable("id") Long id) {
        try {
            Coach coach = coachService.findById(id);
            if (coach != null) {
                return success(coach);
            } else {
                return error("教练不存在");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取教练信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "添加教练")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> addCoach(@Valid @RequestBody Coach coach) {
        try {
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }
            if (coach.getVenueId() == null) {
                return error("请选择所属场馆");
            }
            int result = coachService.add(coach);
            if (result > 0) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("id", coach.getId());
                return success(resultData);
            } else {
                return error("添加教练失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加教练时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新教练信息")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateCoach(@Valid @RequestBody Coach coach) {
        try {
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }
            if (coach.getId() == null) {
                return error("教练ID不能为空");
            }
            int result = coachService.update(coach);
            if (result > 0) {
                return success(null);
            } else {
                return error("教练信息更新失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新教练信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除教练")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteCoach(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除教练
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除教练
            int result = coachService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除教练失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除教练时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新教练状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateCoachStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }
            if (id == null) {
                return error("教练ID不能为空");
            }
            if (status == null || (status != 0 && status != 1)) {
                return error("请选择有效的状态：正常或停用");
            }
            int result = coachService.updateStatus(id, status);
            if (result > 0) {
                return success(null);
            } else {
                return error("更新教练状态失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新教练状态时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "教练统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCoachStatistics() {
        try {
            Map<String, Object> statistics = coachService.getStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场馆下拉列表")
    @GetMapping("/venues")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueList() {
        try {
            List<Venue> venues = venueService.findAll();
            // 只返回必要的字段：ID、名称、状态
            List<Map<String, Object>> venueList = venues.stream()
                    .map(venue -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", venue.getId());
                        item.put("venueName", venue.getVenueName());
                        item.put("status", venue.getStatus());
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList());
            return success(venueList);
        } catch (Exception e) {
            return error("获取场馆列表时发生错误：" + e.getMessage());
        }
    }
}
