package com.eric.blog.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 登录或刷新后的管理端身份数据。Refresh Token 仅通过 HttpOnly Cookie 返回。 */
@Data
@AllArgsConstructor
public class AuthVO {
    private String accessToken;
    private long expiresIn;
    private AdminProfileVO user;
}
