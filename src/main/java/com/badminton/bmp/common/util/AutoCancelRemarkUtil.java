package com.badminton.bmp.common.util;

/**
 * 自动取消备注拼接工具。
 */
public final class AutoCancelRemarkUtil {

    private static final int MAX_REMARK_LENGTH = 500;
    private static final String AUTO_CANCEL_SUFFIX = "支付超时自动取消";
    private static final String AUTO_CANCEL_SEPARATOR = "；";

    private AutoCancelRemarkUtil() {
    }

    public static String buildAutoCancelRemark(String originalRemark) {
        if (originalRemark == null || originalRemark.isBlank()) {
            return AUTO_CANCEL_SUFFIX;
        }
        if (originalRemark.contains(AUTO_CANCEL_SUFFIX)) {
            return trimToMaxLength(originalRemark);
        }
        String suffix = AUTO_CANCEL_SEPARATOR + AUTO_CANCEL_SUFFIX;
        int maxOriginalLength = MAX_REMARK_LENGTH - suffix.length();
        if (maxOriginalLength <= 0) {
            return trimToMaxLength(AUTO_CANCEL_SUFFIX);
        }
        String base = originalRemark;
        if (base.length() > maxOriginalLength) {
            base = base.substring(0, maxOriginalLength);
        }
        return base + suffix;
    }

    private static String trimToMaxLength(String value) {
        if (value == null || value.length() <= MAX_REMARK_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_REMARK_LENGTH);
    }
}
