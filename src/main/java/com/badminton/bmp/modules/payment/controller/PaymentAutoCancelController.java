package com.badminton.bmp.modules.payment.controller;

import com.badminton.bmp.common.Result;
import com.badminton.bmp.config.PaymentAutoCancelProperties;
import com.badminton.bmp.framework.web.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "支付自动取消配置", description = "提供业务订单支付超时自动取消只读配置，供前端倒计时与服务端时间对齐使用")
@RestController
@RequestMapping("/api/payment/auto-cancel")
public class PaymentAutoCancelController extends BaseController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PaymentAutoCancelProperties paymentAutoCancelProperties;

    @Operation(summary = "获取支付超时自动取消配置", description = "主配置字段为 timeoutSeconds；timeoutMinutes 为展示兼容字段；serverTime 格式为 yyyy-MM-dd HH:mm:ss")
    @GetMapping("/config")
    public Result<Object> getConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", paymentAutoCancelProperties.isEnabled());
        data.put("timeoutSeconds", paymentAutoCancelProperties.getTimeoutSeconds());
        data.put("timeoutMinutes", paymentAutoCancelProperties.getTimeoutMinutesForDisplay());
        data.put("serverTime", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return success(data);
    }
}
