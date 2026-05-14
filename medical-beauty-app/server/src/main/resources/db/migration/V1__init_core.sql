-- ============================================================================
-- STARRY 思达芮 · 医美客户管理系统 V1 初始 schema
-- 一期聚焦"规划方案 (PPT 替代)"垂直切片所需表
-- 后续 V2/V3 迁移再补：照片、预约、治疗记录、财务、营销等
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 租户 (SaaS 阶段使用；一期单租户也走该表，便于平滑过渡)
-- ----------------------------------------------------------------------------
CREATE TABLE `tenant` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`                VARCHAR(100)    NOT NULL COMMENT '机构名',
  `legal_entity`        VARCHAR(200)    DEFAULT NULL COMMENT '法律主体',
  `logo_url`            VARCHAR(255)    DEFAULT NULL,
  `brand_primary_color` CHAR(7)         DEFAULT '#0A0A0A',
  `status`              TINYINT         NOT NULL DEFAULT 2 COMMENT '0停用 1试用 2正式',
  `expire_at`           DATETIME        DEFAULT NULL,
  `miniprogram_appid`   VARCHAR(32)     DEFAULT NULL,
  `miniprogram_secret`  VARCHAR(255)    DEFAULT NULL COMMENT 'AES 加密',
  `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`          DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户';

-- ----------------------------------------------------------------------------
-- 门店
-- ----------------------------------------------------------------------------
CREATE TABLE `store` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`      BIGINT UNSIGNED NOT NULL,
  `name`           VARCHAR(100)    NOT NULL,
  `address`        VARCHAR(255)    DEFAULT NULL,
  `phone`          VARCHAR(20)     DEFAULT NULL,
  `lat`            DECIMAL(10, 6)  DEFAULT NULL,
  `lng`            DECIMAL(10, 6)  DEFAULT NULL,
  `business_hours` JSON            DEFAULT NULL,
  `status`         TINYINT         NOT NULL DEFAULT 1,
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at`     DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门店';

-- ----------------------------------------------------------------------------
-- 员工 (顾问 / 操作师 / 前台 / 院长)
-- ----------------------------------------------------------------------------
CREATE TABLE `employee` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`       BIGINT UNSIGNED NOT NULL,
  `store_id`        BIGINT UNSIGNED DEFAULT NULL,
  `name`            VARCHAR(50)     NOT NULL,
  `phone`           VARCHAR(255)    DEFAULT NULL COMMENT 'AES 加密',
  `phone_hash`      CHAR(64)        DEFAULT NULL COMMENT 'SHA-256 索引',
  `password_hash`   VARCHAR(100)    DEFAULT NULL,
  `avatar_url`      VARCHAR(255)    DEFAULT NULL,
  `job_title`       VARCHAR(50)     DEFAULT NULL COMMENT '院长/顾问/操作师/前台',
  `role_code`       VARCHAR(50)     DEFAULT NULL,
  `commission_rate` DECIMAL(5, 2)   DEFAULT 0.00,
  `wx_openid`       VARCHAR(64)     DEFAULT NULL,
  `status`          TINYINT         NOT NULL DEFAULT 1,
  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`      BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at`      DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_store` (`tenant_id`, `store_id`),
  KEY `idx_tenant_phone_hash` (`tenant_id`, `phone_hash`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工';

-- ----------------------------------------------------------------------------
-- 客户
-- ----------------------------------------------------------------------------
CREATE TABLE `customer` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`           BIGINT UNSIGNED NOT NULL,
  `store_id`            BIGINT UNSIGNED DEFAULT NULL COMMENT '主门店',
  `unionid`             VARCHAR(64)     DEFAULT NULL,
  `openid`              VARCHAR(64)     DEFAULT NULL,
  `nickname`            VARCHAR(100)    DEFAULT NULL,
  `avatar_url`          VARCHAR(500)    DEFAULT NULL,
  `real_name`           VARCHAR(255)    DEFAULT NULL COMMENT 'AES',
  `gender`              TINYINT         DEFAULT 0,
  `birthday`            DATE            DEFAULT NULL,
  `phone`               VARCHAR(255)    DEFAULT NULL COMMENT 'AES',
  `phone_hash`          CHAR(64)        DEFAULT NULL,
  `id_card`             VARCHAR(255)    DEFAULT NULL COMMENT 'AES',
  `skin_type`           TINYINT         DEFAULT NULL COMMENT '1干 2油 3混 4敏',
  `allergy`             TEXT            DEFAULT NULL,
  `medical_history`     TEXT            DEFAULT NULL,
  `emergency_contact`   VARCHAR(255)    DEFAULT NULL,
  `level_code`          VARCHAR(30)     DEFAULT 'STARDUST',
  `total_recharge`      DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
  `balance`             DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
  `consultant_id`       BIGINT UNSIGNED DEFAULT NULL,
  `source`              VARCHAR(50)     DEFAULT NULL,
  `first_visit_at`      DATETIME        DEFAULT NULL,
  `last_visit_at`       DATETIME        DEFAULT NULL,
  `status`              TINYINT         NOT NULL DEFAULT 1,
  `remark`              TEXT            DEFAULT NULL,
  `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`          BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at`          DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_openid` (`tenant_id`, `openid`),
  KEY `idx_tenant_unionid` (`tenant_id`, `unionid`),
  KEY `idx_tenant_phone_hash` (`tenant_id`, `phone_hash`),
  KEY `idx_tenant_consultant` (`tenant_id`, `consultant_id`),
  KEY `idx_tenant_last_visit` (`tenant_id`, `last_visit_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户';

-- ----------------------------------------------------------------------------
-- 项目 (黑金超光子 / 童颜水光 / 芮艾缇少女枪 ...)
-- ----------------------------------------------------------------------------
CREATE TABLE `project` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`           BIGINT UNSIGNED NOT NULL,
  `name`                VARCHAR(100)    NOT NULL,
  `category`            VARCHAR(50)     DEFAULT NULL,
  `cover_url`           VARCHAR(500)    DEFAULT NULL,
  `description`         TEXT            DEFAULT NULL,
  `science_text`        LONGTEXT        DEFAULT NULL COMMENT '科普说明，规划方案引用',
  `duration_min`        INT             DEFAULT NULL,
  `default_cycle_rule`  JSON            DEFAULT NULL COMMENT '默认周期规则',
  `unit_price`          DECIMAL(12, 2)  DEFAULT NULL,
  `commission_rate`     DECIMAL(5, 2)   DEFAULT 0.00,
  `status`              TINYINT         NOT NULL DEFAULT 1,
  `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`          BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at`          DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_status` (`tenant_id`, `status`),
  KEY `idx_tenant_category` (`tenant_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目';

-- ----------------------------------------------------------------------------
-- 规划方案 (替代 PPT 的核心)
-- ----------------------------------------------------------------------------
CREATE TABLE `plan` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`            BIGINT UNSIGNED NOT NULL,
  `store_id`             BIGINT UNSIGNED DEFAULT NULL,
  `customer_id`          BIGINT UNSIGNED NOT NULL,
  `title`                VARCHAR(200)    NOT NULL,
  `subtitle`             VARCHAR(200)    DEFAULT NULL,
  `cover_url`            VARCHAR(500)    DEFAULT NULL,
  `consultant_id`        BIGINT UNSIGNED DEFAULT NULL,
  `valid_from`           DATE            DEFAULT NULL,
  `valid_to`             DATE            DEFAULT NULL,
  `total_price`          DECIMAL(12, 2)  DEFAULT 0.00,
  `discount_price`       DECIMAL(12, 2)  DEFAULT 0.00,
  `analysis_text`        LONGTEXT        DEFAULT NULL COMMENT '状况分析富文本',
  `analysis_photo_keys`  JSON            DEFAULT NULL COMMENT '标注图 COS key 数组',
  `status`               TINYINT         NOT NULL DEFAULT 1 COMMENT '1草稿 2已推送 3已签约 4执行中 5已完成 6已作废',
  `version`              INT             NOT NULL DEFAULT 1,
  `pushed_at`            DATETIME        DEFAULT NULL,
  `created_at`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`           BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at`           DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_customer_status` (`tenant_id`, `customer_id`, `status`),
  KEY `idx_tenant_consultant`      (`tenant_id`, `consultant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规划方案';

-- ----------------------------------------------------------------------------
-- 规划方案章节
-- ----------------------------------------------------------------------------
CREATE TABLE `plan_section` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`  BIGINT UNSIGNED NOT NULL,
  `plan_id`    BIGINT UNSIGNED NOT NULL,
  `sort`       INT             NOT NULL DEFAULT 0,
  `type`       VARCHAR(30)     NOT NULL COMMENT 'analysis/region_plan/project_list/material/package/case',
  `title`      VARCHAR(200)    NOT NULL,
  `content`    LONGTEXT        DEFAULT NULL COMMENT '富文本 / markdown',
  `extra`      JSON            DEFAULT NULL,
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at` DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plan_sort` (`plan_id`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规划方案章节';

-- ----------------------------------------------------------------------------
-- 规划方案项目 (规划 → 周期 → 提醒的源头)
-- ----------------------------------------------------------------------------
CREATE TABLE `plan_item` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`      BIGINT UNSIGNED NOT NULL,
  `plan_id`        BIGINT UNSIGNED NOT NULL,
  `section_id`     BIGINT UNSIGNED DEFAULT NULL,
  `project_id`     BIGINT UNSIGNED NOT NULL,
  `project_name`   VARCHAR(100)    DEFAULT NULL COMMENT '快照',
  `planned_count`  INT             NOT NULL DEFAULT 1,
  `done_count`     INT             NOT NULL DEFAULT 0,
  `cycle_rule`     JSON            DEFAULT NULL,
  `unit_price`     DECIMAL(12, 2)  DEFAULT NULL,
  `activity_price` DECIMAL(12, 2)  DEFAULT NULL,
  `total_price`    DECIMAL(12, 2)  DEFAULT NULL,
  `next_due_at`    DATETIME        DEFAULT NULL,
  `status`         TINYINT         NOT NULL DEFAULT 1 COMMENT '1未开始 2进行中 3已完成 4已取消',
  `notes`          VARCHAR(500)    DEFAULT NULL,
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`     BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at`     DATETIME        DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_plan` (`plan_id`),
  KEY `idx_tenant_due_status` (`tenant_id`, `next_due_at`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规划方案项目';

SET FOREIGN_KEY_CHECKS = 1;
