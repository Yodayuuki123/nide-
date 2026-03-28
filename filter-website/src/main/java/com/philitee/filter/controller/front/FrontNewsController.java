package com.philitee.filter.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.News;
import com.philitee.filter.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 前台新闻 Controller
 */
@Controller
@RequiredArgsConstructor
public class FrontNewsController {

    private final NewsService newsService;

    /**
     * 新闻列表页
     */
    @GetMapping("/news")
    public String newsList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String keyword,
            Model model) {

        Page<News> newsPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            newsPage = newsService.searchNews(keyword.trim(), page, size);
            model.addAttribute("keyword", keyword);
        } else {
            newsPage = newsService.getPublishedNews(page, size);
        }
        model.addAttribute("newsPage", newsPage);

        // Recent news for sidebar
        Page<News> recentNews = newsService.getPublishedNews(1, 5);
        model.addAttribute("recentNews", recentNews.getRecords());

        return "front/news-list";
    }

    /**
     * 新闻详情页
     */
    @GetMapping("/news/{slug}")
    public String newsDetail(@PathVariable String slug, Model model) {
        News news = newsService.getBySlug(slug);
        if (news == null) {
            return "redirect:/news";
        }
        model.addAttribute("news", news);

        // Recent news for sidebar
        Page<News> recentNews = newsService.getPublishedNews(1, 5);
        model.addAttribute("recentNews", recentNews.getRecords());

        return "front/news-detail";
    }
}
