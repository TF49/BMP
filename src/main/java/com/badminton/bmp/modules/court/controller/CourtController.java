package com.badminton.bmp.modules.court.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.court.dto.CourtBookingUserDTO;
import com.badminton.bmp.modules.court.entity.Court;
import com.badminton.bmp.modules.court.service.CourtService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "场地管理模块", description = "场地 CRUD、状态、统计、预约时间轴")
@RestController
@RequestMapping("/api/court")
public class CourtController extends BaseController {

    @Autowired
    private CourtService courtService;

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


    @Operation(summary = "场地列表", description = "支持按场馆、状态、关键词搜索与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllCourts(
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            // 验证分页参数
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 100) {
                size = 10;
            }

            // 将空字符串转换为null，便于后端查询
            if (keyword != null && keyword.trim().isEmpty()) {
                keyword = null;
            }

            // 调用查询方法
            List<Court> courts = courtService.findAll(venueId, status, keyword, page, size);
            // 统计符合条件的场地总数
            int total = courtService.count(venueId, status, keyword);

            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", courts);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取场地列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场地详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Court> getCourtInfo(@PathVariable("id") Long id) {
        try {
            Court court = courtService.findById(id);
            if (court != null) {
                return success(court);
            } else {
                return error("场地不存在");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取场地信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "添加场地")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> addCourt(@Valid @RequestBody Court court) {
        try {
            // 权限验证：仅管理员可添加场地
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 添加场地
            int result = courtService.add(court);

            if (result > 0) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("id", court.getId());
                return success(resultData);
            } else {
                return error("添加场地失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加场地时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新场地信息")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateCourt(@Valid @RequestBody Court court) {
        try {
            // 权限验证：仅管理员可更新场地
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (court.getId() == null) {
                return error("场地ID不能为空");
            }

            // 更新场地信息
            int result = courtService.update(court);

            if (result > 0) {
                return success(null);
            } else {
                return error("场地信息更新失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新场地信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除场地")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteCourt(@PathVariable("id") Long id) {
        try {
            // 权限验证：仅管理员可删除场地
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除场地
            int result = courtService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除场地失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除场地时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新场地状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateCourtStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status) {
        try {
            // 权限验证：仅管理员可更新状态
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (id == null) {
                return error("场地ID不能为空");
            }

            // 更新状态
            int result = courtService.updateStatus(id, status);

            if (result > 0) {
                return success(null);
            } else {
                return error("更新场地状态失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新场地状态时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场地统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getCourtStatistics() {
        try {
            Map<String, Object> statistics = courtService.getStatistics();
            return success(statistics);
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "场馆下拉列表", description = "仅 ID、名称、状态，供选择场馆使用")
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

    @Operation(summary = "指定场地当天预约用户", description = "仅管理员，返回脱敏信息")
    @GetMapping("/{id}/bookings/today")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> getTodayBookingUsers(@PathVariable("id") Long id,
                                              @RequestParam(value = "date", required = false) String date) {
        try {
            List<CourtBookingUserDTO> bookingUsers = courtService.getTodayBookingUsers(id, date);
            return success(bookingUsers);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("获取当天预约用户时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "批量获取场地当天预约数量", description = "ids 逗号分隔，返回场地ID与数量映射")
    @GetMapping("/bookings/today/counts")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getTodayBookingCounts(@RequestParam("ids") String ids,
                                               @RequestParam(value = "date", required = false) String date) {
        try {
            if (ids == null || ids.trim().isEmpty()) {
                return success(new HashMap<>());
            }

            // 解析ID列表
            String[] idArray = ids.split(",");
            List<Long> courtIds = new java.util.ArrayList<>();
            for (String idStr : idArray) {
                try {
                    courtIds.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException ignored) {
                    // 忽略无效的ID
                }
            }

            if (courtIds.isEmpty()) {
                return success(new HashMap<>());
            }

            Map<Long, Integer> counts = courtService.getTodayBookingCounts(courtIds, date);
            return success(counts);
        } catch (Exception e) {
            return error("获取预约数量时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "今日场地时间轴", description = "指定日期各场地占用时间轴，date 格式 yyyy-MM-dd")
    @GetMapping("/timeline/today")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getTimelineToday(@RequestParam(value = "date", required = false) String date) {
        try {
            java.util.List<java.util.Map<String, Object>> list = courtService.getTimelineToday(date);
            return success(list);
        } catch (Exception e) {
            return error("获取今日场地时间轴时发生错误：" + e.getMessage());
        }
    }
}
