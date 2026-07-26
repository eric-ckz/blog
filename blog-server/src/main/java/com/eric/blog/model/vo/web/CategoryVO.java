package com.eric.blog.model.vo.web;

import lombok.Builder;
import lombok.Data;

/** 用户端栏目卡片。 */
@Data
@Builder
public class CategoryVO {
    private String id;
    private String key;
    private String label;
    private String description;
    private String icon;
    private int count;
    private int sortOrder;
}
