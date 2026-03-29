package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.Image;
import com.philitee.filter.mapper.ImageMapper;
import com.philitee.filter.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 图片 Service 实现类
 */
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    @Value("${upload.base-path}")
    private String uploadBasePath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    /** 允许上传的文件扩展名（图片 + 视频） */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            // 图片格式
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff",
            // 视频格式
            "mp4", "webm", "avi", "mov", "mkv"
    ));

    /** 允许上传的MIME类型（图片 + 视频） */
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            // 图片类型
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
            "image/svg+xml", "image/x-icon", "image/tiff",
            // 视频类型
            "video/mp4", "video/webm", "video/avi", "video/quicktime", "video/x-matroska"
    ));

    @Override
    public List<Image> getImagesByProductId(Long productId) {
        return baseMapper.selectByProductId(productId);
    }

    @Override
    public Image uploadImage(MultipartFile file) {
        // 校验文件扩展名（支持图片和视频）
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                throw new RuntimeException("不支持的文件格式: " + ext
                        + "，允许的格式: " + ALLOWED_EXTENSIONS);
            }
        }
        // 校验Content-Type（支持图片和视频）
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new RuntimeException("不支持的文件类型: " + contentType);
        }

        // 按WordPress规则生成存储路径：/年/月/文件名
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String dirPath = uploadBasePath + "/" + yearMonth;

        // 创建目录
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名（保留原始文件名，避免重复加UUID前缀）
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "-" + originalFilename;

        // 保存文件
        File destFile = new File(dir, fileName);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }

        // 构建Image实体
        Image image = new Image();
        image.setFileName(fileName);
        image.setFilePath("/" + yearMonth + "/" + fileName);
        image.setUrl(urlPrefix + "/" + yearMonth + "/" + fileName);
        image.setMimeType(file.getContentType());
        image.setUploadedAt(LocalDateTime.now());

        // 保存到数据库
        this.save(image);

        return image;
    }

    @Override
    public boolean deleteImage(Long id) {
        Image image = this.getById(id);
        if (image == null) {
            return false;
        }

        // 删除物理文件
        String physicalPath = uploadBasePath + image.getFilePath();
        File file = new File(physicalPath);
        if (file.exists()) {
            file.delete();
        }

        // 删除数据库记录
        return this.removeById(id);
    }

}
