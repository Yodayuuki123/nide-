package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.*;
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
    @TableField("content")
    private String content;
    private String coverImage;
    private String status; // draft, published
    private Integer viewCount;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
