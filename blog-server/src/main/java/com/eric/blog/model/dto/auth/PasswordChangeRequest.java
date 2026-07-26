package com.eric.blog.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员修改密码请求。 */
@Data
public class PasswordChangeRequest {

    @NotBlank(message = "当前密码不能为空")
    private String currentPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 10, max = 128, message = "新密码长度必须为 10 到 128 位")
    private String newPassword;
}
