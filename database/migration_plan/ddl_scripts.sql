-- 创建新系统的数据库表结构 (Simplified Schema)

-- 1. 菜单管理
CREATE TABLE `sys_menu` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键，菜单ID',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父菜单ID，0表示根菜单',
    `name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
    `url` VARCHAR(255) DEFAULT NULL COMMENT '菜单链接',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序字段',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用 (1: 启用, 0: 禁用)',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单管理表';

-- 2. 图片管理
CREATE TABLE `image` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键，图片ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '图片存储路径',
    `alt_text` VARCHAR(255) DEFAULT NULL COMMENT '图片替代文本',
    `width` INT(11) DEFAULT NULL COMMENT '图片宽度',
    `height` INT(11) DEFAULT NULL COMMENT '图片高度',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    `uploaded_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片管理表';

-- 3. 产品分类
CREATE TABLE `product_category` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键，分类ID',
    `parent_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '父分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `slug` VARCHAR(100) NOT NULL COMMENT '分类别名',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序字段',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- 4. 产品标签
CREATE TABLE `product_tag` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键，标签ID',
    `name` VARCHAR(100) NOT NULL COMMENT '标签名称',
    `slug` VARCHAR(100) NOT NULL COMMENT '标签别名',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品标签表';

-- 5. 产品基本信息
CREATE TABLE `product` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键，产品ID',
    `name` VARCHAR(255) NOT NULL COMMENT '产品名称',
    `sku` VARCHAR(100) DEFAULT NULL COMMENT '产品SKU',
    `regular_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '市场价格',
    `sale_price` DECIMAL(10,2) DEFAULT NULL COMMENT '促销价格',
    `description` TEXT DEFAULT NULL COMMENT '产品详细描述',
    `short_description` VARCHAR(500) DEFAULT NULL COMMENT '产品简短描述',
    `stock_quantity` INT(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
    `status` VARCHAR(50) NOT NULL DEFAULT 'draft' COMMENT '产品状态',
    `main_image_id` BIGINT(20) DEFAULT NULL COMMENT '主图片ID',
    `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_sku` (`sku`),
    CONSTRAINT `fk_product_main_image` FOREIGN KEY (`main_image_id`) REFERENCES `image` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品基本信息表';

-- 6. 产品与分类关联
CREATE TABLE `product_category_relation` (
    `product_id` BIGINT(20) NOT NULL COMMENT '产品ID',
    `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
    PRIMARY KEY (`product_id`, `category_id`),
    CONSTRAINT `fk_pcr_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pcr_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品与分类关联表';

-- 7. 产品与标签关联
CREATE TABLE `product_tag_relation` (
    `product_id` BIGINT(20) NOT NULL COMMENT '产品ID',
    `tag_id` BIGINT(20) NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`product_id`, `tag_id`),
    CONSTRAINT `fk_ptr_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ptr_tag` FOREIGN KEY (`tag_id`) REFERENCES `product_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品与标签关联表';

-- 8. 产品与图片关联 (轮播图)
CREATE TABLE `product_image_relation` (
    `product_id` BIGINT(20) NOT NULL COMMENT '产品ID',
    `image_id` BIGINT(20) NOT NULL COMMENT '图片ID',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序字段',
    `is_main` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为主图 (1: 是, 0: 否)',
    PRIMARY KEY (`product_id`, `image_id`),
    CONSTRAINT `fk_pir_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pir_image` FOREIGN KEY (`image_id`) REFERENCES `image` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品与图片关联表';
