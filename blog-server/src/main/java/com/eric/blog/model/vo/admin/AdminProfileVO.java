package com.eric.blog.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 脱敏后的管理员资料。 */
@Data
@AllArgsConstructor
public class AdminProfileVO {
    private String id;
    private String username;
    private String displayName;
}
