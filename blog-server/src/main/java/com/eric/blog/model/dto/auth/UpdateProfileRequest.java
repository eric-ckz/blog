package com.eric.blog.model.dto.auth;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 访客个人资料更新请求。 */
@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 32, message = "昵称长度需在 2 到 32 位之间")
    @Pattern(regexp = "^[\\p{L}\\p{N}_-]+$", message = "昵称只能包含中英文、数字、下划线和连字符")
    private String username;

    @Size(max = 100, message = "展示名称不能超过 100 字")
    private String displayName;

    @Size(max = 500, message = "个人简介不能超过 500 字")
    private String bio;
}
