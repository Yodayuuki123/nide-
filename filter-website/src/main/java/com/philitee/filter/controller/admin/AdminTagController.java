package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.ProductTag;
import com.philitee.filter.service.ProductTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final ProductTagService tagService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Page<ProductTag> pageReq = new Page<>(page, size);
        pageReq.addOrder(OrderItem.desc("created_time"));
        IPage<ProductTag> tagPage = tagService.page(pageReq);
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
                redirectAttributes.addFlashAttribute("message", "Tag created successfully");
            } else {
                tag.setUpdatedTime(LocalDateTime.now());
                tagService.updateById(tag);
                redirectAttributes.addFlashAttribute("message", "Tag updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Save failed: " + e.getMessage());
        }
        return "redirect:/admin/tags";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tagService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "Tag deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/tags";
    }
}
