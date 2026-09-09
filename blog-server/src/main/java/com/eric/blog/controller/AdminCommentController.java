package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.vo.web.CommentPageVO;
import com.eric.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/** 管理端评论管理接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/comments")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public BaseResponse<CommentPageVO> list(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "15") long pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long articleId,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return BaseResponse.success(commentService.listAdminComments(page, pageSize, keyword, articleId, startTime, endTime));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable String id) {
        commentService.deleteComment(id);
        return BaseResponse.success();
    }
}
