-- 访客用户与文章评论表结构（MySQL 8）。
-- 与 admin_user 完全独立：访客通过邮箱注册并激活后登录，管理员仍是单独体系。
-- 所有业务 ID 由 MyBatis-Plus 雪花算法生成，不依赖数据库自增列。

CREATE TABLE blog_user (
    id BIGINT NOT NULL COMMENT '用户ID（雪花算法）',
    email VARCHAR(255) NOT NULL COMMENT '登录邮箱，唯一',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Argon2id 密码哈希',
    username VARCHAR(64) NOT NULL COMMENT '用户昵称，唯一',
    display_name VARCHAR(100) NULL COMMENT '展示名称，默认与昵称一致',
    avatar_url VARCHAR(500) NULL COMMENT '头像公开地址',
    bio VARCHAR(500) NULL COMMENT '个人简介',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否启用：1启用，0禁用',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT '邮箱是否已验证：1已验证，0未验证',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_blog_user_email UNIQUE (email),
    CONSTRAINT uk_blog_user_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='访客用户表';

CREATE TABLE email_verification_code (
    id BIGINT NOT NULL COMMENT '验证码记录ID（雪花算法）',
    email VARCHAR(255) NOT NULL COMMENT '接收验证码的邮箱',
    code_hash VARCHAR(64) NOT NULL COMMENT '6位验证码的 SHA-256 哈希',
    purpose VARCHAR(20) NOT NULL COMMENT '用途：REGISTER注册激活',
    expires_at TIMESTAMP NOT NULL COMMENT '验证码过期时间',
    used_at TIMESTAMP NULL COMMENT '使用时间，为空表示未使用',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT uk_email_code_hash UNIQUE (code_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮箱验证码表';
CREATE INDEX idx_email_code_email ON email_verification_code(email, create_time);

CREATE TABLE comment (
    id BIGINT NOT NULL COMMENT '评论ID（雪花算法）',
    article_id BIGINT NOT NULL COMMENT '所属文章ID',
    user_id BIGINT NOT NULL COMMENT '发表评论的用户ID',
    content TEXT NOT NULL COMMENT '评论正文（纯文本，服务端转义）',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章评论表';
CREATE INDEX idx_comment_article_time ON comment(article_id, create_time);
CREATE INDEX idx_comment_user ON comment(user_id);

-- 访客 Refresh Token 会话记录，与管理员 refresh_token 表分离，避免角色混淆。
CREATE TABLE user_refresh_token (
    id BIGINT NOT NULL COMMENT '刷新令牌记录ID（雪花算法）',
    user_id BIGINT NOT NULL COMMENT '所属访客用户ID',
    token_id VARCHAR(64) NOT NULL COMMENT 'JWT 唯一标识 jti',
    token_hash VARCHAR(64) NOT NULL COMMENT '刷新令牌 SHA-256 哈希',
    expires_at TIMESTAMP NOT NULL COMMENT '令牌过期时间',
    revoked_at TIMESTAMP NULL COMMENT '令牌撤销时间，为空表示未撤销',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT uk_user_refresh_token_hash UNIQUE (token_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='访客刷新令牌表';
CREATE INDEX idx_user_refresh_token_user ON user_refresh_token(user_id);
CREATE INDEX idx_user_refresh_token_expiry ON user_refresh_token(expires_at);
