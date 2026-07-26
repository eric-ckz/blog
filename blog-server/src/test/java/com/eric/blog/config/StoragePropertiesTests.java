package com.eric.blog.config;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** 存储目录解析测试，防止 IDEA 工作目录变化后上传文件写入错误位置。 */
class StoragePropertiesTests {

    /** 测试文件放入 Maven target，避免 Windows 环境无法清理系统临时目录。 */
    private final Path testDirectory = Path.of("target", "storage-properties-tests").toAbsolutePath().normalize();

    @Test
    void shouldResolveRelativeRootAgainstServerModuleWhenStartedFromRepositoryRoot() throws Exception {
        Path repositoryDirectory = testDirectory.resolve("repository-root");
        Path serverDirectory = Files.createDirectories(repositoryDirectory.resolve("blog-server"));
        StorageProperties properties = new StorageProperties();
        properties.setRoot(Path.of("./uploads"));

        assertThat(properties.resolveRoot(repositoryDirectory))
                .isEqualTo(serverDirectory.resolve("uploads").toAbsolutePath().normalize());
    }

    @Test
    void shouldResolveRelativeRootAgainstCurrentDirectoryWhenStartedFromServerModule() {
        Path serverDirectory = testDirectory.resolve("module-root").resolve("blog-server");
        StorageProperties properties = new StorageProperties();
        properties.setRoot(Path.of("./uploads"));

        assertThat(properties.resolveRoot(serverDirectory))
                .isEqualTo(serverDirectory.resolve("uploads").toAbsolutePath().normalize());
    }

    @Test
    void shouldKeepConfiguredAbsoluteRoot() {
        Path absoluteRoot = testDirectory.resolve("external-uploads").toAbsolutePath().normalize();
        StorageProperties properties = new StorageProperties();
        properties.setRoot(absoluteRoot);

        assertThat(properties.resolveRoot(testDirectory))
                .isEqualTo(absoluteRoot);
    }
}
