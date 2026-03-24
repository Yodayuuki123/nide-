package com.philitee.filter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.ProductTag;

import java.util.List;

/**
 * 产品标签 Service 接口
 */
public interface ProductTagService extends IService<ProductTag> {

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<ProductTag> getAllTags();

    /**
     * 根据slug获取标签
     *
     * @param slug 标签别名
     * @return 标签
     */
    ProductTag getTagBySlug(String slug);

    /**
     * 根据产品ID获取产品的所有标签
     *
     * @param productId 产品ID
     * @return 标签列表
     */
    List<ProductTag> getTagsByProductId(Long productId);

}
