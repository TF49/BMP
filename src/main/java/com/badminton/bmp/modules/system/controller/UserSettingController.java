package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.system.entity.UserSetting;
import com.badminton.bmp.modules.system.service.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "用户设置", description = "当前用户设置 key-value")
@RestController
@RequestMapping("/api/settings")
public class UserSettingController extends BaseController {

    @Autowired
    private UserSettingService userSettingService;

    @Operation(summary = "获取当前用户设置")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<List<UserSetting>> getUserSettings() {
        try {
            Long userId = getCurrentUserId();
            List<UserSetting> settings = userSettingService.getUserSettings(userId);
            return success(settings);
        } catch (Exception e) {
            return error("获取用户设置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "按 key 获取设置值")
    @GetMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    public Result<UserSetting> getSettingByKey(@PathVariable("key") String key) {
        try {
            Long userId = getCurrentUserId();
            String value = userSettingService.getSetting(userId, key);
            if (value == null) {
                return error("设置不存在");
            }
            UserSetting setting = new UserSetting();
            setting.setUserId(userId);
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            return success(setting);
        } catch (Exception e) {
            return error("获取设置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新或创建设置")
    @PutMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> updateSetting(
            @PathVariable("key") String key,
            @RequestBody Map<String, String> request) {
        try {
            if (key == null || key.trim().isEmpty()) {
                return error("设置key不能为空");
            }

            String value = request.get("value");
            if (value == null || value.trim().isEmpty()) {
                return error("设置value不能为空");
            }

            if (key.length() > 100 || value.length() > 1000) {
                return error("设置内容超过长度限制");
            }

            Long userId = getCurrentUserId();
            userSettingService.updateSetting(userId, key, value);
            return success(null);
        } catch (Exception e) {
            return error("更新设置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除指定 key 的设置")
    @DeleteMapping("/{key}")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> deleteSetting(@PathVariable("key") String key) {
        try {
            if (key == null || key.trim().isEmpty()) {
                return error("设置key不能为空");
            }

            Long userId = getCurrentUserId();
            userSettingService.deleteSetting(userId, key);
            return success(null);
        } catch (Exception e) {
            return error("删除设置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除当前用户全部设置")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Object> deleteAllSettings() {
        try {
            Long userId = getCurrentUserId();
            userSettingService.deleteAllSettings(userId);
            return success(null);
        } catch (Exception e) {
            return error("删除所有设置失败：" + e.getMessage());
        }
    }

    @Operation(summary = "批量更新设置")
    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> batchUpdateSettings(@RequestBody Map<String, String> settings) {
        try {
            if (settings == null || settings.isEmpty()) {
                return error("设置不能为空");
            }

            for (Map.Entry<String, String> entry : settings.entrySet()) {
                if (entry.getKey().length() > 100 || entry.getValue().length() > 1000) {
                    return error("设置内容超过长度限制");
                }
            }

            Long userId = getCurrentUserId();
            userSettingService.batchUpdateSettings(userId, settings);
            return success(null);
        } catch (Exception e) {
            return error("批量更新设置失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID（从 SecurityContext 当前用户获取，避免与 JWT subject/username 混淆）
     */
    private Long getCurrentUserId() {
        com.badminton.bmp.modules.system.entity.User user = com.badminton.bmp.common.util.SecurityUtils.getCurrentUser();
        if (user == null || user.getId() == null) {
            throw new IllegalStateException("未登录或无法获取当前用户");
        }
        return user.getId();
    }
}
