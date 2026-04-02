package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.AdminPermission;
import com.philitee.filter.entity.AdminRole;
import com.philitee.filter.mapper.AdminPermissionMapper;
import com.philitee.filter.mapper.AdminRoleMapper;
import com.philitee.filter.service.AdminRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoleServiceImpl extends ServiceImpl<AdminRoleMapper, AdminRole> implements AdminRoleService {

    private final AdminPermissionMapper permissionMapper;

    @Override
    public List<AdminRole> getAllRolesWithPermissions() {
        List<AdminRole> roles = this.list();
        roles.forEach(role -> {
            List<AdminPermission> permissions = baseMapper.selectPermissionsByRoleId(role.getId());
            role.setPermissions(permissions);
        });
        return roles;
    }

    @Override
    public AdminRole getRoleDetail(Long id) {
        AdminRole role = this.getById(id);
        if (role != null) {
            List<AdminPermission> permissions = baseMapper.selectPermissionsByRoleId(role.getId());
            role.setPermissions(permissions);
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRole(AdminRole role, List<Long> permissionIds) {
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        this.save(role);

        if (permissionIds != null) {
            for (Long permId : permissionIds) {
                baseMapper.insertRolePermission(role.getId(), permId);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(AdminRole role, List<Long> permissionIds) {
        role.setUpdatedAt(LocalDateTime.now());
        this.updateById(role);

        // 重新分配权限（permissionIds为null表示全部取消勾选，也需要清理）
        baseMapper.deletePermissionsByRoleId(role.getId());
        if (permissionIds != null) {
            for (Long permId : permissionIds) {
                baseMapper.insertRolePermission(role.getId(), permId);
            }
        }
        return true;
    }

    @Override
    public List<AdminPermission> getAllPermissions() {
        return permissionMapper.selectList(null);
    }

    @Override
    public Map<String, List<AdminPermission>> getPermissionsByModule() {
        List<AdminPermission> all = permissionMapper.selectList(null);
        // 按模块分组，保持插入顺序
        return all.stream().collect(Collectors.groupingBy(
                AdminPermission::getModule,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }
}
