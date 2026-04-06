package com.badminton.bmp.modules.system.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.framework.web.BaseController;
import com.badminton.bmp.modules.system.vo.ReverseGeocodeVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 地图逆地理编码（后端代理腾讯位置服务，避免在小程序端暴露 key）
 */
@Tag(name = "地图服务", description = "逆地理编码等")
@RestController
@RequestMapping("/api/map")
public class MapGeocodeController extends BaseController {

    private static final String TENCENT_GEOCODER = "https://apis.map.qq.com/ws/geocoder/v1/";

    @Value("${bmp.map.tencent.key:}")
    private String tencentMapKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(summary = "逆地理编码", description = "根据 GCJ-02 经纬度解析地址（需配置 bmp.map.tencent.key）")
    @GetMapping("/reverse-geocode")
    @PreAuthorize("isAuthenticated()")
    public Result<ReverseGeocodeVO> reverseGeocode(
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng) {
        if (tencentMapKey == null || tencentMapKey.isBlank()) {
            return error("服务器未配置地图密钥（bmp.map.tencent.key），无法解析地址");
        }
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            return error("经纬度参数不合法");
        }
        try {
            String loc = URLEncoder.encode(lat + "," + lng, StandardCharsets.UTF_8);
            String key = URLEncoder.encode(tencentMapKey.trim(), StandardCharsets.UTF_8);
            String url = TENCENT_GEOCODER + "?location=" + loc + "&get_poi=0&key=" + key;

            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("User-Agent", "BMP-Server/1.0");

            int code = conn.getResponseCode();
            String body;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            code >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                            StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                body = sb.toString();
            }
            conn.disconnect();

            JsonNode root = objectMapper.readTree(body);
            int status = root.path("status").asInt(-1);
            if (status != 0) {
                String msg = root.path("message").asText("地图服务返回错误");
                return error(msg);
            }
            JsonNode result = root.path("result");
            String recommend = result.path("formatted_addresses").path("recommend").asText("");
            String address = recommend.isEmpty() ? result.path("address").asText("") : recommend;
            if (address.isEmpty()) {
                return error("未能解析该坐标对应的地址");
            }
            String title = result.path("address_reference").path("famous_area").path("title").asText("");
            if (title.isEmpty()) {
                title = result.path("address_reference").path("landmark_l2").path("title").asText("");
            }
            return success(new ReverseGeocodeVO(address, title.isEmpty() ? null : title, lat, lng));
        } catch (Exception e) {
            return error("逆地理编码失败：" + e.getMessage());
        }
    }
}
