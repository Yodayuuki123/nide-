package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.ProductTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 产品标签 Mapper 接口
 */
@Mapper
public interface ProductTagMapper extends BaseMapper<ProductTag> {

    /**
     * 根据产品ID查询该产品的所有标签
     *
     * @param productId 产品ID
     * @return 标签列表
     */
    @Select("SELECT pt.* FROM product_tag pt " +
            "INNER JOIN product_tag_relation ptr ON pt.id = ptr.tag_id " +
            "WHERE ptr.product_id = #{productId}")
    List<ProductTag> selectByProductId(@Param("productId") Long productId);

    /**
     * 根据slug查询标签
     *
     * @param slug 标签别名
     * @return 标签
     */
    @Select("SELECT * FROM product_tag WHERE slug = #{slug}")
    ProductTag selectBySlug(@Param("slug") String slug);

}
