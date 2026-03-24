package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单组实体类
 * 对应数据库表：menu
 */
@Data
@TableName("menu")
public class Menu {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 菜单名称
     */
    @TableField("name")
    private String name;

    /**
     * 菜单位置标识（如 custom_27）
     */
    @TableField("location")
    private String location;

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
     * 菜单项列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<MenuItem> menuItems;

}
