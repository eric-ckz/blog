package com.eric.blog.service;

import com.eric.blog.model.dto.auth.LoginRequest;
import com.eric.blog.model.dto.auth.PasswordChangeRequest;
import com.eric.blog.model.vo.admin.AdminProfileVO;
import com.eric.blog.model.vo.admin.AuthVO;
import jakarta.servlet.http.HttpServletResponse;

/** 管理员认证与会话服务。 */
public interface AuthService {
    AuthVO login(LoginRequest request, HttpServletResponse response);
    AuthVO refresh(String refreshToken, HttpServletResponse response);
    void logout(String refreshToken, HttpServletResponse response);
    AdminProfileVO currentProfile();
    void changePassword(PasswordChangeRequest request, HttpServletResponse response);
}
