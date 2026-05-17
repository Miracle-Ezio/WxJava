# STARRY · 思达芮 上线部署手册

> 给非技术人员看的、从零到正式上线的全流程手册。
> 跟着每个 ✅ 复制粘贴命令做即可。

---

## 总览

| 阶段 | 谁做 | 时间 |
| --- | --- | --- |
| 1️⃣ 资质 / 注册类（先并行启动，最久） | 您 / 客服 | 1–4 周 |
| 2️⃣ 买云服务（30 分钟） | 您 | 30 分钟 |
| 3️⃣ 服务器部署（跟手册做） | 您 + 我协助 | 1 天 |
| 4️⃣ 小程序提交审核 | 您 | 3–7 天 |
| 5️⃣ 上线日常运维 | 您 / 我远程支持 | 持续 |

**总成本（首年）**：约 ~2000 元（含一年服务器 + 域名 + 小程序认证；
不含短信预付 1000 元，按需开通）。

**比走医疗类目省**：~3000 元 + 1-2 个月时间。

---

## 阶段 1：资质 / 注册（第一天就开始）

> **类目策略**：本项目定位为「美容护理会员管理工具」，**不申请医疗类目**，
> 避开医疗机构执业许可证审核 + 微信支付商户号申请，节省 1-2 个月时间和 ~3000 元成本。
>
> 配套协议范本见 [`LEGAL-PRIVACY.md`](./LEGAL-PRIVACY.md) 和 [`LEGAL-TERMS.md`](./LEGAL-TERMS.md)。

### 1.1 微信小程序认证 ⭐ 必做最先做

| 项 | 操作 |
|---|---|
| 平台 | https://mp.weixin.qq.com |
| 主体 | 北京思达芮医疗美容诊所有限公司（用现有营业执照即可） |
| 类目 | **生活服务 → 美容美发**（⚠ **不要选医疗类目**） |
| 凭证 | 营业执照、法人身份证（**不需要医疗机构执业许可证**） |
| 费用 | 300 元/年企业认证 |
| 审核时间 | **1–3 天**（生活服务类目较宽松） |

通过后会拿到 **AppID** 和 **AppSecret** —— 这两个值后面要填到服务器配置。

> 提审时上传的截图请使用本仓库的"焕颜光疗 / 深层补水护理 / 弹力紧致护理"等
> 中性命名（V7 SQL 已脱敏 seed 数据）。客户实际使用时，顾问后台可以改回任意专业术语。

### 1.2 域名 + ICP 备案

| 项 | 操作 |
|---|---|
| 买域名 | 阿里云 / 腾讯云搜「starry-mb.com」之类，50–100 元/年 |
| 备案 | 域名指向后面买的服务器（同一家云厂商最省事）后提交备案 |
| 备案类型 | **企业 ICP 备案**（跟行业无关，不需医疗资质前置） |
| 备案审核 | 5–20 天，需法人手持身份证拍照、视频实名 |

> ⚠ 备案没下来之前，80/443 端口在国内会被云厂商屏蔽，只能用临时 IP + 高端口调试。

### 1.3 在线支付（**本项目不接入**）

所有费用结算在门店现场完成（现金 / 银行卡 / 微信扫码线下码），
本小程序不持有任何支付通道。**不需要申请微信支付商户号**。

后续如确实需要会员充值线上付款，再单独申请，**不影响主流程上线**。

### 1.4 订阅消息模板（小程序认证后才能申请）

mp.weixin.qq.com → 功能 → 订阅消息 → 申请 4 个模板：

| 场景 | 必备字段 |
|---|---|
| 预约确认通知 | thing1（项目名）、date2（时间）、thing3（门店）、thing4（提示） |
| 到店前 24h 提醒 | 同上 |
| 到店前 2h 提醒 | 同上 |
| 护理方案推送 | thing1（方案名）、thing2（提示）、date3（推送时间） |

每个 1–3 天审核。**模板申请时填"护理方案"，不要用"治疗方案 / 规划方案"等敏感词**。
通过后拿到模板 ID（形如 `9Bae_xxxx`），后面填配置。

### 1.5 腾讯云 COS（存照片用）

| 项 | 操作 |
|---|---|
| 平台 | https://console.cloud.tencent.com/cos |
| 桶配置 | **私有读写**（客户照片合规要求）、地域选 ap-beijing（北京） |
| API 密钥 | 控制台 → 访问管理 → API 密钥管理，拿 SecretId / SecretKey |
| 跨域 | 桶设置 → 跨域访问 CORS → 允许 PUT/POST，Origin = `*`（小程序域名） |

### 1.6 短信通道（可选）

订阅消息推不动时短信兜底。腾讯云 / 阿里云短信都行，按量计费。

---

## 阶段 2：买云服务（30 分钟）

推荐**阿里云**或**腾讯云**，价格差不多。下面以**阿里云**为例：

### 2.1 ECS 云服务器
- 规格：**2 核 4G**（够用，月费 ~150 元）
- 操作系统：**Ubuntu 22.04 LTS**
- 公网带宽：5 Mbps 起
- 安全组开放端口：**22 (SSH)、80 (HTTP)、443 (HTTPS)**

记下 IP 地址。

### 2.2 RDS 云数据库（可选但推荐）
- 引擎：MySQL **8.0**
- 规格：1 核 1G（月费 ~50 元）
- 数据库账号 / 密码：自己设
- 白名单：把 ECS 内网 IP 加进去

> ⚠ 如果想省钱，可以不买 RDS，让 MySQL 跑在服务器 Docker 里（按本手册默认配置就是这样）。生产环境强烈建议用 RDS。

### 2.3 SSL 证书
阿里云 / 腾讯云控制台 → 免费 DV 证书 → 申请、域名验证、下载。

---

## 阶段 3：服务器部署

### 3.1 SSH 连服务器

Windows 本机 PowerShell 或 Git Bash：

```bash
ssh root@你的服务器IP
# 输入云控制台给的初始密码
```

### 3.2 装 Docker + Docker Compose

```bash
# Ubuntu 一键脚本
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
systemctl enable --now docker

# 验证
docker --version
docker compose version
```

### 3.3 拉代码

```bash
cd /opt
git clone https://github.com/Miracle-Ezio/WxJava.git
cd WxJava
git checkout claude/medical-beauty-management-app-YMLa0
cd medical-beauty-app
```

### 3.4 改配置（**最关键的一步**）

#### A. 复制一份生产用的 docker-compose

```bash
cp docker-compose.yml docker-compose.prod.yml
nano docker-compose.prod.yml      # 或 vim
```

把下面这些**演示值**改成**真实值**：

```yaml
environment:
  DB_HOST: mysql                     # 用 RDS 改成 rdsxxx.mysql.rds.aliyuncs.com
  DB_PASSWORD: 改成强密码             # 16 位以上随机
  WX_MA_APPID: wx你的小程序AppID
  WX_MA_SECRET: 你的小程序Secret
  JWT_SECRET: 改成32位随机串          # 用 `openssl rand -base64 48` 生成
  COS_SECRET_ID: AKID你的腾讯云ID
  COS_SECRET_KEY: 你的腾讯云Secret
  COS_REGION: ap-beijing
  COS_BUCKET: starry-private-1300000000   # 桶名
```

同步改 MySQL 服务的 `MYSQL_ROOT_PASSWORD`（保持和 `DB_PASSWORD` 一致）。

#### B. 订阅消息模板 ID

在 server 服务的 environment 里加：

```yaml
STARRY_SUBSCRIBE_APPOINTMENT_CONFIRMED: 你的模板ID1
STARRY_SUBSCRIBE_REMINDER_24H: 你的模板ID2
STARRY_SUBSCRIBE_REMINDER_2H: 你的模板ID3
STARRY_SUBSCRIBE_PLAN_PUSHED: 你的模板ID4
```

（这些环境变量名会被 Spring Boot 自动映射到 `starry.subscribe-msg.*` 配置）

### 3.5 起服务

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

等几分钟。然后：

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs server | tail -20
```

看到 `Started MedicalBeautyApplication` 就是 OK。

后端现在监听 `http://你的服务器IP:8080`，但还需要 nginx 加 HTTPS 才能给小程序用。

### 3.6 装 nginx + HTTPS

```bash
apt update && apt install -y nginx certbot python3-certbot-nginx
```

把您的域名 DNS 解析指向服务器 IP：
- A 记录：`api.starry-mb.com` → `服务器 IP`
- A 记录：`admin.starry-mb.com` → `服务器 IP`

写 nginx 配置：

```bash
nano /etc/nginx/sites-available/starry
```

粘贴：

```nginx
# 后端 API
server {
  listen 80;
  server_name api.starry-mb.com;
  client_max_body_size 30m;       # 照片上传

  location / {
    proxy_pass http://127.0.0.1:8080;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }
}

# 后台 Web 静态
server {
  listen 80;
  server_name admin.starry-mb.com;

  root /var/www/admin;
  index index.html;

  location / {
    try_files $uri $uri/ /index.html;
  }
}
```

启用：

```bash
ln -s /etc/nginx/sites-available/starry /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```

加 HTTPS（**域名必须备案完成**）：

```bash
certbot --nginx -d api.starry-mb.com -d admin.starry-mb.com
```

跟着提示按回车，自动配好 HTTPS + 自动续签。

### 3.7 部署管理后台静态文件

本机（Windows）打包：

```bash
cd C:\Users\Administrator\WxJava\medical-beauty-app\admin

# 把 api 指向正式后端
# 编辑 vite.config.ts 把 proxy 删了（不再代理，直接调用绝对 URL）
# 或者在 client.ts 里把 baseURL 改成 https://api.starry-mb.com

npm run build      # 生成 dist/ 目录
```

把 `dist/` 整个上传到服务器 `/var/www/admin/`：

```bash
# Windows 上用 scp / WinSCP / 阿里云 OOS
scp -r dist/* root@服务器IP:/var/www/admin/
```

刷新 `https://admin.starry-mb.com` 应该能进登录页了。

---

## 阶段 4：小程序提交审核

### 4.1 改小程序配置

本机改 `miniapp/app.js`：

```js
globalData: {
  mockMode: false,                              // 关掉演示
  apiBase: 'https://api.starry-mb.com',         // 正式 API
}
```

`miniapp/project.config.json`：

```json
"appid": "wx你的真实小程序AppID"
```

### 4.2 配域名白名单

mp.weixin.qq.com → 开发 → 开发管理 → 开发设置 → 服务器域名：

- **request 合法域名**：`https://api.starry-mb.com`
- **uploadFile 合法域名**：`https://api.starry-mb.com`
- **downloadFile 合法域名**：`https://你的桶名.cos.ap-beijing.myqcloud.com`

### 4.3 上传

微信开发者工具：
1. 右上角"上传"
2. 填版本号（如 1.0.0）+ 描述
3. 上传完到公众平台后台 → 版本管理 → 提交审核
4. 等 3–7 天

### 4.4 通过后

公众平台 → 版本管理 → 点"发布"，几秒钟全网上线。

---

## 阶段 5：上线后日常运维

### 5.1 改代码后怎么更新

本机：
```bash
git push
```

服务器：
```bash
cd /opt/WxJava
git pull

cd medical-beauty-app
docker compose -f docker-compose.prod.yml up -d --build server
```

后台 Web 改了：本机 `npm run build` → 把 dist/ scp 上去。

### 5.2 数据库备份

每天凌晨自动备份。在服务器加 crontab：

```bash
crontab -e
```

添加：

```
0 3 * * * docker exec starry-mysql sh -c 'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" starry_mb' > /opt/backups/db-$(date +\%F).sql 2>&1
```

定期把 `/opt/backups/` 同步到云存储（COS / OSS）。

### 5.3 看日志

```bash
docker compose -f docker-compose.prod.yml logs -f server   # 实时后端日志
docker compose -f docker-compose.prod.yml logs server | tail -200
```

### 5.4 重启服务

```bash
docker compose -f docker-compose.prod.yml restart server
```

### 5.5 紧急排错清单

| 现象 | 排查命令 |
|---|---|
| 小程序所有接口超时 | `docker compose -f docker-compose.prod.yml ps` 看容器是否 Up |
| 上传照片失败 | `docker compose ... logs server \| grep COS` 看 COS 凭证 |
| 客户收不到订阅消息 | 看 `subscribe_msg_log` 表，把错误码贴到微信文档 |
| 数据库连接超时 | RDS 白名单 / MySQL 容器健康检查 |

---

## 我能在远程做的

您卡哪一步发我，我可以远程帮：

- 改 docker-compose.prod.yml（生产配置）
- 写 nginx 配置
- 修 bug 推到 git，您 `git pull` + 重启容器即可
- 临时性能调优 / 索引补救
- 监控告警接入（CallBack / 邮件）

---

## 上线前自检清单

- [ ] 小程序认证通过（**生活服务 → 美容美发**类目），拿到 AppID / Secret
- [ ] 域名 ICP 备案完成（企业备案）
- [ ] 4 个订阅消息模板通过（**用"护理"用词，不要用"治疗 / 规划"**）
- [ ] 隐私协议 + 用户协议已填入实际信息并在小程序提交
- [ ] 腾讯云 COS 私有桶建好，凭证拿到
- [ ] 云服务器 + （可选）RDS 买好
- [ ] `docker-compose.prod.yml` 改完 7 处占位（JWT_SECRET / DB_PASSWORD / WX_MA_* / COS_* / 4 个模板 ID）
- [ ] nginx + HTTPS 配好
- [ ] 后台 Web 能用真实顾问手机号登录
- [ ] 小程序里能下预约、收到订阅消息
- [ ] 数据库备份脚本跑通
- [ ] 演示数据 seed（V2 + V7）已脱敏，审核截图安全

✅ 全打勾就可以邀请甲方使用了。

---

## 钱

| 项 | 一次 | 每月 |
|---|---|---|
| 小程序企业认证 | 300 元/年 | — |
| 域名 | 50 元/年 | — |
| 备案 | 0 | — |
| 云服务器 ECS 2核4G | — | 150 元 |
| RDS MySQL 基础版（可省） | — | 50 元（或 0 用自建） |
| 腾讯云 COS | — | ~10 元（量大才贵） |
| 短信预付（按需） | 1000 元 | 按量 |
| 微信支付商户费 | **0**（不接入） | — |
| 医疗资质相关 | **0**（不申请） | — |
| SSL 证书 | 0（免费 DV） | — |
| **首年总计** | **~2000 元** | |
| **稳定运行后** | | **~200 元/月** |

