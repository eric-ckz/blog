package com.eric.blog.service;

import com.eric.blog.model.dto.auth.RegisterRequest;
import com.eric.blog.model.dto.auth.UpdateProfileRequest;
import com.eric.blog.model.dto.auth.UserLoginRequest;
import com.eric.blog.model.dto.auth.VerifyEmailRequest;
import com.eric.blog.model.vo.web.UserAuthVO;
import com.eric.blog.model.vo.web.UserProfileVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

/** 访客注册、登录与会话服务。 */
public interface UserAuthService {
    void register(RegisterRequest request);
    void verifyEmail(VerifyEmailRequest request);
    UserAuthVO login(UserLoginRequest request, HttpServletResponse response);
    UserAuthVO refresh(String refreshToken, HttpServletResponse response);
    void logout(String refreshToken, HttpServletResponse response);
    UserProfileVO currentProfile();
    UserProfileVO updateProfile(UpdateProfileRequest request);
    UserProfileVO uploadAvatar(MultipartFile file);
}
