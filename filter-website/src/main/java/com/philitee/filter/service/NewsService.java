package com.philitee.filter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.News;

public interface NewsService extends IService<News> {
    IPage<News> getPublishedNews(int page, int size);
    IPage<News> searchNews(String keyword, String startDate, String endDate, String sort, int page, int size);
    News getBySlug(String slug);
    News getPreviousNews(java.time.LocalDateTime createdTime);
    News getNextNews(java.time.LocalDateTime createdTime);
}
