package com.philitee.filter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片 Service 接口
 */
public interface ImageService extends IService<Image> {

    /**
     * 根据产品ID查询产品的所有图片
     *
     * @param productId 产品ID
     * @return 图片列表
     */
    List<Image> getImagesByProductId(Long productId);

    /**
     * 上传图片（按WordPress年/月目录规则存储）
     *
     * @param file 上传的文件
     * @return 保存后的图片实体
     */
    Image uploadImage(MultipartFile file);

    /**
     * 删除图片（同时删除物理文件）
     *
     * @param id 图片ID
     * @return 是否成功
     */
    boolean deleteImage(Long id);

}
