package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色实体
 */
@Data
@TableName("admin_role")
public class AdminRole {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String displayName;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ===== 非数据库字段 =====

    @TableField(exist = false)
    private List<AdminPermission> permissions;
}
