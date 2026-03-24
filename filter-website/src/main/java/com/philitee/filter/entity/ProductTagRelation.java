package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 产品与标签关联实体类
 * 对应数据库表：product_tag_relation
 * 联合主键：product_id + tag_id
 */
@Data
@TableName("product_tag_relation")
public class ProductTagRelation {

    /**
     * 产品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 标签ID
     */
    @TableField("tag_id")
    private Long tagId;

}
