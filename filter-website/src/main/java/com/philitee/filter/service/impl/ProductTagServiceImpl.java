package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.ProductTag;
import com.philitee.filter.mapper.ProductTagMapper;
import com.philitee.filter.service.ProductTagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品标签 Service 实现类
 */
@Service
public class ProductTagServiceImpl extends ServiceImpl<ProductTagMapper, ProductTag> implements ProductTagService {

    @Override
    public List<ProductTag> getAllTags() {
        return this.list();
    }

    @Override
    public ProductTag getTagBySlug(String slug) {
        return baseMapper.selectBySlug(slug);
    }

    @Override
    public List<ProductTag> getTagsByProductId(Long productId) {
        return baseMapper.selectByProductId(productId);
    }

}
