-- ============================================================================
-- V6 · 预约闭环 + 订阅消息
-- ============================================================================

SET NAMES utf8mb4;

-- 预约表加提醒标记，避免重复发送
ALTER TABLE `appointment`
  ADD COLUMN `reminded_24h_at` DATETIME DEFAULT NULL COMMENT '24h 前提醒发送时间' AFTER `cancelled_at`,
  ADD COLUMN `reminded_2h_at`  DATETIME DEFAULT NULL COMMENT '2h 前提醒发送时间'  AFTER `reminded_24h_at`;

-- 订阅消息发送日志
CREATE TABLE `subscribe_msg_log` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`      BIGINT UNSIGNED NOT NULL,
  `customer_id`    BIGINT UNSIGNED NOT NULL,
  `openid`         VARCHAR(64)     NOT NULL,
  `template_id`    VARCHAR(64)     NOT NULL,
  `scene`          VARCHAR(50)     NOT NULL COMMENT 'appointment_confirmed/reminder_24h/reminder_2h/plan_pushed',
  `ref_type`       VARCHAR(50)     DEFAULT NULL COMMENT 'appointment/plan',
  `ref_id`         BIGINT UNSIGNED DEFAULT NULL,
  `data_json`      JSON            DEFAULT NULL,
  `success`        TINYINT(1)      NOT NULL DEFAULT 0,
  `wx_errcode`     INT             DEFAULT NULL,
  `wx_errmsg`      VARCHAR(500)    DEFAULT NULL,
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_tenant_scene_created` (`tenant_id`, `scene`, `created_at` DESC),
  KEY `idx_ref` (`ref_type`, `ref_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅消息发送日志';
