package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.News;
import com.philitee.filter.mapper.NewsMapper;
import com.philitee.filter.service.NewsService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements NewsService {

    @Override
    public IPage<News> getPublishedNews(int page, int size) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, "published")
               .orderByDesc(News::getCreatedTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<News> searchNews(String keyword, String startDate, String endDate, String sort, int page, int size) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, "published");
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(News::getTitle, keyword).or().like(News::getSummary, keyword));
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(News::getCreatedTime, LocalDateTime.of(LocalDate.parse(startDate), LocalTime.MIN));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(News::getCreatedTime, LocalDateTime.of(LocalDate.parse(endDate), LocalTime.MAX));
        }
        if ("oldest".equals(sort)) {
            wrapper.orderByAsc(News::getCreatedTime);
        } else {
            wrapper.orderByDesc(News::getCreatedTime);
        }
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public News getBySlug(String slug) {
        return this.getOne(new LambdaQueryWrapper<News>().eq(News::getSlug, slug));
    }
}
