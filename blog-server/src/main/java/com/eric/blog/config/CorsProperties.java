package com.eric.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** 允许调用管理 API 的浏览器来源。 */
@Data
@ConfigurationProperties(prefix = "blog.cors")
public class CorsProperties {
    private List<String> allowedOrigins = new ArrayList<>();
}
