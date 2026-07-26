package com.eric.blog.model.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 首页一次请求所需的全部数据，减少首屏瀑布请求。 */
@Data
@AllArgsConstructor
public class HomeVO {
    private Map<String, Object> stats;
    private List<ArticleSummaryVO> heroSlides;
    private List<ArticleSummaryVO> featured;
    private List<ArticleSummaryVO> recent;
    private List<CategoryVO> categories;
    private Map<String, String> manifesto;
}
