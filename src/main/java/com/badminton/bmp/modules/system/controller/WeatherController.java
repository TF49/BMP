package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "天气服务", description = "IP 定位与天气代理")
@RestController
@RequestMapping("/api/weather")
public class WeatherController extends BaseController {

    @Value("${bmp.weather.qweather.key:}")
    private String qweatherKey;

    @Value("${bmp.weather.qweather.weather-base:https://devapi.qweather.com/v7}")
    private String qweatherWeatherBase;

    @Value("${bmp.weather.qweather.geo-base:https://geoapi.qweather.com/v2}")
    private String qweatherGeoBase;

    @Value("${bmp.weather.qweather.default-location:101010100}")
    private String qweatherDefaultLocation;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    private String resolveLocationId(String cityOrLocationId) throws Exception {
        String input = cityOrLocationId == null ? "" : cityOrLocationId.trim();
        if (!input.isEmpty() && input.matches("\\d+")) {
            return input;
        }

        // 常用兜底：默认城市（例如北京 101010100）。避免 Geo API 异常导致接口不可用。
        if (input.isEmpty() || "北京".equals(input)) {
            return qweatherDefaultLocation;
        }

        // 其他城市：尝试 Geo API lookup
        String encodedCity = java.net.URLEncoder.encode(input, StandardCharsets.UTF_8);
        String geoUrlStr = qweatherGeoBase.replaceAll("/+$", "")
            + "/city/lookup?location=" + encodedCity
            + "&key=" + java.net.URLEncoder.encode(qweatherKey, StandardCharsets.UTF_8);

        String geoRaw = httpGet(geoUrlStr, 15000, 20000);
        JsonNode geoJson = objectMapper.readTree(geoRaw);
        if (!"200".equals(geoJson.path("code").asText())) {
            throw new IllegalStateException("城市解析失败，code=" + geoJson.path("code").asText());
        }

        JsonNode locations = geoJson.path("location");
        if (!locations.isArray() || locations.size() == 0) {
            throw new IllegalStateException("未找到城市");
        }

        JsonNode loc0 = locations.get(0);
        String locationId = loc0.path("id").asText();
        if (locationId == null || locationId.isEmpty()) {
            throw new IllegalStateException("城市解析结果缺少 locationId");
        }
        return locationId;
    }

    @Operation(summary = "获取天气数据", description = "代理 wttr.in，city 默认北京")
    @GetMapping("/wttr")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getWeatherByWttr(@RequestParam(value = "city", required = false, defaultValue = "北京") String city) {
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
                return error("天气服务暂时不可用，请稍后重试");
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

    @Operation(summary = "获取天气数据（和风天气）", description = "后端代理和风天气：先用 Geo API 将城市名解析为 locationId，再拉取 weather/now")
    @GetMapping("/qweather")
    @PreAuthorize("isAuthenticated()")
    public Result<Object> getWeatherByQWeather(@RequestParam(required = false, defaultValue = "北京") String city) {
        if (qweatherKey == null || qweatherKey.trim().isEmpty()) {
            return error("天气服务暂时不可用，请稍后重试");
        }

        try {
            String locationId = resolveLocationId(city);
            String resolvedCity = city;

            String weatherUrlStr = qweatherWeatherBase.replaceAll("/+$", "")
                + "/weather/now?location=" + java.net.URLEncoder.encode(locationId, StandardCharsets.UTF_8)
                + "&key=" + java.net.URLEncoder.encode(qweatherKey, StandardCharsets.UTF_8);

            String weatherRaw;
            try {
                weatherRaw = httpGet(weatherUrlStr, 15000, 20000);
            } catch (IllegalStateException ex) {
                // QWeather 可能针对不同 Key/套餐使用不同 Host：devapi 或 api
                String msg = ex.getMessage() == null ? "" : ex.getMessage();
                if (msg.contains("invalid-host") || msg.contains("Invalid Host")) {
                    String altBase = switchQweatherHostBase(qweatherWeatherBase);
                    if (altBase != null && !altBase.equals(qweatherWeatherBase)) {
                        String altUrl = altBase.replaceAll("/+$", "")
                            + "/weather/now?location=" + java.net.URLEncoder.encode(locationId, StandardCharsets.UTF_8)
                            + "&key=" + java.net.URLEncoder.encode(qweatherKey, StandardCharsets.UTF_8);
                        weatherRaw = httpGet(altUrl, 15000, 20000);
                    } else {
                        throw ex;
                    }
                } else {
                    throw ex;
                }
            }
            JsonNode weatherJson = objectMapper.readTree(weatherRaw);
            if (!"200".equals(weatherJson.path("code").asText())) {
                return error("天气服务暂时不可用，请稍后重试");
            }

            JsonNode now = weatherJson.path("now");
            if (now.isMissingNode() || now.isNull()) {
                return error("天气数据暂时不可用，请稍后重试");
            }

            String temp = now.path("temp").asText();
            String text = now.path("text").asText();
            String iconCode = now.path("icon").asText();

            Map<String, Object> data = new HashMap<>();
            data.put("temp", temp);
            data.put("feelsLike", now.path("feelsLike").asText());
            data.put("text", text);
            data.put("icon", mapIconType(iconCode));
            data.put("humidity", now.path("humidity").asText());
            data.put("windDir", now.path("windDir").asText());
            data.put("windScale", now.path("windScale").asText());
            data.put("advice", getSportAdvice(temp, text));
            data.put("city", resolvedCity);
            data.put("updateTime", now.path("obsTime").asText());

            return success(data);
        } catch (java.net.SocketTimeoutException e) {
            return error("获取天气失败：请求超时，请稍后重试");
        } catch (java.net.ConnectException e) {
            return error("获取天气失败：无法连接到天气服务");
        } catch (java.io.IOException e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("timeout")) {
                return error("获取天气失败：请求超时，请稍后重试");
            }
            return error("获取天气失败：" + errorMsg);
        } catch (Exception e) {
            return error("获取天气失败：" + e.getMessage());
        }
    }

    private String httpGet(String urlStr, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(readTimeoutMs);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errBody = "";
            try {
                if (conn.getErrorStream() != null) {
                    errBody = readResponseBody(conn, conn.getErrorStream());
                }
            } catch (Exception ignored) {
            }

            String safeUrl = redactUrlSecrets(urlStr);
            String msg = "HTTP状态码：" + responseCode + "，URL=" + safeUrl;
            if (errBody != null && !errBody.isEmpty()) {
                msg = msg + "，响应=" + errBody;
            }
            throw new IllegalStateException(msg);
        }

        return readResponseBody(conn, conn.getInputStream());
    }

    private String readResponseBody(HttpURLConnection conn, InputStream rawStream) throws Exception {
        if (rawStream == null) return "";

        InputStream in = rawStream;
        String encoding = conn.getHeaderField("Content-Encoding");
        boolean gzipByHeader = encoding != null && encoding.toLowerCase().contains("gzip");

        // 有些情况下 header 不带 gzip，但 body 是 gzip（二进制以 1F 8B 开头）
        if (!gzipByHeader) {
            PushbackInputStream pb = new PushbackInputStream(in, 2);
            int b1 = pb.read();
            int b2 = pb.read();
            if (b2 != -1) {
                pb.unread(b2);
            }
            if (b1 != -1) {
                pb.unread(b1);
            }
            boolean gzipByMagic = (b1 == 0x1f && b2 == 0x8b);
            in = gzipByMagic ? new GZIPInputStream(pb) : pb;
        } else {
            in = new GZIPInputStream(in);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    private String redactUrlSecrets(String urlStr) {
        if (urlStr == null) return null;
        // 将 query 中的 key=xxxx 替换为 key=***
        return urlStr.replaceAll("(key=)[^&]+", "$1***");
    }

    private String switchQweatherHostBase(String base) {
        if (base == null) return null;
        if (base.contains("devapi.qweather.com")) {
            return base.replace("devapi.qweather.com", "api.qweather.com");
        }
        if (base.contains("api.qweather.com")) {
            return base.replace("api.qweather.com", "devapi.qweather.com");
        }
        return null;
    }

    private String mapIconType(String iconCode) {
        if (iconCode == null) return "sunny";
        String code = String.valueOf(iconCode);
        if ("100".equals(code) || "150".equals(code)) return "sunny";
        if ("101".equals(code) || "102".equals(code) || "103".equals(code) || "151".equals(code)) return "cloudy";
        if ("104".equals(code)) return "overcast";
        if (code.startsWith("3")) return "rainy";
        if (code.startsWith("4")) return "snowy";
        if (code.startsWith("5")) return "foggy";
        return "sunny";
    }

    private String getSportAdvice(String temp, String weather) {
        int tempNum;
        try {
            tempNum = Integer.parseInt(temp);
        } catch (Exception ignored) {
            tempNum = 0;
        }

        String w = weather == null ? "" : weather;
        if (w.contains("雨") || w.contains("雪") || w.contains("暴")) {
            return "不宜户外运动";
        }
        if (w.contains("霾") || w.contains("沙") || w.contains("尘")) {
            return "建议室内运动";
        }
        if (tempNum < 5) {
            return "天气寒冷，注意保暖";
        }
        if (tempNum < 15) {
            return "凉爽舒适，适合运动";
        }
        if (tempNum < 28) {
            return "天气适宜运动";
        }
        if (tempNum < 35) {
            return "天气较热，注意防暑";
        }
        return "高温预警，避免剧烈运动";
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
