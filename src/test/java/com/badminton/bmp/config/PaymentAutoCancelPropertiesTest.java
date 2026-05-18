package com.badminton.bmp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PaymentAutoCancelPropertiesTest {

    @Test
    void timeoutSecondsTakesPriorityOverLegacyTimeoutMinutes() {
        PaymentAutoCancelProperties properties = new PaymentAutoCancelProperties();

        properties.setTimeoutMinutes(5L);
        properties.setTimeoutSeconds(7L);

        assertEquals(7L, properties.getTimeoutSeconds());
        assertEquals(7.0 / 60.0, properties.getTimeoutMinutesForDisplay());
    }

    @Test
    void timeoutMinutesRemainLegacyFallbackWhenSecondsMissing() {
        PaymentAutoCancelProperties properties = new PaymentAutoCancelProperties();

        properties.setTimeoutMinutes(5L);

        assertEquals(300L, properties.getTimeoutSeconds());
    }

    @Test
    void defaultTimeoutIsFiveMinutesWhenNoTimeoutConfigured() {
        PaymentAutoCancelProperties properties = new PaymentAutoCancelProperties();

        assertEquals(300L, properties.getTimeoutSeconds());
    }
}
