package com.eric.blog.storage;

import com.eric.blog.common.ErrorCode;
import com.eric.blog.config.StorageProperties;
import com.eric.blog.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/** 本地磁盘存储实现。所有路径在写入和删除前都进行规范化与根目录边界检查。 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "blog.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final StorageProperties properties;

    @Override
    public StorageObject store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(ErrorCode.FILE_ERROR, "请选择要上传的图片");
        }
        if (file.getSize() > properties.getMaxSize().toBytes()) {
            throw new BaseException(ErrorCode.FILE_ERROR, "图片大小不能超过 " + properties.getMaxSize().toMegabytes() + "MB");
        }
        try {
            byte[] content = file.getBytes();
            ImageType type = detectImageType(content);
            int[] dimensions = readAndValidateDimensions(content, type);
            LocalDate now = LocalDate.now();
            String storagePath = "%04d/%02d/%s.%s".formatted(now.getYear(), now.getMonthValue(),
                    UUID.randomUUID(), type.extension());
            // 统一通过配置对象解析根目录，避免 IDEA 工作目录改变导致文件写到仓库根目录。
            Path root = properties.resolveRoot();
            Path target = root.resolve(storagePath).normalize();
            // 即使未来 storagePath 的构造方式改变，也不能允许目标逃逸出上传根目录。
            if (!target.startsWith(root)) {
                throw new BaseException(ErrorCode.FILE_ERROR, "非法文件路径");
            }
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return new StorageObject(storagePath, getPublicUrl(storagePath), type.mimeType(), content.length,
                    dimensions[0], dimensions[1]);
        } catch (IOException exception) {
            throw new BaseException(ErrorCode.OPERATION_ERROR, "图片保存失败");
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path root = properties.resolveRoot();
            Path target = root.resolve(storagePath).normalize();
            if (!target.startsWith(root)) {
                throw new BaseException(ErrorCode.FILE_ERROR, "非法文件路径");
            }
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new BaseException(ErrorCode.OPERATION_ERROR, "图片删除失败");
        }
    }

    @Override
    public String getPublicUrl(String storagePath) {
        return "/uploads/" + storagePath.replace('\\', '/');
    }

    private ImageType detectImageType(byte[] bytes) {
        if (bytes.length >= 3 && unsigned(bytes[0]) == 0xFF && unsigned(bytes[1]) == 0xD8 && unsigned(bytes[2]) == 0xFF) {
            return new ImageType("jpg", "image/jpeg");
        }
        if (bytes.length >= 8 && unsigned(bytes[0]) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
            return new ImageType("png", "image/png");
        }
        if (bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8') {
            return new ImageType("gif", "image/gif");
        }
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return new ImageType("webp", "image/webp");
        }
        throw new BaseException(ErrorCode.FILE_ERROR, "仅支持 JPEG、PNG、WebP 和 GIF 图片");
    }

    private int[] readAndValidateDimensions(byte[] content, ImageType type) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image != null) {
                return new int[]{image.getWidth(), image.getHeight()};
            }
            // JDK 默认没有 WebP ImageIO 插件，因此 WebP 通过 RIFF/WEBP 魔数校验后允许保留未知尺寸。
            if ("webp".equals(type.extension())) {
                return new int[]{0, 0};
            }
            throw new BaseException(ErrorCode.FILE_ERROR, "图片内容已损坏或格式不完整");
        } catch (IOException exception) {
            throw new BaseException(ErrorCode.FILE_ERROR, "图片内容无法解析");
        }
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private record ImageType(String extension, String mimeType) {
    }
}
