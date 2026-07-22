package com.badminton.bmp.modules.agent;

import com.badminton.bmp.modules.agent.security.AgentContext;
import com.badminton.bmp.modules.agent.security.AgentReplayGuard;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class AgentReplayGuardTest {

    private AgentReplayGuard replayGuard;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.ofEpochMilli(1000000L), ZoneId.of("UTC"));
        replayGuard = new AgentReplayGuard(fixedClock);
    }

    @Test
    void tryClaimAcceptsFirstRequestAndRejectsReplay() {
        AgentContext context = new AgentContext("user1", "ROLE_USER", 1L, 1000000L, 2000000L, "nonce123", "trace1");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/agent-tools/venues");
        when(request.getQueryString()).thenReturn("limit=10");

        assertTrue(replayGuard.tryClaim(context, request));
        assertFalse(replayGuard.tryClaim(context, request), "Second claim with identical nonce & fingerprint should be rejected");
    }

    @Test
    void tryClaimRejectsNullOrExpiredContext() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        assertFalse(replayGuard.tryClaim(null, request));

        AgentContext expiredContext = new AgentContext("user1", "ROLE_USER", 1L, 500000L, 900000L, "nonce123", "trace1");
        assertFalse(replayGuard.tryClaim(expiredContext, request));
    }
}
