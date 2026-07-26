package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 博客文章持久化模型。正文保存经过服务端清理后的 TipTap HTML。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("article")
public class Article extends BaseEntity {

    /** 文章标题。 */
    private String title;

    /** 卡片和 SEO 使用的摘要。 */
    private String summary;

    /** 所属栏目 ID。 */
    private Long categoryId;

    /** 封面媒体 ID，可为空。 */
    private Long coverMediaId;

    /** 封面公开 URL，保留该字段便于以后切换对象存储。 */
    private String coverUrl;

    /** 清理后的富文本 HTML。 */
    private String contentHtml;

    /** DRAFT、PUBLISHED 或 ARCHIVED。 */
    private String status;

    /** 对外发布时间；草稿可为空。 */
    private LocalDateTime publishedAt;

    /** 阅读次数。 */
    private Long viewCount;

    /** 预计阅读分钟数。 */
    private Integer readMinutes;

    /** 原公众号等外部原文地址。 */
    private String originalUrl;
}
