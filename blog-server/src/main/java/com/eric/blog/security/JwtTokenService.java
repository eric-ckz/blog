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
    private final JwtProperties properties;
    private byte[] secret;

    @PostConstruct
    void validateConfiguration() {
        secret = properties.getSecret() == null ? new byte[0] : properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("BLOG_JWT_SECRET 必须至少包含 32 个 UTF-8 字节");
        }
    }

    /** 创建短期 Access Token。 */
    public String createAccessToken(Long userId, String username) {
        return createToken(userId, username, ACCESS_TYPE, properties.getAccessTokenTtl().toSeconds());
    }

    /** 创建可轮换的 Refresh Token。 */
    public String createRefreshToken(Long userId, String username) {
        return createToken(userId, username, REFRESH_TYPE, properties.getRefreshTokenTtl().toSeconds());
    }

    /** 校验并解析 Access Token。 */
    public TokenClaims parseAccessToken(String token) {
        return parse(token, ACCESS_TYPE);
    }

    /** 校验并解析 Refresh Token。 */
    public TokenClaims parseRefreshToken(String token) {
        return parse(token, REFRESH_TYPE);
    }

    private String createToken(Long userId, String username, String type, long ttlSeconds) {
        try {
            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(properties.getIssuer()).subject(String.valueOf(userId)).jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .claim("username", username).claim("type", type).build();
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (Exception exception) {
            throw new BaseException(ErrorCode.SYSTEM_ERROR, "Token 签发失败");
        }
    }

    private TokenClaims parse(String token, String expectedType) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            boolean valid = jwt.verify(new MACVerifier(secret))
                    && properties.getIssuer().equals(claims.getIssuer())
                    && claims.getExpirationTime() != null && claims.getExpirationTime().after(new Date())
                    && expectedType.equals(claims.getStringClaim("type"));
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
