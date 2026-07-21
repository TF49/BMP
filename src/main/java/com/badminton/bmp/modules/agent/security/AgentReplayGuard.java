package com.badminton.bmp.modules.agent.security;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

/** Rejects replay of the same signed context against the same Tool request. */
@Component
public class AgentReplayGuard {

    private static final int MAX_CLAIMS = 100_000;

    private final Clock clock;
    private final ConcurrentMap<String, Long> claims = new ConcurrentHashMap<>();

    public AgentReplayGuard() {
        this(Clock.systemUTC());
    }

    public AgentReplayGuard(Clock clock) {
        this.clock = clock;
    }

    public boolean tryClaim(AgentContext context, HttpServletRequest request) {
        long now = clock.millis();
        if (context == null || context.expiresAt() <= now || context.nonce() == null
                || context.nonce().isBlank()) {
            return false;
        }

        claims.entrySet().removeIf(entry -> entry.getValue() <= now);
        String key = context.nonce() + ':' + requestFingerprint(request);
        if (!claims.containsKey(key) && claims.size() >= MAX_CLAIMS) {
            return false;
        }
        return claims.putIfAbsent(key, context.expiresAt()) == null;
    }

    private String requestFingerprint(HttpServletRequest request) {
        String material = request.getMethod() + '\n' + request.getRequestURI() + '\n'
                + canonicalParameters(request);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(material.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private String canonicalParameters(HttpServletRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();
        if (parameters == null || parameters.isEmpty()) {
            return request.getQueryString() == null ? "" : request.getQueryString();
        }
        Map<String, String[]> sorted = new TreeMap<>(parameters);
        StringBuilder result = new StringBuilder();
        sorted.forEach((name, values) -> {
            String[] ordered = values == null ? new String[0] : values.clone();
            Arrays.sort(ordered);
            for (String value : ordered) {
                if (!result.isEmpty()) {
                    result.append('&');
                }
                result.append(name).append('=').append(value);
            }
        });
        return result.toString();
    }
}
