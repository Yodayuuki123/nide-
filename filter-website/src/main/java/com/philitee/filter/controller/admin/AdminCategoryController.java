package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final ProductCategoryService categoryService;
    private final CacheManager cacheManager;
    private final com.philitee.filter.mapper.ProductProductCategoryMapper productCategoryMapper;

    private void evictCategoryCache() {
        try {
            if (cacheManager.getCache("categoryTree") != null)
                cacheManager.getCache("categoryTree").clear();
            if (cacheManager.getCache("topLevelCategories") != null)
                cacheManager.getCache("topLevelCategories").clear();
        } catch (Exception ignored) {}
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       Model model) {
        QueryWrapper<ProductCategory> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like("name", keyword);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge("created_at", LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le("created_at", LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc("COALESCE(updated_at, created_at)");

        IPage<ProductCategory> categoryPage = categoryService.page(new Page<>(page, size), wrapper);
        List<ProductCategory> allCategories = categoryService.list();
        Map<Long, String> parentNameMap = allCategories.stream()
                .collect(Collectors.toMap(ProductCategory::getId, ProductCategory::getName, (first, second) -> first));
        Map<Long, Integer> productCountMap = categoryService.getCategoryTree().stream()
                .flatMap(cat -> {
                    List<ProductCategory> children = cat.getChildren();
                    return children == null || children.isEmpty()
                            ? java.util.stream.Stream.of(cat)
                            : java.util.stream.Stream.concat(java.util.stream.Stream.of(cat), children.stream());
                })
                .collect(Collectors.toMap(ProductCategory::getId, cat -> cat.getProductCount() != null ? cat.getProductCount() : 0, (first, second) -> first));

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("parentNameMap", parentNameMap);
        model.addAttribute("productCountMap", productCountMap);
        return "admin/category-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new ProductCategory());
        model.addAttribute("parentCategories", categoryService.getTopLevelCategories());
        return "admin/category-form";
    }

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

    @PostMapping("/save")
    public String save(ProductCategory category, RedirectAttributes redirectAttributes) {
        try {
            if (category.getParentId() != null && category.getParentId() == 0L) {
                category.setParentId(null);
            }
            if (category.getId() == null) {
                category.setCreatedAt(LocalDateTime.now());
                category.setUpdatedAt(LocalDateTime.now());
                categoryService.save(category);
                evictCategoryCache();
                redirectAttributes.addFlashAttribute("message", "Category created successfully");
            } else {
                category.setUpdatedAt(LocalDateTime.now());
                categoryService.updateById(category);
                evictCategoryCache();
                redirectAttributes.addFlashAttribute("message", "Category updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Operation failed: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // 先清理产品-分类关联表数据
            productCategoryMapper.deleteByCategoryId(id);
            categoryService.removeById(id);
            evictCategoryCache();
            redirectAttributes.addFlashAttribute("message", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}