package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品图片关联实体类
 * 对应数据库表：product_image
 */
@Data
@TableName("product_image")
public class ProductImage {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 图片ID
     */
    @TableField("image_id")
    private Long imageId;

    /**
     * 排序字段
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
