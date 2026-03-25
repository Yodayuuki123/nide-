package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@TableName("admin_permission")
public class AdminPermission {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String displayName;

    private String module;

    private LocalDateTime createdAt;
}
