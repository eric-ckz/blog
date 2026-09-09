package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 访客用户账号，与管理员账号体系完全独立。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_user")
public class BlogUser extends BaseEntity {

    /** 登录邮箱，数据库中唯一。 */
    private String email;

    /** Argon2id 密码摘要，绝不向任何 VO 暴露。 */
    private String passwordHash;

    /** 用户昵称，数据库中唯一。 */
    private String username;

    /** 展示名称，默认与昵称一致。 */
    private String displayName;

    /** 头像公开地址。 */
    private String avatarUrl;

    /** 个人简介。 */
    private String bio;

    /** 是否允许登录。 */
    private Boolean enabled;

    /** 邮箱是否已验证。 */
    private Boolean emailVerified;

    /** 最后登录时间。 */
    private LocalDateTime lastLoginAt;
}
