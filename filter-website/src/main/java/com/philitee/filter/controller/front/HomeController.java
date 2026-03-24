package com.philitee.filter.controller.front;

import com.philitee.filter.entity.Product;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.service.MenuService;
import com.philitee.filter.service.ProductCategoryService;
import com.philitee.filter.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 前台首页 Controller
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final MenuService menuService;

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model) {
        // 获取精选产品（最新12个）
        List<Product> featuredProducts = productService.getFeaturedProducts(12);
        model.addAttribute("featuredProducts", featuredProducts);

        // 获取顶级分类
        List<ProductCategory> categories = categoryService.getTopLevelCategories();
        model.addAttribute("categories", categories);

        return "front/index";
    }

    /**
     * 关于我们
     */
    @GetMapping("/about")
    public String about(Model model) {
        return "front/about";
    }

    /**
     * 工厂介绍
     */
    @GetMapping("/factory")
    public String factory(Model model) {
        return "front/factory";
    }

    /**
     * 联系我们
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        return "front/contact";
    }

}
