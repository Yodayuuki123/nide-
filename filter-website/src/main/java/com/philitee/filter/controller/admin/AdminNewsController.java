package com.philitee.filter.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.News;
import com.philitee.filter.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/news")
@RequiredArgsConstructor
public class AdminNewsController {

    private final NewsService newsService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String startDate,
                       Model model) {

        QueryWrapper<News> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("title", keyword);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge("created_time", LocalDate.parse(startDate).atStartOfDay());
        }

        // 按修改时间倒序
        queryWrapper.orderByDesc("COALESCE(updated_time, created_time)");

        Page<News> pageReq = new Page<>(page, size);
        IPage<News> newsPage = newsService.page(pageReq, queryWrapper);
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
                redirectAttributes.addFlashAttribute("message", "新闻创建成功");
            } else {
                news.setUpdatedTime(LocalDateTime.now());
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
