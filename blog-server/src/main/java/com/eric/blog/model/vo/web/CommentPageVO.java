package com.eric.blog.model.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 评论分页返回结构，附带未登录时的可见性提示。 */
@Data
@AllArgsConstructor
public class CommentPageVO {
    private List<CommentVO> items;
    private long total;
    private long page;
    private long pageSize;
    private boolean hasMore;
    private boolean needLogin;
}
