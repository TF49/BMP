package com.badminton.bmp.modules.system.service.impl;

import com.badminton.bmp.common.JwtUtils;
import com.badminton.bmp.modules.system.entity.User;
import com.badminton.bmp.modules.system.service.AuthResult;
import com.badminton.bmp.modules.system.service.AuthService;
import com.badminton.bmp.modules.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证业务实现类
 * 支持双Token认证机制（AccessToken + RefreshToken）
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public AuthResult login(String username, String password) {
        try {
            // 查找用户
            User user = userService.findByUsername(username);
            if (user == null) {
                return new AuthResult(false, null, "用户不存在，请先注册", null);
            }

            // 检查用户状态（status: 0-禁用，1-启用）
            if (user.getStatus() == null || user.getStatus() == 0) {
                return new AuthResult(false, null, "用户已被禁用", null);
            }

            // 验证密码（仅使用BCrypt加密验证）
            boolean passwordMatch = userService.verifyPassword(password, user.getPassword());

            if (!passwordMatch) {
                return new AuthResult(false, null, "密码或用户名错误", null);
            }

            // 生成AccessToken（包含venueId）
            String accessToken = jwtUtils.generateToken(username, user.getId(), user.getRole(), user.getVenueId());

            // 生成RefreshToken
            String refreshToken = jwtUtils.generateRefreshToken(username, user.getId());

            // 获取AccessToken有效期（秒）
            Long expiresIn = jwtUtils.getExpirationTimeInSeconds();

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userService.updateLastLoginTime(user.getId());

            // 隐藏密码字段
            user.setPassword(null);

            return new AuthResult(true, accessToken, refreshToken, expiresIn, "登录成功", user);

        } catch (Exception e) {
            logger.error("登录过程中发生异常", e);
            return new AuthResult(false, null, "登录失败，请稍后重试", null);
        }
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtils.validateToken(token);
    }

    @Override
    public User getCurrentUser(String token) {
        if (!validateToken(token)) {
            return null;
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        return userService.findById(userId);
    }

    @Override
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userService.findByUsername(user.getUsername());
            if (existingUser != null) {
                return false;
            }

            // 检查管理员唯一性
            if ("ADMIN".equals(user.getRole())) {
                // 查找所有用户
                List<User> allUsers = userService.findAll();
                // 检查是否已存在管理员
                for (User u : allUsers) {
                    if ("ADMIN".equals(u.getRole())) {
                        return false;
                    }
                }
            }

            // 注册新用户
            int result = userService.register(user);
            return result > 0;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
        try {
            // 验证RefreshToken
            if (!jwtUtils.validateRefreshToken(refreshToken)) {
                return new AuthResult(false, null, "RefreshToken无效或已过期", null);
            }

            // 从RefreshToken获取用户ID
            Long userId = jwtUtils.getUserIdFromRefreshToken(refreshToken);
            if (userId == null) {
                return new AuthResult(false, null, "无法解析RefreshToken", null);
            }

            // 查找用户
            User user = userService.findById(userId);
            if (user == null) {
                return new AuthResult(false, null, "用户不存在", null);
            }

            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() == 0) {
                return new AuthResult(false, null, "用户已被禁用", null);
            }

            // 生成新的AccessToken
            String newAccessToken = jwtUtils.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getRole(),
                    user.getVenueId()
            );

            // 获取AccessToken有效期（秒）
            Long expiresIn = jwtUtils.getExpirationTimeInSeconds();

            // 隐藏密码字段
            user.setPassword(null);

            // 返回新的AccessToken，RefreshToken保持不变
            return new AuthResult(true, newAccessToken, refreshToken, expiresIn, "Token刷新成功", user);

        } catch (Exception e) {
            logger.error("刷新Token过程中发生异常", e);
            return new AuthResult(false, null, "Token刷新失败，请重新登录", null);
        }
    }
}
