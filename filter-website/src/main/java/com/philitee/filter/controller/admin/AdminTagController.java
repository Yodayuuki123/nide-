package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.ProductTag;
import com.philitee.filter.service.ProductTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final ProductTagService tagService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       Model model) {

        QueryWrapper<ProductTag> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("name", keyword);
        }
        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge("COALESCE(updated_time, created_time)", LocalDate.parse(startDate).atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            queryWrapper.le("COALESCE(updated_time, created_time)", LocalDate.parse(endDate).atTime(23, 59, 59));
        }

        // 按修改时间倒序
        queryWrapper.orderByDesc("COALESCE(updated_time, created_time)");

        Page<ProductTag> pageReq = new Page<>(page, size);
        IPage<ProductTag> tagPage = tagService.page(pageReq, queryWrapper);
        model.addAttribute("tagPage", tagPage);
        return "admin/tag-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("tag", new ProductTag());
        return "admin/tag-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        ProductTag tag = tagService.getById(id);
        if (tag == null) return "redirect:/admin/tags";
        model.addAttribute("tag", tag);
        return "admin/tag-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute ProductTag tag, RedirectAttributes redirectAttributes) {
        try {
            if (tag.getId() == null) {
                tag.setCreatedTime(LocalDateTime.now());
                tag.setUpdatedTime(LocalDateTime.now());
                tagService.save(tag);
                redirectAttributes.addFlashAttribute("message", "标签创建成功");
            } else {
                tag.setUpdatedTime(LocalDateTime.now());
                tagService.updateById(tag);
                redirectAttributes.addFlashAttribute("message", "标签更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/tags";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tagService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "标签删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/tags";
    }
}
