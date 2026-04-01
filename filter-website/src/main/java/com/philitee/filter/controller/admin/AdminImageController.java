package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Image;
import com.philitee.filter.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminImageController {

    private final ImageService imageService;

    @GetMapping("/images")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       Model model) {
        QueryWrapper<Image> queryWrapper = buildImageQuery(keyword, startDate, endDate);
        IPage<Image> imagePage = imageService.page(new Page<>(page, size), queryWrapper);
        model.addAttribute("imagePage", imagePage);
        return "admin/image-list";
    }

    @PostMapping("/api/images/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            Image image = imageService.uploadImage(file);
            result.put("success", true);
            result.put("image", image);
            result.put("url", image.getUrl());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/api/images/batch-upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> batchUpload(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> uploaded = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                Image image = imageService.uploadImage(file);
                Map<String, Object> item = new HashMap<>();
                item.put("id", image.getId());
                item.put("fileName", image.getFileName());
                item.put("uploadedAt", image.getUploadedAt());
                item.put("url", image.getUrl());
                uploaded.add(item);
            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        result.put("success", errors.isEmpty());
        result.put("uploaded", uploaded);
        result.put("uploadedCount", uploaded.size());
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/images/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listApi(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int size,
                                                       @RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String startDate,
                                                       @RequestParam(required = false) String endDate) {
        QueryWrapper<Image> queryWrapper = buildImageQuery(keyword, startDate, endDate);
        IPage<Image> imagePage = imageService.page(new Page<>(page, size), queryWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", imagePage.getRecords());
        result.put("current", imagePage.getCurrent());
        result.put("size", imagePage.getSize());
        result.put("pages", imagePage.getPages());
        result.put("total", imagePage.getTotal());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/images/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = imageService.deleteImage(id);
            result.put("success", success);
            if (!success) {
                result.put("message", "Image not found");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Delete failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    private QueryWrapper<Image> buildImageQuery(String keyword, String startDate, String endDate) {
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("file_name", keyword);
        }
        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge("uploaded_at", LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            queryWrapper.le("uploaded_at", LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }
        queryWrapper.orderByDesc("uploaded_at");
        return queryWrapper;
    }
}