package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.vo.web.ArticleDetailVO;
import com.eric.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 两端共用的文章详情接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common/articles")
public class CommonArticleController {

    private final ArticleService articleService;

    @GetMapping("/{id}")
    public BaseResponse<ArticleDetailVO> detail(@PathVariable String id) {
        return BaseResponse.success(articleService.getPublicArticle(id));
    }
}
