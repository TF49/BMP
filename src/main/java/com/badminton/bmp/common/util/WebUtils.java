package com.badminton.bmp.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Web工具类
 * 用于获取客户端IP地址、User-Agent等信息
 */
public class WebUtils {

    /**
     * 获取客户端真实IP地址
     * 支持代理和负载均衡场景
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }

        // 优先从 X-Forwarded-For 获取（经过代理）
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            return getFirstIp(ip);
        }

        // 尝试 X-Real-IP（Nginx代理）
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试 Proxy-Client-IP（Apache代理）
        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试 WL-Proxy-Client-IP（WebLogic代理）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试 HTTP_CLIENT_IP
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        // 尝试 HTTP_X_FORWARDED_FOR
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return getFirstIp(ip);
        }

        // 最后使用 RemoteAddr
        ip = request.getRemoteAddr();
        return ip != null ? ip : "UNKNOWN";
    }

    /**
     * 验证IP地址是否有效
     *
     * @param ip IP地址字符串
     * @return 是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 从多个IP地址中获取第一个（处理X-Forwarded-For多级代理）
     *
     * @param ips 逗号分隔的IP地址列表
     * @return 第一个有效IP
     */
    private static String getFirstIp(String ips) {
        if (ips == null || ips.isEmpty()) {
            return "UNKNOWN";
        }
        String[] ipArray = ips.split(",");
        for (String ip : ipArray) {
            String trimmedIp = ip.trim();
            if (isValidIp(trimmedIp)) {
                return trimmedIp;
            }
        }
        return "UNKNOWN";
    }

    /**
     * 获取User-Agent信息
     *
     * @param request HTTP请求对象
     * @return User-Agent字符串，截取前500字符（避免过长）
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "UNKNOWN";
        }
        // 限制长度，避免数据库字段溢出（数据库定义为varchar(500)）
        return userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent;
    }

    /**
     * 获取简化的设备类型
     *
     * @param userAgent User-Agent字符串
     * @return 设备类型: MOBILE/TABLET/PC/BOT/UNKNOWN
     */
    public static String getDeviceType(String userAgent) {
        if (userAgent == null || "UNKNOWN".equals(userAgent)) {
            return "UNKNOWN";
        }

        String ua = userAgent.toLowerCase();

        // 检测机器人/爬虫
        if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) {
            return "BOT";
        }

        // 检测移动设备
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "MOBILE";
        }

        // 检测平板
        if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        }

        // 默认为PC
        return "PC";
    }
}
