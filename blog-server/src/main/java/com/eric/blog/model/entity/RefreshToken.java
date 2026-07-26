package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Refresh Token 会话记录。数据库只保存不可逆哈希，即使数据库泄露也不能直接用于刷新。
 */
@Data
@TableName("refresh_token")
public class RefreshToken {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** Token 所属管理员。 */
    private Long adminUserId;

    /** JWT jti，用于审计和快速定位一次会话。 */
    private String tokenId;

    /** 完整 Refresh Token 的 SHA-256 哈希。 */
    private String tokenHash;

    /** Token 到期时间。 */
    private LocalDateTime expiresAt;

    /** 撤销时间；非空表示该 Token 已失效。 */
    private LocalDateTime revokedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
