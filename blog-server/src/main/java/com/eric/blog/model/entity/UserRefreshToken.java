package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 访客 Refresh Token 会话记录，数据库只保存不可逆哈希。 */
@Data
@TableName("user_refresh_token")
public class UserRefreshToken {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** Token 所属访客用户。 */
    private Long userId;

    /** JWT jti。 */
    private String tokenId;

    /** 完整 Refresh Token 的 SHA-256 哈希。 */
    private String tokenHash;

    /** Token 到期时间。 */
    private LocalDateTime expiresAt;

    /** 撤销时间；非空表示已失效。 */
    private LocalDateTime revokedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
