package com.philitee.filter.config;

import com.philitee.filter.entity.Menu;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.service.MenuService;
import com.philitee.filter.service.ProductCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.List;

/**
 * 全局模型属性注入
 * 将导航菜单、分类树、当前URI等公共数据注入到所有前台页面
 */
@ControllerAdvice(basePackages = "com.philitee.filter.controller.front")
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final MenuService menuService;
    private final ProductCategoryService categoryService;

    /**
     * 注入公共属性到所有前台页面
     */
    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        // 注入当前请求URI，用于导航高亮
        model.addAttribute("currentUrl", request.getRequestURI());
        model.addAttribute("currentUri", request.getRequestURI());

        try {
            // 获取所有菜单（前台可根据location选择使用哪个菜单）
            List<Menu> menus = menuService.getAllMenus();
            model.addAttribute("menus", menus);

            // 注入分类树（用于导航下拉菜单、侧边栏或页脚）
            List<ProductCategory> categoryTree = categoryService.getCategoryTree();
            model.addAttribute("globalCategoryTree", categoryTree);
        } catch (Exception e) {
            // 数据库异常时不影响页面渲染
        }
    }

}
