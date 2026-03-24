package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.Image;
import com.philitee.filter.entity.ProductCategory;
import com.philitee.filter.mapper.ImageMapper;
import com.philitee.filter.mapper.ProductCategoryMapper;
import com.philitee.filter.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产品分类 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory>
        implements ProductCategoryService {

    private final ImageMapper imageMapper;

    @Override
    @Cacheable(value = "categoryTree", unless = "#result == null")
    public List<ProductCategory> getCategoryTree() {
        // 查询所有分类
        List<ProductCategory> allCategories = this.list();

        // 填充图片和产品数量
        allCategories.forEach(category -> {
            if (category.getImageId() != null) {
                Image image = imageMapper.selectById(category.getImageId());
                category.setImage(image);
            }
            Integer count = baseMapper.countProductsByCategoryId(category.getId());
            category.setProductCount(count);
        });

        // 构建树形结构
        Map<Long, List<ProductCategory>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(ProductCategory::getParentId));

        allCategories.forEach(category -> {
            List<ProductCategory> children = childrenMap.getOrDefault(category.getId(), new ArrayList<>());
            category.setChildren(children);
        });

        // 返回顶级分类
        return allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductCategory> getTopLevelCategories() {
        List<ProductCategory> categories = baseMapper.selectTopLevel();
        categories.forEach(category -> {
            Integer count = baseMapper.countProductsByCategoryId(category.getId());
            category.setProductCount(count);
            if (category.getImageId() != null) {
                Image image = imageMapper.selectById(category.getImageId());
                category.setImage(image);
            }
        });
        return categories;
    }

    @Override
    public List<ProductCategory> getChildCategories(Long parentId) {
        return baseMapper.selectByParentId(parentId);
    }

    @Override
    public ProductCategory getCategoryBySlug(String slug) {
        return baseMapper.selectBySlug(slug);
    }

    @Override
    public List<ProductCategory> getCategoriesByProductId(Long productId) {
        return baseMapper.selectByProductId(productId);
    }

}
