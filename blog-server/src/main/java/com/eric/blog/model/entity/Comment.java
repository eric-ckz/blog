package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文章评论。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("comment")
public class Comment extends BaseEntity {

    /** 所属文章 ID。 */
    private Long articleId;

    /** 发表评论的用户 ID。 */
    private Long userId;

    /** 评论正文，纯文本。 */
    private String content;
}
