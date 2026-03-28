package com.philitee.filter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.News;

public interface NewsService extends IService<News> {

    /**
     * 分页查询已发布的新闻
     */
    Page<News> getPublishedNews(int page, int size);

    /**
     * 根据slug获取新闻
     */
    News getBySlug(String slug);

    /**
     * 搜索新闻
     */
    Page<News> searchNews(String keyword, int page, int size);

}
