package com.eric.blog.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.eric.blog.common.PageResult;
import com.eric.blog.model.entity.MediaAsset;
import com.eric.blog.model.vo.admin.MediaVO;
import org.springframework.web.multipart.MultipartFile;

/** 媒体库业务服务。 */
public interface MediaService extends IService<MediaAsset> {
    MediaVO upload(MultipartFile file);
    PageResult<MediaVO> listMedia(long page, long pageSize);
    void deleteMedia(String id);
}
