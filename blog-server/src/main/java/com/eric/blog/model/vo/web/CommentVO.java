package com.eric.blog.model.vo.web;

import lombok.Builder;
import lombok.Data;

/** 用户端评论展示结构。 */
@Data
@Builder
public class CommentVO {
    private String id;
    private String articleId;
    private String articleTitle;
    private String content;
    private String createdAt;
    private UserProfileVO author;
}
