package com.eric.blog.model.vo.admin;

import lombok.Builder;
import lombok.Data;

/** 管理端媒体库条目。 */
@Data
@Builder
public class MediaVO {
    private String id;
    private String originalName;
    private String url;
    private String mimeType;
    private long size;
    private int width;
    private int height;
    private String createdAt;
}
