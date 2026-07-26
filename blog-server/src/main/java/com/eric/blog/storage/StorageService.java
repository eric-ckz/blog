package com.eric.blog.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象。业务层只依赖该接口，未来接入 MinIO/S3 时无需修改 Controller 和媒体服务。
 */
public interface StorageService {

    /** 校验并保存上传文件。 */
    StorageObject store(MultipartFile file);

    /** 根据内部相对路径删除文件。 */
    void delete(String storagePath);

    /** 将内部相对路径转换成当前存储实现的公开 URL。 */
    String getPublicUrl(String storagePath);
}
