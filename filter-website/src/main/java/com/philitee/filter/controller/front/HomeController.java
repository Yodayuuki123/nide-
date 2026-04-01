package com.philitee.filter.controller.front;

import com.philitee.filter.entity.Product;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.service.MenuService;
import com.philitee.filter.service.ProductCategoryService;
import com.philitee.filter.service.ProductService;
import com.philitee.filter.util.BreadcrumbItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final MenuService menuService;

    @GetMapping("/")
    public String index(Model model) {
        List<Product> featuredProducts = productService.getFeaturedProducts(12);
        model.addAttribute("featuredProducts", featuredProducts);

        List<String> featuredCategorySlugs = List.of(
                "industrial-filter-bags",
                "industrial-filter-cloth",
                "bag-house-filters",
                "filter-cartridges",
                "filter-bag-making-machine",
                "air-slide-fabric",
                "filter-systems",
                "filter-elements",
                "air-filters"
        );
        Map<String, ProductCategory> categoryMap = categoryService.getTopLevelCategories().stream()
                .collect(Collectors.toMap(ProductCategory::getSlug, Function.identity(), (first, second) -> first));
        List<ProductCategory> categories = featuredCategorySlugs.stream()
                .map(categoryMap::get)
                .filter(category -> category != null)
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);

        return "front/index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("About Us")
        ));
        return "front/about";
    }

    @GetMapping("/factory")
    public String factory(Model model) {
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("Factory")
        ));
        return "front/factory";
    }

    @GetMapping("/solutions")
    public String solutions(Model model) {
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("Solutions")
        ));
        return "front/solutions";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("Contact Us")
        ));
        return "front/contact";
    }
}