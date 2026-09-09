package com.eric.blog.service;

import com.eric.blog.model.dto.comment.CommentCreateRequest;
import com.eric.blog.model.vo.web.CommentPageVO;
import com.eric.blog.model.vo.web.CommentVO;

import java.time.LocalDateTime;

/** 文章评论服务。 */
public interface CommentService {
    CommentPageVO listComments(String articleId, long page, long pageSize, boolean authenticated);
    CommentVO createComment(String articleId, CommentCreateRequest request);
    CommentPageVO listAdminComments(long page, long pageSize, String keyword, Long articleId,
                                    LocalDateTime startTime, LocalDateTime endTime);
    void deleteComment(String id);
}
