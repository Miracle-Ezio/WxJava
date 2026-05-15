# 一键启动指南（Windows · 非开发者友好）

> 看完此文 15 分钟内可见完整后台 Web + 后端 + 数据库跑起来，无需手动装 JDK / Maven / MySQL / Redis。

## 你需要安装的唯二两个程序

| 软件 | 用途 | 下载 |
| --- | --- | --- |
| **Docker Desktop** | 帮你跑后端 + 数据库，免装 JDK / Maven | https://www.docker.com/products/docker-desktop/ |
| **Node.js LTS** | 跑前端开发服务器 | https://nodejs.org/zh-cn （选 LTS 版） |

装完后重启电脑一次（Docker Desktop 需要）。

---

## 第一次启动（约 10 分钟，主要是首次下载镜像）

### 1️⃣ 启动 Docker Desktop

打开 Docker Desktop，等左下角变绿色"engine running"。

### 2️⃣ 启动后端 + 数据库

开个 Git Bash，cd 到项目根目录：

```bash
cd /c/Users/Administrator/WxJava/medical-beauty-app
docker compose up -d --build
```

第一次会下载 MySQL / Redis / Maven / OpenJDK 镜像，**慢一些是正常的**。喝口水。

完成后看看是不是三个容器都起来了：

```bash
docker compose ps
```

应该看到 `starry-mysql`、`starry-redis`、`starry-server` 三个都是 `Up` / `healthy`。

确认后端日志说明 seed 加载成功：

```bash
docker compose logs server | tail -50
```

找到这行就 OK：

```
[EmployeeSeedBootstrap] demo employee credentials ready: 13800000001 / starry123
Started MedicalBeautyApplication in X.X seconds
```

### 3️⃣ 启动前端 Web

另开一个 Git Bash：

```bash
cd /c/Users/Administrator/WxJava/medical-beauty-app/admin
npm install        # 首次需要
npm run dev
```

看到：

```
VITE v5.4.x  ready in 800 ms
➜ Local: http://localhost:5173/
```

浏览器打开这个地址 → 应该能进登录页。

### 4️⃣ 登录

```
手机号：13800000001
密码：starry123
```

---

## 日常使用（启动后）

每次电脑重启或想用的时候：

```bash
# 后端（一行起所有服务，下次秒级启动）
cd /c/Users/Administrator/WxJava/medical-beauty-app
docker compose up -d

# 前端
cd /c/Users/Administrator/WxJava/medical-beauty-app/admin
npm run dev
```

不用了就停：

```bash
docker compose down       # 停服务但保留数据
docker compose down -v    # 连数据库数据一起清掉（重置演示数据用）
```

---

## 改了后端代码怎么办

每次改后端 Java 代码，必须重新 build 镜像：

```bash
docker compose up -d --build server
```

前端 Vue / TS 代码改了不用任何操作，**Vite 自动热重载**。

---

## 常见问题

**Q：浏览器报"网络异常"**
A：99% 是后端没起。`docker compose ps` 看看 `starry-server` 是不是 healthy。如果没起：

```bash
docker compose logs server
```

把错误发我。

**Q：Vite 跑在 5174 / 5175 端口**
A：说明 5173 被占了。Vite 会自动换端口，打开它打印的新 URL 就行。

**Q：照片上传报"对象存储未配置"**
A：演示阶段没配腾讯云 COS。上线前在 `docker-compose.yml` 把 `COS_*` 填上即可。

**Q：想清空数据库重来一次**
```bash
docker compose down -v
docker compose up -d --build
```

**Q：Docker Desktop 占内存太多**
A：在它的 Settings → Resources 里把 RAM 限制调到 2GB 就够用。

---

## 完整三端同开

理想布局：**3 个窗口**

```
窗口 A：Docker Desktop          // 看容器健康状态
窗口 B：Git Bash → npm run dev  // 前端开发服务器
窗口 C：微信开发者工具          // 客户端小程序
```

窗口 B 改前端代码立即生效，窗口 C 在小程序里下预约、上传照片、看规划。
后端日志在 Docker Desktop 里点 `starry-server` 容器能看。
