package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.dto.comment.CommentCreateRequest;
import com.eric.blog.model.vo.web.CommentPageVO;
import com.eric.blog.model.vo.web.CommentVO;
import com.eric.blog.security.SecurityContextUtils;
import com.eric.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 用户端评论公开接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/articles/{articleId}/comments")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public BaseResponse<CommentPageVO> list(@PathVariable String articleId,
                                            @RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "15") long pageSize) {
        return BaseResponse.success(commentService.listComments(articleId, page, pageSize,
                SecurityContextUtils.isUserAuthenticated()));
    }

    @PostMapping
    public BaseResponse<CommentVO> create(@PathVariable String articleId,
                                          @Valid @RequestBody CommentCreateRequest request) {
        return BaseResponse.success(commentService.createComment(articleId, request));
    }
}
