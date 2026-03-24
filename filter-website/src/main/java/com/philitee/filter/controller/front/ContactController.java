package com.philitee.filter.controller.front;

import com.philitee.filter.entity.Inquiry;
import com.philitee.filter.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final InquiryService inquiryService;

    @PostMapping("/contact")
    public String submitInquiry(Inquiry inquiry, RedirectAttributes redirectAttributes) {
        try {
            inquiry.setIsRead(false);
            inquiry.setCreatedTime(LocalDateTime.now());
            inquiryService.save(inquiry);
            redirectAttributes.addFlashAttribute("success", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit inquiry. Please try again.");
        }
        return "redirect:/contact";
    }
}
