package com.eric.blog.security;

/** Spring Security 上下文中保存的最小访客身份。 */
public record UserPrincipal(Long id, String username, String email) {
}
