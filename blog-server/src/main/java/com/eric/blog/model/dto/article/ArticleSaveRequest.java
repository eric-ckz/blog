package com.eric.blog.model.dto.article;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/** 新建和编辑文章请求。正文会在 Service 层清理后才写入数据库。 */
@Data
public class ArticleSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题不能超过 255 字")
    private String title;

    @NotBlank(message = "摘要不能为空")
    @Size(max = 1000, message = "摘要不能超过 1000 字")
    private String summary;

    @NotNull(message = "请选择栏目")
    private String categoryId;

    /** 封面媒体 ID；允许直接使用 coverUrl 兼容外部对象存储。 */
    private String coverMediaId;

    private String coverUrl;

    @NotBlank(message = "正文不能为空")
    private String contentHtml;

    @NotBlank(message = "请选择文章状态")
    private String status;

    private LocalDateTime publishedAt;

    @Min(value = 1, message = "阅读时间至少为 1 分钟")
    @Max(value = 240, message = "阅读时间不能超过 240 分钟")
    private Integer readMinutes = 3;

    @Size(max = 1000, message = "原文地址过长")
    private String originalUrl;
}
