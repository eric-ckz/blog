package com.eric.blog;

import com.eric.blog.service.impl.HtmlSanitizerServiceImpl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 富文本服务必须删除脚本和事件属性，同时保留正常段落。 */
class HtmlSanitizerServiceTests {

    private final HtmlSanitizerServiceImpl sanitizer = new HtmlSanitizerServiceImpl();

    @Test
    void removesExecutableHtml() {
        String result = sanitizer.sanitize("<p onclick=\"alert(1)\">正文</p><script>alert(1)</script>");
        assertThat(result).contains("<p>正文</p>").doesNotContain("onclick").doesNotContain("script");
    }

    @Test
    void keepsSafeUploadedImageAndRemovesJavascriptUrl() {
        String result = sanitizer.sanitize("<img src=\"/uploads/2026/07/example.jpg\" alt=\"封面\">"
                + "<a href=\"javascript:alert(1)\">危险链接</a>");
        assertThat(result)
                .contains("/uploads/2026/07/example.jpg")
                .doesNotContain("javascript:");
    }
}
