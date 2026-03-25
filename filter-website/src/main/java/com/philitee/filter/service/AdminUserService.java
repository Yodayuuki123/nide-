package com.philitee.filter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.AdminUser;

import java.util.List;

/**
 * 管理员用户 Service 接口
 */
public interface AdminUserService extends IService<AdminUser> {

    /**
     * 根据用户名查询用户（含角色和权限）
     */
    AdminUser getUserByUsername(String username);

    /**
     * 根据ID查询用户详情（含角色）
     */
    AdminUser getUserDetail(Long id);

    /**
     * 分页查询用户列表
     */
    IPage<AdminUser> getUserPage(int pageNum, int pageSize);

    /**
     * 创建用户并分配角色
     */
    boolean createUser(AdminUser user, List<Long> roleIds);

    /**
     * 更新用户并重新分配角色
     */
    boolean updateUser(AdminUser user, List<Long> roleIds);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);
}
