package com.badminton.bmp.common.util;

import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Security 工具类
 * 提供静态方法获取当前用户信息（基于 SecurityContext 或 JWT）
 */
@Component
public class SecurityUtils {

    private static UserService staticUserService;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void init() {
        SecurityUtils.staticUserService = this.userService;
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // 尝试从请求头中解析 - 但通常 JwtAuthenticationFilter 已设置 Authentication
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        } else if (principal != null) {
            return principal.toString();
        }
        return null;
    }

    public static User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) {
            // 无法从 SecurityContext 获取用户名，尝试从请求头的 token 中解析（交给 JwtUtils/Filter）
            return null;
        }
        return staticUserService.findByUsername(username);
    }

    public static Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = new HashSet<>();
        if (authentication != null && authentication.isAuthenticated()) {
            Collection<?> authorities = authentication.getAuthorities();
            if (authorities != null) {
                authorities.forEach(a -> {
                    String s = a.toString();
                    if (s.startsWith("ROLE_")) {
                        roles.add(s.substring(5));
                    } else {
                        roles.add(s);
                    }
                });
            }
        }
        return roles;
    }

    public static boolean isPresident() {
        return getCurrentUserRoles().stream().anyMatch(r -> "PRESIDENT".equalsIgnoreCase(r));
    }

    public static boolean isVenueManager() {
        return getCurrentUserRoles().stream().anyMatch(r -> "VENUE_MANAGER".equalsIgnoreCase(r));
    }

    /** 是否为用户端角色（USER / MEMBER） */
    public static boolean isUser() {
        return getCurrentUserRoles().stream().anyMatch(r ->
                "USER".equalsIgnoreCase(r) || "MEMBER".equalsIgnoreCase(r));
    }

    /** 是否为会员角色 */
    public static boolean isMember() {
        return getCurrentUserRoles().stream().anyMatch(r -> "MEMBER".equalsIgnoreCase(r));
    }

    public static Long getCurrentUserVenueId() {
        // 首先尝试从Authentication的details中获取venueId（更高效）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() != null) {
            if (authentication.getDetails() instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
                Object venueId = details.get("venueId");
                if (venueId instanceof Long) {
                    return (Long) venueId;
                } else if (venueId instanceof Integer) {
                    return ((Integer) venueId).longValue();
                }
            }
        }
        // 如果无法从details获取，则从User对象获取（需要查询数据库）
        User user = getCurrentUser();
        return user != null ? user.getVenueId() : null;
    }
    
    /**
     * 获取当前用户角色（返回单个角色字符串）
     * @return 用户角色，如果未登录返回null
     */
    public static String getCurrentUserRole() {
        Set<String> roles = getCurrentUserRoles();
        if (roles.isEmpty()) {
            return null;
        }
        // 返回第一个角色（通常用户只有一个角色）
        return roles.iterator().next();
    }
}
