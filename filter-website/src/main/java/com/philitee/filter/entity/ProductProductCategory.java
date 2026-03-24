package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 产品与分类关联实体类
 * 对应数据库表：product_product_category
 * 注意：该表无主键，无自增ID
 */
@Data
@TableName("product_product_category")
public class ProductProductCategory {

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

}
