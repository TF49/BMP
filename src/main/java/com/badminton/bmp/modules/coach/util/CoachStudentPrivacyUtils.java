package com.badminton.bmp.modules.coach.util;

public final class CoachStudentPrivacyUtils {
    private CoachStudentPrivacyUtils() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "未填写";
        }
        String normalized = phone.trim();
        if (normalized.matches("1\\d{10}")) {
            return normalized.substring(0, 3) + "****" + normalized.substring(7);
        }
        return normalized;
    }

    public static String idCardTail(String idCard) {
        if (idCard == null || idCard.isBlank()) {
            return null;
        }
        String normalized = idCard.trim();
        return normalized.length() <= 4 ? normalized : normalized.substring(normalized.length() - 4);
    }
}
