package com.eric.blog.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/** 管理端首页聚合统计。 */
@Data
@AllArgsConstructor
public class DashboardVO {
    private long articles;
    private long published;
    private long drafts;
    private long categories;
    private long media;
    private long totalViews;
    private Map<String, Long> categoryDistribution;
}
