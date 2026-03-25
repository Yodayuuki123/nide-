package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户实体
 */
@Data
@TableName("admin_user")
public class AdminUser {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String email;

    private String phone;

    private String avatar;

    /**
     * 状态：1=启用，0=禁用
     */
    private Integer status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ===== 非数据库字段 =====

    @TableField(exist = false)
    private List<AdminRole> roles;

    @TableField(exist = false)
    private List<AdminPermission> permissions;
}
