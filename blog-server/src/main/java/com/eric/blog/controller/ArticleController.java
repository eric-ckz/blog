package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.common.PageResult;
import com.eric.blog.model.dto.article.ArticleQueryRequest;
import com.eric.blog.model.vo.web.ArchiveYearVO;
import com.eric.blog.model.vo.web.ArticleDetailVO;
import com.eric.blog.model.vo.web.ArticleSummaryVO;
import com.eric.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 用户端文章列表、详情与归档公开接口。 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public BaseResponse<PageResult<ArticleSummaryVO>> list(@Valid ArticleQueryRequest request) {
        return BaseResponse.success(articleService.listPublicArticles(request));
    }

    @GetMapping("/articles/{id}")
    public BaseResponse<ArticleDetailVO> detail(@PathVariable String id) {
        return BaseResponse.success(articleService.getPublicArticle(id));
    }

    @GetMapping("/archive")
    public BaseResponse<List<ArchiveYearVO>> archive() {
        return BaseResponse.success(articleService.getArchive());
    }
}
