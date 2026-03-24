package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.Menu;
import com.philitee.filter.entity.MenuItem;
import com.philitee.filter.mapper.MenuItemMapper;
import com.philitee.filter.mapper.MenuMapper;
import com.philitee.filter.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    private final MenuItemMapper menuItemMapper;

    @Override
    @Cacheable(value = "menuWithItems", key = "#menuId", unless = "#result == null")
    public Menu getMenuWithItems(Long menuId) {
        Menu menu = this.getById(menuId);
        if (menu != null) {
            List<MenuItem> tree = getMenuItemTree(menuId);
            menu.setMenuItems(tree);
        }
        return menu;
    }

    @Override
    public Menu getMenuByLocation(String location) {
        LambdaQueryWrapper<Menu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Menu::getLocation, location);
        Menu menu = this.getOne(wrapper);
        if (menu != null) {
            List<MenuItem> tree = getMenuItemTree(menu.getId());
            menu.setMenuItems(tree);
        }
        return menu;
    }

    @Override
    @Cacheable(value = "allMenus", unless = "#result == null")
    public List<Menu> getAllMenus() {
        return this.list();
    }

    @Override
    public List<MenuItem> getMenuItemTree(Long menuId) {
        // 查询该菜单下所有菜单项
        List<MenuItem> allItems = menuItemMapper.selectByMenuId(menuId);

        // 构建树形结构
        Map<Long, List<MenuItem>> childrenMap = allItems.stream()
                .filter(item -> item.getParentId() != null && item.getParentId() > 0)
                .collect(Collectors.groupingBy(MenuItem::getParentId));

        // 设置子菜单
        allItems.forEach(item -> {
            List<MenuItem> children = childrenMap.getOrDefault(item.getId(), new ArrayList<>());
            item.setChildren(children);
        });

        // 返回顶级菜单项
        return allItems.stream()
                .filter(item -> item.getParentId() == null || item.getParentId() == 0)
                .collect(Collectors.toList());
    }

}
