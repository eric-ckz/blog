package com.eric.blog.model.vo.web;

import lombok.Builder;
import lombok.Data;

/** 用户端文章详情。字段命名与现有 Repository 契约保持一致。 */
@Data
@Builder
public class ArticleDetailVO {
    private String id;
    private String title;
    private String summary;
    private String category;
    private String categoryLabel;
    private String coverUrl;
    private String publishedAt;
    private long views;
    private int readMinutes;
    private String contentHtml;
    private String originalUrl;
    private ArticleLinkVO previous;
    private ArticleLinkVO next;
}
