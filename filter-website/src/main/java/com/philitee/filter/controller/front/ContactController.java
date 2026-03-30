package com.philitee.filter.controller.front;

import com.philitee.filter.entity.Inquiry;
import com.philitee.filter.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final InquiryService inquiryService;

    @PostMapping("/contact")
    public Object submitInquiry(
            Inquiry inquiry,
            @RequestHeader(value = "X-Requested-With", required = false) String xRequestedWith,
            @RequestHeader(value = "Accept", required = false) String accept,
            RedirectAttributes redirectAttributes) {
        try {
            inquiry.setIsRead(false);
            inquiry.setCreatedTime(LocalDateTime.now());
            inquiryService.save(inquiry);
            // If AJAX request, return JSON
            if ("XMLHttpRequest".equals(xRequestedWith) || (accept != null && accept.contains("application/json"))) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Inquiry submitted successfully!"));
            }
            redirectAttributes.addFlashAttribute("success", true);
        } catch (Exception e) {
            if ("XMLHttpRequest".equals(xRequestedWith) || (accept != null && accept.contains("application/json"))) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to submit inquiry."));
            }
            redirectAttributes.addFlashAttribute("error", "Failed to submit inquiry. Please try again.");
        }
        return "redirect:/contact";
    }
}
