package com.eric.blog.service;

import com.eric.blog.model.dto.site.AboutSaveRequest;
import com.eric.blog.model.dto.site.SiteSettingsRequest;
import com.eric.blog.model.vo.web.AboutVO;
import com.eric.blog.model.vo.web.HomeVO;

import java.util.Map;

/** 首页和关于页配置服务。 */
public interface SiteService {
    HomeVO getHome();
    AboutVO getAbout();
    SiteSettingsRequest getAdminSettings();
    void updateSettings(SiteSettingsRequest request);
    Map<String, Object> getAdminAbout();
    void updateAbout(AboutSaveRequest request);
}
