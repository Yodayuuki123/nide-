package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.ProductTagRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 产品与标签关联 Mapper 接口
 */
@Mapper
public interface ProductTagRelationMapper extends BaseMapper<ProductTagRelation> {

    /**
     * 删除产品的所有标签关联
     *
     * @param productId 产品ID
     * @return 删除行数
     */
    @Delete("DELETE FROM product_tag_relation WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);

    /**
     * 删除标签的所有产品关联
     *
     * @param tagId 标签ID
     * @return 删除行数
     */
    @Delete("DELETE FROM product_tag_relation WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 插入产品与标签关联
     *
     * @param productId 产品ID
     * @param tagId     标签ID
     * @return 插入行数
     */
    @Insert("INSERT INTO product_tag_relation (product_id, tag_id) VALUES (#{productId}, #{tagId})")
    int insertRelation(@Param("productId") Long productId, @Param("tagId") Long tagId);

}
