package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.AdminUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminUserRoleMapper extends BaseMapper<AdminUserRole> {

    @Delete("DELETE FROM admin_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO admin_user_role (user_id, role_id) VALUES (#{userId}, #{roleId})")
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
