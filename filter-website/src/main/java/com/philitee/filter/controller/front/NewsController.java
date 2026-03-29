package com.philitee.filter.controller.front;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.philitee.filter.entity.News;
import com.philitee.filter.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/news")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false, defaultValue = "newest") String sort,
                       Model model) {
        IPage<News> newsPage = newsService.searchNews(keyword, startDate, endDate, sort, page, size);
        model.addAttribute("newsPage", newsPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sort", sort);
        return "front/news-list";
    }

    @GetMapping("/news/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        News news = newsService.getBySlug(slug);
        if (news == null) {
            return "redirect:/news";
        }
        // Increment view count
        news.setViewCount(news.getViewCount() != null ? news.getViewCount() + 1 : 1);
        newsService.updateById(news);
        model.addAttribute("news", news);
        // Recent news for sidebar
        IPage<News> recentNews = newsService.getPublishedNews(1, 5);
        model.addAttribute("recentNews", recentNews.getRecords());
        return "front/news-detail";
    }
}
