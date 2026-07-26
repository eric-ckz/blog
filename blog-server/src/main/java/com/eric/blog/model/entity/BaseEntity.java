package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通用持久化字段。所有业务表使用雪花 ID，避免数据库自增策略影响 H2/MySQL 切换。
 */
@Data
public abstract class BaseEntity {

    /** 数据库主键；仅在 VO 层转换为字符串返回。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建时间，由 MyBatis-Plus 自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 最后更新时间，由 MyBatis-Plus 自动填充。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除标记：0 正常，1 已删除。 */
    @TableLogic
    private Integer isDelete;
}
