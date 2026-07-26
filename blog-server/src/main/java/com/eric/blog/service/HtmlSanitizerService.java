package com.eric.blog.service;

/** 富文本 HTML 安全清理服务。 */
public interface HtmlSanitizerService {

    /**
     * 移除脚本、事件属性、危险协议等不安全内容，同时保留博客正文需要的排版元素。
     */
    String sanitize(String html);
}
