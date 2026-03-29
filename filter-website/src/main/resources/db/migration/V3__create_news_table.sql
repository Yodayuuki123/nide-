-- News table
CREATE TABLE IF NOT EXISTS `news` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL UNIQUE,
    `summary` TEXT,
    `content` LONGTEXT,
    `cover_image` VARCHAR(500),
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT 'draft, published',
    `view_count` INT DEFAULT 0,
    `deleted` TINYINT(1) DEFAULT 0,
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_slug` (`slug`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='News articles';
