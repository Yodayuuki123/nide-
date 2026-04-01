package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Menu;
import com.philitee.filter.entity.MenuItem;
import com.philitee.filter.mapper.MenuItemMapper;
import com.philitee.filter.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;
    private final MenuItemMapper menuItemMapper;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String location,
                       Model model) {
        QueryWrapper<Menu> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like("name", keyword);
        }
        if (StringUtils.hasText(location)) {
            wrapper.eq("location", location);
        }
        wrapper.orderByDesc("COALESCE(updated_at, created_at)");

        IPage<Menu> menuPage = menuService.page(new Page<>(page, size), wrapper);
        menuPage.getRecords().forEach(menu -> menu.setMenuItems(menuService.getMenuItemTree(menu.getId())));
        model.addAttribute("menuPage", menuPage);
        return "admin/menu-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Menu menu = menuService.getMenuWithItems(id);
        if (menu == null) {
            return "redirect:/admin/menus";
        }
        model.addAttribute("menu", menu);
        return "admin/menu-detail";
    }

    @PostMapping("/save")
    public String saveMenu(Menu menu, RedirectAttributes redirectAttributes) {
        try {
            if (menu.getId() == null) {
                menu.setCreatedAt(LocalDateTime.now());
                menu.setUpdatedAt(LocalDateTime.now());
                menuService.save(menu);
                redirectAttributes.addFlashAttribute("message", "Menu created successfully");
            } else {
                menu.setUpdatedAt(LocalDateTime.now());
                menuService.updateById(menu);
                redirectAttributes.addFlashAttribute("message", "Menu updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Operation failed: " + e.getMessage());
        }
        return "redirect:/admin/menus";
    }

    @PostMapping("/delete/{id}")
    public String deleteMenu(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            QueryWrapper<MenuItem> wrapper = new QueryWrapper<>();
            wrapper.eq("menu_id", id);
            menuItemMapper.delete(wrapper);
            menuService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "Menu deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/menus";
    }

    @PostMapping("/items/save")
    public String saveMenuItem(MenuItem menuItem, RedirectAttributes redirectAttributes) {
        try {
            if (menuItem.getParentId() != null && menuItem.getParentId() == 0L) {
                menuItem.setParentId(null);
            }
            if (menuItem.getId() == null) {
                menuItem.setCreatedAt(LocalDateTime.now());
                menuItem.setUpdatedAt(LocalDateTime.now());
                menuItemMapper.insert(menuItem);
                redirectAttributes.addFlashAttribute("message", "Menu item created successfully");
            } else {
                menuItem.setUpdatedAt(LocalDateTime.now());
                menuItemMapper.updateById(menuItem);
                redirectAttributes.addFlashAttribute("message", "Menu item updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Operation failed: " + e.getMessage());
        }
        return "redirect:/admin/menus/" + menuItem.getMenuId();
    }

    @PostMapping("/items/delete/{id}")
    public String deleteMenuItem(@PathVariable Long id,
                                 @RequestParam Long menuId,
                                 RedirectAttributes redirectAttributes) {
        try {
            menuItemMapper.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Menu item deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/menus/" + menuId;
    }
}