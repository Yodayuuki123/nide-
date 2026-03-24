package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Inquiry;
import com.philitee.filter.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
