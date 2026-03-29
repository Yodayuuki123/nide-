package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.News;
import com.philitee.filter.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/news")
@RequiredArgsConstructor
public class AdminNewsController {

    private final NewsService newsService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Page<News> pageReq = new Page<>(page, size);
        pageReq.addOrder(OrderItem.desc("created_time"));
        IPage<News> newsPage = newsService.page(pageReq);
        model.addAttribute("newsPage", newsPage);
        return "admin/news-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("news", new News());
        return "admin/news-form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        News news = newsService.getById(id);
        if (news == null) {
            return "redirect:/admin/news";
        }
        model.addAttribute("news", news);
        return "admin/news-form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute News news, RedirectAttributes redirectAttributes) {
        try {
            if (news.getId() == null) {
                news.setCreatedTime(LocalDateTime.now());
                news.setUpdatedTime(LocalDateTime.now());
                if (news.getViewCount() == null) news.setViewCount(0);
                if (news.getStatus() == null) news.setStatus("draft");
                newsService.save(news);
                redirectAttributes.addFlashAttribute("message", "News created successfully");
            } else {
                news.setUpdatedTime(LocalDateTime.now());
                newsService.updateById(news);
                redirectAttributes.addFlashAttribute("message", "News updated successfully");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Save failed: " + e.getMessage());
        }
        return "redirect:/admin/news";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "News deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Delete failed: " + e.getMessage());
        }
        return "redirect:/admin/news";
    }
}
