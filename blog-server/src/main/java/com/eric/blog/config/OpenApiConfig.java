package com.eric.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OpenAPI 文档配置，开发环境可通过 /doc.html 查看。 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blogOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Charles Blog API")
                .description("博客用户端与管理端统一接口")
                .version("1.0.0"));
    }
}
