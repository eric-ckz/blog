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

    /** 获取当前访客身份；未登录或身份类型不符时抛出未登录异常。 */
    public static UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new BaseException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return principal;
    }

    /** 判断当前请求是否已通过访客身份认证。 */
    public static boolean isUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof UserPrincipal;
    }
}
