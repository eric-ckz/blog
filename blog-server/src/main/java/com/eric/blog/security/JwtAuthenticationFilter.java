package com.eric.blog.security;

import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.AdminUserMapper;
import com.eric.blog.model.entity.AdminUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Access Token 认证过滤器。错误 Token 保持匿名身份，由 SecurityConfig 统一输出 401。 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AdminUserMapper adminUserMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            try {
                JwtTokenService.TokenClaims claims = jwtTokenService.parseAccessToken(authorization.substring(7));
                AdminUser admin = adminUserMapper.selectById(claims.userId());
                if (admin != null && Boolean.TRUE.equals(admin.getEnabled())) {
                    AdminPrincipal principal = new AdminPrincipal(admin.getId(), admin.getUsername(), admin.getDisplayName());
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(principal, null, List.of()));
                }
            } catch (BaseException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
