package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("news")
public class News {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String content;

    private Long coverImageId;

    private String status;

    private LocalDateTime publishedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 非数据库字段：封面图对象 */
    @TableField(exist = false)
    private Image coverImage;

}
