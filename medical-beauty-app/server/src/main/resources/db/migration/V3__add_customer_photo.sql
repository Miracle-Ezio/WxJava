-- ============================================================================
-- V3 · 客户照片模块
-- 设计要点：
--   1. 只存 object_key，不存 URL（URL 由签名服务实时生成，5 分钟过期）
--   2. pose 固定枚举，便于"同机位"对比
--   3. 软删除（deleted_at）+ COS 对象 90 天硬删（由调度任务执行）
--   4. visibility 控制员工可见范围
-- ============================================================================

SET NAMES utf8mb4;

CREATE TABLE `customer_photo` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `tenant_id`            BIGINT UNSIGNED NOT NULL,
  `store_id`             BIGINT UNSIGNED DEFAULT NULL,
  `customer_id`          BIGINT UNSIGNED NOT NULL,

  `pose`                 TINYINT         NOT NULL
    COMMENT '1正面 2左45 3右45 4顶光 5全脸特写 6局部',
  `body_part`            VARCHAR(30)     DEFAULT NULL
    COMMENT 'face/eye/mouth/neck/custom',
  `shot_at`              DATETIME        NOT NULL,

  `object_key`           VARCHAR(500)    NOT NULL COMMENT 'COS 私有桶 key',
  `thumb_key`            VARCHAR(500)    DEFAULT NULL,
  `width`                INT             DEFAULT NULL,
  `height`               INT             DEFAULT NULL,
  `size_bytes`           INT             DEFAULT NULL,

  `treatment_record_id`  BIGINT UNSIGNED DEFAULT NULL COMMENT '关联本次治疗（可空）',
  `uploaded_by`          TINYINT         NOT NULL DEFAULT 1 COMMENT '1客户 2员工',
  `visibility`           TINYINT         NOT NULL DEFAULT 2
    COMMENT '1仅自己 2自己+顾问 3全店',
  `locked`               TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '客户单独锁定，员工不可见',
  `ai_face_landmarks`    JSON            DEFAULT NULL COMMENT '二期：人脸 SDK 自动对齐',
  `remark`               VARCHAR(255)    DEFAULT NULL,

  `created_at`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by`           BIGINT UNSIGNED DEFAULT NULL,
  `deleted_at`           DATETIME        DEFAULT NULL,

  PRIMARY KEY (`id`),
  KEY `idx_tenant_customer_pose_shot` (`tenant_id`, `customer_id`, `pose`, `shot_at` DESC),
  KEY `idx_tenant_customer_shot`      (`tenant_id`, `customer_id`, `shot_at` DESC),
  KEY `idx_treatment`                 (`treatment_record_id`),
  KEY `idx_deleted_at`                (`deleted_at`) COMMENT '硬删调度任务用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户照片';
