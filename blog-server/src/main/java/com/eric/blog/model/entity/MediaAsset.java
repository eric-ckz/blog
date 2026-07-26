package com.eric.blog.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 媒体库文件元数据，真实二进制内容由 StorageService 管理。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("media_asset")
public class MediaAsset extends BaseEntity {

    /** 上传时的原文件名，仅用于管理端展示。 */
    private String originalName;

    /** 存储实现内部使用的相对路径。 */
    private String storagePath;

    /** 可直接被用户端访问的 URL。 */
    private String publicUrl;

    /** 经过内容检测后的 MIME 类型。 */
    private String mimeType;

    /** 文件字节数。 */
    private Long sizeBytes;

    /** 图片宽度；无法读取时为 0。 */
    private Integer width;

    /** 图片高度；无法读取时为 0。 */
    private Integer height;
}
