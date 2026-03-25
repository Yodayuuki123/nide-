package com.philitee.filter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.AdminPermission;
import com.philitee.filter.entity.AdminRole;

import java.util.List;

/**
 * 角色 Service 接口
 */
public interface AdminRoleService extends IService<AdminRole> {

    /**
     * 获取所有角色（含权限列表）
     */
    List<AdminRole> getAllRolesWithPermissions();

    /**
     * 获取角色详情（含权限列表）
     */
    AdminRole getRoleDetail(Long id);

    /**
     * 保存角色并分配权限
     */
    boolean saveRole(AdminRole role, List<Long> permissionIds);

    /**
     * 更新角色并重新分配权限
     */
    boolean updateRole(AdminRole role, List<Long> permissionIds);

    /**
     * 获取所有权限列表
     */
    List<AdminPermission> getAllPermissions();

    /**
     * 按模块分组获取权限
     */
    java.util.Map<String, List<AdminPermission>> getPermissionsByModule();
}
