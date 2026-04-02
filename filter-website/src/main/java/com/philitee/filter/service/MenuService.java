package com.philitee.filter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.Menu;
import com.philitee.filter.entity.MenuItem;

import java.util.List;

/**
 * 菜单 Service 接口
 */
public interface MenuService extends IService<Menu> {

    /**
     * 根据菜单ID获取完整菜单（包含层级菜单项）
     *
     * @param menuId 菜单ID
     * @return 菜单（含菜单项树）
     */
    Menu getMenuWithItems(Long menuId);

    /**
     * 根据菜单位置标识获取菜单
     *
     * @param location 位置标识
     * @return 菜单（含菜单项树）
     */
    Menu getMenuByLocation(String location);

    /**
     * 获取所有菜单列表
     *
     * @return 菜单列表
     */
    List<Menu> getAllMenus();

    /**
     * 获取菜单项树形结构
     *
     * @param menuId 菜单ID
     * @return 树形菜单项列表
     */
    List<MenuItem> getMenuItemTree(Long menuId);

    /**
     * 清除指定菜单的缓存（菜单项增删改后调用）
     *
     * @param menuId 菜单ID
     */
    void evictMenuCache(Long menuId);

}
