package com.philitee.filter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.Product;

import java.util.List;

/**
 * 产品 Service 接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 根据ID获取产品详情（含图片、分类、标签）
     *
     * @param id 产品ID
     * @return 产品详情
     */
    Product getProductDetail(Long id);

    /**
     * 根据slug获取产品详情
     *
     * @param slug URL别名
     * @return 产品详情
     */
    Product getProductBySlug(String slug);

    /**
     * 分页查询已发布的产品
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    IPage<Product> getPublishedProducts(int pageNum, int pageSize);

    /**
     * 根据分类ID分页查询产品
     *
     * @param categoryId 分类ID
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 分页结果
     */
    IPage<Product> getProductsByCategory(Long categoryId, int pageNum, int pageSize);

    /**
     * 根据标签ID查询产品
     *
     * @param tagId 标签ID
     * @return 产品列表
     */
    List<Product> getProductsByTag(Long tagId);

    /**
     * 搜索产品
     *
     * @param keyword 关键词
     * @return 产品列表
     */
    List<Product> searchProducts(String keyword);

    /**
     * 获取精选产品（首页展示用）
     *
     * @param limit 数量限制
     * @return 产品列表
     */
    List<Product> getFeaturedProducts(int limit);

    /**
     * 保存产品（含分类、标签、图片关联）
     *
     * @param product     产品实体
     * @param categoryIds 分类ID列表
     * @param tagIds      标签ID列表
     * @param imageIds    图片ID列表
     * @return 是否成功
     */
    boolean saveProduct(Product product, List<Long> categoryIds, List<Long> tagIds, List<Long> imageIds);

    /**
     * 更新产品（含分类、标签、图片关联）
     *
     * @param product     产品实体
     * @param categoryIds 分类ID列表
     * @param tagIds      标签ID列表
     * @param imageIds    图片ID列表
     * @return 是否成功
     */
    boolean updateProduct(Product product, List<Long> categoryIds, List<Long> tagIds, List<Long> imageIds);

    /**
     * 删除产品（同时清理分类、标签、图片关联表数据）
     *
     * @param id 产品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long id);

}
