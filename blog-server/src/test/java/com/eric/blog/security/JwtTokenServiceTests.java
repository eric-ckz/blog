package com.eric.blog.security;

import com.eric.blog.config.JwtProperties;
import com.eric.blog.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 验证管理员与访客 Token 通过 role 声明严格隔离，防止跨角色误用。 */
class JwtTokenServiceTests {

    private JwtTokenService service;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-that-is-longer-than-thirty-two-characters");
        properties.setIssuer("eric-blog-test");
        properties.setAccessTokenTtl(Duration.ofMinutes(15));
        properties.setRefreshTokenTtl(Duration.ofDays(7));
        service = new JwtTokenService(properties);
        service.validateConfiguration();
    }

    @Test
    void adminTokenCannotBeParsedAsUserToken() {
        String adminToken = service.createAccessToken(1L, "admin");
        assertThat(service.parseAccessToken(adminToken).userId()).isEqualTo(1L);
        assertThatThrownBy(() -> service.parseUserAccessToken(adminToken))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void userTokenCannotBeParsedAsAdminToken() {
        String userToken = service.createUserAccessToken(2L, "reader");
        assertThat(service.parseUserAccessToken(userToken).userId()).isEqualTo(2L);
        assertThatThrownBy(() -> service.parseAccessToken(userToken))
                .isInstanceOf(BaseException.class);
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        String refreshToken = service.createUserRefreshToken(3L, "reader");
        assertThatThrownBy(() -> service.parseUserAccessToken(refreshToken))
                .isInstanceOf(BaseException.class);
    }
}
