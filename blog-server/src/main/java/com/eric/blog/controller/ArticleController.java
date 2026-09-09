package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.common.PageResult;
import com.eric.blog.model.dto.article.ArticleQueryRequest;
import com.eric.blog.model.vo.web.ArchiveYearVO;
import com.eric.blog.model.vo.web.ArticleSummaryVO;
import com.eric.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 用户端文章列表与归档公开接口；文章详情归入 common 供两端共用。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public BaseResponse<PageResult<ArticleSummaryVO>> list(@Valid ArticleQueryRequest request) {
        return BaseResponse.success(articleService.listPublicArticles(request));
    }

    @GetMapping("/archive")
    public BaseResponse<List<ArchiveYearVO>> archive() {
        return BaseResponse.success(articleService.getArchive());
    }
}
