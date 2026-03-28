-- 新闻表
CREATE TABLE IF NOT EXISTS `news` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL COMMENT '新闻标题',
    `slug` VARCHAR(255) COMMENT 'URL别名',
    `summary` TEXT COMMENT '新闻摘要',
    `content` LONGTEXT COMMENT '新闻正文（HTML）',
    `cover_image_id` BIGINT COMMENT '封面图ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'draft' COMMENT '状态：publish/draft',
    `published_at` DATETIME COMMENT '发布时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻表';
