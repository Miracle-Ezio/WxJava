-- ============================================================================
-- V5 · 操作日志
--
-- 关键设计：
--   1. 不建外键，最大限度避免业务表锁。索引保证查询性能。
--   2. before/after 字段存 JSON，用于字段级 diff（前端可展示"改了什么"）。
--   3. operator_type 区分员工 / 客户 / 系统（自动任务）。
--   4. 写入路径：业务 Service 显式调用 AuditLogger，不走 AOP，便于精准控制摘要。
-- ============================================================================

SET NAMES utf8mb4;

CREATE TABLE `operation_log` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`       BIGINT UNSIGNED NOT NULL,
  `store_id`        BIGINT UNSIGNED DEFAULT NULL,

  `operator_type`   TINYINT         NOT NULL COMMENT '1员工 2客户 3系统',
  `operator_id`     BIGINT UNSIGNED DEFAULT NULL,
  `operator_name`   VARCHAR(100)    DEFAULT NULL COMMENT '快照',

  `module`          VARCHAR(50)     NOT NULL COMMENT 'plan/appointment/customer/auth/...',
  `action`          VARCHAR(50)     NOT NULL COMMENT 'create/update/push/cancel/login/...',
  `target_id`       BIGINT UNSIGNED DEFAULT NULL,
  `target_summary`  VARCHAR(255)    DEFAULT NULL COMMENT '可读摘要：客户彭蕾 · 抗衰整体规划方案',

  `before_json`     JSON            DEFAULT NULL,
  `after_json`      JSON            DEFAULT NULL,

  `ip`              VARCHAR(50)     DEFAULT NULL,
  `user_agent`      VARCHAR(500)    DEFAULT NULL,

  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  KEY `idx_tenant_module_created` (`tenant_id`, `module`, `created_at` DESC),
  KEY `idx_tenant_operator_created` (`tenant_id`, `operator_type`, `operator_id`, `created_at` DESC),
  KEY `idx_tenant_target` (`tenant_id`, `module`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';
