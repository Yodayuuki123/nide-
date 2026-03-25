package com.philitee.filter.controller.admin;

import com.philitee.filter.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 后台管理 Controller
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final ProductTagService tagService;
    private final ImageService imageService;
    private final AdminUserService adminUserService;
    private final InquiryService inquiryService;

    /**
     * 后台登录页
     */
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    /**
     * 后台仪表盘
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("productCount", productService.count());
        model.addAttribute("categoryCount", categoryService.count());
        model.addAttribute("tagCount", tagService.count());
        model.addAttribute("imageCount", imageService.count());
        model.addAttribute("userCount", adminUserService.count());
        model.addAttribute("inquiryCount", inquiryService.count());
        return "admin/dashboard";
    }
}
