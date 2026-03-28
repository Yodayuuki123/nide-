package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.ProductTag;
import com.philitee.filter.service.ProductTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/**
 * 后台标签管理 Controller
 */
@Controller
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final ProductTagService tagService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Page<ProductTag> tagPage = tagService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<ProductTag>().orderByDesc(ProductTag::getCreatedTime)
        );
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
        if (tag == null) {
            return "redirect:/admin/tags";
        }
        model.addAttribute("tag", tag);
        return "admin/tag-form";
    }

    @PostMapping("/save")
    public String save(ProductTag tag, RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (tag.getId() == null) {
                tag.setCreatedTime(now);
                tag.setUpdatedTime(now);
                tagService.save(tag);
                redirectAttributes.addFlashAttribute("message", "标签创建成功");
            } else {
                tag.setUpdatedTime(now);
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
