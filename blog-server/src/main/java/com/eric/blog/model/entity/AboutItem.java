package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 关于页的介绍段落、热爱事项、时间线和联系方式。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("about_item")
public class AboutItem extends BaseEntity {

    /** INTRO、LOVE、TIMELINE 或 CONTACT。 */
    private String itemType;

    /** 标题或年份。 */
    private String title;

    /** 正文描述。 */
    private String description;

    /** 可选 Lucide 图标标识。 */
    private String icon;

    /** 同类型内的展示顺序。 */
    private Integer sortOrder;
}
