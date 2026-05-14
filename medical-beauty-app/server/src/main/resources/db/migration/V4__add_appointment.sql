-- ============================================================================
-- V4 · 预约模块
--
-- 设计要点：
--   1. MVP 不预生成 schedule_slot 库存表，availability 由 business_hours
--      与已有 appointment 实时计算（store_id × 时间窗口 + concurrent_capacity）。
--      并发预约场景下，下单事务中对涉及时间窗 SELECT ... FOR UPDATE 防超卖。
--   2. status 状态机：1 待确认 → 2 已确认 → 3 已到店 → 4 已完成
--                    其它分支：5 客户取消 / 6 机构取消 / 7 未到（爽约）
--   3. project_name 等做快照，避免回看时项目改名 / 改价后数据漂移。
-- ============================================================================

SET NAMES utf8mb4;

ALTER TABLE `store`
  ADD COLUMN `concurrent_capacity` INT NOT NULL DEFAULT 1
  COMMENT '同一时段并发可接待数（房间 / 椅位）' AFTER `business_hours`;

UPDATE `store` SET `concurrent_capacity` = 2 WHERE `id` = 1;

-- 补 V2 seed 缺失的 duration_min，预约要用
UPDATE `project` SET `duration_min` = CASE `id`
  WHEN 1 THEN 60    -- 黑金超光子
  WHEN 2 THEN 90    -- 双生水光
  WHEN 3 THEN 60    -- 芮艾缇少女枪
  WHEN 4 THEN 90    -- 伊妍仕少女针
  WHEN 5 THEN 60    -- 艾维岚童颜针
  WHEN 6 THEN 30    -- 衡力包年
  ELSE 60
END WHERE `id` BETWEEN 1 AND 6;

CREATE TABLE `appointment` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`       BIGINT UNSIGNED NOT NULL,
  `store_id`        BIGINT UNSIGNED NOT NULL,
  `customer_id`     BIGINT UNSIGNED NOT NULL,

  `project_id`      BIGINT UNSIGNED NOT NULL,
  `project_name`    VARCHAR(100)    NOT NULL COMMENT '项目名快照',
  `unit_price`      DECIMAL(12, 2)  DEFAULT NULL COMMENT '价格快照',

  `consultant_id`   BIGINT UNSIGNED DEFAULT NULL,
  `operator_id`     BIGINT UNSIGNED DEFAULT NULL,
  `plan_item_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT '关联规划方案项目',

  `start_at`        DATETIME        NOT NULL,
  `end_at`          DATETIME        NOT NULL,
  `duration_min`    INT             NOT NULL,

  `source`          TINYINT         NOT NULL DEFAULT 1
    COMMENT '1客户自助 2顾问代约 3到店现约',
  `status`          TINYINT         NOT NULL DEFAULT 1
    COMMENT '1待确认 2已确认 3已到店 4已完成 5客户取消 6机构取消 7未到',

  `customer_note`   VARCHAR(500)    DEFAULT NULL,
  `staff_note`      VARCHAR(500)    DEFAULT NULL,
  `cancel_reason`   VARCHAR(255)    DEFAULT NULL,

  `confirmed_at`    DATETIME        DEFAULT NULL,
  `checked_in_at`   DATETIME        DEFAULT NULL,
  `completed_at`    DATETIME        DEFAULT NULL,
  `cancelled_at`    DATETIME        DEFAULT NULL,

  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`      BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at`      DATETIME        DEFAULT NULL,

  PRIMARY KEY (`id`),
  KEY `idx_tenant_store_start`     (`tenant_id`, `store_id`, `start_at`),
  KEY `idx_tenant_customer_start`  (`tenant_id`, `customer_id`, `start_at` DESC),
  KEY `idx_tenant_status_start`    (`tenant_id`, `status`, `start_at`),
  KEY `idx_plan_item`              (`plan_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约';
