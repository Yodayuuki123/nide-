package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.MenuItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单项 Mapper 接口
 */
@Mapper
public interface MenuItemMapper extends BaseMapper<MenuItem> {

    /**
     * 根据菜单ID查询所有菜单项（按排序字段排序）
     *
     * @param menuId 菜单ID
     * @return 菜单项列表
     */
    @Select("SELECT * FROM menu_item WHERE menu_id = #{menuId} ORDER BY sort_order ASC, id ASC")
    List<MenuItem> selectByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据菜单ID查询顶级菜单项
     *
     * @param menuId 菜单ID
     * @return 顶级菜单项列表
     */
    @Select("SELECT * FROM menu_item WHERE menu_id = #{menuId} AND (parent_id IS NULL OR parent_id = 0) ORDER BY sort_order ASC, id ASC")
    List<MenuItem> selectTopLevelByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据父菜单项ID查询子菜单项
     *
     * @param parentId 父菜单项ID
     * @return 子菜单项列表
     */
    @Select("SELECT * FROM menu_item WHERE parent_id = #{parentId} ORDER BY sort_order ASC, id ASC")
    List<MenuItem> selectByParentId(@Param("parentId") Long parentId);

}
