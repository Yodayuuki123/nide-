# 外贸独立站 - Philitee Filter Website

基于 **Spring Boot + Thymeleaf + MyBatis-Plus + MySQL** 构建的外贸独立站系统，从 WordPress + WooCommerce 迁移而来。

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 |
| 页面渲染 | Thymeleaf（原生 HTML，SEO 友好） |
| ORM | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8.0 / MariaDB 10.6 |
| 安全 | Spring Security |
| 连接池 | Druid |

## 项目结构

```
nide-鸣至/
├── filter-website/          # Spring Boot 主项目
│   ├── src/main/java/       # Java 源代码
│   ├── src/main/resources/  # 配置文件 + Thymeleaf 模板 + i18n
│   └── pom.xml
├── database/                # 数据库相关文件
│   ├── web_site_full_dump.sql              # 完整数据库备份（MySQL 8.0）
│   ├── web_site_mariadb_compatible.sql     # MariaDB 兼容版本
│   └── migration_plan/                     # 迁移方案文档
│       ├── migration_plan.md
│       ├── new_schema_design.md
│       ├── wordpress_structure_analysis.md
│       ├── ddl_scripts.sql
│       └── migration_scripts.sql
├── 独立站改造.txt            # 客户需求文档
└── README.md
```

## 快速启动

### 1. 准备数据库

```bash
# MySQL 8.0
mysql -u root -p -e "CREATE DATABASE web_site CHARACTER SET utf8mb4;"
mysql -u root -p web_site < database/web_site_full_dump.sql

# MariaDB
mysql -u root -p web_site < database/web_site_mariadb_compatible.sql
```

### 2. 修改配置

编辑 `filter-website/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/web_site?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: Root@1234
```

### 3. 编译运行

```bash
cd filter-website
mvn clean package -DskipTests
java -jar target/filter-website-1.0.0.jar
```

访问 http://localhost:8080

## 功能模块

### 前台（用户访问）
- 首页（Hero + 统计 + 分类 + 精选产品 + 关于我们）
- 产品列表页（分页 + 分类筛选）
- 产品详情页（简要介绍 + 详细介绍）
- 关于我们 / 工厂介绍 / 联系我们
- 多语言切换（英文 / 中文）

### 后台（管理员）
- 管理员登录（账号：admin，密码：admin123）
- 产品管理（CRUD + 富文本编辑器）
- 分类管理（树形结构）
- 菜单管理
- 图片上传（按 WordPress 年/月目录规则存储）

## 数据库表结构

| 表名 | 说明 | 数据量 |
|------|------|--------|
| `product` | 产品主表 | 295 条 |
| `product_category` | 产品分类（支持层级） | 37 条 |
| `image` | 图片统一管理 | 1,650 条 |
| `menu` | 菜单组 | 21 条 |
| `menu_item` | 菜单项 | 138 条 |
| `product_image` | 产品-图片关联 | 417 条 |
| `product_product_category` | 产品-分类关联 | 335 条 |
| `product_tag` | 产品标签 | 2 条 |
| `product_tag_relation` | 产品-标签关联 | 180 条 |

## 图片目录

图片文件存储在服务器 `/opt/website/wordpress/wp-content/uploads/` 目录，按 WordPress 年/月规则组织。新系统通过 Spring Boot `ResourceHandler` 直接映射读取，无需迁移文件。
