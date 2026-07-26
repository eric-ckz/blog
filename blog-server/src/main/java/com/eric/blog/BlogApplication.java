package com.eric.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * 博客 API 服务入口。
 *
 * <p>配置属性扫描用于加载 JWT、跨域和文件存储等类型安全配置，避免在业务代码中
 * 到处读取字符串形式的环境变量。</p>
 */
// 项目使用自定义 JWT 过滤器，不需要 Spring Security 创建内存用户或打印随机开发密码。
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
