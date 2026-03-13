package com.badminton.bmp.websocket;

import com.badminton.bmp.common.JwtUtils;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.List;

/**
 * WebSocket CONNECT 阶段鉴权：从 header 或 query 取 token，校验 JWT，绑定 Principal（name = userId 字符串，便于 sendToUser）。
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String TOKEN_HEADER = "token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtils jwtUtils;

    public WebSocketAuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            if (!StringUtils.hasText(token) || !jwtUtils.validateToken(token)) {
                throw new IllegalArgumentException("WebSocket: 未提供或无效的 token");
            }
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            if (userId == null) {
                throw new IllegalArgumentException("WebSocket: token 中无 userId");
            }
            Principal principal = new StompPrincipal(userId.toString(), username);
            accessor.setUser(principal);
        }
        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        List<String> tokenList = accessor.getNativeHeader(TOKEN_HEADER);
        if (tokenList != null && !tokenList.isEmpty() && StringUtils.hasText(tokenList.get(0))) {
            return tokenList.get(0).trim();
        }
        tokenList = accessor.getNativeHeader(AUTHORIZATION_HEADER);
        if (tokenList != null && !tokenList.isEmpty()) {
            String v = tokenList.get(0);
            if (v != null && v.startsWith(BEARER_PREFIX)) {
                return v.substring(BEARER_PREFIX.length()).trim();
            }
        }
        return null;
    }

    /**
     * Principal 的 name 为 userId 字符串，便于 SimpMessagingTemplate.convertAndSendToUser(userId, ...) 匹配。
     */
    public static final class StompPrincipal implements Principal {
        private final String name;
        private final String username;

        public StompPrincipal(String name, String username) {
            this.name = name;
            this.username = username != null ? username : name;
        }

        @Override
        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }
    }
}
