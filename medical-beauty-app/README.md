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

**已完成（"规划方案"垂直切片）：**
- ✅ 设计文档 v0.2
- ✅ 后端骨架：Spring Boot + WxJava 登录 + JWT + 异常 + 拦截器
- ✅ 多租户 / 多门店字段 + MyBatis-Plus 自动填充
- ✅ Flyway 初始迁移（8 张核心表）+ PPT 同款演示数据
- ✅ 微信小程序登录（`wx.login` → `code2Session` → 入库 → 发 JWT）
- ✅ 规划方案 API：`GET /api/plans/mine`、`GET /api/plans/{id}`
- ✅ 小程序前端：登录页 / 首页 / 规划详情页 / 列表页 / Tab 占位

**待开发（一期剩余）：**
- ⏳ 照片对比模块（COS 私有桶 + 签名 URL）
- ⏳ 预约模块（schedule_slot 库存 + 行锁防超卖）
- ⏳ 储值卡 / 等级
- ⏳ 顾问端"规划方案制作器"
- ⏳ 管理后台 Web

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

# 2. 启动后端
cd server
mvn spring-boot:run \
  -Dspring-boot.run.jvmArguments="-DWX_MA_APPID=xxx -DWX_MA_SECRET=xxx"

# 3. 用微信开发者工具打开 miniapp/ 目录
#    修改 app.js apiBase / project.config.json appid 后导入
```
