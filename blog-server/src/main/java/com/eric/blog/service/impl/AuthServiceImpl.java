package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.config.JwtProperties;
import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.AdminUserMapper;
import com.eric.blog.mapper.RefreshTokenMapper;
import com.eric.blog.model.dto.auth.LoginRequest;
import com.eric.blog.model.dto.auth.PasswordChangeRequest;
import com.eric.blog.model.entity.AdminUser;
import com.eric.blog.model.entity.RefreshToken;
import com.eric.blog.model.vo.admin.AdminProfileVO;
import com.eric.blog.model.vo.admin.AuthVO;
import com.eric.blog.security.JwtTokenService;
import com.eric.blog.security.SecurityContextUtils;
import com.eric.blog.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

/** JWT 双令牌认证实现。 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminUserMapper adminUserMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public AuthVO login(LoginRequest request, HttpServletResponse response) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername()));
        if (admin == null || !Boolean.TRUE.equals(admin.getEnabled())
                || !passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            // 账号不存在和密码错误使用相同提示，避免攻击者枚举有效账号。
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR, "账号或密码错误");
        }
        return issueSession(admin, response);
    }

    @Override
    @Transactional
    public AuthVO refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        JwtTokenService.TokenClaims claims = jwtTokenService.parseRefreshToken(refreshToken);
        RefreshToken stored = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getTokenHash, hash(refreshToken))
                .eq(RefreshToken::getTokenId, claims.tokenId()));
        if (stored == null || stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        AdminUser admin = adminUserMapper.selectById(claims.userId());
        if (admin == null || !Boolean.TRUE.equals(admin.getEnabled())) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // Refresh Token 单次使用：先撤销旧记录，再签发并持久化新记录。
        stored.setRevokedAt(LocalDateTime.now());
        refreshTokenMapper.updateById(stored);
        return issueSession(admin, response);
    }

    @Override
    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenMapper.update(null, new LambdaUpdateWrapper<RefreshToken>()
                    .eq(RefreshToken::getTokenHash, hash(refreshToken))
                    .isNull(RefreshToken::getRevokedAt)
                    .set(RefreshToken::getRevokedAt, LocalDateTime.now()));
        }
        clearRefreshCookie(response);
    }

    @Override
    public AdminProfileVO currentProfile() {
        var principal = SecurityContextUtils.currentAdmin();
        return new AdminProfileVO(String.valueOf(principal.id()), principal.username(), principal.displayName());
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request, HttpServletResponse response) {
        var principal = SecurityContextUtils.currentAdmin();
        AdminUser admin = adminUserMapper.selectById(principal.id());
        if (admin == null || !passwordEncoder.matches(request.getCurrentPassword(), admin.getPasswordHash())) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "当前密码不正确");
        }
        if (passwordEncoder.matches(request.getNewPassword(), admin.getPasswordHash())) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "新密码不能与当前密码相同");
        }
        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        adminUserMapper.updateById(admin);
        revokeAll(principal.id());
        clearRefreshCookie(response);
    }

    private AuthVO issueSession(AdminUser admin, HttpServletResponse response) {
        String accessToken = jwtTokenService.createAccessToken(admin.getId(), admin.getUsername());
        String refreshToken = jwtTokenService.createRefreshToken(admin.getId(), admin.getUsername());
        JwtTokenService.TokenClaims claims = jwtTokenService.parseRefreshToken(refreshToken);
        RefreshToken record = new RefreshToken();
        record.setAdminUserId(admin.getId());
        record.setTokenId(claims.tokenId());
        record.setTokenHash(hash(refreshToken));
        record.setExpiresAt(LocalDateTime.ofInstant(claims.expiresAt(), ZoneId.systemDefault()));
        refreshTokenMapper.insert(record);
        writeRefreshCookie(response, refreshToken);
        return new AuthVO(accessToken, jwtProperties.getAccessTokenTtl().toSeconds(),
                new AdminProfileVO(String.valueOf(admin.getId()), admin.getUsername(), admin.getDisplayName()));
    }

    private void revokeAll(Long adminId) {
        refreshTokenMapper.update(null, new LambdaUpdateWrapper<RefreshToken>()
                .eq(RefreshToken::getAdminUserId, adminId)
                .isNull(RefreshToken::getRevokedAt)
                .set(RefreshToken::getRevokedAt, LocalDateTime.now()));
    }

    private void writeRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefreshCookieName(), token)
                .httpOnly(true).secure(jwtProperties.isSecureCookie()).sameSite("Strict")
                .path("/api/admin/auth").maxAge(jwtProperties.getRefreshTokenTtl()).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefreshCookieName(), "")
                .httpOnly(true).secure(jwtProperties.isSecureCookie()).sameSite("Strict")
                .path("/api/admin/auth").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new BaseException(ErrorCode.SYSTEM_ERROR, "Token 摘要计算失败");
        }
    }
}
