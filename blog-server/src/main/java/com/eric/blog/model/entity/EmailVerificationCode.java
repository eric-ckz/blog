package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 邮箱验证码记录，只保存 SHA-256 哈希。 */
@Data
@TableName("email_verification_code")
public class EmailVerificationCode {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 接收验证码的邮箱。 */
    private String email;

    /** 6 位验证码的 SHA-256 哈希。 */
    private String codeHash;

    /** 用途：REGISTER 注册激活。 */
    private String purpose;

    /** 验证码过期时间。 */
    private LocalDateTime expiresAt;

    /** 使用时间，为空表示未使用。 */
    private LocalDateTime usedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
