package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.config.JwtProperties;
import com.eric.blog.model.dto.auth.RegisterRequest;
import com.eric.blog.model.dto.auth.UpdateProfileRequest;
import com.eric.blog.model.dto.auth.UserLoginRequest;
import com.eric.blog.model.dto.auth.VerifyEmailRequest;
import com.eric.blog.model.vo.web.UserAuthVO;
import com.eric.blog.model.vo.web.UserProfileVO;
import com.eric.blog.service.UserAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/** 访客注册、登录、刷新和注销接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web/auth")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public BaseResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userAuthService.register(request);
        return BaseResponse.success();
    }

    @PostMapping("/verify-email")
    public BaseResponse<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        userAuthService.verifyEmail(request);
        return BaseResponse.success();
    }

    @PostMapping("/login")
    public BaseResponse<UserAuthVO> login(@Valid @RequestBody UserLoginRequest request, HttpServletResponse response) {
        return BaseResponse.success(userAuthService.login(request, response));
    }

    @PostMapping("/refresh")
    public BaseResponse<UserAuthVO> refresh(HttpServletRequest request, HttpServletResponse response) {
        return BaseResponse.success(userAuthService.refresh(readRefreshCookie(request), response));
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        userAuthService.logout(readRefreshCookie(request), response);
        return BaseResponse.success();
    }

    @GetMapping("/me")
    public BaseResponse<UserProfileVO> me() {
        return BaseResponse.success(userAuthService.currentProfile());
    }

    @PutMapping("/profile")
    public BaseResponse<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return BaseResponse.success(userAuthService.updateProfile(request));
    }

    @PostMapping("/avatar")
    public BaseResponse<UserProfileVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return BaseResponse.success(userAuthService.uploadAvatar(file));
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> jwtProperties.getUserRefreshCookieName().equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }
}
