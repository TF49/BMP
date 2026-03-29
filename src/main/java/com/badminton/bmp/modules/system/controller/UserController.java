package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.service.UserService;
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

@Tag(name = "用户管理模块", description = "用户 CRUD、按角色查询、个人资料")
@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户列表", description = "需协会会长权限，支持用户名/身份证/角色/状态筛选")
    @GetMapping("/list")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> getAllUsers(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "idCard", required = false) String idCard,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) Integer status,
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
            if (username != null && username.trim().isEmpty()) {
                username = null;
            }
            if (idCard != null && idCard.trim().isEmpty()) {
                idCard = null;
            }
            if (role != null && role.trim().isEmpty()) {
                role = null;
            }

            // 调用搜索方法
            List<User> users = userService.findByUsernameOrIdCard(username, idCard, role, status, page, size);
            // 统计符合条件的用户总数
            int total = userService.countByUsernameOrIdCard(username, idCard, role, status);

            // 隐藏所有用户的密码
            users.forEach(user -> user.setPassword(null));

            // 构造分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("data", users);
            result.put("total", total);
            result.put("page", page);
            result.put("size", size);
            result.put("pages", (total + size - 1) / size);

            return success(result);
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取用户列表时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "用户详情")
    @GetMapping("/info/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<User> getUserInfo(@PathVariable("id") Long id) {
        try {
            User user = userService.findById(id);
            if (user != null) {
                user.setPassword(null); // 隐藏密码
                return success(user);
            } else {
                return error("用户不存在");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("获取用户信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "按角色查询用户", description = "用于前端检查 PRESIDENT 唯一性")
    @GetMapping("/findByRole")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<User> findByRole(@RequestParam("role") String role) {
        try {
            User user = userService.findByRole(role);
            if (user != null) {
                user.setPassword(null);
                return success(user);
            } else {
                return success(null);
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("按角色查询用户失败：" + e.getMessage());
        }
    }

    @Operation(summary = "添加用户")
    @PostMapping("/add")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> addUser(@Valid @RequestBody User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userService.findByUsername(user.getUsername());
            if (existingUser != null) {
                return error("用户名已存在");
            }

            // 检查协会会长唯一性
            if ("PRESIDENT".equals(user.getRole())) {
                if (userService.checkPresidentExists(null)) {
                    return error("系统中已存在协会会长，请先修改现有协会会长的角色");
                }
            }

            // 添加用户
            int result = userService.register(user);

            if (result > 0) {
                return success(null);
            } else {
                return error("添加用户失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("添加用户时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/update")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> updateUser(@Valid @RequestBody User user) {
        try {
            if (user.getId() == null) {
                return error("用户ID不能为空");
            }

            // 检查用户是否存在
            User existingUser = userService.findById(user.getId());
            if (existingUser == null) {
                return error("用户不存在");
            }

            // 检查协会会长唯一性（如果修改角色为PRESIDENT）
            if ("PRESIDENT".equals(user.getRole())) {
                if (userService.checkPresidentExists(user.getId())) {
                    return error("系统中已存在协会会长，请先修改现有协会会长的角色");
                }
            }

            // 更新用户信息
            int result = userService.update(user);

            if (result > 0) {
                return success(null);
            } else {
                return error("用户信息更新失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("更新用户信息时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public Result<Object> deleteUser(@PathVariable("id") Long id) {
        try {
            // 检查用户是否存在
            User existingUser = userService.findById(id);
            if (existingUser == null) {
                return error("用户不存在");
            }

            // 删除用户
            int result = userService.deleteById(id);

            if (result > 0) {
                return success(null);
            } else {
                return error("删除用户失败");
            }
        } catch (AccessDeniedException e) {
            throw e;
        } catch (Exception e) {
            return error("删除用户时发生错误：" + e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户资料")
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<User> getCurrentUserProfile() {
        try {
            Long userId = getCurrentUserId();
            User user = userService.findById(userId);
            if (user != null) {
                user.setPassword(null);
                return success(user);
            } else {
                return error("用户不存在");
            }
        } catch (Exception e) {
            return error("获取个人资料失败：" + e.getMessage());
        }
    }

    @Operation(summary = "更新当前用户资料", description = "仅可编辑手机号、头像、性别、个性签名")
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> updateCurrentUserProfile(@RequestBody Map<String, String> profileData) {
        try {
            Long userId = getCurrentUserId();

            String phone = profileData.get("phone");
            String avatar = profileData.get("avatar");
            String gender = profileData.get("gender");
            String signature = profileData.get("signature");

            // 验证手机号格式（中国手机号）
            if (phone != null && !phone.trim().isEmpty()) {
                if (!phone.matches("^1[3-9]\\d{9}$")) {
                    return error("手机号格式不正确");
                }
            }

            // 验证性别值
            if (gender != null && !gender.trim().isEmpty()) {
                if (!gender.matches("^(MALE|FEMALE|OTHER)$")) {
                    return error("性别值不正确");
                }
            }

            // 验证字段长度
            if (signature != null && signature.length() > 200) {
                return error("个性签名不能超过200个字符");
            }

            int result = userService.updateProfile(userId, phone, avatar, gender, signature);
            if (result > 0) {
                return success(null);
            } else {
                return error("更新个人资料失败");
            }
        } catch (Exception e) {
            return error("更新个人资料时发生错误：" + e.getMessage());
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
