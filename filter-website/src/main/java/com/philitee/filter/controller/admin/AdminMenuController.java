package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.philitee.filter.entity.Menu;
import com.philitee.filter.entity.MenuItem;
import com.philitee.filter.mapper.MenuItemMapper;
import com.philitee.filter.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台菜单管理 Controller
 */
@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;
    private final MenuItemMapper menuItemMapper;

    /**
     * 菜单列表
     */
    @GetMapping
    public String list(Model model) {
        List<Menu> menus = menuService.getAllMenus();
        // 为每个菜单加载菜单项以显示数量
        menus.forEach(menu -> {
            menu.setMenuItems(menuService.getMenuItemTree(menu.getId()));
        });
        model.addAttribute("menus", menus);
        return "admin/menu-list";
    }

    /**
     * 查看菜单详情（含菜单项树）
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Menu menu = menuService.getMenuWithItems(id);
        if (menu == null) {
            return "redirect:/admin/menus";
        }
        model.addAttribute("menu", menu);
        return "admin/menu-detail";
    }

    /**
     * 新增/更新菜单
     */
    @PostMapping("/save")
    public String saveMenu(Menu menu, RedirectAttributes redirectAttributes) {
        try {
            if (menu.getId() == null) {
                menu.setCreatedAt(LocalDateTime.now());
                menu.setUpdatedAt(LocalDateTime.now());
                menuService.save(menu);
                redirectAttributes.addFlashAttribute("message", "菜单创建成功");
            } else {
                menu.setUpdatedAt(LocalDateTime.now());
                menuService.updateById(menu);
                redirectAttributes.addFlashAttribute("message", "菜单更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/menus";
    }

    /**
     * 删除菜单（同时删除所有菜单项）
     */
    @PostMapping("/delete/{id}")
    public String deleteMenu(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 先删除该菜单下的所有菜单项
            LambdaQueryWrapper<MenuItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MenuItem::getMenuId, id);
            menuItemMapper.delete(wrapper);
            // 再删除菜单本身
            menuService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "菜单删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/menus";
    }

    /**
     * 保存菜单项
     */
    @PostMapping("/items/save")
    public String saveMenuItem(MenuItem menuItem, RedirectAttributes redirectAttributes) {
        try {
            if (menuItem.getId() == null) {
                menuItem.setCreatedAt(LocalDateTime.now());
                menuItem.setUpdatedAt(LocalDateTime.now());
                menuItemMapper.insert(menuItem);
                redirectAttributes.addFlashAttribute("message", "菜单项创建成功");
            } else {
                menuItem.setUpdatedAt(LocalDateTime.now());
                menuItemMapper.updateById(menuItem);
                redirectAttributes.addFlashAttribute("message", "菜单项更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/menus/" + menuItem.getMenuId();
    }

    /**
     * 删除菜单项
     */
    @PostMapping("/items/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id,
                                 @RequestParam Long menuId,
                                 RedirectAttributes redirectAttributes) {
        try {
            menuItemMapper.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "菜单项删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/menus/" + menuId;
    }

}
