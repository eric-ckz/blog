package com.eric.blog.security;

import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.BlogUserMapper;
import com.eric.blog.model.entity.BlogUser;
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

/** 访客 Access Token 认证过滤器。错误 Token 保持匿名身份，由 SecurityConfig 统一输出 401。 */
@Component
@RequiredArgsConstructor
public class UserJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final BlogUserMapper blogUserMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            try {
                JwtTokenService.TokenClaims claims = jwtTokenService.parseUserAccessToken(authorization.substring(7));
                BlogUser user = blogUserMapper.selectById(claims.userId());
                if (user != null && Boolean.TRUE.equals(user.getEnabled()) && Boolean.TRUE.equals(user.getEmailVerified())) {
                    UserPrincipal principal = new UserPrincipal(user.getId(), user.getUsername(), user.getEmail());
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(principal, null, List.of()));
                }
            } catch (BaseException ignored) {
                // 非访客 Token（如管理员 Token）解析失败时保持上下文不变，避免误清管理员身份。
            }
        }
        filterChain.doFilter(request, response);
    }
}
