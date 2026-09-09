package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.common.PageResult;
import com.eric.blog.model.vo.admin.BlogUserAdminVO;
import com.eric.blog.service.BlogUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 管理端访客用户管理接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminBlogUserController {

    private final BlogUserAdminService blogUserAdminService;

    @GetMapping
    public BaseResponse<PageResult<BlogUserAdminVO>> list(@RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "15") long pageSize,
                                                          @RequestParam(required = false) String keyword) {
        return BaseResponse.success(blogUserAdminService.listUsers(page, pageSize, keyword));
    }

    @PutMapping("/{id}/enabled")
    public BaseResponse<Void> setEnabled(@PathVariable String id, @RequestBody Map<String, Boolean> body) {
        blogUserAdminService.setUserEnabled(id, Boolean.TRUE.equals(body.get("enabled")));
        return BaseResponse.success();
    }
}
