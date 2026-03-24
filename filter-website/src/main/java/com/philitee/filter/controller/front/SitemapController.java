package com.philitee.filter.controller.front;

import com.philitee.filter.entity.Product;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.service.ProductCategoryService;
import com.philitee.filter.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SitemapController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        String baseUrl = "https://filter.philitee.com";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDateTime.now().format(fmt);

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Static pages
        sb.append(url(baseUrl, "/", today, "1.0", "daily"));
        sb.append(url(baseUrl, "/products", today, "0.9", "daily"));
        sb.append(url(baseUrl, "/about", today, "0.7", "monthly"));
        sb.append(url(baseUrl, "/factory", today, "0.7", "monthly"));
        sb.append(url(baseUrl, "/contact", today, "0.7", "monthly"));

        // Categories
        List<ProductCategory> categories = categoryService.getCategoryTree();
        for (ProductCategory cat : categories) {
            sb.append(url(baseUrl, "/categories/" + cat.getSlug(), today, "0.8", "weekly"));
            if (cat.getChildren() != null) {
                for (ProductCategory child : cat.getChildren()) {
                    sb.append(url(baseUrl, "/categories/" + child.getSlug(), today, "0.8", "weekly"));
                }
            }
        }

        // Products
        List<Product> products = productService.getFeaturedProducts(10000);
        for (Product p : products) {
            String lastMod = p.getUpdatedAt() != null ? p.getUpdatedAt().format(fmt) : today;
            sb.append(url(baseUrl, "/products/" + p.getSlug(), lastMod, "0.7", "weekly"));
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    private String url(String baseUrl, String path, String lastmod, String priority, String changefreq) {
        return "  <url>\n" +
                "    <loc>" + baseUrl + path + "</loc>\n" +
                "    <lastmod>" + lastmod + "</lastmod>\n" +
                "    <priority>" + priority + "</priority>\n" +
                "    <changefreq>" + changefreq + "</changefreq>\n" +
                "  </url>\n";
    }
}
