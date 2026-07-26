package com.eric.blog.model.vo.admin;

import lombok.Builder;
import lombok.Data;

/** 管理端文章模型，额外包含状态和数据库关联 ID。 */
@Data
@Builder
public class AdminArticleVO {
    private String id;
    private String title;
    private String summary;
    private String categoryId;
    private String categoryLabel;
    private String coverMediaId;
    private String coverUrl;
    private String contentHtml;
    private String status;
    private String publishedAt;
    private long views;
    private int readMinutes;
    private String originalUrl;
    private String createTime;
    private String updateTime;
}
