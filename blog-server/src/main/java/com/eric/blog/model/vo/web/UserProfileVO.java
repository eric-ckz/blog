package com.eric.blog.model.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 脱敏后的访客用户资料。 */
@Data
@AllArgsConstructor
public class UserProfileVO {
    private String id;
    private String email;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String bio;
}
