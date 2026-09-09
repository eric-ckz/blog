package com.eric.blog.model.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 访客登录或刷新后的身份数据。Refresh Token 仅通过 HttpOnly Cookie 返回。 */
@Data
@AllArgsConstructor
public class UserAuthVO {
    private String accessToken;
    private long expiresIn;
    private UserProfileVO user;
}
