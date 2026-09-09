package com.eric.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** SMTP 邮件发送配置。 */
@Data
@ConfigurationProperties(prefix = "blog.mail")
public class MailProperties {
    private String host;
    private int port = 465;
    private String username;
    private String password;
    private String from;
    private boolean enabled = false;
}
