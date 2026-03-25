package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.AdminPermission;
import com.philitee.filter.entity.AdminRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminRoleMapper extends BaseMapper<AdminRole> {

    @Select("SELECT p.* FROM admin_permission p " +
            "INNER JOIN admin_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    List<AdminPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    @Delete("DELETE FROM admin_role_permission WHERE role_id = #{roleId}")
    int deletePermissionsByRoleId(@Param("roleId") Long roleId);

    @Insert("INSERT INTO admin_role_permission (role_id, permission_id) VALUES (#{roleId}, #{permissionId})")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
