package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 图片 Mapper 接口
 */
@Mapper
public interface ImageMapper extends BaseMapper<Image> {

    /**
     * 根据产品ID查询产品的所有图片（通过 product_image 关联表）
     *
     * @param productId 产品ID
     * @return 图片列表（按 sort_order 排序）
     */
    @Select("SELECT i.* FROM image i " +
            "INNER JOIN product_image pi ON i.id = pi.image_id " +
            "WHERE pi.product_id = #{productId} " +
            "ORDER BY pi.sort_order ASC")
    List<Image> selectByProductId(@Param("productId") Long productId);

    /**
     * 根据图片ID列表批量查询
     *
     * @param ids 图片ID列表
     * @return 图片列表
     */
    List<Image> selectBatchByIds(@Param("ids") List<Long> ids);

}
