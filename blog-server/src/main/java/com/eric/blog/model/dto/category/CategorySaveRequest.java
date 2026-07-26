package com.eric.blog.model.dto.category;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 新建和编辑栏目请求。 */
@Data
public class CategorySaveRequest {

    @NotBlank(message = "栏目标识不能为空")
    @Pattern(regexp = "[a-z0-9-]{2,50}", message = "栏目标识只能包含小写字母、数字和连字符")
    private String categoryKey;

    @NotBlank(message = "栏目名称不能为空")
    @Size(max = 100)
    private String label;

    @NotBlank(message = "栏目描述不能为空")
    @Size(max = 255)
    private String description;

    @NotBlank(message = "图标标识不能为空")
    @Size(max = 50)
    private String icon;

    @Min(0)
    private Integer displayCount = 0;

    @Min(0)
    private Integer sortOrder = 0;
}
