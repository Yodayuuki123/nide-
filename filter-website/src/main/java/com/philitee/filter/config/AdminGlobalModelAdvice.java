package com.philitee.filter.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/**
 * 后台全局模型属性注入
 * 将当前URI等公共数据注入到所有后台页面，用于侧边栏导航高亮
 */
@ControllerAdvice(basePackages = "com.philitee.filter.controller.admin")
public class AdminGlobalModelAdvice {

    @ModelAttribute
    public void addAdminAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("currentUri", request.getRequestURI());
    }
}
