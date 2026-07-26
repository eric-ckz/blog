package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 管理员账号。第一版只创建一个管理员，但数据结构不依赖硬编码用户名。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_user")
public class AdminUser extends BaseEntity {

    /** 登录账号，数据库中唯一。 */
    private String username;

    /** Argon2id 密码摘要，绝不向任何 VO 暴露。 */
    private String passwordHash;

    /** 管理端导航栏显示名称。 */
    private String displayName;

    /** 是否允许登录。 */
    private Boolean enabled;
}
