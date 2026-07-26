package com.eric.blog.config;

import com.eric.blog.storage.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Files;
import java.nio.file.Path;

/** 本地及未来对象存储的统一入口配置。 */
@Data
@ConfigurationProperties(prefix = "blog.storage")
public class StorageProperties {
    private StorageType type = StorageType.LOCAL;
    private Path root = Path.of("./uploads");
    private DataSize maxSize = DataSize.ofMegabytes(10);

    /**
     * 返回已经完成模块目录解析的存储根目录。
     *
     * <p>显式覆盖 Lombok 生成的普通 getter，防止后续代码再次拿到原始相对路径后直接调用
     * {@code toAbsolutePath()}，从而重现仓库根目录偏移问题。</p>
     *
     * @return 规范化后的本地存储绝对路径
     */
    public Path getRoot() {
        return resolveRoot();
    }

    /**
     * 获取本地存储的最终绝对路径。
     *
     * <p>{@link Path#toAbsolutePath()} 会直接以 JVM 工作目录为基准。在 IDEA 从仓库根目录
     * 启动时，这会把 {@code ./uploads} 错误解析成 {@code blog/uploads}。这里把相对路径统一
     * 绑定到 {@code blog-server} 模块目录；若生产环境传入绝对路径，则完全尊重显式配置。</p>
     *
     * @return 规范化后的本地存储绝对路径
     */
    public Path resolveRoot() {
        return resolveRoot(Path.of(System.getProperty("user.dir")));
    }

    /**
     * 基于指定工作目录解析存储路径，独立方法便于覆盖不同 IDEA/Maven 启动方式的测试。
     *
     * @param workingDirectory JVM 当前工作目录
     * @return 规范化后的本地存储绝对路径
     */
    Path resolveRoot(Path workingDirectory) {
        Path configuredRoot = root.normalize();
        if (configuredRoot.isAbsolute()) {
            return configuredRoot;
        }

        Path normalizedWorkingDirectory = workingDirectory.toAbsolutePath().normalize();
        Path serverDirectory = locateServerDirectory(normalizedWorkingDirectory);
        return serverDirectory.resolve(configuredRoot).normalize();
    }

    /**
     * 同时兼容两种常用启动目录：仓库根目录 {@code blog} 和后端模块目录 {@code blog-server}。
     * 其他部署目录不会被强行改写，仍按该目录下的相对路径处理。
     */
    private Path locateServerDirectory(Path workingDirectory) {
        Path fileName = workingDirectory.getFileName();
        if (fileName != null && "blog-server".equals(fileName.toString())) {
            return workingDirectory;
        }

        Path nestedServerDirectory = workingDirectory.resolve("blog-server");
        if (Files.isDirectory(nestedServerDirectory)) {
            return nestedServerDirectory;
        }
        return workingDirectory;
    }
}
