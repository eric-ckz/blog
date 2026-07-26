package com.eric.blog.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 通用分页请求，限制单次查询规模，避免管理端误操作拖垮数据库。 */
@Data
public class PageRequest {

    /** 从 1 开始的页码。 */
    @Min(1)
    private long page = 1;

    /** 每页数量，公开端和管理端最多一次读取 100 条。 */
    @Min(1)
    @Max(100)
    private long pageSize = 15;

    /** 业务允许的排序字段；Service 层必须通过白名单转换。 */
    private String sortField;

    /** asc 或 desc，默认按降序展示。 */
    private String sortOrder = "desc";
}
