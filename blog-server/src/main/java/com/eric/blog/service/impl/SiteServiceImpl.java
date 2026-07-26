package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.mapper.AboutItemMapper;
import com.eric.blog.mapper.HomeArticleSlotMapper;
import com.eric.blog.mapper.SiteSettingMapper;
import com.eric.blog.model.dto.site.AboutSaveRequest;
import com.eric.blog.model.dto.site.SiteSettingsRequest;
import com.eric.blog.model.entity.AboutItem;
import com.eric.blog.model.entity.Article;
import com.eric.blog.model.entity.HomeArticleSlot;
import com.eric.blog.model.entity.SiteSetting;
import com.eric.blog.model.enums.AboutItemType;
import com.eric.blog.model.enums.ArticleStatus;
import com.eric.blog.model.enums.HomeSlotType;
import com.eric.blog.model.vo.web.AboutVO;
import com.eric.blog.model.vo.web.ArticleSummaryVO;
import com.eric.blog.model.vo.web.HomeVO;
import com.eric.blog.service.ArticleService;
import com.eric.blog.service.CategoryService;
import com.eric.blog.service.SiteService;
import com.eric.blog.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** 站点组合数据实现。 */
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private static final Set<String> EDITABLE_SETTING_KEYS = Set.of(
            "stat_articles", "stat_words", "stat_years", "stat_columns", "manifesto_title", "manifesto_text");

    private final SiteSettingMapper siteSettingMapper;
    private final HomeArticleSlotMapper homeArticleSlotMapper;
    private final AboutItemMapper aboutItemMapper;
    private final ArticleService articleService;
    private final CategoryService categoryService;

    @Override
    public HomeVO getHome() {
        Map<String, String> settings = settingMap();
        List<ArticleSummaryVO> hero = articlesForSlot(HomeSlotType.HERO);
        List<ArticleSummaryVO> featured = articlesForSlot(HomeSlotType.FEATURED);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("articles", parseNumber(settings.get("stat_articles"), 528));
        stats.put("words", settings.getOrDefault("stat_words", "30万"));
        stats.put("years", parseNumber(settings.get("stat_years"), 7));
        stats.put("columns", parseNumber(settings.get("stat_columns"), 5));
        Map<String, String> manifesto = Map.of(
                "title", settings.getOrDefault("manifesto_title", ""),
                "text", settings.getOrDefault("manifesto_text", ""));
        return new HomeVO(stats, hero, featured, articleService.listRecent(6),
                categoryService.listPublicCategories(), manifesto);
    }

    @Override
    public AboutVO getAbout() {
        List<AboutItem> items = listAboutItems();
        List<String> intro = items.stream().filter(item -> AboutItemType.INTRO.name().equals(item.getItemType()))
                .map(AboutItem::getDescription).toList();
        List<AboutVO.LoveVO> loves = items.stream().filter(item -> AboutItemType.LOVE.name().equals(item.getItemType()))
                .map(item -> new AboutVO.LoveVO(item.getTitle(), item.getIcon(), item.getDescription())).toList();
        List<AboutVO.TimelineVO> timeline = items.stream()
                .filter(item -> AboutItemType.TIMELINE.name().equals(item.getItemType()))
                .map(item -> new AboutVO.TimelineVO(item.getTitle(), item.getDescription())).toList();
        AboutVO.ContactVO contact = items.stream().filter(item -> AboutItemType.CONTACT.name().equals(item.getItemType()))
                .findFirst().map(item -> new AboutVO.ContactVO(item.getTitle(), item.getDescription()))
                .orElse(new AboutVO.ContactVO("", ""));
        return new AboutVO(intro, loves, timeline, contact);
    }

    @Override
    public SiteSettingsRequest getAdminSettings() {
        SiteSettingsRequest response = new SiteSettingsRequest();
        response.setSettings(settingMap());
        response.setHeroArticleIds(idsForSlot(HomeSlotType.HERO));
        response.setFeaturedArticleIds(idsForSlot(HomeSlotType.FEATURED));
        return response;
    }

    @Override
    @Transactional
    public void updateSettings(SiteSettingsRequest request) {
        request.getSettings().forEach((key, value) -> {
            ThrowUtils.throwIf(!EDITABLE_SETTING_KEYS.contains(key), ErrorCode.PARAMS_ERROR,
                    "不允许修改配置项：" + key);
            SiteSetting setting = siteSettingMapper.selectOne(new LambdaQueryWrapper<SiteSetting>()
                    .eq(SiteSetting::getSettingKey, key));
            if (setting == null) {
                setting = new SiteSetting();
                setting.setSettingKey(key);
                setting.setDescription(key);
                setting.setSettingValue(value == null ? "" : value);
                siteSettingMapper.insert(setting);
            } else {
                setting.setSettingValue(value == null ? "" : value);
                siteSettingMapper.updateById(setting);
            }
        });
        replaceSlots(HomeSlotType.HERO, request.getHeroArticleIds(), 3);
        replaceSlots(HomeSlotType.FEATURED, request.getFeaturedArticleIds(), 4);
    }

    @Override
    public Map<String, Object> getAdminAbout() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", listAboutItems());
        return result;
    }

    @Override
    @Transactional
    public void updateAbout(AboutSaveRequest request) {
        // BlockAttackInnerInterceptor 要求 DELETE 必须带条件，因此使用主键非空作为安全全量替换条件。
        aboutItemMapper.delete(new LambdaQueryWrapper<AboutItem>().isNotNull(AboutItem::getId));
        for (AboutSaveRequest.Item source : request.getItems()) {
            String itemType = source.getItemType().toUpperCase(Locale.ROOT);
            try {
                AboutItemType.valueOf(itemType);
            } catch (IllegalArgumentException exception) {
                throw new com.eric.blog.exception.BaseException(ErrorCode.PARAMS_ERROR, "关于页条目类型无效");
            }
            AboutItem item = new AboutItem();
            item.setItemType(itemType);
            item.setTitle(source.getTitle());
            item.setDescription(source.getDescription());
            item.setIcon(source.getIcon());
            item.setSortOrder(source.getSortOrder());
            aboutItemMapper.insert(item);
        }
    }

    private List<ArticleSummaryVO> articlesForSlot(HomeSlotType type) {
        List<Long> ids = homeArticleSlotMapper.selectList(new LambdaQueryWrapper<HomeArticleSlot>()
                        .eq(HomeArticleSlot::getSlotType, type.name()).orderByAsc(HomeArticleSlot::getSortOrder))
                .stream().map(HomeArticleSlot::getArticleId).toList();
        return articleService.listPublishedByIds(ids);
    }

    private List<String> idsForSlot(HomeSlotType type) {
        return homeArticleSlotMapper.selectList(new LambdaQueryWrapper<HomeArticleSlot>()
                        .eq(HomeArticleSlot::getSlotType, type.name()).orderByAsc(HomeArticleSlot::getSortOrder))
                .stream().map(item -> String.valueOf(item.getArticleId())).toList();
    }

    private void replaceSlots(HomeSlotType type, List<String> rawIds, int limit) {
        homeArticleSlotMapper.delete(new LambdaQueryWrapper<HomeArticleSlot>().eq(HomeArticleSlot::getSlotType, type.name()));
        List<String> ids = rawIds == null ? List.of() : rawIds.stream().distinct().limit(limit).toList();
        int order = 1;
        for (String rawId : ids) {
            long articleId = parseId(rawId);
            Article article = articleService.getById(articleId);
            ThrowUtils.throwIf(article == null || !ArticleStatus.PUBLISHED.name().equals(article.getStatus()),
                    ErrorCode.PARAMS_ERROR, "首页只能选择已发布文章");
            HomeArticleSlot slot = new HomeArticleSlot();
            slot.setSlotType(type.name());
            slot.setArticleId(articleId);
            slot.setSortOrder(order++);
            homeArticleSlotMapper.insert(slot);
        }
    }

    private Map<String, String> settingMap() {
        Map<String, String> result = new LinkedHashMap<>();
        siteSettingMapper.selectList(new LambdaQueryWrapper<SiteSetting>().orderByAsc(SiteSetting::getId))
                .forEach(setting -> result.put(setting.getSettingKey(), setting.getSettingValue()));
        return result;
    }

    private List<AboutItem> listAboutItems() {
        return aboutItemMapper.selectList(new LambdaQueryWrapper<AboutItem>()
                .orderByAsc(AboutItem::getItemType).orderByAsc(AboutItem::getSortOrder));
    }

    private long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new com.eric.blog.exception.BaseException(ErrorCode.PARAMS_ERROR, "文章 ID 格式不正确");
        }
    }

    private int parseNumber(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
