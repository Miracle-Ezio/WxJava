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

## 当前进度（一期 MVP）

**已完成（一期已覆盖 3 个垂直切片）：**
- ✅ 设计文档 v0.2
- ✅ 后端骨架：Spring Boot + WxJava 登录 + JWT + 异常 + 拦截器
- ✅ 多租户 / 多门店字段 + MyBatis-Plus 自动填充
- ✅ Flyway 迁移：V1 核心 8 表 + V2 PPT 同款 seed + V3 customer_photo + V4 appointment
- ✅ 微信小程序登录（`wx.login` → `code2Session` → 入库 → 发 JWT）
- ✅ 规划方案 API + 详情页（替代 PPT 的核心）
- ✅ **照片模块**：腾讯云 COS（私有桶 + 签名 URL 5 min） + 后端中转上传 + tenant/customer 前缀隔离
- ✅ **小程序照片端**：时间轴 / 4 机位拍照引导 / 滑动 + 并排对比
- ✅ **预约模块**：项目库 + 实时 availability 算法 + 事务下单（`REPEATABLE_READ + FOR UPDATE` 防超卖）+ 客户取消（2h 前）
- ✅ **小程序预约端**：我的预约列表 / 详情 / 4 步预约向导（项目 → 14 天日期带 → 时段宫格 → 备注确认）/ 规划详情项目→一键预约联动

**待开发（一期剩余）：**
- ⏳ 储值卡 / 等级
- ⏳ 顾问端"规划方案制作器" + 管理后台 Web
- ⏳ 员工端预约确认 / 到店 / 完成、订阅消息提醒（24h / 2h）、PDF 导出
- ⏳ 照片增强：缩略图（异步生成）+ 90 天硬删调度任务 + 直传 STS 升级

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
