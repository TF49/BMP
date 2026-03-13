package com.badminton.bmp.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和验证JWT Token
 *
 * 安全说明：
 * - JWT密钥从配置文件读取，支持环境变量覆盖
 * - 生产环境请务必通过环境变量JWT_SECRET设置强密钥
 * - 密钥长度至少256位（32字节）以满足HS256算法要求
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    /**
     * JWT密钥，从配置文件注入
     * 生产环境应通过环境变量JWT_SECRET设置
     */
    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Token有效期（毫秒），从配置文件注入
     * 默认2小时
     */
    @Value("${jwt.expiration:7200000}")
    private long expirationTime;

    /**
     * RefreshToken有效期（毫秒），从配置文件注入
     * 默认7天
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpirationTime;

    private SecretKey signingKey;

    /**
     * 初始化签名密钥
     * 在Bean创建后自动调用
     */
    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.length() < 32) {
            logger.warn("JWT密钥长度不足32字节，可能存在安全风险，请检查jwt.secret配置");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        logger.info("JWT工具类初始化完成，AccessToken有效期: {}ms，RefreshToken有效期: {}ms", expirationTime, refreshExpirationTime);
    }

    private SecretKey getSigningKey() {
        return this.signingKey;
    }
    
    /**
     * 生成JWT Token
     * @param username 用户名
     * @param userId 用户ID
     * @param role 用户角色
     * @return JWT Token
     */
    public String generateToken(String username, Long userId, String role) {
        return generateToken(username, userId, role, null);
    }
    
    /**
     * 生成JWT Token（包含venueId）
     * @param username 用户名
     * @param userId 用户ID
     * @param role 用户角色
     * @param venueId 场馆ID（可为null）
     * @return JWT Token
     */
    public String generateToken(String username, Long userId, String role, Long venueId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("username", username);
        if (venueId != null) {
            claims.put("venueId", venueId);
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 验证JWT Token
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 从Token中获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }
    
    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }
    
    /**
     * 从Token中获取角色
     * @param token JWT Token
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }
    
    /**
     * 从Token中获取场馆ID
     * @param token JWT Token
     * @return 场馆ID（可能为null）
     */
    public Long getVenueIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object venueId = claims.get("venueId");
            if (venueId instanceof Integer) {
                return ((Integer) venueId).longValue();
            } else if (venueId instanceof Long) {
                return (Long) venueId;
            }
        }
        return null;
    }
    
    /**
     * 从Token中获取Claims
     * @param token JWT Token
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== RefreshToken相关方法 ====================

    /**
     * 生成RefreshToken
     * @param username 用户名
     * @param userId 用户ID
     * @return RefreshToken
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh"); // 标记为RefreshToken

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证RefreshToken
     * @param refreshToken RefreshToken
     * @return 是否有效
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = getClaimsFromToken(refreshToken);
            if (claims == null) {
                return false;
            }
            // 验证是RefreshToken类型
            String type = (String) claims.get("type");
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从RefreshToken中获取用户ID
     * @param refreshToken RefreshToken
     * @return 用户ID
     */
    public Long getUserIdFromRefreshToken(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            return null;
        }
        return getUserIdFromToken(refreshToken);
    }

    /**
     * 获取AccessToken有效期（秒）
     * @return 有效期秒数
     */
    public long getExpirationTimeInSeconds() {
        return expirationTime / 1000;
    }

    /**
     * 检查Token是否即将过期（剩余时间小于30分钟）
     * @param token JWT Token
     * @return 是否即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            // 剩余时间小于30分钟（1800000毫秒）
            return expiration.getTime() - System.currentTimeMillis() < 1800000;
        } catch (Exception e) {
            return true;
        }
    }
}
