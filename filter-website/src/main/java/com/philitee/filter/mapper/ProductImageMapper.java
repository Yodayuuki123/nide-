package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 产品图片关联 Mapper 接口
 */
@Mapper
public interface ProductImageMapper extends BaseMapper<ProductImage> {

    /**
     * 根据产品ID查询所有关联记录
     *
     * @param productId 产品ID
     * @return 关联记录列表
     */
    @Select("SELECT * FROM product_image WHERE product_id = #{productId} ORDER BY sort_order ASC")
    List<ProductImage> selectByProductId(@Param("productId") Long productId);

    /**
     * 删除产品的所有图片关联
     *
     * @param productId 产品ID
     * @return 删除行数
     */
    @Delete("DELETE FROM product_image WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

}
