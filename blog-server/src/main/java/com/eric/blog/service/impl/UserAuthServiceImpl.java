package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.config.JwtProperties;
import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.BlogUserMapper;
import com.eric.blog.mapper.EmailVerificationCodeMapper;
import com.eric.blog.mapper.UserRefreshTokenMapper;
import com.eric.blog.model.dto.auth.RegisterRequest;
import com.eric.blog.model.dto.auth.UpdateProfileRequest;
import com.eric.blog.model.dto.auth.UserLoginRequest;
import com.eric.blog.model.dto.auth.VerifyEmailRequest;
import com.eric.blog.model.entity.BlogUser;
import com.eric.blog.model.entity.EmailVerificationCode;
import com.eric.blog.model.entity.UserRefreshToken;
import com.eric.blog.model.vo.web.UserAuthVO;
import com.eric.blog.model.vo.web.UserProfileVO;
import com.eric.blog.security.JwtTokenService;
import com.eric.blog.security.SecurityContextUtils;
import com.eric.blog.service.MailService;
import com.eric.blog.service.UserAuthService;
import com.eric.blog.storage.StorageObject;
import com.eric.blog.storage.StorageService;
import com.eric.blog.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;

/** 访客注册、登录与会话实现。 */
@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private static final String PURPOSE_REGISTER = "REGISTER";
    private static final long CODE_TTL_MINUTES = 10;
    private static final long CODE_RESEND_SECONDS = 60;

    private final BlogUserMapper blogUserMapper;
    private final EmailVerificationCodeMapper emailCodeMapper;
    private final UserRefreshTokenMapper userRefreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final MailService mailService;
    private final StorageService storageService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();
        ThrowUtils.throwIf(blogUserMapper.selectCount(new LambdaQueryWrapper<BlogUser>()
                .eq(BlogUser::getEmail, email)) > 0, ErrorCode.CONFLICT_ERROR, "该邮箱已注册");
        ThrowUtils.throwIf(blogUserMapper.selectCount(new LambdaQueryWrapper<BlogUser>()
                .eq(BlogUser::getUsername, username)) > 0, ErrorCode.CONFLICT_ERROR, "该昵称已被使用");

        BlogUser user = new BlogUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(username);
        user.setDisplayName(username);
        user.setEnabled(true);
        user.setEmailVerified(false);
        blogUserMapper.insert(user);

        sendCode(email);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        BlogUser user = blogUserMapper.selectOne(new LambdaQueryWrapper<BlogUser>()
                .eq(BlogUser::getEmail, email));
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "该邮箱尚未注册");
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }
        String codeHash = hash(request.getCode());
        EmailVerificationCode record = emailCodeMapper.selectOne(new LambdaQueryWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getCodeHash, codeHash)
                .eq(EmailVerificationCode::getPurpose, PURPOSE_REGISTER)
                .isNull(EmailVerificationCode::getUsedAt)
                .orderByDesc(EmailVerificationCode::getCreateTime)
                .last("LIMIT 1"));
        ThrowUtils.throwIf(record == null || record.getExpiresAt().isBefore(LocalDateTime.now()),
                ErrorCode.PARAMS_ERROR, "验证码错误或已过期");
        record.setUsedAt(LocalDateTime.now());
        emailCodeMapper.updateById(record);
        user.setEmailVerified(true);
        blogUserMapper.updateById(user);
    }

    @Override
    @Transactional
    public UserAuthVO login(UserLoginRequest request, HttpServletResponse response) {
        String email = request.getEmail().trim().toLowerCase();
        BlogUser user = blogUserMapper.selectOne(new LambdaQueryWrapper<BlogUser>()
                .eq(BlogUser::getEmail, email));
        if (user == null || !Boolean.TRUE.equals(user.getEnabled())
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR, "邮箱或密码错误");
        }
        ThrowUtils.throwIf(!Boolean.TRUE.equals(user.getEmailVerified()),
                ErrorCode.NOT_LOGIN_ERROR, "邮箱尚未验证，请先完成验证");
        user.setLastLoginAt(LocalDateTime.now());
        blogUserMapper.updateById(user);
        return issueSession(user, response);
    }

    @Override
    @Transactional
    public UserAuthVO refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        JwtTokenService.TokenClaims claims = jwtTokenService.parseUserRefreshToken(refreshToken);
        UserRefreshToken stored = userRefreshTokenMapper.selectOne(new LambdaQueryWrapper<UserRefreshToken>()
                .eq(UserRefreshToken::getTokenHash, hash(refreshToken))
                .eq(UserRefreshToken::getTokenId, claims.tokenId()));
        if (stored == null || stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        BlogUser user = blogUserMapper.selectById(claims.userId());
        if (user == null || !Boolean.TRUE.equals(user.getEnabled()) || !Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        stored.setRevokedAt(LocalDateTime.now());
        userRefreshTokenMapper.updateById(stored);
        return issueSession(user, response);
    }

    @Override
    @Transactional
    public void logout(String refreshToken, HttpServletResponse response) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            userRefreshTokenMapper.update(null, new LambdaUpdateWrapper<UserRefreshToken>()
                    .eq(UserRefreshToken::getTokenHash, hash(refreshToken))
                    .isNull(UserRefreshToken::getRevokedAt)
                    .set(UserRefreshToken::getRevokedAt, LocalDateTime.now()));
        }
        clearRefreshCookie(response);
    }

    @Override
    public UserProfileVO currentProfile() {
        var principal = SecurityContextUtils.currentUser();
        BlogUser user = blogUserMapper.selectById(principal.id());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        return toProfile(user);
    }

    @Override
    @Transactional
    public UserProfileVO updateProfile(UpdateProfileRequest request) {
        var principal = SecurityContextUtils.currentUser();
        BlogUser user = blogUserMapper.selectById(principal.id());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        if (StringUtils.isNotBlank(request.getUsername()) && !request.getUsername().equals(user.getUsername())) {
            ThrowUtils.throwIf(blogUserMapper.selectCount(new LambdaQueryWrapper<BlogUser>()
                    .eq(BlogUser::getUsername, request.getUsername().trim())) > 0,
                    ErrorCode.CONFLICT_ERROR, "该昵称已被使用");
            user.setUsername(request.getUsername().trim());
        }
        if (request.getDisplayName() != null) {
            user.setDisplayName(StringUtils.trimToNull(request.getDisplayName()));
        }
        if (request.getBio() != null) {
            user.setBio(StringUtils.trimToNull(request.getBio()));
        }
        blogUserMapper.updateById(user);
        return toProfile(user);
    }

    @Override
    @Transactional
    public UserProfileVO uploadAvatar(MultipartFile file) {
        var principal = SecurityContextUtils.currentUser();
        BlogUser user = blogUserMapper.selectById(principal.id());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
        StorageObject stored = storageService.store(file);
        user.setAvatarUrl(stored.publicUrl());
        blogUserMapper.updateById(user);
        return toProfile(user);
    }

    private void sendCode(String email) {
        EmailVerificationCode latest = emailCodeMapper.selectOne(new LambdaQueryWrapper<EmailVerificationCode>()
                .eq(EmailVerificationCode::getEmail, email)
                .eq(EmailVerificationCode::getPurpose, PURPOSE_REGISTER)
                .orderByDesc(EmailVerificationCode::getCreateTime)
                .last("LIMIT 1"));
        if (latest != null && latest.getCreateTime().plusSeconds(CODE_RESEND_SECONDS).isAfter(LocalDateTime.now())) {
            throw new BaseException(ErrorCode.CONFLICT_ERROR, "验证码发送过于频繁，请稍后再试");
        }
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        EmailVerificationCode record = new EmailVerificationCode();
        record.setEmail(email);
        record.setCodeHash(hash(code));
        record.setPurpose(PURPOSE_REGISTER);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_TTL_MINUTES));
        emailCodeMapper.insert(record);
        mailService.sendVerificationCode(email, code);
    }

    private UserAuthVO issueSession(BlogUser user, HttpServletResponse response) {
        String accessToken = jwtTokenService.createUserAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenService.createUserRefreshToken(user.getId(), user.getUsername());
        JwtTokenService.TokenClaims claims = jwtTokenService.parseUserRefreshToken(refreshToken);
        UserRefreshToken record = new UserRefreshToken();
        record.setUserId(user.getId());
        record.setTokenId(claims.tokenId());
        record.setTokenHash(hash(refreshToken));
        record.setExpiresAt(LocalDateTime.ofInstant(claims.expiresAt(), ZoneId.systemDefault()));
        userRefreshTokenMapper.insert(record);
        writeRefreshCookie(response, refreshToken);
        return new UserAuthVO(accessToken, jwtProperties.getAccessTokenTtl().toSeconds(), toProfile(user));
    }

    private UserProfileVO toProfile(BlogUser user) {
        return new UserProfileVO(String.valueOf(user.getId()), user.getEmail(), user.getUsername(),
                user.getDisplayName() == null ? user.getUsername() : user.getDisplayName(),
                user.getAvatarUrl(), user.getBio());
    }

    private void writeRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getUserRefreshCookieName(), token)
                .httpOnly(true).secure(jwtProperties.isSecureCookie()).sameSite("Strict")
                .path("/api/web/auth").maxAge(jwtProperties.getRefreshTokenTtl()).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getUserRefreshCookieName(), "")
                .httpOnly(true).secure(jwtProperties.isSecureCookie()).sameSite("Strict")
                .path("/api/web/auth").maxAge(0).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new BaseException(ErrorCode.SYSTEM_ERROR, "摘要计算失败");
        }
    }
}
