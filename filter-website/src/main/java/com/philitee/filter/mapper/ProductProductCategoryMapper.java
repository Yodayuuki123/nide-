package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.ProductProductCategory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 产品与分类关联 Mapper 接口
 */
@Mapper
public interface ProductProductCategoryMapper extends BaseMapper<ProductProductCategory> {

    /**
     * 删除产品的所有分类关联
     *
     * @param productId 产品ID
     * @return 删除行数
     */
    @Delete("DELETE FROM product_product_category WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    /**
     * 删除分类的所有产品关联
     *
     * @param categoryId 分类ID
     * @return 删除行数
     */
    @Delete("DELETE FROM product_product_category WHERE category_id = #{categoryId}")
    int deleteByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 插入产品与分类关联
     *
     * @param productId  产品ID
     * @param categoryId 分类ID
     * @return 插入行数
     */
    @Insert("INSERT INTO product_product_category (product_id, category_id) VALUES (#{productId}, #{categoryId})")
    int insertRelation(@Param("productId") Long productId, @Param("categoryId") Long categoryId);

}
