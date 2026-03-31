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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台图片管理 Controller
 * 提供图片上传、批量上传、搜索、分页、删除功能
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminImageController {

    private final ImageService imageService;

    /**
     * 图片管理页面 - 支持搜索+分页
     */
    @GetMapping("/images")
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("file_name", keyword);
        }
        queryWrapper.orderByDesc("uploaded_at");

        IPage<Image> imagePage = imageService.page(new Page<>(page, size), queryWrapper);
        model.addAttribute("imagePage", imagePage);
        return "admin/image-list";
    }

    /**
     * 图片上传接口（AJAX调用）- 单张上传
     */
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
            result.put("message", "上传失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 批量上传接口（AJAX调用）
     */
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
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 视频上传接口（AJAX调用）
     */
    @PostMapping("/api/media/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadMedia(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            Image media = imageService.uploadImage(file);
            result.put("success", true);
            result.put("media", media);
            result.put("url", media.getUrl());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 图片列表JSON接口（供产品表单图片选择器AJAX调用）- 支持搜索+分页
     */
    @GetMapping("/api/images/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listApi(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("file_name", keyword);
        }
        queryWrapper.orderByDesc("uploaded_at");

        IPage<Image> imagePage = imageService.page(new Page<>(page, size), queryWrapper);
        Map<String, Object> result = new HashMap<>();
        result.put("records", imagePage.getRecords());
        result.put("current", imagePage.getCurrent());
        result.put("pages", imagePage.getPages());
        result.put("total", imagePage.getTotal());
        return ResponseEntity.ok(result);
    }

    /**
     * 删除图片接口（AJAX调用）
     */
    @PostMapping("/api/images/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = imageService.deleteImage(id);
            result.put("success", success);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

}
