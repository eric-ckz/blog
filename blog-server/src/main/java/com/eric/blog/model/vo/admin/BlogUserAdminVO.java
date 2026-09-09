package com.eric.blog.model.vo.admin;

import lombok.Builder;
import lombok.Data;

/** 管理端访客用户模型，绝不包含密码摘要。 */
@Data
@Builder
public class BlogUserAdminVO {
    private String id;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean enabled;
    private Boolean emailVerified;
    private String lastLoginAt;
    private String createTime;
}
