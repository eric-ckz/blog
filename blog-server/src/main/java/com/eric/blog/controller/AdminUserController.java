package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.dto.auth.PasswordChangeRequest;
import com.eric.blog.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理员账号设置接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final AuthService authService;

    /** 修改密码后撤销全部 Refresh Token，调用端必须重新登录。 */
    @PutMapping("/password")
    public BaseResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request,
                                             HttpServletResponse response) {
        authService.changePassword(request, response);
        return BaseResponse.success();
    }
}
