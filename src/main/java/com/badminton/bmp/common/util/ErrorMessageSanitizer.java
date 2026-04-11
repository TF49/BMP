package com.badminton.bmp.common.util;

/**
 * 统一清洗返回给前端的错误文案，尽量避免暴露底层异常、SQL 或第三方接口细节。
 */
public final class ErrorMessageSanitizer {

    private static final String GENERIC_ERROR = "操作失败，请稍后重试";
    private static final String DATA_DUPLICATED_ERROR = "数据已存在，请检查后重试";

    private static final String[] TECHNICAL_MARKERS = {
            "exception",
            "nullpointer",
            "illegalstate",
            "illegalargument",
            "sql",
            "jdbc",
            "json",
            "parse",
            "could not",
            "cannot invoke",
            "for input string",
            "index out of bounds",
            "classcast",
            "constraint",
            "column ",
            "table ",
            "syntax",
            "http status",
            "url=",
            "code=",
            "invalid host",
            "invalid-host",
            "stack trace",
            "unexpected",
            "ioexception",
            "数据库",
            "空指针",
            "状态码",
            "错误码",
            "接口返回异常",
            "服务返回异常",
            "响应body",
            "第三方",
            "缺少 now"
    };

    private ErrorMessageSanitizer() {
    }

    public static String sanitize(String message) {
        if (message == null) {
            return GENERIC_ERROR;
        }

        String trimmed = message.trim();
        if (trimmed.isEmpty()) {
            return GENERIC_ERROR;
        }

        String lower = trimmed.toLowerCase();

        if (lower.contains("qweather_api_key")) {
            return "天气服务暂时不可用，请稍后重试";
        }

        if (lower.contains("bmp.map.tencent.key")) {
            return "地图服务暂时不可用，请稍后重试";
        }

        if (trimmed.startsWith("数据重复")) {
            return DATA_DUPLICATED_ERROR;
        }

        if (isUserFriendlyMessage(trimmed)) {
            return trimmed;
        }

        String action = extractAction(trimmed);
        return action == null ? GENERIC_ERROR : action + "失败，请稍后重试";
    }

    public static String userFriendlyError(String action, Throwable throwable) {
        String sanitized = sanitize(throwable == null ? null : throwable.getMessage());
        if (!isGenericOperationMessage(sanitized)) {
            return sanitized;
        }

        if (action == null || action.trim().isEmpty()) {
            return GENERIC_ERROR;
        }
        return action.trim() + "失败，请稍后重试";
    }

    public static boolean isGenericOperationMessage(String message) {
        return GENERIC_ERROR.equals(message)
                || (message != null && message.endsWith("失败，请稍后重试"));
    }

    private static boolean isUserFriendlyMessage(String message) {
        if (message.length() > 120) {
            return false;
        }

        String lower = message.toLowerCase();
        for (String marker : TECHNICAL_MARKERS) {
            if (lower.contains(marker)) {
                return false;
            }
        }
        return true;
    }

    private static String extractAction(String message) {
        String normalized = message;

        int zhColonIndex = normalized.indexOf('：');
        int colonIndex = normalized.indexOf(':');
        int splitIndex = zhColonIndex >= 0 ? zhColonIndex : colonIndex;
        if (splitIndex > 0) {
            normalized = normalized.substring(0, splitIndex).trim();
        }

        normalized = normalized
                .replace("时发生错误", "")
                .replace("发生错误", "")
                .replace("出错", "")
                .replace("失败", "")
                .replace("异常", "")
                .trim();

        if ("数据重复".equals(normalized)) {
            return "数据已存在，请检查后重试";
        }

        return normalized.isEmpty() ? null : normalized;
    }
}
