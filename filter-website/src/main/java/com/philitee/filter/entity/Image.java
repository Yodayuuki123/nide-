package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片实体类
 * 对应数据库表：image
 */
@Data
@TableName("image")
public class Image {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件相对路径（如 /08/Filter-bag-P84-100x100-1.jpg）
     */
    @TableField("file_path")
    private String filePath;

    /**
     * 图片完整URL
     */
    @TableField("url")
    private String url;

    /**
     * 图片alt文本（SEO用）
     */
    @TableField("alt_text")
    private String altText;

    /**
     * 图片宽度（像素）
     */
    @TableField("width")
    private Integer width;

    /**
     * 图片高度（像素）
     */
    @TableField("height")
    private Integer height;

    /**
     * MIME类型（如 image/jpeg）
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 上传时间
     */
    @TableField("uploaded_at")
    private LocalDateTime uploadedAt;

    /**
     * 元数据（WordPress序列化格式，包含缩略图信息等）
     */
    @TableField("meta_value")
    private String metaValue;

}
