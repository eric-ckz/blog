package com.eric.blog.storage;

/** 存储实现类型。MINIO 与 S3 当前仅保留配置枚举和接口扩展点。 */
public enum StorageType {
    LOCAL,
    MINIO,
    S3
}
