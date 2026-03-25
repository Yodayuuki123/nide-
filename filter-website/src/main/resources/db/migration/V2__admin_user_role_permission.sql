-- ============================================
-- V2: 管理员用户、角色、权限管理表
-- ============================================

-- 管理员用户表
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `real_name` VARCHAR(100) DEFAULT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `admin_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '角色标识（如 ADMIN, OPERATOR）',
    `display_name` VARCHAR(100) NOT NULL COMMENT '角色显示名',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `admin_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '权限标识（如 product:view, product:edit）',
    `display_name` VARCHAR(100) NOT NULL COMMENT '权限显示名',
    `module` VARCHAR(50) NOT NULL COMMENT '所属模块（product, category, menu, image, inquiry, user, setting）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS `admin_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS `admin_role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 网站设置表
CREATE TABLE IF NOT EXISTS `site_setting` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `setting_key` VARCHAR(100) NOT NULL COMMENT '设置键',
    `setting_value` TEXT DEFAULT NULL COMMENT '设置值',
    `setting_group` VARCHAR(50) NOT NULL DEFAULT 'general' COMMENT '设置分组',
    `display_name` VARCHAR(100) DEFAULT NULL COMMENT '显示名称',
    `input_type` VARCHAR(20) DEFAULT 'text' COMMENT '输入类型：text, textarea, richtext, image, switch',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站设置表';

-- ============================================
-- 初始化数据
-- ============================================

-- 初始化超级管理员（密码: admin123，BCrypt加密）
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'Super Admin', 1);

-- 初始化角色
INSERT INTO `admin_role` (`name`, `display_name`, `description`) VALUES
('ADMIN', '系统管理员', '拥有所有权限的超级管理员'),
('OPERATOR', '运营人员', '负责产品发布和内容管理');

-- 初始化权限
INSERT INTO `admin_permission` (`name`, `display_name`, `module`) VALUES
-- 产品模块
('product:view', '查看产品', 'product'),
('product:create', '创建产品', 'product'),
('product:edit', '编辑产品', 'product'),
('product:delete', '删除产品', 'product'),
-- 分类模块
('category:view', '查看分类', 'category'),
('category:create', '创建分类', 'category'),
('category:edit', '编辑分类', 'category'),
('category:delete', '删除分类', 'category'),
-- 菜单模块
('menu:view', '查看菜单', 'menu'),
('menu:edit', '编辑菜单', 'menu'),
-- 图片模块
('image:view', '查看图片', 'image'),
('image:upload', '上传图片', 'image'),
('image:delete', '删除图片', 'image'),
-- 询盘模块
('inquiry:view', '查看询盘', 'inquiry'),
('inquiry:delete', '删除询盘', 'inquiry'),
-- 用户管理模块
('user:view', '查看用户', 'user'),
('user:create', '创建用户', 'user'),
('user:edit', '编辑用户', 'user'),
('user:delete', '删除用户', 'user'),
-- 角色管理模块
('role:view', '查看角色', 'role'),
('role:create', '创建角色', 'role'),
('role:edit', '编辑角色', 'role'),
('role:delete', '删除角色', 'role'),
-- 网站设置模块
('setting:view', '查看设置', 'setting'),
('setting:edit', '编辑设置', 'setting');

-- 超级管理员拥有所有权限
INSERT INTO `admin_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `admin_permission`;

-- 运营人员拥有产品、分类、图片、询盘的权限
INSERT INTO `admin_role_permission` (`role_id`, `permission_id`)
SELECT 2, id FROM `admin_permission`
WHERE `module` IN ('product', 'category', 'image', 'inquiry');

-- 给admin用户分配超级管理员角色
INSERT INTO `admin_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 初始化网站设置
INSERT INTO `site_setting` (`setting_key`, `setting_value`, `setting_group`, `display_name`, `input_type`, `sort_order`) VALUES
('site_name', 'Philitee Filter', 'general', '网站名称', 'text', 1),
('site_description', 'Professional Filtration Solutions', 'general', '网站描述', 'textarea', 2),
('site_logo', '', 'general', '网站Logo', 'image', 3),
('site_favicon', '', 'general', 'Favicon', 'image', 4),
('contact_email', '', 'contact', '联系邮箱', 'text', 10),
('contact_phone', '', 'contact', '联系电话', 'text', 11),
('contact_address', '', 'contact', '联系地址', 'textarea', 12),
('footer_copyright', '© 2026 Philitee Filter. All rights reserved.', 'general', '页脚版权', 'text', 20),
('seo_keywords', 'filter, filtration, industrial filter', 'seo', 'SEO关键词', 'text', 30),
('seo_description', '', 'seo', 'SEO描述', 'textarea', 31);
