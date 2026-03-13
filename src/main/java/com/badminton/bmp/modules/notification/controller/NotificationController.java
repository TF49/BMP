package com.badminton.bmp.modules.notification.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.notification.entity.Notification;
import com.badminton.bmp.modules.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "通知管理模块", description = "通知列表、已读、未读数量")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends BaseController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "通知列表", description = "分页查询")
    @GetMapping
    public Result<Object> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 20;

            List<Notification> data = notificationService.findByPage(page, size);
            int total = notificationService.countAll();

            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", total == 0 ? 0 : (total + size - 1) / size);
            return success(result);
        } catch (Exception e) {
            return error("获取通知列表失败：" + e.getMessage());
        }
    }

    @Operation(summary = "发布通知", description = "仅协会会长、场馆管理员")
    @PostMapping
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> create(@Valid @RequestBody Notification notification) {
        try {
            int rows = notificationService.create(notification);
            if (rows > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", notification.getId());
                return success(data);
            }
            return error("发布通知失败");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("发布通知失败：" + e.getMessage());
        }
    }

    @Operation(summary = "通知详情")
    @GetMapping("/{id}")
    public Result<Object> getById(@PathVariable Long id) {
        try {
            Notification notification = notificationService.findById(id);
            if (notification == null) {
                return error("通知不存在");
            }
            return success(notification);
        } catch (Exception e) {
            return error("获取通知详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> delete(@PathVariable Long id) {
        try {
            Notification notification = notificationService.findById(id);
            if (notification == null) {
                return error("通知不存在");
            }

            int rows = notificationService.delete(id);
            if (rows > 0) {
                return success("删除成功");
            }
            return error("删除通知失败");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("删除通知失败：" + e.getMessage());
        }
    }

    @Operation(summary = "编辑通知")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRESIDENT','VENUE_MANAGER')")
    public Result<Object> update(@PathVariable Long id, @Valid @RequestBody Notification notification) {
        try {
            notification.setId(id);
            int rows = notificationService.update(notification);
            if (rows > 0) {
                return success("编辑成功");
            }
            return error("编辑通知失败");
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("编辑通知失败：" + e.getMessage());
        }
    }
}
