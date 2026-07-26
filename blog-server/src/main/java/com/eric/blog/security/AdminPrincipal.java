package com.eric.blog.security;

/** Spring Security 上下文中保存的最小管理员身份。 */
public record AdminPrincipal(Long id, String username, String displayName) {
}
