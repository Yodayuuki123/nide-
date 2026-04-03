-- V5: Add attachment fields to inquiry table
SET @exist_path := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inquiry' AND COLUMN_NAME = 'attachment_path');
SET @sql_path := IF(@exist_path = 0, 'ALTER TABLE `inquiry` ADD COLUMN `attachment_path` VARCHAR(500) DEFAULT NULL AFTER `message`', 'SELECT 1');
PREPARE stmt_path FROM @sql_path;
EXECUTE stmt_path;
DEALLOCATE PREPARE stmt_path;

SET @exist_name := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'inquiry' AND COLUMN_NAME = 'attachment_name');
SET @sql_name := IF(@exist_name = 0, 'ALTER TABLE `inquiry` ADD COLUMN `attachment_name` VARCHAR(255) DEFAULT NULL AFTER `attachment_path`', 'SELECT 1');
PREPARE stmt_name FROM @sql_name;
EXECUTE stmt_name;
DEALLOCATE PREPARE stmt_name;
