package com.eric.blog.security;

import com.eric.blog.common.ErrorCode;
import com.eric.blog.exception.BaseException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** 获取当前管理员身份的安全工具，避免 Controller 接触认证实现细节。 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
    }

    public static AdminPrincipal currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return principal;
    }
}
