# 新系统表结构设计

根据将 WordPress/WooCommerce 迁移至 Spring Boot + MyBatis + Thymeleaf + MySQL 的需求，我们对原有的复杂表结构进行了简化和优化设计。核心原则是确保每个表存储单一业务数据，提高数据清晰度和维护性。

## 1. 菜单管理 (Menu Management)

考虑到菜单的层级结构和灵活性，我们设计了 `sys_menu` 表来统一管理网站导航菜单。

### `sys_menu` 表

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT        | 20        | 否       |        | 主键，菜单ID                           |
| `parent_id`    | BIGINT        | 20        | 否       | 0      | 父菜单ID，0表示根菜单                  |
| `name`         | VARCHAR       | 100       | 否       |        | 菜单名称                               |
| `url`          | VARCHAR       | 255       | 是       |        | 菜单链接                               |
| `sort_order`   | INT           | 11        | 否       | 0      | 排序字段，用于菜单显示顺序             |
| `is_active`    | TINYINT       | 1         | 否       | 1      | 是否启用 (1: 启用, 0: 禁用)            |
| `created_time` | DATETIME      |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME      |           | 否       |        | 更新时间                               |

## 2. 产品管理 (Product Management)

产品信息将拆分为 `product` (产品基本信息)、`product_category` (产品分类)、`product_tag` (产品标签) 以及它们之间的关联表。

### `product` 表

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |\n| `id`           | BIGINT        | 20        | 否       |        | 主键，产品ID                           |
| `name`         | VARCHAR       | 255       | 否       |        | 产品名称                               |
| `sku`          | VARCHAR       | 100       | 是       |        | 产品SKU                                |
| `regular_price`| DECIMAL       | 10,2      | 否       | 0.00   | 市场价格                               |
| `sale_price`   | DECIMAL       | 10,2      | 是       | NULL   | 促销价格 (如果无促销则为NULL)          |
| `description`  | TEXT          |           | 是       |        | 产品详细描述                           |
| `short_description` | VARCHAR       | 500       | 是       |        | 产品简短描述                           |
| `stock_quantity` | INT           | 11        | 否       | 0      | 库存数量                               |
| `status`       | VARCHAR       | 50        | 否       | 'draft'| 产品状态 (e.g., 'publish', 'draft')    |
| `main_image_id`| BIGINT        | 20        | 是       | NULL   | 主图片ID，关联 `image` 表              |
| `created_time` | DATETIME      |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME      |           | 否       |        | 更新时间                               |

### `product_category` 表

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT        | 20        | 否       |        | 主键，分类ID                           |
| `parent_id`    | BIGINT        | 20        | 否       | 0      | 父分类ID，0表示根分类                  |
| `name`         | VARCHAR       | 100       | 否       |        | 分类名称                               |
| `slug`         | VARCHAR       | 100       | 否       |        | 分类别名 (URL友好名称)                 |
| `description`  | VARCHAR       | 500       | 是       |        | 分类描述                               |
| `sort_order`   | INT           | 11        | 否       | 0      | 排序字段                               |
| `created_time` | DATETIME      |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME      |           | 否       |        | 更新时间                               |

### `product_tag` 表

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT        | 20        | 否       |        | 主键，标签ID                           |
| `name`         | VARCHAR       | 100       | 否       |        | 标签名称                               |
| `slug`         | VARCHAR       | 100       | 否       |        | 标签别名 (URL友好名称)                 |
| `created_time` | DATETIME      |           | 否       |        | 创建时间                               |
| `updated_time` | DATETIME      |           | 否       |        | 更新时间                               |

### `product_category_relation` 表 (产品与分类关联)

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |
| `product_id`   | BIGINT        | 20        | 否       |        | 产品ID，关联 `product` 表              |
| `category_id`  | BIGINT        | 20        | 否       |        | 分类ID，关联 `product_category` 表     |

### `product_tag_relation` 表 (产品与标签关联)

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |
| `product_id`   | BIGINT        | 20        | 否       |        | 产品ID，关联 `product` 表              |
| `tag_id`       | BIGINT        | 20        | 否       |        | 标签ID，关联 `product_tag` 表          |

## 3. 图片管理 (Image Management)

我们将所有图片信息统一存储在 `image` 表中，并通过关联表与产品进行多对多关联。

### `image` 表

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |
| `id`           | BIGINT        | 20        | 否       |        | 主键，图片ID                           |
| `file_name`    | VARCHAR       | 255       | 否       |        | 文件名 (包含扩展名)                    |
| `file_path`    | VARCHAR       | 500       | 否       |        | 图片存储路径 (相对路径或URL)           |
| `alt_text`     | VARCHAR       | 255       | 是       |        | 图片替代文本 (SEO友好)                 |
| `width`        | INT           | 11        | 是       | NULL   | 图片宽度                               |
| `height`       | INT           | 11        | 是       | NULL   | 图片高度                               |
| `mime_type`    | VARCHAR       | 100       | 是       |        | MIME类型 (e.g., image/jpeg)            |
| `uploaded_time`| DATETIME      |           | 否       |        | 上传时间                               |

### `product_image_relation` 表 (产品与图片关联)

| 字段名         | 数据类型      | 长度/精度 | 是否可空 | 默认值 | 备注                                   |
| :------------- | :------------ | :-------- | :------- | :----- | :------------------------------------- |
| `product_id`   | BIGINT        | 20        | 否       |        | 产品ID，关联 `product` 表              |
| `image_id`     | BIGINT        | 20        | 否       |        | 图片ID，关联 `image` 表                |
| `sort_order`   | INT           | 11        | 否       | 0      | 排序字段，用于产品图片显示顺序         |
| `is_main`      | TINYINT       | 1         | 否       | 0      | 是否为主图 (1: 是, 0: 否)              |


## 总结

新的表结构设计遵循了单一职责原则，将不同业务数据分离到独立的表中，并通过外键关联建立联系。这将大大提高数据库的清晰度、可维护性和扩展性，更符合 Spring Boot 应用的开发模式。
