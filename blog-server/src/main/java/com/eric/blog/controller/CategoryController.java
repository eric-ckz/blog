package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.vo.web.CategoryVO;
import com.eric.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 用户端栏目公开接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public BaseResponse<List<CategoryVO>> list() {
        return BaseResponse.success(categoryService.listPublicCategories());
    }
}
