package com.badminton.bmp.modules.tournament.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.tournament.entity.Tournament;
import com.badminton.bmp.modules.tournament.service.TournamentService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "赛事管理模块", description = "赛事 CRUD、统计")
@RestController
@RequestMapping("/api/tournament")
public class TournamentController extends BaseController {
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private VenueService venueService;

    private boolean isAdmin() {
        return com.badminton.bmp.common.util.SecurityUtils.isPresident()
                || com.badminton.bmp.common.util.SecurityUtils.isVenueManager();
    }

    @Operation(summary = "赛事列表", description = "支持场馆/状态/类型/关键词/时间筛选与分页")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getAllTournaments(
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 10;
            if (keyword != null && keyword.trim().isEmpty()) keyword = null;
            if (type != null && type.trim().isEmpty()) type = null;
            LocalDateTime start = null;
            LocalDateTime end = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                if (startTime != null && !startTime.trim().isEmpty()) {
                    start = LocalDateTime.parse(startTime.trim(), formatter);
                }
                if (endTime != null && !endTime.trim().isEmpty()) {
                    end = LocalDateTime.parse(endTime.trim(), formatter);
                }
            } catch (DateTimeParseException e) {
                return error("时间格式错误，应为 yyyy-MM-dd HH:mm:ss");
            }
            List<Tournament> tournaments = tournamentService.findAll(venueId, status, type, keyword, start, end, page, size);
            int total = tournamentService.count(venueId, status, type, keyword, start, end);
            Map<String, Object> result = new HashMap<>();
            result.put("data", tournaments);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);
            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取赛事列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "赛事详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Tournament> getTournamentInfo(@PathVariable Long id) {
        try {
            Tournament tournament = tournamentService.findById(id);
            return tournament != null ? success(tournament) : error("赛事不存在");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取赛事信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 添加赛事（需要ADMIN权限）
     * @param tournament 赛事对象
     * @return 添加结果
     */
    @Operation(summary = "添加赛事")
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> addTournament(@Valid @RequestBody Tournament tournament) {
        try {
            // 权限验证：仅管理员可添加赛事
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 添加赛事
            int result = tournamentService.add(tournament);
            
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", tournament.getId());
                return success(data);
            } else {
                return error("添加赛事失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("添加赛事时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新赛事信息（需要ADMIN权限）
     * @param tournament 赛事对象
     * @return 更新结果
     */
    @Operation(summary = "更新赛事")
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateTournament(@Valid @RequestBody Tournament tournament) {
        try {
            // 权限验证：仅管理员可更新赛事
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (tournament.getId() == null) {
                return error("赛事ID不能为空");
            }

            // 更新赛事信息
            int result = tournamentService.update(tournament);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("赛事信息更新失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新赛事信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 删除赛事（需要ADMIN权限）
     * @param id 赛事ID
     * @return 删除结果
     */
    @Operation(summary = "删除赛事")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> deleteTournament(@PathVariable Long id) {
        try {
            // 权限验证：仅管理员可删除赛事
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            // 删除赛事
            int result = tournamentService.deleteById(id);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("删除赛事失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("删除赛事时发生错误：" + e.getMessage());
        }
    }

    /**
     * 更新赛事状态（需要ADMIN权限）
     * @param id 赛事ID
     * @param status 状态值（0-已取消，1-报名中，2-进行中，3-已结束）
     * @return 更新结果
     */
    @Operation(summary = "更新赛事状态")
    @PutMapping("/status")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> updateTournamentStatus(@RequestParam Long id, @RequestParam Integer status) {
        try {
            // 权限验证：仅管理员可更新状态
            if (!isAdmin()) {
                return error("权限不足，仅管理员可执行此操作");
            }

            if (id == null) {
                return error("赛事ID不能为空");
            }
            
            // 验证状态值是否有效（与定时任务、统计一致：仅 0～3）
            if (status == null || status < 0 || status > 3) {
                return error("状态值无效，必须是0（已取消）、1（报名中）、2（进行中）或3（已结束）");
            }
            
            // 更新赛事状态
            int result = tournamentService.updateStatus(id, status);
            
            if (result > 0) {
                return success(null);
            } else {
                return error("更新赛事状态失败");
            }
        } catch (RuntimeException e) {
            return error(e.getMessage());
        } catch (Exception e) {
            return error("更新赛事状态时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "赛事统计")
    @GetMapping("/statistics")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getTournamentStatistics() {
        try {
            return success(tournamentService.getStatistics());
        } catch (Exception e) {
            return error("获取统计信息时发生错误：" + e.getMessage());
        }
    }

    /**
     * 获取场馆下拉列表（供选择场馆使用）
     * @return 场馆列表（仅包含ID和名称）
     */
    @Operation(summary = "场馆下拉列表")
    @GetMapping("/venues")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getVenueList() {
        try {
            List<Venue> venues = venueService.findAll();
            List<Map<String, Object>> venueList = venues.stream()
                    .map(venue -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", venue.getId());
                        item.put("venueName", venue.getVenueName());
                        item.put("status", venue.getStatus());
                        return item;
                    })
                    .collect(Collectors.toList());
            return success(venueList);
        } catch (Exception e) {
            return error("获取场馆列表时发生错误：" + e.getMessage());
        }
    }
}
