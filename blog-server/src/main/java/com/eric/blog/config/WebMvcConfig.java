package com.eric.blog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Web MVC 静态资源配置。上传目录只映射为只读 HTTP 资源。 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 静态资源映射与上传服务必须使用同一个解析结果，否则可能出现“上传成功但访问 404”。
        String location = storageProperties.resolveRoot().toUri().toString();
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
