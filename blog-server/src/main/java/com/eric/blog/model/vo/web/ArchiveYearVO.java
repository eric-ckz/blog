package com.eric.blog.model.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 按年份分组的归档数据。 */
@Data
@AllArgsConstructor
public class ArchiveYearVO {
    private int year;
    private int count;
    private List<ArchiveMonthVO> months;

    /** 按月份分组的文章。 */
    @Data
    @AllArgsConstructor
    public static class ArchiveMonthVO {
        private int month;
        private String label;
        private int count;
        private List<ArticleSummaryVO> items;
    }
}
