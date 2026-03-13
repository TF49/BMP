package com.badminton.bmp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地上传文件静态资源映射
 *
 * 通过 /api/uploads/** 访问 upload-dir 下的文件，便于前端在开发环境走 /api 代理加载图片。
 */
@Configuration
public class WebMvcUploadConfig implements WebMvcConfigurer {

    @Value("${bmp.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(location);
    }
}

