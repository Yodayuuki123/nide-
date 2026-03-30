package com.philitee.filter.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.philitee.filter.entity.News;
import com.philitee.filter.entity.Product;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.entity.ProductTag;
import com.philitee.filter.service.NewsService;
import com.philitee.filter.service.ProductCategoryService;
import com.philitee.filter.service.ProductService;
import com.philitee.filter.service.ProductTagService;
import com.philitee.filter.util.BreadcrumbItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台产品 Controller
 */
@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final ProductTagService tagService;
    private final NewsService newsService;

    /**
     * 产品列表页（全部产品）- 默认一页16个
     */
    @GetMapping("/products")
    public String productList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size,
            Model model) {

        IPage<Product> productPage = productService.getPublishedProducts(page, size);
        model.addAttribute("productPage", productPage);

        // 侧边栏分类树
        List<ProductCategory> categoryTree = categoryService.getCategoryTree();
        model.addAttribute("categoryTree", categoryTree);

        // 面包屑
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("Products")
        ));

        return "front/product-list";
    }

    /**
     * 按分类查看产品
     */
    @GetMapping("/categories/{slug}")
    public String productsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "16") int size,
            Model model) {

        ProductCategory category = categoryService.getCategoryBySlug(slug);
        if (category == null) {
            return "redirect:/products";
        }

        IPage<Product> productPage = productService.getProductsByCategory(category.getId(), page, size);
        model.addAttribute("productPage", productPage);
        model.addAttribute("currentCategory", category);

        // 子分类
        List<ProductCategory> childCategories = categoryService.getChildCategories(category.getId());
        model.addAttribute("childCategories", childCategories);

        // 侧边栏分类树
        List<ProductCategory> categoryTree = categoryService.getCategoryTree();
        model.addAttribute("categoryTree", categoryTree);

        // 面包屑
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(BreadcrumbItem.of("Products", "/products"));
        breadcrumbs.add(BreadcrumbItem.of(category.getName()));
        model.addAttribute("breadcrumbItems", breadcrumbs);

        return "front/product-list";
    }

    /**
     * 按标签查看产品
     */
    @GetMapping("/tags/{slug}")
    public String productsByTag(
            @PathVariable String slug,
            Model model) {

        ProductTag tag = tagService.getTagBySlug(slug);
        if (tag == null) {
            return "redirect:/products";
        }

        List<Product> products = productService.getProductsByTag(tag.getId());
        model.addAttribute("products", products);
        model.addAttribute("currentTag", tag);

        // 面包屑
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("Products", "/products"),
            BreadcrumbItem.of(tag.getName())
        ));

        return "front/product-list";
    }

    /**
     * 产品详情页（通过slug访问，SEO友好）
     */
    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        Product product = productService.getProductBySlug(slug);
        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);

        // 相关产品（同分类的其他产品，取5个过滤自身后保留4个）
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            Long categoryId = product.getCategories().get(0).getId();
            IPage<Product> relatedPage = productService.getProductsByCategory(categoryId, 1, 5);
            List<Product> relatedProducts = relatedPage.getRecords().stream()
                    .filter(p -> !p.getId().equals(product.getId()))
                    .limit(4)
                    .toList();
            model.addAttribute("relatedProducts", relatedProducts);
        }

        // 面包屑
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(BreadcrumbItem.of("Products", "/products"));
        if (product.getCategories() != null && !product.getCategories().isEmpty()) {
            ProductCategory cat = product.getCategories().get(0);
            breadcrumbs.add(BreadcrumbItem.of(cat.getName(), "/categories/" + cat.getSlug()));
        }
        breadcrumbs.add(BreadcrumbItem.of(product.getName()));
        model.addAttribute("breadcrumbItems", breadcrumbs);

        return "front/product-detail";
    }

    /**
     * 全局搜索（产品 + 新闻）
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            // 搜索产品
            List<Product> products = productService.searchProducts(kw);
            model.addAttribute("products", products);
            // 搜索新闻
            IPage<News> newsPage = newsService.searchNews(kw, null, null, "newest", 1, 20);
            model.addAttribute("newsList", newsPage.getRecords());
            // 总数
            long total = products.size() + newsPage.getTotal();
            model.addAttribute("totalResults", total);
        }
        model.addAttribute("keyword", keyword);

        // 面包屑
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("Search Results")
        ));

        return "front/search";
    }

    /**
     * 产品搜索 JSON API（供弹窗 AJAX 调用）
     */
    @GetMapping("/api/search")
    @ResponseBody
    public Map<String, Object> searchApi(@RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            List<Product> products = productService.searchProducts(keyword.trim());
            List<Map<String, Object>> items = new ArrayList<>();
            for (Product p : products) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", p.getName());
                item.put("slug", p.getSlug());
                item.put("sku", p.getSku());
                item.put("mainImage", p.getMainImage() != null ? p.getMainImage().getUrl() : null);
                if (p.getCategories() != null && !p.getCategories().isEmpty()) {
                    item.put("category", p.getCategories().get(0).getName());
                }
                items.add(item);
            }
            result.put("products", items);
            result.put("total", items.size());
        } else {
            result.put("products", List.of());
            result.put("total", 0);
        }
        result.put("keyword", keyword);
        return result;
    }

}
