package com.badminton.bmp.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger 配置：文档信息与 JWT 认证方案，便于 Swagger UI 中填写 Token 调试需认证接口。
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_BEARER = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("羽毛球管理系统（BMP）API")
                        .description("羽毛球管理系统后端接口文档，在线调试请使用 Swagger UI。认证方式：登录后获取 AccessToken，在 Swagger UI 顶部点击 Authorize 填写 Bearer {token}。")
                        .version("1.0.0")
                        .contact(new Contact().name("BMP").url("http://www.badminton.vip")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("登录接口返回的 AccessToken，请求头格式：Authorization: Bearer {token}")))
                .security(List.of(new SecurityRequirement().addList(SECURITY_SCHEME_BEARER)));
    }
}
