package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 首页轮播和精选文章的有序关联。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("home_article_slot")
public class HomeArticleSlot extends BaseEntity {

    /** HERO 或 FEATURED。 */
    private String slotType;

    /** 关联的已发布文章 ID。 */
    private Long articleId;

    /** 同一位置类型下的显示顺序。 */
    private Integer sortOrder;
}
