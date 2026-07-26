package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.dto.category.CategorySaveRequest;
import com.eric.blog.model.vo.web.CategoryVO;
import com.eric.blog.service.CategoryService;
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

import java.util.List;
import java.util.Map;

/** 管理端栏目 CRUD 接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public BaseResponse<List<CategoryVO>> list() {
        return BaseResponse.success(categoryService.listAdminCategories());
    }

    @PostMapping
    public BaseResponse<Map<String, String>> create(@Valid @RequestBody CategorySaveRequest request) {
        return BaseResponse.success(Map.of("id", categoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    public BaseResponse<Void> update(@PathVariable String id, @Valid @RequestBody CategorySaveRequest request) {
        categoryService.updateCategory(id, request);
        return BaseResponse.success();
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return BaseResponse.success();
    }
}
