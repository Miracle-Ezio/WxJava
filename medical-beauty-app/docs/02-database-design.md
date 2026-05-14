# 数据库设计（MySQL 8.x）

> 版本：v0.1（草案）
> 字符集：`utf8mb4` / 排序：`utf8mb4_0900_ai_ci`
> 引擎：InnoDB
> 命名规范：表名小写下划线、单数；主键 `id BIGINT UNSIGNED AUTO_INCREMENT`；外键不建数据库 FK 约束（应用层保证），但建索引

---

## 0. 公共约定

每张业务表都包含以下"5 + 2"通用字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT UNSIGNED PK | 自增主键 |
| `tenant_id` | BIGINT UNSIGNED NOT NULL | 租户隔离（SaaS） |
| `store_id` | BIGINT UNSIGNED NULL | 门店隔离（连锁）。租户级数据为 NULL |
| `created_at` | DATETIME NOT NULL | 默认 CURRENT_TIMESTAMP |
| `updated_at` | DATETIME NOT NULL | ON UPDATE CURRENT_TIMESTAMP |
| `created_by` | BIGINT UNSIGNED NULL | 创建人 (employee.id 或 customer.id) |
| `deleted_at` | DATETIME NULL | 软删除（NULL = 未删除） |

**索引规约**：任何业务查询都从 `tenant_id` 起手 → 联合索引第一列必须是 `tenant_id`。

---

## 1. 实体关系总览

```
租户 (tenant)
 └── 门店 (store)
      ├── 员工 (employee)
      │    └── 角色 (role) ─── 权限 (permission)
      ├── 客户 (customer)
      │    ├── 客户标签 (customer_tag_relation → tag)
      │    ├── 照片 (customer_photo)
      │    ├── 规划方案 (plan) ─── 规划章节 (plan_section)
      │    │                     └── 规划项目 (plan_item)
      │    ├── 预约 (appointment)
      │    ├── 治疗记录 (treatment_record) ─── 材料用量 (treatment_material)
      │    ├── 储值卡 (wallet) ─── 流水 (wallet_transaction)
      │    ├── 套餐订单 (package_order) ─── 余次 (package_balance)
      │    ├── 消费订单 (consume_order) ─── 明细 (consume_order_item)
      │    ├── 提醒 (reminder)
      │    └── 等级 (level + level_history)
      ├── 项目 (project)
      ├── 产品 / 材料 (product)
      ├── 套餐 (package) ─── 套餐项目 (package_project)
      ├── 仪器 (device)
      ├── 排期日历 (schedule_slot)
      └── 优惠券 (coupon + coupon_grant)
```

---

## 2. 租户与门店

### `tenant` 租户
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| name | VARCHAR(100) | 机构名称 |
| logo_url | VARCHAR(255) | |
| brand_primary_color | CHAR(7) | 品牌主色（覆盖小程序主题）|
| status | TINYINT | 0 停用 1 试用 2 正式 |
| expire_at | DATETIME | SaaS 计费到期 |
| miniprogram_appid | VARCHAR(32) | |
| miniprogram_secret | VARCHAR(128) | AES 加密存储 |

### `store` 门店
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| tenant_id | BIGINT | |
| name | VARCHAR(100) | |
| address | VARCHAR(255) | |
| phone | VARCHAR(20) | |
| lat / lng | DECIMAL(10,6) | |
| business_hours | JSON | `{"mon":["10:00","21:00"], ...}` |
| status | TINYINT | |

索引：`UNIQUE(tenant_id, name)`，`INDEX(tenant_id, status)`

---

## 3. 员工与权限

### `employee` 员工
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| tenant_id / store_id | | |
| name | VARCHAR(50) | |
| phone | VARCHAR(20) | AES 加密 |
| password_hash | VARCHAR(100) | bcrypt |
| avatar_url | VARCHAR(255) | |
| role_id | BIGINT | |
| job_title | VARCHAR(50) | 院长 / 顾问 / 操作师 / 前台 |
| commission_rate | DECIMAL(5,2) | 业绩抽成 |
| status | TINYINT | |
| wx_open_id | VARCHAR(64) | 员工自身可绑微信，二期对接企微 |

索引：`UNIQUE(tenant_id, phone_hash)`

### `role` 角色 / `permission` 权限 / `role_permission`
经典 RBAC 三表，权限粒度按 **"模块 + 操作 + 数据范围"** 三段式：
`customer:read:self_only` / `customer:read:store_all` / `customer:read:tenant_all`

---

## 4. 客户

### `customer` 客户
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| tenant_id | | |
| primary_store_id | BIGINT | 主门店 |
| unionid | VARCHAR(64) | 微信 UnionID（跨租户唯一） |
| openid_app | VARCHAR(64) | 当前租户小程序 OpenID |
| nickname | VARCHAR(50) | 微信昵称 |
| real_name | VARCHAR(50) | AES 加密 |
| gender | TINYINT | 0 未知 1 女 2 男 |
| birthday | DATE | |
| phone | VARCHAR(20) | AES 加密 |
| phone_hash | CHAR(64) | SHA-256，用于查询 |
| id_card | VARCHAR(20) | AES 加密 |
| skin_type | TINYINT | 干 / 油 / 混 / 敏 |
| allergy | TEXT | 过敏史 |
| medical_history | TEXT | 病史 |
| emergency_contact | VARCHAR(50) | AES 加密 |
| level_id | BIGINT | 当前等级 |
| total_recharge | DECIMAL(12,2) | 累计充值（用于晋级判断） |
| balance | DECIMAL(12,2) | 当前余额（冗余 wallet.balance，便于列表） |
| consultant_id | BIGINT | 主顾问 employee.id |
| source | VARCHAR(50) | 来源（转介绍 / 抖音 / 朋友圈 …）|
| first_visit_at | DATETIME | |
| last_visit_at | DATETIME | |
| status | TINYINT | |
| remark | TEXT | |

索引：
- `INDEX(tenant_id, last_visit_at)`
- `INDEX(tenant_id, consultant_id)`
- `INDEX(tenant_id, phone_hash)`
- `INDEX(tenant_id, unionid)`

### `customer_tag` / `customer_tag_relation`
标签字典 + 多对多。

---

## 5. 照片（核心）

### `customer_photo`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| tenant_id / store_id | | |
| customer_id | BIGINT | |
| pose | TINYINT | 1 正面 2 左45 3 右45 4 顶光 5 全脸特写 6 局部 |
| body_part | VARCHAR(30) | face / eye / mouth / neck / 自定义 |
| shot_at | DATETIME | |
| object_key | VARCHAR(255) | COS 私有桶 key（不直接存 URL） |
| width / height | INT | |
| size_bytes | INT | |
| thumb_key | VARCHAR(255) | 缩略图 |
| treatment_record_id | BIGINT NULL | 关联本次治疗（可空，自拍也可） |
| uploaded_by | TINYINT | 1 客户 2 员工 |
| visibility | TINYINT | 1 仅自己 2 自己+顾问 3 全店 |
| locked | TINYINT(1) | 客户单独锁定 |
| ai_face_landmarks | JSON | 二期：用人脸 SDK 自动对齐 |
| remark | VARCHAR(255) | |

索引：
- `INDEX(tenant_id, customer_id, pose, shot_at DESC)` ← **照片时间轴主查询索引**
- `INDEX(tenant_id, customer_id, shot_at DESC)`
- `INDEX(treatment_record_id)`

> URL 生成：服务层取 `object_key` → 调 COS 临时签名（5 min）→ 返给前端。

---

## 6. 规划方案（替代 PPT）

### `plan` 规划方案
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| tenant_id / store_id | | |
| customer_id | BIGINT | |
| title | VARCHAR(100) | 例：彭蕾抗衰整体规划方案 |
| consultant_id | BIGINT | |
| valid_from / valid_to | DATE | |
| total_price | DECIMAL(12,2) | |
| discount_price | DECIMAL(12,2) | 活动价 |
| status | TINYINT | 1 草稿 2 已推送 3 已签约 4 执行中 5 已完成 6 已作废 |
| version | INT | 历史版本 |
| analysis_text | LONGTEXT | 状况分析（富文本 / Markdown） |
| analysis_photo_keys | JSON | 标注图 key 数组 |

### `plan_section` 规划章节
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| plan_id | BIGINT | |
| sort | INT | |
| type | VARCHAR(30) | analysis / region_plan / project_list / material / package / case |
| title | VARCHAR(100) | 例：第一步 T 区轮廓固定 |
| content | LONGTEXT | 富文本 |
| extra | JSON | 类型相关扩展 |

### `plan_item` 规划项目（核心：周期 → 提醒）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| plan_id | BIGINT | |
| section_id | BIGINT | |
| project_id | BIGINT | 引用 project |
| material_id | BIGINT NULL | |
| planned_count | INT | 计划次数 |
| done_count | INT | 已完成次数（冗余） |
| cycle_rule | JSON | `{"phase1":{"count":3,"interval_days":30},"phase2":{"interval_days":75}}` |
| unit_price / activity_price / total_price | DECIMAL(12,2) | |
| status | TINYINT | 1 未开始 2 进行中 3 已完成 4 已取消 |
| next_due_at | DATETIME | 由调度任务计算，驱动提醒 |

索引：`INDEX(tenant_id, next_due_at, status)` ← 提醒扫描

---

## 7. 项目 / 产品 / 套餐 / 仪器

### `project` 项目
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| tenant_id | | |
| name | VARCHAR(100) | 黑金超光子 / 童颜水光 ...|
| category | VARCHAR(50) | 光电 / 注射 / 手术 / 皮肤管理 |
| cover_url | VARCHAR(255) | |
| description | TEXT | |
| science_text | LONGTEXT | 科普说明（规划方案引用） |
| duration_min | INT | |
| default_device_ids | JSON | |
| default_material_ids | JSON | |
| default_cycle_rule | JSON | 同 plan_item.cycle_rule |
| unit_price | DECIMAL(12,2) | |
| commission_rate | DECIMAL(5,2) | |
| status | TINYINT | |

### `product` 产品 / 材料
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / tenant_id | | |
| name | VARCHAR(100) | 伊妍仕 / 艾维岚 / 童颜针 / 衡力 ... |
| type | VARCHAR(30) | injection / device_consumable / skincare |
| spec | VARCHAR(50) | 规格 |
| unit | VARCHAR(20) | 支 / 次 / ml |
| brand | VARCHAR(50) | |
| science_text | LONGTEXT | |

### `product_batch` 批号管理
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| product_id / tenant_id / store_id | | |
| batch_no | VARCHAR(50) | |
| qty_in / qty_remaining | DECIMAL(10,2) | |
| produce_date / expire_date | DATE | |
| supplier | VARCHAR(100) | |

索引：`INDEX(tenant_id, store_id, product_id, expire_date)` 临期预警

### `package` 套餐 / `package_project` 套餐内项目
套餐 = 多个项目 × 次数 + 总价 + 有效期。

### `device` 仪器
名称、型号、占用日历由 `schedule_slot` 关联。

---

## 8. 预约与排期

### `schedule_slot` 排期时段（库存模型）
将每天按 30 分钟切片，按"门店 + 顾问/操作师/仪器/房间"四维占用：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / tenant_id / store_id | | |
| resource_type | TINYINT | 1 员工 2 仪器 3 房间 |
| resource_id | BIGINT | |
| date | DATE | |
| start_time | TIME | |
| end_time | TIME | |
| capacity | INT | 默认 1 |
| used | INT | |
| appointment_id | BIGINT NULL | |
| status | TINYINT | 0 空闲 1 占用 2 锁定（自助预约 5 分钟内） |

索引：`UNIQUE(tenant_id, store_id, resource_type, resource_id, date, start_time)`

> 提交自助预约时：事务内对涉及到的 slot `SELECT ... FOR UPDATE`，避免超卖。

### `appointment` 预约单
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / tenant_id / store_id | | |
| customer_id | BIGINT | |
| project_ids | JSON | 单次可能多项目 |
| package_balance_id | BIGINT NULL | 如使用套餐余次 |
| consultant_id / operator_id | BIGINT NULL | |
| device_id | BIGINT NULL | |
| room_id | BIGINT NULL | |
| start_at / end_at | DATETIME | |
| source | TINYINT | 1 客户自助 2 顾问代约 3 到店现约 |
| status | TINYINT | 1 待确认 2 已确认 3 已到店 4 已完成 5 客户取消 6 机构取消 7 未到（爽约） |
| cancel_reason | VARCHAR(255) | |
| created_by | BIGINT | |

索引：`INDEX(tenant_id, store_id, start_at)`、`INDEX(tenant_id, customer_id, start_at DESC)`

---

## 9. 治疗记录（"做了什么"，合规核心）

### `treatment_record`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / tenant_id / store_id | | |
| customer_id | BIGINT | |
| appointment_id | BIGINT NULL | |
| project_id | BIGINT | |
| plan_item_id | BIGINT NULL | 推进 plan_item.done_count |
| operator_id / consultant_id | BIGINT | |
| device_id | BIGINT NULL | |
| performed_at | DATETIME | |
| pre_photo_ids | JSON | 关联 customer_photo.id |
| post_photo_ids | JSON | |
| signature_url | VARCHAR(255) | 知情同意签名图 |
| consent_form_id | BIGINT | |
| notes | TEXT | |

### `treatment_material` 本次实际材料用量（合规追溯）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| treatment_record_id | BIGINT | |
| product_id / batch_no | | |
| qty | DECIMAL(10,2) | |
| unit_cost | DECIMAL(12,2) | |

---

## 10. 财务

### `wallet` 储值钱包
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / tenant_id | | |
| customer_id | BIGINT UNIQUE | |
| balance | DECIMAL(12,2) | |
| frozen | DECIMAL(12,2) | |
| total_recharge / total_consume / total_gift | DECIMAL(12,2) | |

### `wallet_transaction` 流水（不可改、只可追加）
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | |
| wallet_id / customer_id / tenant_id / store_id | | |
| type | TINYINT | 1 充值 2 消费 3 退款 4 赠送 5 调整 |
| amount | DECIMAL(12,2) | 正负 |
| balance_after | DECIMAL(12,2) | 流水后余额（冗余便于对账） |
| ref_type / ref_id | VARCHAR / BIGINT | 关联订单 |
| pay_method | VARCHAR(20) | cash / wx_pay / card / transfer |
| operator_id | BIGINT | |
| remark | VARCHAR(255) | |

索引：`INDEX(tenant_id, customer_id, created_at DESC)`

### `package_order` 套餐订单 / `package_balance` 套餐余次
余次 = 客户在某店购买的某套餐还能用多少次，预约时扣减。

### `consume_order` 消费订单 / `consume_order_item`
单次到店的开单（可能扣余次 + 扣余额 + 现金 + 微信支付）。

---

## 11. 等级体系

### `level_config`
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / tenant_id | | |
| name | VARCHAR(50) | 普通 / 银卡 / 金卡 / 黑卡 / 钻石 |
| min_recharge | DECIMAL(12,2) | 累计充值门槛 |
| discount | DECIMAL(5,2) | 折扣 |
| benefits | JSON | 权益描述 |
| color | CHAR(7) | 徽章配色 |
| icon_url | VARCHAR(255) | |
| sort | INT | |

### `customer_level_history` 升降级历史
变更前后等级、触发原因（充值 / 手动 / 降级）、操作人。

---

## 12. 提醒与消息

### `reminder` 提醒
| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id / tenant_id / store_id | | |
| customer_id | BIGINT | |
| type | TINYINT | 1 项目周期 2 套餐到期 3 余额过低 4 生日 5 预约前 6 自定义 |
| title | VARCHAR(100) | |
| content | VARCHAR(500) | |
| ref_type / ref_id | | 关联 plan_item / appointment / package_balance ...|
| trigger_at | DATETIME | 触发时间 |
| channels | JSON | `["subscribe_msg","sms","inner"]` |
| status | TINYINT | 0 待发 1 已发 2 已读 3 已完成 4 已忽略 |

索引：`INDEX(tenant_id, status, trigger_at)` ← 调度扫描

### `subscribe_msg_log` 订阅消息发送日志
模板 ID、客户 OpenID、参数、微信返回码、错误信息。

### `wx_subscribe_quota` 订阅消息额度
每个客户每个模板剩余多少次（订阅消息一次性，需累积授权次数）。

---

## 13. 营销

### `coupon` 优惠券模板 / `coupon_grant` 客户持有
类型：满减 / 折扣 / 项目代金 / 升级券。

### `campaign` 活动 / `campaign_target` 投放目标客户

---

## 14. 系统

### `operation_log` 操作日志
| 字段 | 说明 |
| --- | --- |
| operator_type | 1 员工 2 客户 3 系统 |
| operator_id / tenant_id / store_id | |
| module / action / target_id | customer / update / 123 |
| before / after | JSON 字段级 diff |
| ip / ua | |

### `dict` 字典表
项目分类、来源、肤质等枚举可后台维护。

### `wx_template` 订阅消息模板
模板 ID、字段映射、模板内容。

---

## 15. 关键设计决策说明

1. **多租户 + 多门店双隔离**：所有业务表都带 `tenant_id`（不可空）+ `store_id`（租户级数据可空）。应用层用 ThreadLocal 注入，MyBatis Interceptor 自动追加 WHERE。
2. **照片只存 `object_key`**：URL 临时签名生成，杜绝公网泄露；前端不缓存原图 URL。
3. **不建数据库外键**：高并发下外键开销大；应用层用 Service 保证一致性；保留索引。
4. **金额一律 DECIMAL(12,2)**：禁用 FLOAT/DOUBLE。
5. **状态字段一律 TINYINT + 枚举类**：可读性靠应用层枚举。
6. **软删除**：业务表统一 `deleted_at`；财务流水绝不删（合规）。
7. **JSON 字段慎用**：仅用于"配置型 / 扩展型"字段（cycle_rule、business_hours），不用于需要查询的业务字段。
8. **历史与时点**：`plan` 走版本号，价格 / 等级要保留快照（订单内冗余 `level_snapshot_id`），避免回查时数据漂移。
9. **手机号双字段**：`phone`（AES 密文）+ `phone_hash`（SHA-256 索引）满足"加密存储 + 可查询"。

---

## 16. 一期建表清单（按依赖顺序）

1. tenant, store, role, permission, role_permission
2. employee
3. level_config
4. customer, customer_tag, customer_tag_relation
5. project, product, product_batch, device, package, package_project
6. plan, plan_section, plan_item
7. schedule_slot, appointment
8. treatment_record, treatment_material
9. customer_photo
10. wallet, wallet_transaction, package_order, package_balance, consume_order, consume_order_item
11. customer_level_history
12. reminder, subscribe_msg_log, wx_subscribe_quota, wx_template
13. coupon, coupon_grant
14. operation_log, dict

共约 30 张表，预估代码生成（MyBatis-Plus + 代码生成器）后约 1.5 天可建好骨架。
