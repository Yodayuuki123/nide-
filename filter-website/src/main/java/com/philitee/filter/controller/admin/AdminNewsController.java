package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Image;
import com.philitee.filter.entity.News;
import com.philitee.filter.service.ImageService;
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
    private final ImageService imageService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {
        Page<News> newsPage = newsService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<News>().orderByDesc(News::getCreatedAt)
        );
        // 填充封面图
        for (News news : newsPage.getRecords()) {
            if (news.getCoverImageId() != null) {
                Image img = imageService.getById(news.getCoverImageId());
                news.setCoverImage(img);
            }
        }
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
        if (news.getCoverImageId() != null) {
            news.setCoverImage(imageService.getById(news.getCoverImageId()));
        }
        model.addAttribute("news", news);
        return "admin/news-form";
    }

    @PostMapping("/save")
    public String save(News news, RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (news.getId() == null) {
                news.setCreatedAt(now);
                news.setUpdatedAt(now);
                if ("publish".equals(news.getStatus()) && news.getPublishedAt() == null) {
                    news.setPublishedAt(now);
                }
                newsService.save(news);
                redirectAttributes.addFlashAttribute("message", "新闻创建成功");
            } else {
                news.setUpdatedAt(now);
                // 如果从草稿变为发布且没有发布时间
                if ("publish".equals(news.getStatus()) && news.getPublishedAt() == null) {
                    news.setPublishedAt(now);
                }
                newsService.updateById(news);
                redirectAttributes.addFlashAttribute("message", "新闻更新成功");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/news";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            newsService.removeById(id);
            redirectAttributes.addFlashAttribute("message", "新闻删除成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/news";
    }

}
