package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.common.PageResult;
import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.AboutItemMapper;
import com.eric.blog.mapper.ArticleMapper;
import com.eric.blog.mapper.MediaAssetMapper;
import com.eric.blog.mapper.SiteSettingMapper;
import com.eric.blog.model.entity.AboutItem;
import com.eric.blog.model.entity.Article;
import com.eric.blog.model.entity.MediaAsset;
import com.eric.blog.model.entity.SiteSetting;
import com.eric.blog.model.vo.admin.MediaVO;
import com.eric.blog.service.MediaService;
import com.eric.blog.storage.StorageObject;
import com.eric.blog.storage.StorageService;
import com.eric.blog.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** 媒体库实现，负责协调二进制存储和数据库元数据。 */
@Service
@RequiredArgsConstructor
public class MediaServiceImpl extends ServiceImpl<MediaAssetMapper, MediaAsset> implements MediaService {

    private final StorageService storageService;
    private final ArticleMapper articleMapper;
    private final SiteSettingMapper siteSettingMapper;
    private final AboutItemMapper aboutItemMapper;

    @Override
    @Transactional
    public MediaVO upload(MultipartFile file) {
        StorageObject stored = storageService.store(file);
        MediaAsset asset = new MediaAsset();
        asset.setOriginalName(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        asset.setStoragePath(stored.storagePath());
        asset.setPublicUrl(stored.publicUrl());
        asset.setMimeType(stored.mimeType());
        asset.setSizeBytes(stored.size());
        asset.setWidth(stored.width());
        asset.setHeight(stored.height());
        if (!save(asset)) {
            // 数据库写入失败时立即回收刚保存的孤儿文件。
            storageService.delete(stored.storagePath());
            throw new BaseException(ErrorCode.OPERATION_ERROR, "媒体记录保存失败");
        }
        return toVO(asset);
    }

    @Override
    public PageResult<MediaVO> listMedia(long pageNumber, long pageSize) {
        Page<MediaAsset> page = page(new Page<>(Math.max(pageNumber, 1), Math.min(Math.max(pageSize, 1), 100)),
                new LambdaQueryWrapper<MediaAsset>().orderByDesc(MediaAsset::getCreateTime));
        List<MediaVO> items = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.from(page, items);
    }

    @Override
    @Transactional
    public void deleteMedia(String id) {
        long mediaId = parseId(id);
        MediaAsset asset = getById(mediaId);
        ThrowUtils.throwIf(asset == null, ErrorCode.NOT_FOUND_ERROR, "媒体不存在");
        boolean usedByArticle = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getCoverMediaId, mediaId)
                .or().eq(Article::getCoverUrl, asset.getPublicUrl())
                .or().like(Article::getContentHtml, asset.getPublicUrl())) > 0;
        boolean usedBySetting = siteSettingMapper.selectCount(new LambdaQueryWrapper<SiteSetting>()
                .like(SiteSetting::getSettingValue, asset.getPublicUrl())) > 0;
        boolean usedByAbout = aboutItemMapper.selectCount(new LambdaQueryWrapper<AboutItem>()
                .like(AboutItem::getDescription, asset.getPublicUrl())) > 0;
        ThrowUtils.throwIf(usedByArticle || usedBySetting || usedByAbout, ErrorCode.CONFLICT_ERROR,
                "图片正在被文章或站点内容使用，不能删除");
        storageService.delete(asset.getStoragePath());
        ThrowUtils.throwIf(!removeById(mediaId), ErrorCode.OPERATION_ERROR, "媒体记录删除失败");
    }

    private MediaVO toVO(MediaAsset asset) {
        return MediaVO.builder()
                .id(String.valueOf(asset.getId()))
                .originalName(asset.getOriginalName())
                .url(asset.getPublicUrl())
                .mimeType(asset.getMimeType())
                .size(asset.getSizeBytes())
                .width(asset.getWidth())
                .height(asset.getHeight())
                .createdAt(asset.getCreateTime() == null ? null : asset.getCreateTime().toString())
                .build();
    }

    private long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "媒体 ID 格式不正确");
        }
    }
}
