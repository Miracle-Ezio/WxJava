# STARRY Mini-Program

微信原生小程序客户端。

## 目录

```
miniapp/
├── app.js / app.json / app.wxss
├── project.config.json    ← 用前修改 appid
├── sitemap.json
├── utils/
│   ├── request.js         统一请求封装 + JWT
│   └── auth.js            登录流程
└── pages/
    ├── login/             登录页（自定义品牌封面）
    ├── home/              首页 Tab：欢迎 + 下一项提醒 + 规划进度
    ├── photos/            对比 Tab（占位）
    ├── appointment/       预约 Tab（占位）
    ├── profile/           我的 Tab
    └── plan/
        ├── list           我的全部规划方案
        └── detail         规划方案详情（替代 PPT 的核心页面）
```

## 启动

1. 用微信开发者工具导入本目录
2. 修改 `project.config.json` 的 `appid` 为真实小程序 AppID
3. 修改 `app.js` 的 `apiBase` 为后端域名
4. 本地调试可在工具中勾选「不校验合法域名」

## 设计要点

- **风格**：纯白 / 暖白底 + 极简黑色衬线 logo + 星辰金点缀（呼应 STARRY · 思达芮"天空中闪耀的繁星"调性）。
- **登录态**：JWT 写入 `wx.setStorageSync`，`onShow` 中守门 `app.isLoggedIn()`。
- **请求**：`utils/request.js` 自动注入 `Authorization`；遇 401 自动重新登录；统一 toast。
- **页面切换**：tabBar 4 个 Tab + 内部 `wx.navigateTo` 进规划详情。
- **规划详情页**：复刻 PPT 章节结构（分析 / 分区规划 / 项目建议 / 材料 / 套餐报价），但每项可状态化 / 可点击 / 可联动预约。

## 待开发

- 拍照引导组件（4 机位蒙版 + 上一张半透明叠加）
- 时间轴对比 + 滑块叠加
- 预约日历组件
- 微信订阅消息授权封装
- 等级徽章 SVG / 启动图
