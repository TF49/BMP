package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "天气服务", description = "IP 定位与天气代理")
@RestController
@RequestMapping("/api/weather")
public class WeatherController extends BaseController {

    @Operation(summary = "IP 定位", description = "获取当前用户所在城市")
    @GetMapping("/ip-location")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getIpLocation() {
        try {
            // 使用ipapi.co获取IP定位信息
            URL url = new URL("https://ipapi.co/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // 增加超时时间：连接超时15秒，读取超时20秒
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 解析JSON响应（简化处理，实际可以使用Jackson等库）
                String jsonStr = response.toString();
                Map<String, Object> result = new HashMap<>();
                
                // 简单解析city字段
                String city = extractJsonField(jsonStr, "city");
                if (city == null || city.isEmpty()) {
                    city = "北京"; // 默认城市
                }
                
                result.put("city", city);
                result.put("raw", jsonStr); // 保留原始数据供前端使用
                
                return success(result);
            } else {
                // 如果请求失败，返回默认城市
                Map<String, Object> result = new HashMap<>();
                result.put("city", "北京");
                return success(result);
            }
        } catch (Exception e) {
            // 异常时返回默认城市
            Map<String, Object> result = new HashMap<>();
            result.put("city", "北京");
            return success(result);
        }
    }

    @Operation(summary = "获取天气数据", description = "代理 wttr.in，city 默认北京")
    @GetMapping("/wttr")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getWeatherByWttr(@RequestParam(required = false, defaultValue = "北京") String city) {
        try {
            String encodedCity = java.net.URLEncoder.encode(city, StandardCharsets.UTF_8);
            URL url = new URL("https://wttr.in/" + encodedCity + "?format=j1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // 增加超时时间：连接超时20秒，读取超时30秒
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                Map<String, Object> result = new HashMap<>();
                result.put("raw", response.toString()); // 返回原始JSON供前端解析
                result.put("city", city);
                
                return success(result);
            } else {
                return error("天气服务暂时不可用，HTTP状态码：" + responseCode);
            }
        } catch (java.net.SocketTimeoutException e) {
            // 超时异常
            return error("获取天气失败：连接超时，请稍后重试");
        } catch (java.net.ConnectException e) {
            // 连接异常
            return error("获取天气失败：无法连接到天气服务");
        } catch (java.io.IOException e) {
            // IO异常
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("timeout")) {
                return error("获取天气失败：请求超时，请稍后重试");
            }
            return error("获取天气失败：" + errorMsg);
        } catch (Exception e) {
            // 其他异常
            return error("获取天气失败：" + e.getMessage());
        }
    }

    /**
     * 简单提取JSON字段值（用于提取city字段）
     */
    private String extractJsonField(String json, String fieldName) {
        try {
            String pattern = "\"" + fieldName + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
