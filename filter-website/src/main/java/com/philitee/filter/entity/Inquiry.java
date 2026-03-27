package com.philitee.filter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inquiry")
public class Inquiry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String company;

    @TableField("product_interest")
    private String productInterest;

    private String message;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("created_time")
    private LocalDateTime createdTime;
}
