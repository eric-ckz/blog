package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 博客栏目。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    /** URL 查询使用的稳定英文标识，例如 travel。 */
    private String categoryKey;

    /** 中文栏目名称。 */
    private String label;

    /** 首页栏目卡片描述。 */
    private String description;

    /** 与 Lucide 图标表对应的标识。 */
    private String icon;

    /** 对外展示数量，可与当前导入的文章数量不同。 */
    private Integer displayCount;

    /** 数值越小越靠前。 */
    private Integer sortOrder;
}
