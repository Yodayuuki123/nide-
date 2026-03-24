package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 产品实体类
 * 对应数据库表：product
 */
@Data
@TableName("product")
public class Product {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称
     */
    @TableField("name")
    private String name;

    /**
     * URL别名（SEO友好）
     */
    @TableField("slug")
    private String slug;

    /**
     * 产品详细描述（HTML片段）
     */
    @TableField("description")
    private String description;

    /**
     * 产品简短描述（HTML片段）
     */
    @TableField("short_description")
    private String shortDescription;

    /**
     * 市场价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 促销价格
     */
    @TableField("sale_price")
    private BigDecimal salePrice;

    /**
     * 产品SKU编号
     */
    @TableField("sku")
    private String sku;

    /**
     * 库存数量
     */
    @TableField("stock_quantity")
    private Integer stockQuantity;

    /**
     * 产品状态（publish / draft）
     */
    @TableField("status")
    private String status;

    /**
     * 主图片ID，关联image表
     */
    @TableField("main_image_id")
    private Long mainImageId;

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

    // ===== 非数据库字段（关联查询用） =====

    /**
     * 主图片对象
     */
    @TableField(exist = false)
    private Image mainImage;

    /**
     * 产品图片列表
     */
    @TableField(exist = false)
    private List<Image> images;

    /**
     * 产品分类列表
     */
    @TableField(exist = false)
    private List<ProductCategory> categories;

    /**
     * 产品标签列表
     */
    @TableField(exist = false)
    private List<ProductTag> tags;

}
