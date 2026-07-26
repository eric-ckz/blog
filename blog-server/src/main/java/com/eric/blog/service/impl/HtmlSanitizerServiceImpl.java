package com.eric.blog.service.impl;

import com.eric.blog.service.HtmlSanitizerService;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

/** 基于 OWASP Java HTML Sanitizer 的正文清理实现。 */
@Service
public class HtmlSanitizerServiceImpl implements HtmlSanitizerService {

    private final PolicyFactory policy = new HtmlPolicyBuilder()
            .allowElements("p", "br", "h1", "h2", "h3", "h4", "blockquote", "pre", "code",
                    "strong", "em", "u", "s", "ul", "ol", "li", "a", "img", "figure", "figcaption")
            .allowAttributes("href", "title", "target", "rel").onElements("a")
            .allowAttributes("src", "alt", "title", "width", "height", "loading").onElements("img")
            .allowAttributes("class").onElements("code", "pre", "p", "figure")
            .allowUrlProtocols("http", "https")
            // 为外链补充安全属性，防止新窗口获得管理端页面的 window.opener。
            .requireRelNofollowOnLinks()
            .toFactory();

    @Override
    public String sanitize(String html) {
        return policy.sanitize(html == null ? "" : html);
    }
}
