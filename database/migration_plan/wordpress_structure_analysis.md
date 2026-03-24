# WordPress/WooCommerce 原始表结构分析

## 1. 产品 (Products)
- **wp_posts**: 存储产品基本信息。
    - `post_type = 'product'` 表示产品。
    - `post_title`: 产品名称。
    - `post_content`: 产品描述。
    - `post_excerpt`: 产品短描述。
    - `post_status`: 发布状态（publish, draft等）。
- **wp_postmeta**: 存储产品的扩展属性（价格、库存、SKU等）。
    - `_sku`: SKU。
    - `_regular_price`: 正式价格。
    - `_sale_price`: 促销价格。
    - `_price`: 当前价格。
    - `_stock`: 库存数量。
    - `_thumbnail_id`: 主图 ID（关联到 wp_posts 的附件）。

## 2. 分类与标签 (Categories & Tags)
- **wp_terms**: 存储名称和别名（slug）。
- **wp_term_taxonomy**: 存储 taxonomy 类型。
    - `taxonomy = 'product_cat'` 表示产品分类。
    - `taxonomy = 'product_tag'` 表示产品标签。
- **wp_term_relationships**: 关联产品与分类/标签。
    - `object_id`: 对应 wp_posts.ID。
    - `term_taxonomy_id`: 对应 wp_term_taxonomy.term_taxonomy_id。

## 3. 图片 (Images/Media)
- **wp_posts**: 存储图片元数据。
    - `post_type = 'attachment'` 且 `post_mime_type LIKE 'image/%'`。
    - `guid`: 图片原始 URL。
- **wp_postmeta**: 存储图片额外信息。
    - `_wp_attached_file`: 相对路径（如 `2023/10/image.jpg`）。
    - `_wp_attachment_metadata`: 序列化数据（包含缩略图信息）。
- **产品图片关联**:
    - 主图: `wp_postmeta` 中 `meta_key = '_thumbnail_id'`。
    - 轮播图: `wp_postmeta` 中 `meta_key = '_product_image_gallery'`（逗号分隔的 ID 列表）。

## 4. 菜单 (Menus)
- **wp_terms**: 存储菜单名称。
- **wp_term_taxonomy**: `taxonomy = 'nav_menu'`。
- **wp_posts**: 存储菜单项。
    - `post_type = 'nav_menu_item'`。
- **wp_postmeta**: 存储菜单项配置。
    - `_menu_item_object_id`: 指向的目标 ID（如页面 ID 或分类 ID）。
    - `_menu_item_menu_item_parent`: 父级菜单项 ID。
    - `_menu_item_url`: 自定义链接。
