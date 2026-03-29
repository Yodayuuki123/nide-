package com.philitee.filter.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.philitee.filter.entity.News;
import com.philitee.filter.service.NewsService;
import com.philitee.filter.util.BreadcrumbItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/news")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "12") int size,
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

        // 面包屑
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("News")
        ));

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

        // Recent news for sidebar (8 items like best-filter.com)
        IPage<News> recentNews = newsService.getPublishedNews(1, 8);
        model.addAttribute("recentNews", recentNews.getRecords());

        // Previous and Next news
        News prevNews = newsService.getPreviousNews(news.getCreatedTime());
        News nextNews = newsService.getNextNews(news.getCreatedTime());
        model.addAttribute("prevNews", prevNews);
        model.addAttribute("nextNews", nextNews);

        // 面包屑
        model.addAttribute("breadcrumbItems", List.of(
            BreadcrumbItem.of("News", "/news"),
            BreadcrumbItem.of(news.getTitle())
        ));

        return "front/news-detail";
    }
}
