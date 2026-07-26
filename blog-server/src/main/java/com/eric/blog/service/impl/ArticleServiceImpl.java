package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.common.PageResult;
import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.ArticleMapper;
import com.eric.blog.mapper.HomeArticleSlotMapper;
import com.eric.blog.mapper.MediaAssetMapper;
import com.eric.blog.model.dto.article.ArticleQueryRequest;
import com.eric.blog.model.dto.article.ArticleSaveRequest;
import com.eric.blog.model.entity.Article;
import com.eric.blog.model.entity.Category;
import com.eric.blog.model.entity.HomeArticleSlot;
import com.eric.blog.model.entity.MediaAsset;
import com.eric.blog.model.enums.ArticleStatus;
import com.eric.blog.model.vo.admin.AdminArticleVO;
import com.eric.blog.model.vo.web.ArchiveYearVO;
import com.eric.blog.model.vo.web.ArticleDetailVO;
import com.eric.blog.model.vo.web.ArticleLinkVO;
import com.eric.blog.model.vo.web.ArticleSummaryVO;
import com.eric.blog.service.ArticleService;
import com.eric.blog.service.CategoryService;
import com.eric.blog.service.HtmlSanitizerService;
import com.eric.blog.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** 文章业务实现，集中处理安全筛选、状态约束与实体到 VO 的转换。 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private static final DateTimeFormatter PUBLIC_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CategoryService categoryService;
    private final MediaAssetMapper mediaAssetMapper;
    private final HomeArticleSlotMapper homeArticleSlotMapper;
    private final HtmlSanitizerService htmlSanitizerService;

    @Override
    public PageResult<ArticleSummaryVO> listPublicArticles(ArticleQueryRequest request) {
        LambdaQueryWrapper<Article> wrapper = publicWrapper(request);
        Page<Article> page = page(new Page<>(request.getPage(), request.getPageSize()), wrapper);
        Map<Long, Category> categories = categoryMap(page.getRecords());
        List<ArticleSummaryVO> items = page.getRecords().stream()
                .map(article -> toSummary(article, categories.get(article.getCategoryId())))
                .toList();
        return PageResult.from(page, items);
    }

    @Override
    @Transactional
    public ArticleDetailVO getPublicArticle(String id) {
        long articleId = parseId(id);
        Article article = lambdaQuery().eq(Article::getId, articleId)
                .eq(Article::getStatus, ArticleStatus.PUBLISHED.name()).one();
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在或尚未发布");

        // 使用数据库原子自增，避免并发请求互相覆盖阅读量。
        lambdaUpdate().eq(Article::getId, articleId).setSql("view_count = view_count + 1").update();
        article.setViewCount(article.getViewCount() + 1);

        Category category = categoryService.getById(article.getCategoryId());
        Article previous = adjacent(article, true);
        Article next = adjacent(article, false);
        return ArticleDetailVO.builder()
                .id(String.valueOf(article.getId()))
                .title(article.getTitle())
                .summary(article.getSummary())
                .category(category == null ? "" : category.getCategoryKey())
                .categoryLabel(category == null ? "" : category.getLabel())
                .coverUrl(article.getCoverUrl())
                .publishedAt(formatPublicDate(article.getPublishedAt()))
                .views(article.getViewCount())
                .readMinutes(article.getReadMinutes())
                .contentHtml(article.getContentHtml())
                .originalUrl(article.getOriginalUrl())
                .previous(toLink(previous))
                .next(toLink(next))
                .build();
    }

    @Override
    public List<ArchiveYearVO> getArchive() {
        List<Article> articles = lambdaQuery().eq(Article::getStatus, ArticleStatus.PUBLISHED.name())
                .isNotNull(Article::getPublishedAt).orderByDesc(Article::getPublishedAt).list();
        Map<Long, Category> categories = categoryMap(articles);
        Map<Integer, Map<Integer, List<Article>>> grouped = new LinkedHashMap<>();
        for (Article article : articles) {
            grouped.computeIfAbsent(article.getPublishedAt().getYear(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(article.getPublishedAt().getMonthValue(), ignored -> new ArrayList<>())
                    .add(article);
        }
        List<ArchiveYearVO> result = new ArrayList<>();
        grouped.forEach((year, months) -> {
            List<ArchiveYearVO.ArchiveMonthVO> monthItems = new ArrayList<>();
            months.forEach((month, items) -> {
                List<ArticleSummaryVO> summaries = items.stream()
                        .map(item -> toSummary(item, categories.get(item.getCategoryId())))
                        .toList();
                // 与用户端 mock Repository 保持同一结构，页面切换数据源时无需增加条件分支。
                monthItems.add(new ArchiveYearVO.ArchiveMonthVO(
                        month,
                        Month.of(month).getDisplayName(TextStyle.FULL, Locale.CHINA),
                        summaries.size(),
                        summaries));
            });
            int count = months.values().stream().mapToInt(List::size).sum();
            result.add(new ArchiveYearVO(year, count, monthItems));
        });
        return result;
    }

    @Override
    public PageResult<AdminArticleVO> listAdminArticles(ArticleQueryRequest request) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        applyCommonFilters(wrapper, request);
        if (StringUtils.isNotBlank(request.getStatus())) {
            validateStatus(request.getStatus());
            wrapper.eq(Article::getStatus, request.getStatus().toUpperCase(Locale.ROOT));
        }
        applyAdminSort(wrapper, request.getSortField(), request.getSortOrder());
        Page<Article> page = page(new Page<>(request.getPage(), request.getPageSize()), wrapper);
        Map<Long, Category> categories = categoryMap(page.getRecords());
        List<AdminArticleVO> items = page.getRecords().stream()
                .map(article -> toAdminVO(article, categories.get(article.getCategoryId()))).toList();
        return PageResult.from(page, items);
    }

    @Override
    public AdminArticleVO getAdminArticle(String id) {
        Article article = getById(parseId(id));
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        return toAdminVO(article, categoryService.getById(article.getCategoryId()));
    }

    @Override
    @Transactional
    public String createArticle(ArticleSaveRequest request) {
        Article article = new Article();
        applySaveRequest(article, request);
        article.setViewCount(0L);
        ThrowUtils.throwIf(!save(article), ErrorCode.OPERATION_ERROR, "文章创建失败");
        return String.valueOf(article.getId());
    }

    @Override
    @Transactional
    public void updateArticle(String id, ArticleSaveRequest request) {
        Article article = getById(parseId(id));
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        applySaveRequest(article, request);
        ThrowUtils.throwIf(!updateById(article), ErrorCode.OPERATION_ERROR, "文章更新失败");
    }

    @Override
    @Transactional
    public void deleteArticle(String id) {
        long articleId = parseId(id);
        ThrowUtils.throwIf(getById(articleId) == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");
        // 先移除首页关联，避免首页继续引用逻辑删除后的文章。
        homeArticleSlotMapper.delete(new LambdaQueryWrapper<HomeArticleSlot>()
                .eq(HomeArticleSlot::getArticleId, articleId));
        ThrowUtils.throwIf(!removeById(articleId), ErrorCode.OPERATION_ERROR, "文章删除失败");
    }

    @Override
    public List<ArticleSummaryVO> listPublishedByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Article> articles = lambdaQuery().in(Article::getId, ids)
                .eq(Article::getStatus, ArticleStatus.PUBLISHED.name()).list();
        Map<Long, Article> byId = articles.stream().collect(java.util.stream.Collectors.toMap(Article::getId, item -> item));
        Map<Long, Category> categories = categoryMap(articles);
        return ids.stream().map(byId::get).filter(Objects::nonNull)
                .map(article -> toSummary(article, categories.get(article.getCategoryId()))).toList();
    }

    @Override
    public List<ArticleSummaryVO> listRecent(int limit) {
        List<Article> articles = lambdaQuery().eq(Article::getStatus, ArticleStatus.PUBLISHED.name())
                .orderByDesc(Article::getPublishedAt).last("LIMIT " + Math.max(1, Math.min(limit, 20))).list();
        Map<Long, Category> categories = categoryMap(articles);
        return articles.stream().map(article -> toSummary(article, categories.get(article.getCategoryId()))).toList();
    }

    private LambdaQueryWrapper<Article> publicWrapper(ArticleQueryRequest request) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, ArticleStatus.PUBLISHED.name());
        applyCommonFilters(wrapper, request);
        wrapper.orderByDesc(Article::getPublishedAt);
        return wrapper;
    }

    private void applyCommonFilters(LambdaQueryWrapper<Article> wrapper, ArticleQueryRequest request) {
        if (StringUtils.isNotBlank(request.getCategory())) {
            Category category = categoryService.lambdaQuery()
                    .eq(Category::getCategoryKey, request.getCategory()).one();
            // 未知栏目直接制造空结果，不把它误解释成“全部栏目”。
            wrapper.eq(Article::getCategoryId, category == null ? Long.MIN_VALUE : category.getId());
        }
        if (request.getYear() != null) {
            LocalDateTime start = LocalDateTime.of(request.getYear(), 1, 1, 0, 0);
            wrapper.ge(Article::getPublishedAt, start).lt(Article::getPublishedAt, start.plusYears(1));
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            String keyword = request.getKeyword().trim();
            wrapper.and(query -> query.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
    }

    private void applyAdminSort(LambdaQueryWrapper<Article> wrapper, String field, String order) {
        boolean ascending = "asc".equalsIgnoreCase(order);
        // 显式白名单阻止客户端把任意 SQL 片段当作排序字段传入。
        if ("title".equals(field)) {
            wrapper.orderBy(true, ascending, Article::getTitle);
        } else if ("publishedAt".equals(field)) {
            wrapper.orderBy(true, ascending, Article::getPublishedAt);
        } else if ("viewCount".equals(field)) {
            wrapper.orderBy(true, ascending, Article::getViewCount);
        } else {
            wrapper.orderBy(true, ascending, Article::getUpdateTime);
        }
    }

    private void applySaveRequest(Article article, ArticleSaveRequest request) {
        long categoryId = parseId(request.getCategoryId());
        ThrowUtils.throwIf(categoryService.getById(categoryId) == null, ErrorCode.PARAMS_ERROR, "所选栏目不存在");
        String status = request.getStatus().toUpperCase(Locale.ROOT);
        validateStatus(status);
        article.setTitle(request.getTitle().trim());
        article.setSummary(request.getSummary().trim());
        article.setCategoryId(categoryId);
        article.setContentHtml(htmlSanitizerService.sanitize(request.getContentHtml()));
        article.setStatus(status);
        article.setReadMinutes(request.getReadMinutes());
        article.setOriginalUrl(StringUtils.trimToNull(request.getOriginalUrl()));
        article.setPublishedAt(ArticleStatus.PUBLISHED.name().equals(status)
                ? Objects.requireNonNullElse(request.getPublishedAt(), LocalDateTime.now())
                : request.getPublishedAt());

        if (StringUtils.isNotBlank(request.getCoverMediaId())) {
            long mediaId = parseId(request.getCoverMediaId());
            MediaAsset media = mediaAssetMapper.selectById(mediaId);
            ThrowUtils.throwIf(media == null, ErrorCode.PARAMS_ERROR, "封面图片不存在");
            article.setCoverMediaId(mediaId);
            article.setCoverUrl(media.getPublicUrl());
        } else {
            article.setCoverMediaId(null);
            article.setCoverUrl(StringUtils.trimToNull(request.getCoverUrl()));
        }
    }

    private void validateStatus(String status) {
        try {
            ArticleStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "无效的文章状态");
        }
    }

    private Article adjacent(Article current, boolean newer) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, ArticleStatus.PUBLISHED.name())
                .ne(Article::getId, current.getId());
        if (newer) {
            wrapper.gt(Article::getPublishedAt, current.getPublishedAt()).orderByAsc(Article::getPublishedAt);
        } else {
            wrapper.lt(Article::getPublishedAt, current.getPublishedAt()).orderByDesc(Article::getPublishedAt);
        }
        return getOne(wrapper.last("LIMIT 1"), false);
    }

    private Map<Long, Category> categoryMap(List<Article> articles) {
        return categoryService.mapByIds(articles.stream().map(Article::getCategoryId).distinct().toList());
    }

    private ArticleSummaryVO toSummary(Article article, Category category) {
        return ArticleSummaryVO.builder()
                .id(String.valueOf(article.getId()))
                .title(article.getTitle())
                .summary(article.getSummary())
                .category(category == null ? "" : category.getCategoryKey())
                .categoryLabel(category == null ? "" : category.getLabel())
                .coverUrl(article.getCoverUrl())
                .publishedAt(formatPublicDate(article.getPublishedAt()))
                .views(article.getViewCount())
                .build();
    }

    private AdminArticleVO toAdminVO(Article article, Category category) {
        return AdminArticleVO.builder()
                .id(String.valueOf(article.getId()))
                .title(article.getTitle())
                .summary(article.getSummary())
                .categoryId(String.valueOf(article.getCategoryId()))
                .categoryLabel(category == null ? "" : category.getLabel())
                .coverMediaId(article.getCoverMediaId() == null ? null : String.valueOf(article.getCoverMediaId()))
                .coverUrl(article.getCoverUrl())
                .contentHtml(article.getContentHtml())
                .status(article.getStatus())
                .publishedAt(article.getPublishedAt() == null ? null : article.getPublishedAt().toString())
                .views(article.getViewCount())
                .readMinutes(article.getReadMinutes())
                .originalUrl(article.getOriginalUrl())
                .createTime(article.getCreateTime() == null ? null : article.getCreateTime().toString())
                .updateTime(article.getUpdateTime() == null ? null : article.getUpdateTime().toString())
                .build();
    }

    private ArticleLinkVO toLink(Article article) {
        return article == null ? null : new ArticleLinkVO(String.valueOf(article.getId()), article.getTitle());
    }

    private String formatPublicDate(LocalDateTime value) {
        return value == null ? "" : value.format(PUBLIC_DATE);
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException exception) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "ID 格式不正确");
        }
    }
}
