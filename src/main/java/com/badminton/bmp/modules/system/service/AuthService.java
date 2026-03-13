package com.badminton.bmp.modules.system.service;

import com.badminton.bmp.modules.system.entity.User;

/**
 * 认证业务接口
 * 支持双Token认证机制（AccessToken + RefreshToken）
 */
public interface AuthService {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 登录结果（包含AccessToken和RefreshToken）
     */
    AuthResult login(String username, String password);

    /**
     * 验证Token
     * @param token JWT Token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 获取当前用户信息
     * @param token JWT Token
     * @return 用户对象
     */
    User getCurrentUser(String token);

    /**
     * 用户注册
     * @param user 用户对象
     * @return 是否注册成功
     */
    boolean register(User user);

    /**
     * 刷新AccessToken
     * @param refreshToken RefreshToken
     * @return 新的AuthResult（包含新的AccessToken，RefreshToken保持不变）
     */
    AuthResult refreshToken(String refreshToken);
}
