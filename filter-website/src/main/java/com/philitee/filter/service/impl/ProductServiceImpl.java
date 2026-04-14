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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    /**
     * 修复产品描述中的图片URL
     * 将硬编码的绝对路径（如 http://old-domain.com/wp-content/uploads/...）
     * 替换为相对路径（如 /wp-content/uploads/...），以便 ImageProxyController 或本地路径可以正确处理
     */
    private String fixDescriptionImageUrls(String description) {
        if (description == null || description.isEmpty()) {
            return description;
        }
        // 匹配所有 src="http(s)://任意域名/wp-content/uploads/..." 或 src="http(s)://任意域名/wordpress/wp-content/uploads/..."
        // 替换为相对路径
        String fixed = description;

        // Pattern 1: https?://任意域名/wp-content/uploads/路径
        fixed = fixed.replaceAll(
            "(src\\s*=\\s*[\"'])https?://[^/\"']+(/wp-content/uploads/[^\"']*)",
            "$1$2"
        );

        // Pattern 2: https?://任意域名/wordpress/wp-content/uploads/路径
        fixed = fixed.replaceAll(
            "(src\\s*=\\s*[\"'])https?://[^/\"']+(/wordpress/wp-content/uploads/[^\"']*)",
            "$1$2"
        );

        // Pattern 3: 处理 background-image: url(...) 中的图片路径
        fixed = fixed.replaceAll(
            "(url\\s*\\(\\s*[\"']?)https?://[^/\"')]+(/wp-content/uploads/[^\"')]*)",
            "$1$2"
        );

        // 为产品描述中的图片统一追加浏览器原生懒加载与异步解码，减少详情页首屏阻塞
        fixed = fixed.replaceAll("<img(?![^>]*\\bloading=)([^>]*)>", "<img loading=\"lazy\" decoding=\"async\"$1>");
        fixed = fixed.replaceAll("<img([^>]*\\bloading=[\"'][^\"']+[\"'])(?![^>]*\\bdecoding=)([^>]*)>", "<img$1 decoding=\"async\"$2>");

        return fixed;
    }

    @Override
    public Product getProductDetail(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            return null;
        }
        fillProductRelations(product);
        // 修复描述中的图片URL
        product.setDescription(fixDescriptionImageUrls(product.getDescription()));
        product.setShortDescription(fixDescriptionImageUrls(product.getShortDescription()));
        return product;
    }

    @Override
    public Product getProductBySlug(String slug) {
        Product product = baseMapper.selectBySlug(slug);
        if (product == null) {
            return null;
        }
        fillProductRelations(product);
        // 修复描述中的图片URL
        product.setDescription(fixDescriptionImageUrls(product.getDescription()));
        product.setShortDescription(fixDescriptionImageUrls(product.getShortDescription()));
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

    /**
     * 将产品名称转换为 URL 友好的 slug
     */
    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UUID.randomUUID().toString().substring(0, 8);
        }
        String slug = name.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-+|-+$", "");
        if (slug.isEmpty()) {
            slug = UUID.randomUUID().toString().substring(0, 8);
        }
        return slug;
    }

    /**
     * 确保 slug 唯一，若重复则追加数字后缀
     */
    private String ensureUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int suffix = 1;
        while (true) {
            Product existing = baseMapper.selectBySlug(slug);
            if (existing == null || (excludeId != null && existing.getId().equals(excludeId))) {
                break;
            }
            slug = baseSlug + "-" + suffix++;
        }
        return slug;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public boolean saveProduct(Product product, List<Long> categoryIds, List<Long> tagIds, List<Long> imageIds) {
        // 设置时间戳
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        // 自动生成 slug（若未填写）
        if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
            String baseSlug = generateSlug(product.getName());
            product.setSlug(ensureUniqueSlug(baseSlug, null));
        }
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
        // 设置更新时间
        product.setUpdatedAt(LocalDateTime.now());
        // 自动生成 slug（若未填写）
        if (product.getSlug() == null || product.getSlug().trim().isEmpty()) {
            String baseSlug = generateSlug(product.getName());
            product.setSlug(ensureUniqueSlug(baseSlug, product.getId()));
        }
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "featuredProducts", allEntries = true)
    public boolean deleteProduct(Long id) {
        // 先清理关联表数据
        productCategoryMapper.deleteByProductId(id);
        tagRelationMapper.deleteByProductId(id);
        productImageMapper.deleteByProductId(id);
        // 再删除产品本身
        return this.removeById(id);
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
