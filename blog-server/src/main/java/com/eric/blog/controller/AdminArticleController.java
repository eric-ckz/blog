package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.common.PageResult;
import com.eric.blog.model.dto.article.ArticleQueryRequest;
import com.eric.blog.model.dto.article.ArticleSaveRequest;
import com.eric.blog.model.vo.admin.AdminArticleVO;
import com.eric.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 管理端文章 CRUD 接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    @GetMapping
    public BaseResponse<PageResult<AdminArticleVO>> list(@Valid ArticleQueryRequest request) {
        return BaseResponse.success(articleService.listAdminArticles(request));
    }

    @GetMapping("/{id}")
    public BaseResponse<AdminArticleVO> detail(@PathVariable String id) {
        return BaseResponse.success(articleService.getAdminArticle(id));
    }

    @PostMapping
    public BaseResponse<Map<String, String>> create(@Valid @RequestBody ArticleSaveRequest request) {
        return BaseResponse.success(Map.of("id", articleService.createArticle(request)));
    }

    @PutMapping("/{id}")
    public BaseResponse<Void> update(@PathVariable String id, @Valid @RequestBody ArticleSaveRequest request) {
        articleService.updateArticle(id, request);
        return BaseResponse.success();
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable String id) {
        articleService.deleteArticle(id);
        return BaseResponse.success();
    }
}
