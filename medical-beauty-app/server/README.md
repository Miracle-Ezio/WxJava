# STARRY Medical Beauty Server

Spring Boot 3.2 + Java 17 后端，集成 WxJava 微信小程序登录。

## 技术栈

| 类别 | 选型 |
| --- | --- |
| Web 框架 | Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.7 |
| DB Migration | Flyway |
| 微信 SDK | WxJava (`wx-java-miniapp-spring-boot-starter`) 4.8.3.B |
| 缓存 | Redis (Spring Data Redis) |
| Auth | JWT (jjwt 0.12) |
| 工具 | Lombok / Hutool / commons-lang3 |

## 包结构

```
com.starry.mb
├── MedicalBeautyApplication
├── common
│   ├── config        (WebMvc / MyBatisPlus)
│   ├── context       (PrincipalContext — tenant/store/user ThreadLocal)
│   ├── domain        (BaseEntity — 公共字段)
│   ├── exception     (BizException / GlobalExceptionHandler)
│   ├── security      (JwtUtil / AuthInterceptor)
│   └── web           (ApiResponse)
├── auth              (微信登录)
├── customer          (客户)
├── employee          (员工)
└── plan              (规划方案 ← 一期核心模块)
```

## 关键设计

- **多租户 + 多门店双隔离**：所有业务表带 `tenant_id` / `store_id`，由 `MetaObjectHandler` 在写入时从 `PrincipalContext` 自动填充。查询层需开发者显式带条件（一期演示阶段单租户，二期开 SaaS 时再补租户拦截 SQL）。
- **JWT Stateless**：登录返回 `Bearer Token`，含 `subject(id) / type / tid / sid`，由 `AuthInterceptor` 在 `/api/**` 解析并放入 `PrincipalContext`。
- **白名单**：`/api/auth/**`、`/api/public/**`、`/actuator/**`。
- **逻辑删除**：MyBatis-Plus `@TableLogic` 字段 `deleted_at`，业务代码无感知。

## 运行

```bash
# 环境变量（最少必填）
export DB_HOST=127.0.0.1
export DB_USER=root
export DB_PASSWORD=root
export REDIS_HOST=127.0.0.1
export WX_MA_APPID=wxxxxxx          # 小程序 AppID
export WX_MA_SECRET=xxxxxxxxxx      # 小程序 AppSecret
export JWT_SECRET=$(openssl rand -base64 48)

# 启动
mvn spring-boot:run
```

启动后 Flyway 会自动建表（V1）并插入演示数据（V2）。

## API 速查

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST   | `/api/auth/wx-login` | 微信小程序登录 | 否 |
| GET    | `/api/plans/mine` | 我的全部规划方案 | 是 |
| GET    | `/api/plans/{id}` | 规划方案详情 | 是 |
| POST   | `/api/photos` (multipart) | 上传照片（file + pose + visibility） | 是 |
| GET    | `/api/photos/timeline?pose=` | 按月分组时间轴 | 是 |
| GET    | `/api/photos/compare/quick?pose=` | 同机位 首张 vs 最新 | 是 |
| GET    | `/api/photos/compare?before=&after=` | 指定两张对比 | 是 |
| GET    | `/api/photos/{id}` | 照片详情（含签名 URL） | 是 |
| DELETE | `/api/photos/{id}` | 软删除（COS 对象 90 天后硬删） | 是 |
| POST   | `/api/photos/{id}/lock?lock=` | 锁定 / 解锁（员工不可见） | 是 |

返回统一格式：
```json
{ "code": 0, "message": "ok", "data": {...} }
```

## 配置说明（application.yml）

| Key | 默认 | 说明 |
| --- | --- | --- |
| `wx.miniapp.configs[0].appid` | `wx_demo_appid` | 小程序 AppID |
| `wx.miniapp.configs[0].secret` | `wx_demo_secret` | 小程序 AppSecret |
| `starry.jwt.secret` | 开发占位 | **生产必须从环境注入** |
| `starry.jwt.expires-hours` | 168 (7 天) | JWT 有效期 |
| `starry.cos.*` | 空 | 二期对象存储配置 |
| `starry.demo.default-tenant-id` | 1 | 演示阶段默认租户 |

## 下一步开发

按 PRD 优先级：
1. ~~**照片对比模块**~~ ✅ 已完成
2. **预约模块**（`schedule_slot` + `appointment` + 事务行锁）
3. **储值 / 等级**（`wallet` + `wallet_transaction` 不可改流水）
4. **顾问端规划方案编辑器 API**（POST/PUT `/api/admin/plans`）
5. **照片相关增强**：直传 STS 模式、缩略图生成、定时硬删 90 天前的软删对象
