package com.philitee.filter.controller.admin;

import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台分类管理 Controller
 */
@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 分类列表（树形）
     */
    @GetMapping
    public String list(Model model) {
        List<ProductCategory> categoryTree = categoryService.getCategoryTree();
        model.addAttribute("categoryTree", categoryTree);
        return "admin/category-list";
    }

    /**
     * 新增分类页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new ProductCategory());
        model.addAttribute("parentCategories", categoryService.getTopLevelCategories());
        return "admin/category-form";
    }

    /**
     * 编辑分类页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductCategory category = categoryService.getById(id);
        if (category == null) {
            return "redirect:/admin/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("parentCategories", categoryService.getTopLevelCategories());
        return "admin/category-form";
    }

    /**
     * 保存分类
     */
    @PostMapping("/save")
    public String save(ProductCategory category, RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (category.getId() == null) {
                if (category.getCreatedAt() == null) category.setCreatedAt(now);
                category.setUpdatedAt(now);
                categoryService.save(category);
                redirectAttributes.addFlashAttribute("message", "分类创建成功");
            } else {
                category.setUpdatedAt(now);
                categoryService.updateById(category);
                redirectAttributes.addFlashAttribute("message", "分类更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * 删除分类
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "分类删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

}
