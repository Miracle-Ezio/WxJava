# STARRY Admin · 顾问管理后台

Vue 3 + TypeScript + Vite + Element Plus + Pinia。

## 启动

后端必须先起来（`../server` 跑通 `mvn spring-boot:run`）。

```bash
cd medical-beauty-app/admin
npm install            # 国内可用 npm i --registry=https://registry.npmmirror.com
npm run dev
```

打开 http://localhost:5173/ ，会自动重定向到登录页。

### 演示账号

```
手机：13800000001
密码：starry123
```

启动后端时 `EmployeeSeedBootstrap` 会自动在 employee #1 沈妍希 上配好这套凭据。

### 反向代理

`vite.config.ts` 已配 `/api` 转发到 `http://localhost:8080`。后端跑在别的端口时设环境变量：

```bash
VITE_API_BASE=http://10.0.0.5:8080 npm run dev
```

## 页面

| 路径 | 用途 |
| --- | --- |
| `/login` | 顾问登录 |
| `/customers` | 客户管理（搜索 / 等级筛选） |
| `/customers/:id` | 客户 360（基础信息 + 该客户全部规划） |
| `/plans/new?customerId=X` | 新建规划方案 |
| `/plans/:id` | 编辑规划方案 |
| `/dashboard` | 数据看板（占位） |

## 规划方案制作器（核心）

- **顶栏**：标题 / 副标题 / 有效期 / 总价 / 活动价
- **状况分析**：富文本（Markdown）
- **章节区**：动态添加分区规划 / 项目建议 / 材料推荐 / 案例参考；上下移动 / 删除
- **套餐报价**：从项目库选项目，设置次数 / 单价 / 活动价，自动算小计
- **保存草稿**：随时存
- **推送给客户**：状态 → 已推送 + 写入 pushed_at；客户的小程序"我的规划"立即可见

## API

全部走 `/api/admin/**`，要求 JWT 主体类型为 EMPLOYEE：

| 方法 | 路径 |
| --- | --- |
| POST | `/api/admin/auth/login` |
| GET  | `/api/admin/auth/me` |
| GET  | `/api/admin/customers` |
| GET  | `/api/admin/customers/{id}` |
| GET  | `/api/admin/plans?customerId=` |
| GET  | `/api/admin/plans/{id}` |
| POST | `/api/admin/plans` |
| PUT  | `/api/admin/plans/{id}` |
| POST | `/api/admin/plans/{id}/push` |
| DELETE | `/api/admin/plans/{id}` |

## 待开发

- 员工管理（建立 / 改密 / 调整角色）
- 数据看板（今日预约 / 营业额 / 复购率）
- 项目库 / 产品库管理
- 排期日历（按门店 / 顾问 / 仪器视图）
- 操作日志
