package com.eric.blog.model.dto.article;

import com.eric.blog.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 公开端和管理端复用的文章筛选条件。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleQueryRequest extends PageRequest {

    /** 栏目英文标识。 */
    private String category;

    /** 发布年份。 */
    private Integer year;

    /** 标题或摘要关键词。 */
    private String keyword;

    /** 管理端可按状态筛选，公开端忽略此字段。 */
    private String status;
}
