package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Product;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.entity.ProductTag;
import com.philitee.filter.service.ProductCategoryService;
import com.philitee.filter.service.ProductService;
import com.philitee.filter.service.ProductTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 后台产品管理 Controller
 */
@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final ProductTagService tagService;

    /**
     * 产品列表
     */
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        IPage<Product> productPage = productService.page(
                new Page<>(page, size),
                null
        );
        // 填充主图
        productPage.getRecords().forEach(p -> {
            Product detail = productService.getProductDetail(p.getId());
            p.setMainImage(detail.getMainImage());
            p.setCategories(detail.getCategories());
        });

        model.addAttribute("productPage", productPage);
        return "admin/product-list";
    }

    /**
     * 新增产品页面
     */
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getCategoryTree());
        model.addAttribute("tags", tagService.getAllTags());
        return "admin/product-form";
    }

    /**
     * 编辑产品页面
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductDetail(id);
        if (product == null) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getCategoryTree());
        model.addAttribute("tags", tagService.getAllTags());
        return "admin/product-form";
    }

    /**
     * 保存产品（新增或更新）
     */
    @PostMapping("/save")
    public String save(
            Product product,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) List<Long> imageIds,
            RedirectAttributes redirectAttributes) {

        try {
            if (product.getId() == null) {
                productService.saveProduct(product, categoryIds, tagIds, imageIds);
                redirectAttributes.addFlashAttribute("message", "产品创建成功");
            } else {
                productService.updateProduct(product, categoryIds, tagIds, imageIds);
                redirectAttributes.addFlashAttribute("message", "产品更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    /**
     * 删除产品
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "产品删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

}
