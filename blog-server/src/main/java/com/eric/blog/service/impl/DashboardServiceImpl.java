package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eric.blog.mapper.ArticleMapper;
import com.eric.blog.mapper.CategoryMapper;
import com.eric.blog.mapper.MediaAssetMapper;
import com.eric.blog.model.entity.Article;
import com.eric.blog.model.entity.Category;
import com.eric.blog.model.enums.ArticleStatus;
import com.eric.blog.model.vo.admin.DashboardVO;
import com.eric.blog.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Dashboard 聚合统计实现。当前数据规模较小，使用可读性更高的应用层聚合。 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final MediaAssetMapper mediaAssetMapper;

    @Override
    public DashboardVO getDashboard() {
        List<Article> articles = articleMapper.selectList(null);
        Map<Long, String> categoryNames = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getLabel));
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Article article : articles) {
            distribution.merge(categoryNames.getOrDefault(article.getCategoryId(), "未分类"), 1L, Long::sum);
        }
        long published = articles.stream().filter(item -> ArticleStatus.PUBLISHED.name().equals(item.getStatus())).count();
        long drafts = articles.stream().filter(item -> ArticleStatus.DRAFT.name().equals(item.getStatus())).count();
        long totalViews = articles.stream().mapToLong(item -> item.getViewCount() == null ? 0 : item.getViewCount()).sum();
        return new DashboardVO(articles.size(), published, drafts,
                categoryMapper.selectCount(new LambdaQueryWrapper<>()),
                mediaAssetMapper.selectCount(new LambdaQueryWrapper<>()), totalViews, distribution);
    }
}
