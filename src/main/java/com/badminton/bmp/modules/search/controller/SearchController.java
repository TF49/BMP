package com.badminton.bmp.modules.search.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.course.entity.Course;
import com.badminton.bmp.modules.course.service.CourseService;
import com.badminton.bmp.modules.equipment.entity.Equipment;
import com.badminton.bmp.modules.equipment.service.EquipmentService;
import com.badminton.bmp.modules.tournament.entity.Tournament;
import com.badminton.bmp.modules.tournament.service.TournamentService;
import com.badminton.bmp.modules.venue.entity.Venue;
import com.badminton.bmp.modules.venue.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 综合搜索接口（用户端搜索页）
 * 支持按关键词搜索场馆、课程、赛事、器材，数据与管理员端同一套后端。
 */
@Tag(name = "搜索模块", description = "用户端综合搜索：场馆、课程、赛事、器材")
@RestController
@RequestMapping("/api/search")
public class SearchController extends BaseController {

    @Autowired
    private VenueService venueService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private TournamentService tournamentService;
    @Autowired
    private EquipmentService equipmentService;

    private static final int SEARCH_PAGE_SIZE = 50;

    @Operation(summary = "搜索场馆", description = "按名称或地址关键词搜索，需登录")
    @GetMapping("/venues")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchVenues(@RequestParam(required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Venue> list = venueService.findByVenueNameOrAddress(k, k, null, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "搜索课程", description = "按课程名称关键词搜索，需登录")
    @GetMapping("/courses")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchCourses(@RequestParam(required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Course> list = courseService.findAll(null, null, null, k, null, null, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "搜索赛事", description = "按赛事名称关键词搜索，需登录")
    @GetMapping("/tournaments")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchTournaments(@RequestParam(required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Tournament> list = tournamentService.findAll(null, null, null, k, null, null, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "搜索器材", description = "按器材名称/类型关键词搜索，需登录")
    @GetMapping("/equipment")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchEquipment(@RequestParam(required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Equipment> list = equipmentService.findAll(null, null, 1, k, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "综合搜索", description = "同时搜索场馆、课程、赛事、器材，需登录")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchAll(@RequestParam(required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Venue> venues = venueService.findByVenueNameOrAddress(k, k, null, 1, SEARCH_PAGE_SIZE);
        List<Course> courses = courseService.findAll(null, null, null, k, null, null, 1, SEARCH_PAGE_SIZE);
        List<Tournament> tournaments = tournamentService.findAll(null, null, null, k, null, null, 1, SEARCH_PAGE_SIZE);
        List<Equipment> equipment = equipmentService.findAll(null, null, 1, k, 1, SEARCH_PAGE_SIZE);
        Map<String, Object> result = new HashMap<>();
        result.put("venues", venues != null ? venues : new ArrayList<>());
        result.put("courses", courses != null ? courses : new ArrayList<>());
        result.put("tournaments", tournaments != null ? tournaments : new ArrayList<>());
        result.put("equipment", equipment != null ? equipment : new ArrayList<>());
        return success(result);
    }
}
