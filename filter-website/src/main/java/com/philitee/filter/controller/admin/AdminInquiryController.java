package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Inquiry;
import com.philitee.filter.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final InquiryService inquiryService;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String startDate,
            Model model) {

        QueryWrapper<Inquiry> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }
        if (StringUtils.hasText(email)) {
            queryWrapper.like("email", email);
        }
        if (StringUtils.hasText(company)) {
            queryWrapper.like("company", company);
        }
        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge("created_time", LocalDate.parse(startDate).atStartOfDay());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("created_time");

        Page<Inquiry> pageReq = new Page<>(page, size);
        IPage<Inquiry> inquiryPage = inquiryService.page(pageReq, queryWrapper);
        model.addAttribute("inquiryPage", inquiryPage);
        return "admin/inquiry-list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryService.getById(id);
        if (inquiry == null) {
            return "redirect:/admin/inquiries";
        }
        // Auto mark as read when viewing detail
        if (inquiry.getIsRead() == null || !inquiry.getIsRead()) {
            inquiry.setIsRead(true);
            inquiryService.updateById(inquiry);
        }
        model.addAttribute("inquiry", inquiry);
        return "admin/inquiry-detail";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Inquiry inquiry = inquiryService.getById(id);
        if (inquiry == null) {
            return "redirect:/admin/inquiries";
        }
        model.addAttribute("inquiry", inquiry);
        return "admin/inquiry-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Inquiry inquiry, RedirectAttributes redirectAttributes) {
        try {
            inquiryService.updateById(inquiry);
            redirectAttributes.addFlashAttribute("message", "询盘更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/inquiries";
    }

    @PostMapping("/markRead/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Inquiry inquiry = inquiryService.getById(id);
            if (inquiry != null) {
                inquiry.setIsRead(true);
                inquiryService.updateById(inquiry);
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("message", "Inquiry not found");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inquiryService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "询盘删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/inquiries";
    }
}
