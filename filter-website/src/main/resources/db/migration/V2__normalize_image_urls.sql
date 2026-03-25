-- ============================================================
-- V2: Normalize image URLs to relative paths
-- ============================================================
-- 将数据库中的绝对图片URL转换为相对路径，便于新网站统一访问
--
-- 原始URL格式:
--   1. http://filter.philitee.com/wp-content/uploads/...     (1508条)
--   2. http://49.0.247.223/wordpress/wp-content/uploads/...   (114条)
--   3. http://filter.philitee.com/wordpress/wp-content/uploads/... (28条)
--
-- 转换后统一为:
--   /wp-content/uploads/...
--
-- 服务器上的文件映射:
--   /wp-content/uploads/** → /opt/website/wordpress/wp-content/uploads/ (优先)
--                          → /root/static_web/filter.philitee.com/wp-content/uploads/ (备选)
--   /wordpress/wp-content/uploads/** → /opt/website/wordpress/wp-content/uploads/
-- ============================================================

-- Pattern 1: http://filter.philitee.com/wp-content/uploads/... → /wp-content/uploads/...
UPDATE image
SET url = REPLACE(url, 'http://filter.philitee.com/wp-content/uploads/', '/wp-content/uploads/')
WHERE url LIKE 'http://filter.philitee.com/wp-content/uploads/%'
  AND url NOT LIKE 'http://filter.philitee.com/wordpress/%';

-- Pattern 2: http://49.0.247.223/wordpress/wp-content/uploads/... → /wp-content/uploads/...
UPDATE image
SET url = REPLACE(url, 'http://49.0.247.223/wordpress/wp-content/uploads/', '/wp-content/uploads/')
WHERE url LIKE 'http://49.0.247.223/wordpress/wp-content/uploads/%';

-- Pattern 3: http://filter.philitee.com/wordpress/wp-content/uploads/... → /wp-content/uploads/...
UPDATE image
SET url = REPLACE(url, 'http://filter.philitee.com/wordpress/wp-content/uploads/', '/wp-content/uploads/')
WHERE url LIKE 'http://filter.philitee.com/wordpress/wp-content/uploads/%';

-- Also update product description and short_description HTML that may contain absolute image URLs
UPDATE product
SET description = REPLACE(description, 'http://filter.philitee.com/wp-content/uploads/', '/wp-content/uploads/')
WHERE description LIKE '%http://filter.philitee.com/wp-content/uploads/%';

UPDATE product
SET description = REPLACE(description, 'http://49.0.247.223/wordpress/wp-content/uploads/', '/wp-content/uploads/')
WHERE description LIKE '%http://49.0.247.223/wordpress/wp-content/uploads/%';

UPDATE product
SET description = REPLACE(description, 'http://filter.philitee.com/wordpress/wp-content/uploads/', '/wp-content/uploads/')
WHERE description LIKE '%http://filter.philitee.com/wordpress/wp-content/uploads/%';

UPDATE product
SET short_description = REPLACE(short_description, 'http://filter.philitee.com/wp-content/uploads/', '/wp-content/uploads/')
WHERE short_description LIKE '%http://filter.philitee.com/wp-content/uploads/%';

UPDATE product
SET short_description = REPLACE(short_description, 'http://49.0.247.223/wordpress/wp-content/uploads/', '/wp-content/uploads/')
WHERE short_description LIKE '%http://49.0.247.223/wordpress/wp-content/uploads/%';

UPDATE product
SET short_description = REPLACE(short_description, 'http://filter.philitee.com/wordpress/wp-content/uploads/', '/wp-content/uploads/')
WHERE short_description LIKE '%http://filter.philitee.com/wordpress/wp-content/uploads/%';
