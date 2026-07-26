package com.eric.blog.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 与用户端现有 Repository 完全一致的分页返回结构。 */
@Data
@AllArgsConstructor
public class PageResult<T> {

    private List<T> items;
    private long total;
    private long page;
    private long pageSize;
    private boolean hasMore;

    /** 将 MyBatis-Plus 分页对象转换为稳定的公开契约。 */
    public static <S, T> PageResult<T> from(Page<S> source, List<T> items) {
        return new PageResult<>(items, source.getTotal(), source.getCurrent(), source.getSize(),
                source.getCurrent() * source.getSize() < source.getTotal());
    }
}
