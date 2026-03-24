package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品分类实体类
 * 对应数据库表：product_category
 */
@Data
@TableName("product_category")
public class ProductCategory {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;

    /**
     * URL别名（SEO友好）
     */
    @TableField("slug")
    private String slug;

    /**
     * 分类描述
     */
    @TableField("description")
    private String description;

    /**
     * 父分类ID（0或NULL表示顶级分类）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类图片ID，关联image表
     */
    @TableField("image_id")
    private Long imageId;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    // ===== 非数据库字段 =====

    /**
     * 分类图片对象
     */
    @TableField(exist = false)
    private Image image;

    /**
     * 子分类列表
     */
    @TableField(exist = false)
    private List<ProductCategory> children;

    /**
     * 该分类下的产品数量
     */
    @TableField(exist = false)
    private Integer productCount;

}
