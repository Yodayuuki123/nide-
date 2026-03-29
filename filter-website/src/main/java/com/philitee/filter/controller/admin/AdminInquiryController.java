package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Inquiry;
import com.philitee.filter.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            Model model) {
        Page<Inquiry> pageReq = new Page<>(page, size);
        pageReq.addOrder(OrderItem.desc("created_time"));
        IPage<Inquiry> inquiryPage = inquiryService.page(pageReq);
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
            redirectAttributes.addFlashAttribute("message", "Inquiry updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Save failed: " + e.getMessage());
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
            redirectAttributes.addFlashAttribute("message", "Inquiry deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/inquiries";
    }
}
