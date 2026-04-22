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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public Result<Object> searchVenues(@RequestParam(value = "keyword", required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Venue> list = venueService.findByVenueNameOrAddress(k, k, null, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "搜索课程", description = "按课程名称关键词搜索，需登录")
    @GetMapping("/courses")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchCourses(@RequestParam(value = "keyword", required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Course> list = courseService.findAll(null, null, null, k, null, null, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "搜索赛事", description = "按赛事名称关键词搜索，需登录")
    @GetMapping("/tournaments")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchTournaments(@RequestParam(value = "keyword", required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Tournament> list = tournamentService.findAll(null, null, null, k, null, null, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "搜索器材", description = "按器材名称/类型关键词搜索，需登录")
    @GetMapping("/equipment")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchEquipment(@RequestParam(value = "keyword", required = false) String keyword) {
        String k = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Equipment> list = equipmentService.findAll(null, null, 1, k, 1, SEARCH_PAGE_SIZE);
        return success(list != null ? list : new ArrayList<>());
    }

    @Operation(summary = "搜索建议", description = "聚合场馆、课程、赛事、器材名称建议，需登录")
    @GetMapping("/suggestions")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchSuggestions(@RequestParam(value = "keyword", required = false) String keyword) {
        String k = keyword == null ? "" : keyword.trim();
        if (k.isEmpty()) {
            return success(new ArrayList<>());
        }

        Set<String> suggestions = new LinkedHashSet<>();
        collectVenueSuggestions(k, suggestions);
        collectCourseSuggestions(k, suggestions);
        collectTournamentSuggestions(k, suggestions);
        collectEquipmentSuggestions(k, suggestions);

        return success(new ArrayList<>(suggestions).subList(0, Math.min(suggestions.size(), 8)));
    }

    @Operation(summary = "综合搜索", description = "同时搜索场馆、课程、赛事、器材，需登录")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> searchAll(@RequestParam(value = "keyword", required = false) String keyword) {
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

    private void collectVenueSuggestions(String keyword, Set<String> suggestions) {
        try {
            List<Venue> venues = venueService.findByVenueNameOrAddress(keyword, keyword, null, 1, SEARCH_PAGE_SIZE);
            if (venues == null) return;
            for (Venue venue : venues) {
                addSuggestion(suggestions, venue != null ? venue.getVenueName() : null);
                if (suggestions.size() >= 8) return;
            }
        } catch (Exception ignored) {
        }
    }

    private void collectCourseSuggestions(String keyword, Set<String> suggestions) {
        try {
            List<Course> courses = courseService.findAll(null, null, null, keyword, null, null, 1, SEARCH_PAGE_SIZE);
            if (courses == null) return;
            for (Course course : courses) {
                addSuggestion(suggestions, course != null ? course.getCourseName() : null);
                if (suggestions.size() >= 8) return;
            }
        } catch (Exception ignored) {
        }
    }

    private void collectTournamentSuggestions(String keyword, Set<String> suggestions) {
        try {
            List<Tournament> tournaments = tournamentService.findAll(null, null, null, keyword, null, null, 1, SEARCH_PAGE_SIZE);
            if (tournaments == null) return;
            for (Tournament tournament : tournaments) {
                addSuggestion(suggestions, tournament != null ? tournament.getTournamentName() : null);
                if (suggestions.size() >= 8) return;
            }
        } catch (Exception ignored) {
        }
    }

    private void collectEquipmentSuggestions(String keyword, Set<String> suggestions) {
        try {
            List<Equipment> equipments = equipmentService.findAll(null, null, 1, keyword, 1, SEARCH_PAGE_SIZE);
            if (equipments == null) return;
            for (Equipment equipment : equipments) {
                addSuggestion(suggestions, equipment != null ? equipment.getEquipmentName() : null);
                if (suggestions.size() >= 8) return;
            }
        } catch (Exception ignored) {
        }
    }

    private void addSuggestion(Set<String> suggestions, String value) {
        if (value == null) return;
        String normalized = value.trim();
        if (!normalized.isEmpty()) {
            suggestions.add(normalized);
        }
    }
}
