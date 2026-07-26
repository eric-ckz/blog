package com.eric.blog.model.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 文章详情页上一篇和下一篇的最小数据。 */
@Data
@AllArgsConstructor
public class ArticleLinkVO {
    private String id;
    private String title;
}
