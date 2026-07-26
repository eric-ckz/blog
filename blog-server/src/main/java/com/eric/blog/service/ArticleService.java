package com.eric.blog.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.eric.blog.common.PageResult;
import com.eric.blog.model.dto.article.ArticleQueryRequest;
import com.eric.blog.model.dto.article.ArticleSaveRequest;
import com.eric.blog.model.entity.Article;
import com.eric.blog.model.vo.admin.AdminArticleVO;
import com.eric.blog.model.vo.web.ArchiveYearVO;
import com.eric.blog.model.vo.web.ArticleDetailVO;
import com.eric.blog.model.vo.web.ArticleSummaryVO;

import java.util.List;

/** 文章查询与管理服务。 */
public interface ArticleService extends IService<Article> {
    PageResult<ArticleSummaryVO> listPublicArticles(ArticleQueryRequest request);
    ArticleDetailVO getPublicArticle(String id);
    List<ArchiveYearVO> getArchive();
    PageResult<AdminArticleVO> listAdminArticles(ArticleQueryRequest request);
    AdminArticleVO getAdminArticle(String id);
    String createArticle(ArticleSaveRequest request);
    void updateArticle(String id, ArticleSaveRequest request);
    void deleteArticle(String id);
    List<ArticleSummaryVO> listPublishedByIds(List<Long> ids);
    List<ArticleSummaryVO> listRecent(int limit);
}
