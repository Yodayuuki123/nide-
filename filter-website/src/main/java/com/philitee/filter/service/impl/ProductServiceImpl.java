package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.*;
import com.philitee.filter.mapper.*;
import com.philitee.filter.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 产品 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ImageMapper imageMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductTagMapper tagMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductProductCategoryMapper productCategoryMapper;
    private final ProductTagRelationMapper tagRelationMapper;

    @Override
    public Product getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            return null;
        }
        fillProductRelations(product);
        return product;
    }

    @Override
    public Product getProductBySlug(String slug) {
        Product product = baseMapper.selectBySlug(slug);
        if (product == null) {
            return null;
        }
        fillProductRelations(product);
        return product;
    }

    @Override
    public IPage<Product> getPublishedProducts(int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "publish")
                .orderByDesc(Product::getCreatedAt);
        IPage<Product> result = this.page(page, wrapper);

        // 填充主图和分类信息
        result.getRecords().forEach(this::fillMainImageAndCategories);
        return result;
    }

    @Override
    public IPage<Product> getProductsByCategory(Long categoryId, int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        IPage<Product> result = baseMapper.selectPageByCategoryId(page, categoryId);

        // 填充主图和分类信息
        result.getRecords().forEach(this::fillMainImageAndCategories);
        return result;
    }

    @Override
    public List<Product> getProductsByTag(Long tagId) {
        List<Product> products = baseMapper.selectByTagId(tagId);
        products.forEach(this::fillMainImageAndCategories);
        return products;
    }

    @Override
    public List<Product> searchProducts(String keyword) {
        List<Product> products = baseMapper.searchByKeyword(keyword);
        products.forEach(this::fillMainImageAndCategories);
        return products;
    }

    @Override
    @Cacheable(value = "featuredProducts", key = "#limit", unless = "#result == null")
    public List<Product> getFeaturedProducts(int limit) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "publish")
                .orderByDesc(Product::getCreatedAt)
                .last("LIMIT " + limit);
        List<Product> products = this.list(wrapper);
        products.forEach(this::fillMainImageAndCategories);
        return products;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public boolean saveProduct(Product product, List<Long> categoryIds, List<Long> tagIds, List<Long> imageIds) {
        // 自动填充时间戳
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (product.getCreatedAt() == null) {
            product.setCreatedAt(now);
        }
        product.setUpdatedAt(now);
        // 保存产品
        this.save(product);
        Long productId = product.getId();

        // 保存分类关联
        if (categoryIds != null) {
            for (Long categoryId : categoryIds) {
                productCategoryMapper.insertRelation(productId, categoryId);
            }
        }

        // 保存标签关联
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                tagRelationMapper.insertRelation(productId, tagId);
            }
        }

        // 保存图片关联
        if (imageIds != null) {
            for (int i = 0; i < imageIds.size(); i++) {
                ProductImage pi = new ProductImage();
                pi.setProductId(productId);
                pi.setImageId(imageIds.get(i));
                pi.setSortOrder(i);
                productImageMapper.insert(pi);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public boolean updateProduct(Product product, List<Long> categoryIds, List<Long> tagIds, List<Long> imageIds) {
        // 自动更新时间戳
        product.setUpdatedAt(java.time.LocalDateTime.now());
        // 更新产品
        this.updateById(product);
        Long productId = product.getId();

        // 重建分类关联
        if (categoryIds != null) {
            productCategoryMapper.deleteByProductId(productId);
            for (Long categoryId : categoryIds) {
                productCategoryMapper.insertRelation(productId, categoryId);
            }
        }

        // 重建标签关联
        if (tagIds != null) {
            tagRelationMapper.deleteByProductId(productId);
            for (Long tagId : tagIds) {
                tagRelationMapper.insertRelation(productId, tagId);
            }
        }

        // 重建图片关联
        if (imageIds != null) {
            productImageMapper.deleteByProductId(productId);
            for (int i = 0; i < imageIds.size(); i++) {
                ProductImage pi = new ProductImage();
                pi.setProductId(productId);
                pi.setImageId(imageIds.get(i));
                pi.setSortOrder(i);
                productImageMapper.insert(pi);
            }
        }

        return true;
    }

    /**
     * 填充产品的所有关联信息（图片、分类、标签）
     */
    private void fillProductRelations(Product product) {
        // 主图
        fillMainImage(product);

        // 产品图片列表
        List<Image> images = imageMapper.selectByProductId(product.getId());
        product.setImages(images);

        // 产品分类
        List<ProductCategory> categories = categoryMapper.selectByProductId(product.getId());
        product.setCategories(categories);

        // 产品标签
        List<ProductTag> tags = tagMapper.selectByProductId(product.getId());
        product.setTags(tags);
    }

    /**
     * 填充主图和分类信息（用于列表页、搜索页等需要显示分类名称的场景）
     */
    private void fillMainImageAndCategories(Product product) {
        fillMainImage(product);
        // 填充分类信息，便于列表页显示分类名称
        List<ProductCategory> categories = categoryMapper.selectByProductId(product.getId());
        product.setCategories(categories);
    }

    /**
     * 填充主图信息
     */
    private void fillMainImage(Product product) {
        if (product.getMainImageId() != null) {
            Image mainImage = imageMapper.selectById(product.getMainImageId());
            product.setMainImage(mainImage);
        }
    }

}
