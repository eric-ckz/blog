package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.dto.site.AboutSaveRequest;
import com.eric.blog.model.dto.site.SiteSettingsRequest;
import com.eric.blog.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 管理端首页和关于页配置接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/site")
public class AdminSiteController {

    private final SiteService siteService;

    @GetMapping("/settings")
    public BaseResponse<SiteSettingsRequest> settings() {
        return BaseResponse.success(siteService.getAdminSettings());
    }

    @PutMapping("/settings")
    public BaseResponse<Void> updateSettings(@Valid @RequestBody SiteSettingsRequest request) {
        siteService.updateSettings(request);
        return BaseResponse.success();
    }

    @GetMapping("/about")
    public BaseResponse<Map<String, Object>> about() {
        return BaseResponse.success(siteService.getAdminAbout());
    }

    @PutMapping("/about")
    public BaseResponse<Void> updateAbout(@Valid @RequestBody AboutSaveRequest request) {
        siteService.updateAbout(request);
        return BaseResponse.success();
    }
}
