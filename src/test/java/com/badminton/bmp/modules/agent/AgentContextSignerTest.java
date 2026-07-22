package com.badminton.bmp.modules.agent;

import com.badminton.bmp.modules.agent.config.AgentProperties;
import com.badminton.bmp.modules.agent.security.AgentContext;
import com.badminton.bmp.modules.agent.security.AgentContextException;
import com.badminton.bmp.modules.agent.security.AgentContextSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentContextSignerTest {

    private AgentContextSigner signer;

    @BeforeEach
    void setUp() {
        AgentProperties properties = new AgentProperties();
        properties.setContextSecret("test-hmac-secret-32-bytes-length!!");
        properties.setContextTtlSeconds(120);
        signer = new AgentContextSigner(properties);
    }

    @Test
    void issueAndVerifySuccess() {
        AgentContext context = signer.issue("user_123", "ADMIN", 1L, "trace_abc");
        assertNotNull(context);
        assertEquals("user_123", context.userId());
        assertEquals("ADMIN", context.role());
        assertEquals(1L, context.venueId());
        assertEquals("trace_abc", context.traceId());

        String token = signer.sign(context);
        assertNotNull(token);

        AgentContext verified = signer.verify(token);
        assertEquals(context.userId(), verified.userId());
        assertEquals(context.role(), verified.role());
        assertEquals(context.venueId(), verified.venueId());
        assertEquals(context.traceId(), verified.traceId());
    }

    @Test
    void verifyRejectsMissingOrMalformedToken() {
        assertThrows(AgentContextException.class, () -> signer.verify(null));
        assertThrows(AgentContextException.class, () -> signer.verify(""));
        assertThrows(AgentContextException.class, () -> signer.verify("invalid.token.parts"));
        assertThrows(AgentContextException.class, () -> signer.verify("invalid_base64.signature"));
    }

    @Test
    void verifyRejectsTamperedToken() {
        AgentContext context = signer.issue("user_123", "ADMIN", 1L, "trace_abc");
        String token = signer.sign(context);
        int dot = token.indexOf('.');
        String tampered = token.substring(0, dot) + ".invalid_signature_bytes";

        AgentContextException exception = assertThrows(AgentContextException.class, () -> signer.verify(tampered));
        assertEquals(AgentContextException.Reason.TAMPERED, exception.getReason());
    }

    @Test
    void verifyRejectsExpiredToken() {
        AgentProperties properties = new AgentProperties();
        properties.setContextSecret("test-hmac-secret-32-bytes-length!!");
        properties.setContextTtlSeconds(-10); // Expiration in past
        AgentContextSigner expiredSigner = new AgentContextSigner(properties);

        AgentContext context = expiredSigner.issue("user_123", "USER", null, "trace_exp");
        String token = expiredSigner.sign(context);

        AgentContextException exception = assertThrows(AgentContextException.class, () -> expiredSigner.verify(token));
        assertEquals(AgentContextException.Reason.EXPIRED, exception.getReason());
    }
}
