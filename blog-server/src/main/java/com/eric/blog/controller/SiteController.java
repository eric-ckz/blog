package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.vo.web.AboutVO;
import com.eric.blog.model.vo.web.HomeVO;
import com.eric.blog.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户端首页与关于页公开接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web/site")
public class SiteController {

    private final SiteService siteService;

    /** 一次返回首页首屏所需的统计、轮播、精选、最近文章和栏目。 */
    @GetMapping("/home")
    public BaseResponse<HomeVO> home() {
        return BaseResponse.success(siteService.getHome());
    }

    /** 返回关于页结构化内容。 */
    @GetMapping("/about")
    public BaseResponse<AboutVO> about() {
        return BaseResponse.success(siteService.getAbout());
    }
}
