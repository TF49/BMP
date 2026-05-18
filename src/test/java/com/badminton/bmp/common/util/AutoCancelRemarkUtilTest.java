package com.badminton.bmp.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AutoCancelRemarkUtilTest {

    @Test
    void blankRemarkFallsBackToSystemReason() {
        assertEquals("支付超时自动取消", AutoCancelRemarkUtil.buildAutoCancelRemark(null));
        assertEquals("支付超时自动取消", AutoCancelRemarkUtil.buildAutoCancelRemark("   "));
    }

    @Test
    void existingRemarkKeepsReasonAndStaysWithinLimit() {
        String result = AutoCancelRemarkUtil.buildAutoCancelRemark("用户备注");

        assertTrue(result.contains("支付超时自动取消"));
        assertTrue(result.length() <= 500);
    }

    @Test
    void longRemarkIsTrimmedToAvoidOverflow() {
        String result = AutoCancelRemarkUtil.buildAutoCancelRemark("a".repeat(498));

        assertTrue(result.length() <= 500);
        assertTrue(result.endsWith("；支付超时自动取消"));
    }
}
