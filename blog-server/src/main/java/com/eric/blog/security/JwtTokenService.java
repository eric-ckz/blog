package com.eric.blog.security;

import com.eric.blog.common.ErrorCode;
import com.eric.blog.config.JwtProperties;
import com.eric.blog.exception.BaseException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 签发与校验服务。Access/Refresh 使用相同签名密钥，但通过 type 声明严格区分用途，
 * 防止把 Refresh Token 错当成接口访问凭证。
 */
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_USER = "user";
    private final JwtProperties properties;
    private byte[] secret;

    @PostConstruct
    void validateConfiguration() {
        secret = properties.getSecret() == null ? new byte[0] : properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("BLOG_JWT_SECRET 必须至少包含 32 个 UTF-8 字节");
        }
    }

    /** 创建管理员短期 Access Token。 */
    public String createAccessToken(Long userId, String username) {
        return createToken(userId, username, ACCESS_TYPE, ROLE_ADMIN, properties.getAccessTokenTtl().toSeconds());
    }

    /** 创建管理员可轮换的 Refresh Token。 */
    public String createRefreshToken(Long userId, String username) {
        return createToken(userId, username, REFRESH_TYPE, ROLE_ADMIN, properties.getRefreshTokenTtl().toSeconds());
    }

    /** 创建访客短期 Access Token。 */
    public String createUserAccessToken(Long userId, String username) {
        return createToken(userId, username, ACCESS_TYPE, ROLE_USER, properties.getAccessTokenTtl().toSeconds());
    }

    /** 创建访客可轮换的 Refresh Token。 */
    public String createUserRefreshToken(Long userId, String username) {
        return createToken(userId, username, REFRESH_TYPE, ROLE_USER, properties.getRefreshTokenTtl().toSeconds());
    }

    /** 校验并解析管理员 Access Token。 */
    public TokenClaims parseAccessToken(String token) {
        return parse(token, ACCESS_TYPE, ROLE_ADMIN);
    }

    /** 校验并解析管理员 Refresh Token。 */
    public TokenClaims parseRefreshToken(String token) {
        return parse(token, REFRESH_TYPE, ROLE_ADMIN);
    }

    /** 校验并解析访客 Access Token。 */
    public TokenClaims parseUserAccessToken(String token) {
        return parse(token, ACCESS_TYPE, ROLE_USER);
    }

    /** 校验并解析访客 Refresh Token。 */
    public TokenClaims parseUserRefreshToken(String token) {
        return parse(token, REFRESH_TYPE, ROLE_USER);
    }

    private String createToken(Long userId, String username, String type, String role, long ttlSeconds) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(properties.getIssuer()).subject(String.valueOf(userId)).jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .claim("username", username).claim("type", type).claim("role", role).build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (Exception exception) {
            throw new BaseException(ErrorCode.SYSTEM_ERROR, "Token 签发失败");
        }
    }

    private TokenClaims parse(String token, String expectedType, String expectedRole) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            boolean valid = jwt.verify(new MACVerifier(secret))
                    && properties.getIssuer().equals(claims.getIssuer())
                    && claims.getExpirationTime() != null && claims.getExpirationTime().after(new Date())
                    && expectedType.equals(claims.getStringClaim("type"))
                    && expectedRole.equals(claims.getStringClaim("role"));
            if (!valid) {
                throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
            }
            return new TokenClaims(Long.parseLong(claims.getSubject()), claims.getStringClaim("username"),
                    claims.getJWTID(), claims.getExpirationTime().toInstant());
        } catch (BaseException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
    }

    /** 业务层使用的最小 Token 声明。 */
    public record TokenClaims(Long userId, String username, String tokenId, Instant expiresAt) {
    }
}
