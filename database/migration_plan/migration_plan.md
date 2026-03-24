# 企业外贸独立站迁移方案：WordPress/WooCommerce 到 Spring Boot/MyBatis/Thymeleaf/MySQL

## 引言

本方案旨在指导企业外贸独立站从现有的 WordPress + WooCommerce 架构迁移至更现代化、高性能的 Spring Boot + MyBatis + Thymeleaf + MySQL 技术栈。迁移范围主要包括菜单、产品（产品、分类、标签）和图片维护功能。我们将对原有复杂表结构进行简化设计，并提供详细的数据迁移 SQL 脚本和实施步骤，特别是针对图片数据的迁移。

## WordPress/WooCommerce 原始表结构分析

WordPress 和 WooCommerce 的数据存储高度依赖于其核心表 `wp_posts` 和 `wp_postmeta`，以及用于分类体系的 `wp_terms`、`wp_term_taxonomy` 和 `wp_term_relationships`。这种设计虽然灵活，但在特定业务场景下可能导致数据冗余和查询复杂性。

### 关键数据存储概览：

*   **产品 (Products)**: 主要存储在 `wp_posts` 表中，`post_type` 为 `product`。产品的价格、SKU、库存等扩展属性存储在 `wp_postmeta` 表中，通过 `post_id` 关联。主图信息也通过 `_thumbnail_id` 存储在 `wp_postmeta` 中。
*   **分类与标签 (Categories & Tags)**: 分类和标签的名称及别名存储在 `wp_terms` 表。其类型（如 `product_cat` 或 `product_tag`）和层级关系存储在 `wp_term_taxonomy` 表。产品与分类/标签的关联则通过 `wp_term_relationships` 表维护。
*   **图片 (Images/Media)**: 图片作为附件存储在 `wp_posts` 表中，`post_type` 为 `attachment`。图片的原始 URL (`guid`) 和相对路径 (`_wp_attached_file`)、尺寸等元数据存储在 `wp_postmeta` 表。产品轮播图通常以逗号分隔的图片 ID 列表形式存储在 `_product_image_gallery` 元数据中。
*   **菜单 (Menus)**: WordPress 菜单项也是一种特殊的 `post_type` (`nav_menu_item`) 存储在 `wp_posts` 表中。菜单项的父子关系、链接目标等配置信息存储在 `wp_postmeta` 表中。

## 新系统表结构设计

新的表结构设计遵循单一职责原则，将不同业务数据分离到独立的表中，并通过外键关联建立联系，以提高数据库的清晰度、可维护性和扩展性。

### 1. 菜单管理 (`sys_menu`)

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT   | 20        | 否       |        | 主键，菜单ID                           |
| `parent_id`    | BIGINT   | 20        | 否       | 0      | 父菜单ID，0表示根菜单                  |
| `name`         | VARCHAR  | 100       | 否       |        | 菜单名称                               |
| `url`          | VARCHAR  | 255       | 是       |        | 菜单链接                               |
| `sort_order`   | INT      | 11        | 否       | 0      | 排序字段，用于菜单显示顺序             |
| `is_active`    | TINYINT  | 1         | 否       | 1      | 是否启用 (1: 启用, 0: 禁用)            |
| `created_time` | DATETIME |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME |           | 否       |        | 更新时间                               |

### 2. 产品管理

#### `product` 表 (产品基本信息)

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT   | 20        | 否       |        | 主键，产品ID                           |
| `name`         | VARCHAR  | 255       | 否       |        | 产品名称                               |
| `sku`          | VARCHAR  | 100       | 是       |        | 产品SKU                                |
| `regular_price`| DECIMAL  | 10,2      | 否       | 0.00   | 市场价格                               |
| `sale_price`   | DECIMAL  | 10,2      | 是       | NULL   | 促销价格 (如果无促销则为NULL)          |
| `description`  | TEXT     |           | 是       |        | 产品详细描述                           |
| `short_description` | VARCHAR  | 500       | 是       |        | 产品简短描述                           |
| `stock_quantity` | INT      | 11        | 否       | 0      | 库存数量                               |
| `status`       | VARCHAR  | 50        | 否       | 'draft'| 产品状态 (e.g., 'publish', 'draft')    |
| `main_image_id`| BIGINT   | 20        | 是       | NULL   | 主图片ID，关联 `image` 表              |
| `created_time` | DATETIME |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME |           | 否       |        | 更新时间                               |

#### `product_category` 表 (产品分类)

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT   | 20        | 否       |        | 主键，分类ID                           |
| `parent_id`    | BIGINT   | 20        | 否       | 0      | 父分类ID，0表示根分类                  |
| `name`         | VARCHAR  | 100       | 否       |        | 分类名称                               |
| `slug`         | VARCHAR  | 100       | 否       |        | 分类别名 (URL友好名称)                 |
| `description`  | VARCHAR  | 500       | 是       |        | 分类描述                               |
| `sort_order`   | INT      | 11        | 否       | 0      | 排序字段                               |
| `created_time` | DATETIME |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME |           | 否       |        | 更新时间                               |

#### `product_tag` 表 (产品标签)

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT   | 20        | 否       |        | 主键，标签ID                           |
| `name`         | VARCHAR  | 100       | 否       |        | 标签名称                               |
| `slug`         | VARCHAR  | 100       | 否       |        | 标签别名 (URL友好名称)                 |
| `created_time` | DATETIME |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME |           | 否       |        | 更新时间                               |

#### `product_category_relation` 表 (产品与分类关联)

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `product_id`   | BIGINT   | 20        | 否       |        | 产品ID，关联 `product` 表              |
| `category_id`  | BIGINT   | 20        | 否       |        | 分类ID，关联 `product_category` 表     |

#### `product_tag_relation` 表 (产品与标签关联)

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `product_id`   | BIGINT   | 20        | 否       |        | 产品ID，关联 `product` 表              |
| `tag_id`       | BIGINT   | 20        | 否       |        | 标签ID，关联 `product_tag` 表          |

### 3. 图片管理

#### `image` 表

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT   | 20        | 否       |        | 主键，图片ID                           |
| `file_name`    | VARCHAR  | 255       | 否       |        | 文件名 (包含扩展名)                    |
| `file_path`    | VARCHAR  | 500       | 否       |        | 图片存储路径 (相对路径或URL)           |
| `alt_text`     | VARCHAR  | 255       | 是       |        | 图片替代文本 (SEO友好)                 |
| `width`        | INT      | 11        | 是       | NULL   | 图片宽度                               |
| `height`       | INT      | 11        | 是       | NULL   | 图片高度                               |
| `mime_type`    | VARCHAR  | 100       | 是       |        | MIME类型 (e.g., image/jpeg)            |
| `uploaded_time`| DATETIME |           | 否       |        | 上传时间                               |

#### `product_image_relation` 表 (产品与图片关联)

| 字段名         | 数据类型 | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------- | :-------- | :------- | :----- | :------------------------------------- |
| `product_id`   | BIGINT   | 20        | 否       |        | 产品ID，关联 `product` 表              |
| `image_id`     | BIGINT   | 20        | 否       |        | 图片ID，关联 `image` 表                |
| `sort_order`   | INT      | 11        | 否       | 0      | 排序字段，用于产品图片显示顺序         |
| `is_main`      | TINYINT  | 1         | 否       | 0      | 是否为主图 (1: 是, 0: 否)              |

### 新表 DDL 建表 SQL 脚本

```sql
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
```

## 数据迁移方案

数据迁移是一个多阶段的过程，需要仔细规划和执行，以确保数据完整性和业务连续性。

### 迁移步骤

1.  **备份现有数据**: 在进行任何迁移操作之前，务必完整备份 WordPress 数据库和 `wp-content/uploads` 目录。
2.  **创建新数据库和表结构**: 在目标 MySQL 数据库中执行上述 DDL 建表 SQL 脚本，创建新的表结构。
3.  **图片文件迁移**: 将 WordPress `wp-content/uploads` 目录下的所有图片文件复制到新系统指定的图片存储目录（例如，Spring Boot 应用的 `static/images` 目录，或独立的图片服务器/CDN）。
4.  **执行数据迁移 SQL**: 按照以下顺序执行数据迁移 SQL 脚本，确保数据依赖关系正确处理。
5.  **验证数据**: 迁移完成后，对新系统中的数据进行全面验证，包括产品信息、分类、标签、图片关联和菜单等，确保数据准确无误。
6.  **新系统功能测试**: 在新系统上进行全面的功能测试，确保所有业务逻辑正常运行。
7.  **切换流量**: 确认新系统稳定运行后，逐步将流量切换到新系统。

### 数据迁移 SQL 脚本

以下 SQL 脚本用于将 WordPress/WooCommerce 的数据迁移到新设计的表结构中。请注意，这些脚本假定您的 WordPress 表前缀为 `wp_`。如果您的表前缀不同，请相应修改。

```sql
-- 数据迁移 SQL 脚本 (WordPress/WooCommerce -> New Schema)

-- 1. 迁移图片 (Images)
-- WordPress 的图片信息存储在 wp_posts (post_type='attachment')
-- 注意：这里的 file_path 假设您已将图片文件从 WordPress 的 wp-content/uploads 目录复制到新系统的某个可访问路径。
-- file_path 应该存储新系统中的相对路径或完整URL。
INSERT INTO `image` (id, file_name, file_path, alt_text, mime_type, uploaded_time)
SELECT 
    p.ID, 
    SUBSTRING_INDEX(p.guid, '/', -1) as file_name,
    REPLACE(pm.meta_value, CONCAT(YEAR(p.post_date), '/', MONTH(p.post_date), '/'), CONCAT('/images/', YEAR(p.post_date), '/', MONTH(p.post_date), '/')) as file_path, -- 示例：将 wp-content/uploads/2023/10/image.jpg 转换为 /images/2023/10/image.jpg
    p.post_excerpt as alt_text,
    p.post_mime_type as mime_type,
    p.post_date as uploaded_time
FROM wp_posts p
LEFT JOIN wp_postmeta pm ON p.ID = pm.post_id AND pm.meta_key = '_wp_attached_file'
WHERE p.post_type = 'attachment' AND p.post_mime_type LIKE 'image/%';

-- 2. 迁移产品分类 (Product Categories)
INSERT INTO `product_category` (id, parent_id, name, slug, description, sort_order)
SELECT 
    t.term_id, 
    tt.parent, 
    t.name, 
    t.slug, 
    tt.description, 
    0 -- 默认排序为0，可根据实际需求调整
FROM wp_terms t
INNER JOIN wp_term_taxonomy tt ON t.term_id = tt.term_id
WHERE tt.taxonomy = 'product_cat';

-- 3. 迁移产品标签 (Product Tags)
INSERT INTO `product_tag` (id, name, slug)
SELECT 
    t.term_id, 
    t.name, 
    t.slug
FROM wp_terms t
INNER JOIN wp_term_taxonomy tt ON t.term_id = tt.term_id
WHERE tt.taxonomy = 'product_tag';

-- 4. 迁移产品基本信息 (Products)
INSERT INTO `product` (id, name, sku, regular_price, sale_price, description, short_description, stock_quantity, status, main_image_id, created_time, updated_time)
SELECT 
    p.ID,
    p.post_title,
    (SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_sku' LIMIT 1),
    CAST(IFNULL((SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_regular_price' LIMIT 1), 0) AS DECIMAL(10,2)),
    CAST((SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_sale_price' LIMIT 1) AS DECIMAL(10,2)),
    p.post_content,
    p.post_excerpt,
    CAST(IFNULL((SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_stock' LIMIT 1), 0) AS SIGNED),
    p.post_status,
    (SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_thumbnail_id' LIMIT 1),
    p.post_date,
    p.post_modified
FROM wp_posts p
WHERE p.post_type = 'product';

-- 5. 迁移产品与分类关联
INSERT INTO `product_category_relation` (product_id, category_id)
SELECT 
    tr.object_id, 
    tt.term_id
FROM wp_term_relationships tr
INNER JOIN wp_term_taxonomy tt ON tr.term_taxonomy_id = tt.term_taxonomy_id
WHERE tt.taxonomy = 'product_cat'
AND tr.object_id IN (SELECT id FROM product);

-- 6. 迁移产品与标签关联
INSERT INTO `product_tag_relation` (product_id, tag_id)
SELECT 
    tr.object_id, 
    tt.term_id
FROM wp_term_relationships tr
INNER JOIN wp_term_taxonomy tt ON tr.term_taxonomy_id = tt.term_taxonomy_id
WHERE tt.taxonomy = 'product_tag'
AND tr.object_id IN (SELECT id FROM product);

-- 7. 迁移产品图片关联 (主图)
INSERT INTO `product_image_relation` (product_id, image_id, sort_order, is_main)
SELECT 
    id, 
    main_image_id, 
    0, 
    1
FROM product 
WHERE main_image_id IS NOT NULL;

-- 8. 迁移产品图片关联 (轮播图/画廊)
-- WordPress 的轮播图存储在 _product_image_gallery 元数据中，是以逗号分隔的图片 ID 字符串。
-- 纯 SQL 难以直接处理这种字符串拆分和多行插入。建议通过编程方式（如 Python 脚本）进行处理。
-- 示例伪代码逻辑：
/*
FOR EACH product IN new_product_table:
    product_id = product.id
    gallery_image_ids_str = GET_META_VALUE(product_id, '_product_image_gallery') FROM wp_postmeta
    IF gallery_image_ids_str IS NOT NULL:
        gallery_image_ids_array = SPLIT(gallery_image_ids_str, ',')
        sort_order = 1
        FOR EACH image_id IN gallery_image_ids_array:
            IF image_id IS NOT main_image_id:
                INSERT INTO `product_image_relation` (product_id, image_id, sort_order, is_main) VALUES (product_id, image_id, sort_order, 0)
                sort_order = sort_order + 1
*/

-- 9. 迁移菜单 (Menus)
-- WordPress 菜单结构复杂，此处的 SQL 仅迁移基本菜单项。
-- 建议在新系统中手动重新配置菜单，或编写更复杂的程序来解析 WordPress 菜单结构。
INSERT INTO `sys_menu` (id, parent_id, name, url, sort_order, is_active, created_time, updated_time)
SELECT 
    p.ID,
    CAST(IFNULL((SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_menu_item_menu_item_parent' LIMIT 1), 0) AS SIGNED),
    p.post_title,
    (SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_menu_item_url' LIMIT 1),
    p.menu_order,
    1, -- 默认启用
    p.post_date,
    p.post_modified
FROM wp_posts p
WHERE p.post_type = 'nav_menu_item';
```

### 图片数据迁移特别说明

图片数据的迁移不仅仅是数据库中元数据的迁移，更重要的是物理文件的迁移。WordPress 默认将图片存储在 `wp-content/uploads` 目录下，并按年/月组织。新系统需要能够访问这些图片文件。

**具体步骤：**

1.  **复制图片文件**: 将整个 WordPress 站点的 `wp-content/uploads` 目录复制到新 Spring Boot 应用可以访问的静态资源目录，例如 `src/main/resources/static/images`，或者配置一个专门的图片服务器/CDN。
    *   **示例路径**: 如果 WordPress 图片路径是 `wp-content/uploads/2023/10/example.jpg`，在新系统中，您可能希望它位于 `/images/2023/10/example.jpg`。
2.  **调整 `image.file_path`**: 在上述数据迁移 SQL 中，`image.file_path` 字段的 `REPLACE` 函数是一个示例，用于将 WordPress 的相对路径转换为新系统可识别的路径。您需要根据实际的图片存储策略和新系统的访问路径来调整此函数。
    *   **如果使用本地存储**: `file_path` 应存储相对于应用根目录或静态资源目录的路径，例如 `/images/2023/10/example.jpg`。
    *   **如果使用 CDN/对象存储**: `file_path` 应存储完整的 CDN URL 或对象存储的访问路径，例如 `https://cdn.yourdomain.com/images/2023/10/example.jpg`。
3.  **Spring Boot 配置**: 在 Spring Boot 应用中，确保配置了静态资源处理器，以便能够正确访问这些图片文件。例如，在 `application.properties` 或 `application.yml` 中配置：
    ```properties
    # application.properties
    spring.web.resources.static-locations=classpath:/static/,file:/path/to/your/images/
    ```
    或者通过 `WebMvcConfigurer` 配置：
    ```java
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/images/**")
                    .addResourceLocations("file:/path/to/your/images/");
        }
    }
    ```
    这里的 `/path/to/your/images/` 应该替换为实际的图片文件存储路径。
4.  **产品轮播图处理**: 由于 WordPress 的轮播图存储方式（逗号分隔的 ID 字符串）不适合纯 SQL 迁移，建议编写一个简单的 Java 或 Python 脚本来读取 `wp_postmeta` 中 `_product_image_gallery` 的值，解析图片 ID 列表，然后逐一插入到新系统的 `product_image_relation` 表中。

## 总结

本迁移方案提供了一个从 WordPress/WooCommerce 到 Spring Boot + MyBatis + Thymeleaf + MySQL 的清晰路径。通过重新设计数据库结构，我们将获得一个更简洁、高效、易于维护和扩展的系统。详细的 DDL 脚本和数据迁移 SQL 将大大简化迁移过程，而对图片数据迁移的特别说明则确保了所有资产的完整性。在实施过程中，务必进行充分的测试和验证，以确保新系统的稳定性和数据准确性。
