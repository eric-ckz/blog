package com.eric.blog.model.vo.web;

import lombok.Builder;
import lombok.Data;

/** 用户端文章卡片摘要。 */
@Data
@Builder
public class ArticleSummaryVO {
    private String id;
    private String title;
    private String summary;
    private String category;
    private String categoryLabel;
    private String coverUrl;
    private String publishedAt;
    private long views;
}
