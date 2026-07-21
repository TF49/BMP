package com.badminton.bmp.modules.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

/**
 * Agent 网关与 Tool 集成配置。
 *
 * <p>FastAPI 地址、超时、服务凭证与上下文签名密钥全部通过外部配置注入，
 * 严禁在代码中写死主机名或密钥。密钥类字段生产环境必须通过环境变量提供。</p>
 */
@Component
@ConfigurationProperties(prefix = "bmp.agent")
@Validated
public class AgentProperties {

    /** FastAPI Agent 服务基础地址，例如 http://127.0.0.1:8000。 */
    @NotBlank
    private String fastapiBaseUrl = "http://127.0.0.1:8000";

    /** 调用 FastAPI 的连接超时（毫秒）。 */
    @Min(100)
    @Max(60000)
    private int connectTimeoutMs = 2000;

    /** 调用 FastAPI 的读取超时（毫秒）。 */
    @Min(100)
    @Max(120000)
    private int readTimeoutMs = 30000;

    /**
     * 服务间静态凭证：Python Agent 调用 Java Tool 时必须携带，用于证明调用方身份。
     * 通过 {@code X-Agent-Service-Token} 头传递。
     */
    @NotBlank
    private String serviceToken;

    /**
     * 短期用户上下文 HMAC-SHA256 签名密钥。Java 网关签发、Java Tool 与 Python 校验共用同一密钥。
     * 生产环境必须通过环境变量注入，禁止使用占位值。
     */
    @NotBlank
    private String contextSecret;

    /** 短期上下文有效期（秒），必须在 5 分钟以内。 */
    @Min(1)
    @Max(300)
    private long contextTtlSeconds = 120;

    public String getFastapiBaseUrl() {
        return fastapiBaseUrl;
    }

    public void setFastapiBaseUrl(String fastapiBaseUrl) {
        this.fastapiBaseUrl = fastapiBaseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getContextSecret() {
        return contextSecret;
    }

    public void setContextSecret(String contextSecret) {
        this.contextSecret = contextSecret;
    }

    public long getContextTtlSeconds() {
        return contextTtlSeconds;
    }

    public void setContextTtlSeconds(long contextTtlSeconds) {
        this.contextTtlSeconds = contextTtlSeconds;
    }
}
