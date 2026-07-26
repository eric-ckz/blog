package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 简单站点配置。采用键值表便于逐步增加文案而无需频繁改表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("site_setting")
public class SiteSetting extends BaseEntity {

    /** 唯一配置键。 */
    private String settingKey;

    /** 字符串配置值；Service 根据键转换为数字或文本。 */
    private String settingValue;

    /** 管理端展示的配置说明。 */
    private String description;
}
