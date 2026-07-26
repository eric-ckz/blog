package com.eric.blog.model.dto.site;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 首页设置保存请求，包括文案配置和人工编排的文章位置。 */
@Data
public class SiteSettingsRequest {

    @NotNull
    private Map<String, String> settings = new LinkedHashMap<>();

    /** 最多使用前三篇作为轮播。 */
    private List<String> heroArticleIds = new ArrayList<>();

    /** 最多使用前四篇作为精选文章。 */
    private List<String> featuredArticleIds = new ArrayList<>();
}
