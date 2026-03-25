package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.AdminPermission;
import com.philitee.filter.entity.AdminRole;
import com.philitee.filter.entity.AdminUser;
import com.philitee.filter.mapper.AdminUserMapper;
import com.philitee.filter.mapper.AdminUserRoleMapper;
import com.philitee.filter.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    private final AdminUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminUser getUserByUsername(String username) {
        AdminUser user = baseMapper.selectByUsername(username);
        if (user != null) {
            List<AdminRole> roles = baseMapper.selectRolesByUserId(user.getId());
            user.setRoles(roles);
            List<AdminPermission> permissions = baseMapper.selectPermissionsByUserId(user.getId());
            user.setPermissions(permissions);
        }
        return user;
    }

    @Override
    public AdminUser getUserDetail(Long id) {
        AdminUser user = this.getById(id);
        if (user != null) {
            List<AdminRole> roles = baseMapper.selectRolesByUserId(user.getId());
            user.setRoles(roles);
        }
        return user;
    }

    @Override
    public IPage<AdminUser> getUserPage(int pageNum, int pageSize) {
        Page<AdminUser> page = new Page<>(pageNum, pageSize);
        IPage<AdminUser> result = this.page(page);
        result.getRecords().forEach(user -> {
            List<AdminRole> roles = baseMapper.selectRolesByUserId(user.getId());
            user.setRoles(roles);
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(AdminUser user, List<Long> roleIds) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        this.save(user);

        if (roleIds != null) {
            for (Long roleId : roleIds) {
                userRoleMapper.insertUserRole(user.getId(), roleId);
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(AdminUser user, List<Long> roleIds) {
        // 如果密码为空，不更新密码
        AdminUser existing = this.getById(user.getId());
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            user.setPassword(existing.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setUpdatedAt(LocalDateTime.now());
        this.updateById(user);

        // 重新分配角色
        if (roleIds != null) {
            userRoleMapper.deleteByUserId(user.getId());
            for (Long roleId : roleIds) {
                userRoleMapper.insertUserRole(user.getId(), roleId);
            }
        }
        return true;
    }

    @Override
    public void updateLastLoginTime(Long userId) {
        AdminUser user = new AdminUser();
        user.setId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);
    }
}
