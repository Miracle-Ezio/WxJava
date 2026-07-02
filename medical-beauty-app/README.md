# STARRY · 思达芮 客户长期管理小程序

> 北京思达芮医疗美容诊所有限公司
> 客户长期管理 / 治疗跟踪 / 规划提醒 / 储值会员小程序

## 目录结构

```
medical-beauty-app/
├── docs/             设计文档（PRD / DB 设计 / UI 风格稿）
├── server/           Spring Boot 后端 (Java 17 + WxJava + MyBatis-Plus)
└── miniapp/          原生微信小程序
```

## 🚀 想先看效果？

| 想看的东西 | 怎么做 | 用时 |
| --- | --- | --- |
| **只看小程序 UI** | 装"微信开发者工具" → 打开 `miniapp/` 选"测试号" → 编译 | 5 分钟 |
| **完整体验**（小程序 + 后台 Web + 真后端） | 装 Docker Desktop + Node.js → 跑 `docker compose up` → `npm run dev` | 15 分钟 |

完整启动步骤见 [`QUICK-START.md`](./QUICK-START.md) —— Windows 非开发者也能跟着做。

演示数据照《彭蕾抗衰整体规划方案》PPT 还原，登录后所有页面都能点。

## 🚢 想上线给客户用？

选一条路：

| 路线 | 覆盖范围 | 上线周期 | 首年成本 | 手册 |
| --- | --- | --- | --- | --- |
| **体验版（内部授权）** | 100 人以内白名单，甲方员工 + VIP 客户 | 3–7 天 | ~2500 元 | [`DEPLOY-INTERNAL.md`](./DEPLOY-INTERNAL.md) |
| **正式版（公域可搜）** | 全微信用户可搜可用 | 3–6 周（含审核） | ~2000–5000 元 | [`DEPLOY.md`](./DEPLOY.md) |

法务模板：[`LEGAL-PRIVACY.md`](./LEGAL-PRIVACY.md) · [`LEGAL-TERMS.md`](./LEGAL-TERMS.md)

## 当前进度（一期 MVP — 基本完成）

**已完成：**
- ✅ 设计文档 v0.2（PRD / DB / UI）
- ✅ 后端骨架：Spring Boot 3.2 + Java 17 + WxJava + MyBatis-Plus + Flyway
- ✅ 多租户 / 多门店双隔离
- ✅ 6 个 Flyway 版本：V1 核心 8 表 / V2 PPT seed / V3 customer_photo / V4 appointment / V5 operation_log / V6 reminders + subscribe_msg_log
- ✅ 客户端小程序登录（wx.login → code2Session → JWT）+ 员工端手机+密码登录
- ✅ 规划方案：客户端阅读 + 顾问端可视化制作器 + 推送给客户
- ✅ 照片模块：腾讯云 COS 私有桶 + 签名 URL + 4 机位引导 + 滑动/并排对比
- ✅ 预约模块：实时 availability + 事务防超卖 + 客户提交 + 员工状态机闭环（确认→到店→完成 / 取消 / 爽约）
- ✅ 订阅消息：4 个场景模板（预约确认 / 到店前 24h / 到店前 2h / 规划推送），客户端授权 + 后端发送 + 日志
- ✅ 提醒调度：Spring Scheduled，每 10 分钟扫 24h 提醒、每 5 分钟扫 2h 提醒，防重发
- ✅ 管理后台 Web：Vite + Vue 3 + Element Plus —— 数据看板 / 客户管理 / 客户 360 / 规划方案制作器 / 排期日历 / 预约管理 / 操作日志
- ✅ 操作日志：全链路审计（规划、预约、登录、推送）

**后续优化（按需做）：**
- ⏳ 储值卡 / 等级（待定阈值）
- ⏳ 多员工管理（目前一个人用，单账号 OK）
- ⏳ 规划方案 PDF 导出
- ⏳ 照片缩略图 + 90 天硬删调度
- ⏳ 真实订阅消息模板申请并替换占位 ID

## 后端

详见 [`server/README.md`](./server/README.md)

## 小程序

详见 [`miniapp/README.md`](./miniapp/README.md)

## 快速开始

```bash
# 1. 启动 MySQL 8 + Redis（本机或 docker）
docker run -d --name starry-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=starry_mb mysql:8.0
docker run -d --name starry-redis -p 6379:6379 redis:7-alpine

# 2. 配置环境变量（最少必填）
export WX_MA_APPID=wx你的小程序AppID
export WX_MA_SECRET=你的小程序AppSecret
# 照片模块需要：（未配置时应用能起来，上传会返回 50001）
export COS_SECRET_ID=你的腾讯云SecretId
export COS_SECRET_KEY=你的腾讯云SecretKey
export COS_REGION=ap-beijing
export COS_BUCKET=starry-private-1300000000

# 3. 启动后端（Flyway 自动建表 + 灌演示数据）
cd server
mvn spring-boot:run

# 4. 用微信开发者工具打开 miniapp/ 目录
#    修改 app.js apiBase / project.config.json appid 后导入
```

### 腾讯云 COS 桶要求

- 类型：**私有读写**（重要：医美照片不可公开）
- 跨域 CORS：允许 PUT/POST，Origin 允许小程序登录态域名
- 生命周期：建议对 `tenants/*/customers/*` 前缀启用 90 天后转低频，1 年后归档
