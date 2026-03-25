package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.AdminPermission;
import com.philitee.filter.entity.AdminRole;
import com.philitee.filter.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    @Select("SELECT * FROM admin_user WHERE username = #{username}")
    AdminUser selectByUsername(@Param("username") String username);

    @Select("SELECT r.* FROM admin_role r " +
            "INNER JOIN admin_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<AdminRole> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT p.* FROM admin_permission p " +
            "INNER JOIN admin_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN admin_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<AdminPermission> selectPermissionsByUserId(@Param("userId") Long userId);
}
