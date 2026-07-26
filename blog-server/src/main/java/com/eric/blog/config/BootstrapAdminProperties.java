package com.eric.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 首次启动创建管理员所需配置，创建成功后不会覆盖数据库中的账号。 */
@Data
@ConfigurationProperties(prefix = "blog.bootstrap-admin")
public class BootstrapAdminProperties {
    private String username;
    private String password;
    private String displayName;
}
