package com.eric.blog.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 访客注册请求。 */
@Data
public class RegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需在 8 到 64 位之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 32, message = "昵称长度需在 2 到 32 位之间")
    @Pattern(regexp = "^[\\p{L}\\p{N}_-]+$", message = "昵称只能包含中英文、数字、下划线和连字符")
    private String username;
}
