package com.philitee.filter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.ProductCategory;

import java.util.List;

/**
 * 产品分类 Service 接口
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     * 获取分类树形结构
     *
     * @return 树形分类列表
     */
    List<ProductCategory> getCategoryTree();

    /**
     * 获取所有顶级分类
     *
     * @return 顶级分类列表
     */
    List<ProductCategory> getTopLevelCategories();

    /**
     * 根据父分类ID获取子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategory> getChildCategories(Long parentId);

    /**
     * 根据slug获取分类
     *
     * @param slug URL别名
     * @return 分类
     */
    ProductCategory getCategoryBySlug(String slug);

    /**
     * 根据产品ID获取产品所属分类
     *
     * @param productId 产品ID
     * @return 分类列表
     */
    List<ProductCategory> getCategoriesByProductId(Long productId);

}
