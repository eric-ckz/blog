package com.eric.blog.model.dto.site;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 关于页全部内容的批量保存请求。 */
@Data
public class AboutSaveRequest {

    @Valid
    @NotNull
    private List<Item> items = new ArrayList<>();

    /** 单个关于页条目。 */
    @Data
    public static class Item {
        @NotBlank(message = "条目类型不能为空")
        private String itemType;
        private String title;
        @NotBlank(message = "条目描述不能为空")
        private String description;
        private String icon;
        private Integer sortOrder = 0;
    }
}
