package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.Image;
import com.philitee.filter.entity.News;
import com.philitee.filter.mapper.NewsMapper;
import com.philitee.filter.service.ImageService;
import com.philitee.filter.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    private final ImageService imageService;

    @Override
    public Page<News> getPublishedNews(int page, int size) {
        Page<News> newsPage = this.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<News>()
                        .eq(News::getStatus, "publish")
                        .orderByDesc(News::getPublishedAt)
        );
        fillCoverImages(newsPage);
        return newsPage;
    }

    @Override
    public News getBySlug(String slug) {
        News news = this.getOne(
                new LambdaQueryWrapper<News>().eq(News::getSlug, slug)
        );
        if (news != null && news.getCoverImageId() != null) {
            Image img = imageService.getById(news.getCoverImageId());
            news.setCoverImage(img);
        }
        return news;
    }

    @Override
    public Page<News> searchNews(String keyword, int page, int size) {
        Page<News> newsPage = this.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<News>()
                        .eq(News::getStatus, "publish")
                        .and(w -> w.like(News::getTitle, keyword)
                                .or().like(News::getSummary, keyword))
                        .orderByDesc(News::getPublishedAt)
        );
        fillCoverImages(newsPage);
        return newsPage;
    }

    private void fillCoverImages(Page<News> newsPage) {
        for (News news : newsPage.getRecords()) {
            if (news.getCoverImageId() != null) {
                Image img = imageService.getById(news.getCoverImageId());
                news.setCoverImage(img);
            }
        }
    }

}
