package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.config.JwtProperties;
import com.eric.blog.model.dto.auth.LoginRequest;
import com.eric.blog.model.vo.admin.AdminProfileVO;
import com.eric.blog.model.vo.admin.AuthVO;
import com.eric.blog.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/** 管理端登录、刷新和注销接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login")
    public BaseResponse<AuthVO> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return BaseResponse.success(authService.login(request, response));
    }

    @PostMapping("/refresh")
    public BaseResponse<AuthVO> refresh(HttpServletRequest request, HttpServletResponse response) {
        return BaseResponse.success(authService.refresh(readRefreshCookie(request), response));
    }

    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(readRefreshCookie(request), response);
        return BaseResponse.success();
    }

    @GetMapping("/me")
    public BaseResponse<AdminProfileVO> me() {
        return BaseResponse.success(authService.currentProfile());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> jwtProperties.getRefreshCookieName().equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }
}
