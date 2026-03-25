package com.philitee.filter.config;

import com.philitee.filter.entity.AdminUser;
import com.philitee.filter.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库的用户认证服务
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminUserService adminUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserService.getUserByUsername(username);
        if (adminUser == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        if (adminUser.getStatus() != null && adminUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加角色权限 (ROLE_ 前缀)
        if (adminUser.getRoles() != null) {
            adminUser.getRoles().forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()))
            );
        }

        // 添加细粒度权限
        if (adminUser.getPermissions() != null) {
            adminUser.getPermissions().forEach(perm ->
                    authorities.add(new SimpleGrantedAuthority(perm.getName()))
            );
        }

        return User.builder()
                .username(adminUser.getUsername())
                .password(adminUser.getPassword())
                .authorities(authorities)
                .build();
    }
}
