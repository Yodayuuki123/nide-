-- 数据迁移 SQL 脚本 (WordPress/WooCommerce -> New Schema)

-- 1. 迁移图片 (Images)
-- WordPress 的图片信息存储在 wp_posts (post_type='attachment')
INSERT INTO `image` (id, file_name, file_path, alt_text, mime_type, uploaded_time)
SELECT 
    p.ID, 
    SUBSTRING_INDEX(p.guid, '/', -1) as file_name,
    pm.meta_value as file_path, -- 这里使用 wp_postmeta 中的 _wp_attached_file 相对路径
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
    tt.count -- 临时用 count 作为排序参考，或设为 0
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
-- 使用临时表或子查询来处理 postmeta 中的价格、SKU等
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

-- 7. 迁移产品图片关联 (Gallery)
-- WordPress 的轮播图存储在 _product_image_gallery 元数据中，是以逗号分隔的 ID 字符串
-- 注意：此步骤通常需要程序逻辑处理，纯 SQL 拆分字符串较复杂。
-- 这里提供一个思路：先迁移主图到关联表
INSERT INTO `product_image_relation` (product_id, image_id, sort_order, is_main)
SELECT 
    id, 
    main_image_id, 
    0, 
    1
FROM product 
WHERE main_image_id IS NOT NULL;

-- 8. 迁移菜单 (Menus)
-- WordPress 菜单较为复杂，通常建议手动重新配置或使用以下基础迁移
INSERT INTO `sys_menu` (id, parent_id, name, url, sort_order)
SELECT 
    p.ID,
    (SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_menu_item_menu_item_parent' LIMIT 1),
    p.post_title,
    (SELECT meta_value FROM wp_postmeta WHERE post_id = p.ID AND meta_key = '_menu_item_url' LIMIT 1),
    p.menu_order
FROM wp_posts p
WHERE p.post_type = 'nav_menu_item';
