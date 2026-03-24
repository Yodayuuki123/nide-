package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 产品分类 Mapper 接口
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 查询所有顶级分类
     *
     * @return 顶级分类列表
     */
    @Select("SELECT * FROM product_category WHERE parent_id IS NULL OR parent_id = 0 ORDER BY id ASC")
    List<ProductCategory> selectTopLevel();

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    @Select("SELECT * FROM product_category WHERE parent_id = #{parentId} ORDER BY id ASC")
    List<ProductCategory> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 根据slug查询分类
     *
     * @param slug URL别名
     * @return 分类
     */
    @Select("SELECT * FROM product_category WHERE slug = #{slug}")
    ProductCategory selectBySlug(@Param("slug") String slug);

    /**
     * 根据产品ID查询该产品所属的所有分类
     *
     * @param productId 产品ID
     * @return 分类列表
     */
    @Select("SELECT pc.* FROM product_category pc " +
            "INNER JOIN product_product_category ppc ON pc.id = ppc.category_id " +
            "WHERE ppc.product_id = #{productId}")
    List<ProductCategory> selectByProductId(@Param("productId") Long productId);

    /**
     * 统计某分类下的产品数量
     *
     * @param categoryId 分类ID
     * @return 产品数量
     */
    @Select("SELECT COUNT(*) FROM product_product_category WHERE category_id = #{categoryId}")
    Integer countProductsByCategoryId(@Param("categoryId") Long categoryId);

}
