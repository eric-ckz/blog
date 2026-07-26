-- Charles Blog 初始表结构（MySQL 8）。
-- 所有业务 ID 由 MyBatis-Plus 雪花算法生成，因此不依赖数据库自增列。
-- 每张表和每个字段均声明中文 COMMENT，便于在 IDEA、DataGrip 等工具中直接理解数据模型。

CREATE TABLE admin_user (
    id BIGINT NOT NULL COMMENT '管理员ID（雪花算法）',
    username VARCHAR(64) NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Argon2id 密码哈希',
    display_name VARCHAR(100) NOT NULL COMMENT '管理员显示名称',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否启用：1启用，0禁用',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_admin_user_username UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员账号表';

-- Refresh Token 只保存 SHA-256 哈希；revoked_at 非空即代表会话已失效。
CREATE TABLE refresh_token (
    id BIGINT NOT NULL COMMENT '刷新令牌记录ID（雪花算法）',
    admin_user_id BIGINT NOT NULL COMMENT '所属管理员ID',
    token_id VARCHAR(64) NOT NULL COMMENT 'JWT 唯一标识 jti',
    token_hash VARCHAR(64) NOT NULL COMMENT '刷新令牌 SHA-256 哈希',
    expires_at TIMESTAMP NOT NULL COMMENT '令牌过期时间',
    revoked_at TIMESTAMP NULL COMMENT '令牌撤销时间，为空表示未撤销',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_admin FOREIGN KEY (admin_user_id) REFERENCES admin_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员刷新令牌表';
CREATE INDEX idx_refresh_token_admin ON refresh_token(admin_user_id);
CREATE INDEX idx_refresh_token_expiry ON refresh_token(expires_at);

CREATE TABLE category (
    id BIGINT NOT NULL COMMENT '栏目ID（雪花算法）',
    category_key VARCHAR(50) NOT NULL COMMENT '栏目唯一英文标识',
    label VARCHAR(100) NOT NULL COMMENT '栏目显示名称',
    description VARCHAR(255) NOT NULL COMMENT '栏目简介',
    icon VARCHAR(50) NOT NULL COMMENT '前端图标标识',
    display_count INT NOT NULL DEFAULT 0 COMMENT '首页展示的文章数量',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '显示顺序，数值越小越靠前',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_category_key UNIQUE (category_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章栏目表';

CREATE TABLE media_asset (
    id BIGINT NOT NULL COMMENT '媒体资源ID（雪花算法）',
    original_name VARCHAR(255) NOT NULL COMMENT '上传时的原始文件名',
    storage_path VARCHAR(500) NOT NULL COMMENT '存储服务内部相对路径',
    public_url VARCHAR(500) NOT NULL COMMENT '前端可访问的资源地址',
    mime_type VARCHAR(100) NOT NULL COMMENT '经内容检测后的 MIME 类型',
    size_bytes BIGINT NOT NULL COMMENT '文件大小，单位为字节',
    width INT NOT NULL DEFAULT 0 COMMENT '图片宽度，单位为像素',
    height INT NOT NULL DEFAULT 0 COMMENT '图片高度，单位为像素',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_media_storage_path UNIQUE (storage_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='媒体资源表';

CREATE TABLE article (
    id BIGINT NOT NULL COMMENT '文章ID（雪花算法）',
    title VARCHAR(255) NOT NULL COMMENT '文章标题',
    summary VARCHAR(1000) NOT NULL COMMENT '文章摘要',
    category_id BIGINT NOT NULL COMMENT '所属栏目ID',
    cover_media_id BIGINT NULL COMMENT '关联的封面媒体资源ID',
    cover_url VARCHAR(500) NULL COMMENT '封面公开地址，可保存外部图片地址',
    content_html TEXT NOT NULL COMMENT '经过服务端安全清理的正文 HTML',
    status VARCHAR(20) NOT NULL COMMENT '文章状态：DRAFT草稿，PUBLISHED已发布',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT '文章阅读次数',
    read_minutes INT NOT NULL DEFAULT 1 COMMENT '预计阅读分钟数',
    original_url VARCHAR(1000) NULL COMMENT '原文地址',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT fk_article_category FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT fk_article_cover_media FOREIGN KEY (cover_media_id) REFERENCES media_asset(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='博客文章表';
CREATE INDEX idx_article_public_list ON article(status, published_at);
CREATE INDEX idx_article_category ON article(category_id, published_at);

-- 首页位置单独建表，避免把轮播和精选顺序固化成 article 表中的多个布尔字段。
CREATE TABLE home_article_slot (
    id BIGINT NOT NULL COMMENT '首页文章位ID（雪花算法）',
    slot_type VARCHAR(20) NOT NULL COMMENT '位置类型：HERO轮播，FEATURED精选',
    article_id BIGINT NOT NULL COMMENT '关联文章ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '同类型中的显示顺序',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT fk_home_slot_article FOREIGN KEY (article_id) REFERENCES article(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页文章位置配置表';
CREATE INDEX idx_home_slot_type_order ON home_article_slot(slot_type, sort_order);

CREATE TABLE site_setting (
    id BIGINT NOT NULL COMMENT '站点配置ID（雪花算法）',
    setting_key VARCHAR(100) NOT NULL COMMENT '配置项唯一标识',
    setting_value TEXT NOT NULL COMMENT '配置项值',
    description VARCHAR(255) NOT NULL COMMENT '配置项用途说明',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id),
    CONSTRAINT uk_site_setting_key UNIQUE (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点通用配置表';

CREATE TABLE about_item (
    id BIGINT NOT NULL COMMENT '关于页内容项ID（雪花算法）',
    item_type VARCHAR(20) NOT NULL COMMENT '内容类型：INTRO介绍，LOVE热爱，TIMELINE时间线，CONTACT联系方式',
    title VARCHAR(255) NULL COMMENT '内容项标题',
    description TEXT NOT NULL COMMENT '内容项正文或说明',
    icon VARCHAR(50) NULL COMMENT '前端图标标识',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '同类型中的显示顺序',
    create_time TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time TIMESTAMP NOT NULL COMMENT '最后更新时间',
    is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0正常，1已删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关于页内容配置表';
CREATE INDEX idx_about_item_type_order ON about_item(item_type, sort_order);
