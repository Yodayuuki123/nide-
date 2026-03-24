package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单项实体类
 * 对应数据库表：menu_item
 */
@Data
@TableName("menu_item")
public class MenuItem {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属菜单ID
     */
    @TableField("menu_id")
    private Long menuId;

    /**
     * 父菜单项ID（0或NULL表示顶级）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 菜单项标题
     */
    @TableField("title")
    private String title;

    /**
     * 菜单项链接URL
     */
    @TableField("url")
    private String url;

    /**
     * 菜单项类型（custom / post_type / taxonomy）
     */
    @TableField("type")
    private String type;

    /**
     * 关联对象ID（如分类ID、产品ID等）
     */
    @TableField("object_id")
    private Long objectId;

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

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 子菜单项列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<MenuItem> children;

}
