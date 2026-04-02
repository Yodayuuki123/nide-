package com.philitee.filter.controller.front;

import com.philitee.filter.entity.Inquiry;
import com.philitee.filter.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final InquiryService inquiryService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/contact")
    public Object submitInquiry(
            Inquiry inquiry,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            @RequestHeader(value = "X-Requested-With", required = false) String xRequestedWith,
            @RequestHeader(value = "Accept", required = false) String accept,
            RedirectAttributes redirectAttributes) {
        try {
            inquiry.setIsRead(false);
            inquiry.setCreatedTime(LocalDateTime.now());

            // 处理附件上传
            if (attachment != null && !attachment.isEmpty()) {
                String originalName = attachment.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                }
                String savedName = UUID.randomUUID().toString() + ext;
                Path uploadPath = Paths.get(uploadDir, "inquiry");
                Files.createDirectories(uploadPath);
                Path filePath = uploadPath.resolve(savedName);
                attachment.transferTo(filePath.toFile());
                inquiry.setAttachmentPath("inquiry/" + savedName);
                inquiry.setAttachmentName(originalName);
            }

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
