package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.mapper.ArticleMapper;
import com.eric.blog.mapper.CategoryMapper;
import com.eric.blog.model.dto.category.CategorySaveRequest;
import com.eric.blog.model.entity.Article;
import com.eric.blog.model.entity.Category;
import com.eric.blog.model.vo.web.CategoryVO;
import com.eric.blog.service.CategoryService;
import com.eric.blog.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 栏目业务实现。 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ArticleMapper articleMapper;

    @Override
    public List<CategoryVO> listPublicCategories() {
        return lambdaQuery().orderByAsc(Category::getSortOrder).list().stream().map(this::toVO).toList();
    }

    @Override
    public List<CategoryVO> listAdminCategories() {
        return listPublicCategories();
    }

    @Override
    @Transactional
    public String createCategory(CategorySaveRequest request) {
        long duplicate = lambdaQuery().eq(Category::getCategoryKey, request.getCategoryKey()).count();
        ThrowUtils.throwIf(duplicate > 0, ErrorCode.CONFLICT_ERROR, "栏目标识已存在");
        Category category = new Category();
        BeanUtils.copyProperties(request, category);
        ThrowUtils.throwIf(!save(category), ErrorCode.OPERATION_ERROR, "栏目创建失败");
        return String.valueOf(category.getId());
    }

    @Override
    @Transactional
    public void updateCategory(String id, CategorySaveRequest request) {
        long categoryId = parseId(id);
        Category current = getById(categoryId);
        ThrowUtils.throwIf(current == null, ErrorCode.NOT_FOUND_ERROR, "栏目不存在");
        long duplicate = lambdaQuery().eq(Category::getCategoryKey, request.getCategoryKey())
                .ne(Category::getId, categoryId).count();
        ThrowUtils.throwIf(duplicate > 0, ErrorCode.CONFLICT_ERROR, "栏目标识已存在");
        BeanUtils.copyProperties(request, current);
        ThrowUtils.throwIf(!updateById(current), ErrorCode.OPERATION_ERROR, "栏目更新失败");
    }

    @Override
    @Transactional
    public void deleteCategory(String id) {
        long categoryId = parseId(id);
        ThrowUtils.throwIf(getById(categoryId) == null, ErrorCode.NOT_FOUND_ERROR, "栏目不存在");
        Long articleCount = articleMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, categoryId));
        ThrowUtils.throwIf(articleCount > 0, ErrorCode.CONFLICT_ERROR, "栏目下仍有文章，不能删除");
        ThrowUtils.throwIf(!removeById(categoryId), ErrorCode.OPERATION_ERROR, "栏目删除失败");
    }

    @Override
    public Map<Long, Category> mapByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return listByIds(ids.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private CategoryVO toVO(Category category) {
        return CategoryVO.builder()
                .id(String.valueOf(category.getId()))
                .key(category.getCategoryKey())
                .label(category.getLabel())
                .description(category.getDescription())
                .icon(category.getIcon())
                .count(category.getDisplayCount())
                .sortOrder(category.getSortOrder())
                .build();
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException exception) {
            throw new com.eric.blog.exception.BaseException(ErrorCode.PARAMS_ERROR, "ID 格式不正确");
        }
    }
}
