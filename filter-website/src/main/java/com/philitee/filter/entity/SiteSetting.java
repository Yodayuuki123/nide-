package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 网站设置实体
 */
@Data
@TableName("site_setting")
public class SiteSetting {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String settingKey;

    private String settingValue;

    private String settingGroup;

    private String displayName;

    /**
     * 输入类型：text, textarea, richtext, image, switch
     */
    private String inputType;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
