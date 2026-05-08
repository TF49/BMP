package com.badminton.bmp.modules.tournament.support;

import java.util.Locale;

public final class TournamentTypeHelper {
    public static final String EVENT_MS = "MS";
    public static final String EVENT_WS = "WS";
    public static final String EVENT_MD = "MD";
    public static final String EVENT_WD = "WD";
    public static final String EVENT_XD = "XD";

    public static final String FORMAT_SINGLE_ELIMINATION = "单败淘汰制";
    public static final String FORMAT_ROUND_ROBIN = "循环赛制";
    public static final String FORMAT_DOUBLE_ELIMINATION = "双败淘汰制";
    public static final String FORMAT_SWISS = "瑞士轮制";

    private TournamentTypeHelper() {
    }

    public static String normalizeEventType(String eventType, String tournamentType, String tournamentName, String description) {
        String normalized = normalizeEventTypeValue(eventType);
        if (normalized != null) {
            return normalized;
        }
        normalized = normalizeLegacyTournamentType(tournamentType);
        if (normalized != null) {
            return normalized;
        }
        String source = safe(tournamentName) + " " + safe(description);
        if (source.contains("混双")) return EVENT_XD;
        if (source.contains("女双")) return EVENT_WD;
        if (source.contains("男双") || source.contains("双打")) return EVENT_MD;
        if (source.contains("女单")) return EVENT_WS;
        if (source.contains("男单") || source.contains("单打")) return EVENT_MS;
        return EVENT_MS;
    }

    public static String normalizeFormatType(String formatType, String tournamentType) {
        String normalized = normalizeFormatTypeValue(formatType);
        if (normalized != null) {
            return normalized;
        }
        normalized = normalizeFormatTypeValue(tournamentType);
        if (normalized != null) {
            return normalized;
        }
        return FORMAT_SINGLE_ELIMINATION;
    }

    public static boolean isDoublesEvent(String eventType) {
        return EVENT_MD.equals(eventType) || EVENT_WD.equals(eventType) || EVENT_XD.equals(eventType);
    }

    public static String eventTypeLabel(String eventType) {
        String normalized = normalizeEventTypeValue(eventType);
        if (EVENT_MS.equals(normalized)) return "男单";
        if (EVENT_WS.equals(normalized)) return "女单";
        if (EVENT_MD.equals(normalized)) return "男双";
        if (EVENT_WD.equals(normalized)) return "女双";
        if (EVENT_XD.equals(normalized)) return "混双";
        return "男单";
    }

    public static String normalizeEventTypeValue(String raw) {
        if (raw == null) return null;
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty()) return null;
        if (EVENT_MS.equals(value) || EVENT_WS.equals(value) || EVENT_MD.equals(value) || EVENT_WD.equals(value) || EVENT_XD.equals(value)) {
            return value;
        }
        return null;
    }

    public static String normalizeFormatTypeValue(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (value.isEmpty()) return null;
        if (FORMAT_SINGLE_ELIMINATION.equals(value)
                || FORMAT_ROUND_ROBIN.equals(value)
                || FORMAT_DOUBLE_ELIMINATION.equals(value)
                || FORMAT_SWISS.equals(value)) {
            return value;
        }
        return null;
    }

    private static String normalizeLegacyTournamentType(String raw) {
        if (raw == null) return null;
        String value = raw.trim().toUpperCase(Locale.ROOT);
        if (value.isEmpty()) return null;
        if ("SINGLE".equals(value)) return EVENT_MS;
        if ("DOUBLE".equals(value)) return EVENT_MD;
        if ("MIXED".equals(value)) return EVENT_XD;
        return null;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
