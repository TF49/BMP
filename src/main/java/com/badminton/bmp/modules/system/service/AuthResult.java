package com.badminton.bmp.modules.system.service;

import com.badminton.bmp.modules.system.entity.User;

/**
 * 登录结果类
 * 支持双Token认证机制（AccessToken + RefreshToken）
 */
public class AuthResult {
    private boolean success;
    private String token;           // AccessToken
    private String refreshToken;    // RefreshToken
    private Long expiresIn;         // AccessToken有效期（秒）
    private String message;
    private User user;

    /**
     * 构造函数（兼容旧代码）
     */
    public AuthResult(boolean success, String token, String message, User user) {
        this.success = success;
        this.token = token;
        this.message = message;
        this.user = user;
    }

    /**
     * 完整构造函数（包含RefreshToken）
     */
    public AuthResult(boolean success, String token, String refreshToken, Long expiresIn, String message, User user) {
        this.success = success;
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.message = message;
        this.user = user;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
