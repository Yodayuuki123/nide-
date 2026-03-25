package com.philitee.filter.controller.admin;

import com.philitee.filter.entity.SiteSetting;
import com.philitee.filter.service.SiteSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台网站设置 Controller
 */
@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingController {

    private final SiteSettingService siteSettingService;

    /**
     * 设置页面
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('setting:view', 'setting:edit')")
    public String index(Model model) {
        Map<String, List<SiteSetting>> settingMap = siteSettingService.getAllSettingsByGroup();
        model.addAttribute("settingMap", settingMap);
        return "admin/settings";
    }

    /**
     * 保存设置
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('setting:edit')")
    public String save(@RequestParam Map<String, String> allParams, RedirectAttributes redirectAttributes) {
        try {
            // 过滤出设置相关的参数（以 setting_ 开头）
            Map<String, String> settings = new HashMap<>();
            allParams.forEach((key, value) -> {
                if (key.startsWith("setting_")) {
                    String settingKey = key.substring(8); // 去掉 "setting_" 前缀
                    settings.put(settingKey, value);
                }
            });
            siteSettingService.saveSettings(settings);
            redirectAttributes.addFlashAttribute("message", "设置保存成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
