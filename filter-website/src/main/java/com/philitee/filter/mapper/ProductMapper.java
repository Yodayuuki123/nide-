package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.philitee.filter.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 产品 Mapper 接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 根据分类ID查询产品列表（通过 product_product_category 关联表）
     *
     * @param categoryId 分类ID
     * @return 产品列表
     */
    @Select("SELECT p.* FROM product p " +
            "INNER JOIN product_product_category ppc ON p.id = ppc.product_id " +
            "WHERE ppc.category_id = #{categoryId} AND p.status = 'publish' " +
            "ORDER BY p.created_at DESC")
    List<Product> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID分页查询产品
     *
     * @param page       分页参数
     * @param categoryId 分类ID
     * @return 分页产品列表
     */
    @Select("SELECT p.* FROM product p " +
            "INNER JOIN product_product_category ppc ON p.id = ppc.product_id " +
            "WHERE ppc.category_id = #{categoryId} AND p.status = 'publish' " +
            "ORDER BY p.created_at DESC")
    IPage<Product> selectPageByCategoryId(Page<Product> page, @Param("categoryId") Long categoryId);

    /**
     * 根据标签ID查询产品列表
     *
     * @param tagId 标签ID
     * @return 产品列表
     */
    @Select("SELECT p.* FROM product p " +
            "INNER JOIN product_tag_relation ptr ON p.id = ptr.product_id " +
            "WHERE ptr.tag_id = #{tagId} AND p.status = 'publish' " +
            "ORDER BY p.created_at DESC")
    List<Product> selectByTagId(@Param("tagId") Long tagId);

    /**
     * 根据slug查询产品
     *
     * @param slug URL别名
     * @return 产品
     */
    @Select("SELECT * FROM product WHERE slug = #{slug} AND status = 'publish'")
    Product selectBySlug(@Param("slug") String slug);

    /**
     * 模糊搜索产品（按名称）
     *
     * @param keyword 关键词
     * @return 产品列表
     */
    @Select("SELECT * FROM product WHERE status = 'publish' AND name LIKE CONCAT('%', #{keyword}, '%') ORDER BY created_at DESC")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

}
