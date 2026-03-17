package com.badminton.bmp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 * 用于配置权限控制规则
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 启用CORS配置
            .cors(cors -> cors.disable())
            // SockJS 可能会用 iframe transport；DENY 会导致浏览器控制台报错并影响降级策略
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // 关闭CSRF保护
            .csrf(csrf -> csrf.disable())
            // 不使用Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置权限控制规则
            .authorizeHttpRequests(authz -> authz
                // WebSocket 握手放行，鉴权在 STOMP CONNECT 阶段由 WebSocketAuthInterceptor 完成
                .requestMatchers("/ws", "/ws/**").permitAll()
                // 允许匿名：登录、注册、刷新Token
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/check-lock/**", "/api/auth/forgot-password").permitAll()
                // 反馈可匿名提交（可选带 token 关联用户）
                .requestMatchers(HttpMethod.POST, "/api/feedback").permitAll()
                // 允许匿名访问上传的静态资源（图片等）
                .requestMatchers(HttpMethod.GET, "/api/uploads/**").permitAll()
                // Swagger/OpenAPI 文档与调试（无需登录）
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 其余 /api/auth 及其他接口需要认证
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
