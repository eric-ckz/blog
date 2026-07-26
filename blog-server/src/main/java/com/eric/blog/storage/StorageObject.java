package com.eric.blog.storage;

/** StorageService 保存成功后的文件描述。 */
public record StorageObject(
        String storagePath,
        String publicUrl,
        String mimeType,
        long size,
        int width,
        int height) {
}
