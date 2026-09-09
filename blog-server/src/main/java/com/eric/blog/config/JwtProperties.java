package com.eric.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** JWT 与 Refresh Cookie 的集中配置。 */
@Data
@ConfigurationProperties(prefix = "blog.jwt")
public class JwtProperties {
    private String secret;
    private String issuer;
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
    private String refreshCookieName;
    private String userRefreshCookieName;
    private boolean secureCookie;
}
