package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.common.PageResult;
import com.eric.blog.model.vo.admin.MediaVO;
import com.eric.blog.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 管理端媒体库接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/media")
public class AdminMediaController {

    private final MediaService mediaService;

    @GetMapping
    public BaseResponse<PageResult<MediaVO>> list(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "24") long pageSize) {
        return BaseResponse.success(mediaService.listMedia(page, pageSize));
    }

    @PostMapping
    public BaseResponse<MediaVO> upload(@RequestParam("file") MultipartFile file) {
        return BaseResponse.success(mediaService.upload(file));
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable String id) {
        mediaService.deleteMedia(id);
        return BaseResponse.success();
    }
}
