# 羽毛球管理系统（Badminton Management Platform，BMP）

> **项目状态**：Web 端 ✅ 主要功能完成；移动端（`BMP-uniapp/`）🚧 持续迭代中。  
> **后端端口**：`9090`（默认）｜**Web 前端端口**：`8080`（默认）

## 项目简介

BMP 是一个面向羽毛球场馆运营的前后端分离系统，覆盖 **场馆 / 场地 / 会员 / 预约 / 器材 / 教练课程 / 赛事 / 充值 / 财务统计** 等业务，并提供：

- **Web 管理端**（`vue/`）：管理端 + 用户端 + 教练端（多角色路由与按钮级权限）
- **移动端（UniApp）**（`BMP-uniapp/`）：微信小程序/H5/App（按当前工程配置与实现进度）

## 技术栈（按前后端拆分）

### 后端（Java / Spring Boot）

- **语言与构建**
  - Java **17**
  - Maven（Spring Boot Maven Plugin）
- **核心框架**
  - Spring Boot **3.2.0**（Spring Web / Validation / AOP / Scheduling）
  - Spring Security（无状态鉴权，JWT Filter）
  - MyBatis-Plus **3.5.15**（Boot 3 专用 starter）
- **数据与缓存**
  - MySQL Connector/J **8.0.33**（数据库：MySQL 8.x）
  - HikariCP（连接池）
  - Spring Cache + Redis（`spring-boot-starter-data-redis`；未启用 Redis 时回退内存缓存）
- **实时通信**
  - WebSocket（STOMP + SockJS，后端 endpoint：`/ws`）
- **文档与导出**
  - SpringDoc OpenAPI + Swagger UI **2.3.0**（`/swagger-ui.html`）
  - EasyExcel **3.3.2**（Excel 导入/导出）
- **工程与通用能力**
  - Lombok
  - 全局异常处理（`@RestControllerAdvice`）

### 前端（Web 管理端：Vue 3）

- **框架与工程化**
  - Vue **3.5.24**
  - Vue Router **4.6.3**（Hash 路由；多角色路由守卫）
  - Vue CLI **5**（`vue-cli-service`）
  - TypeScript **5.9.3**（工程启用 TS；部分文件仍为 JS）
  - Babel（生产环境移除 console：`transform-remove-console`）
  - Sass（`sass` + `sass-loader`）
- **UI 与可视化**
  - Element Plus **2.11.8**
  - ECharts **6.0.0**
  - `@element-plus/icons-vue`
  - 自定义样式：`src/styles/uiverse/`、`src/styles/site.css`
- **网络与鉴权**
  - Axios **1.13.2**（请求拦截器：Bearer Token；内置 RefreshToken 自动刷新队列）
  - `js-cookie`
  - `jsencrypt`（RSA 加解密工具，见 `src/utils/jsencrypt.js`）
- **实时通信**
  - `@stomp/stompjs` + `sockjs-client`（对接后端 STOMP：订阅 `/topic/...`、`/user/queue/...`）
- **其他**
  - `@ctrl/tinycolor`（颜色工具）

### 前端（移动端：UniApp）

以 `BMP-uniapp/package.json` 为准：

- UniApp（`@dcloudio/uni-app`）+ Vue 3
- Vite（`@dcloudio/vite-plugin-uni`）
- Pinia（状态管理）
- dayjs / lodash-es
- `@dcloudio/uni-ui`
- Vitest（含 `vitest`、`@vitest/ui`）

## 目录结构（核心）

```text
BMP/
├─ pom.xml                      # 后端 Maven 配置（Spring Boot 3.2 / Java 17）
├─ src/main/java/com/badminton/bmp/
│  ├─ modules/                  # 业务模块（controller/service/mapper/entity）
│  ├─ config/                   # 安全、缓存、Swagger、定时任务、WebSocket 等配置
│  ├─ common/                   # JWT/Result/异常/通用工具等
│  └─ websocket/                # STOMP + 推送服务
├─ src/main/resources/
│  ├─ application.properties    # 公共配置（端口/DB/JWT/Swagger/Redis）
│  ├─ application-dev.properties
│  └─ application-prod.properties
├─ vue/                         # Web 管理端（Vue 3 + Element Plus）
└─ BMP-uniapp/                  # 移动端（UniApp）
```

## 快速开始（本地开发）

### 环境要求

- **JDK 17**
- **Maven 3.6+**（建议 3.9+）
- **MySQL 8.x**
- **Node.js 16+**（建议 18+）
- **Redis（可选）**：仅当你需要启用 Redis 缓存

### 1) 初始化数据库

使用 `src/main/resources/sql/badminton.sql` 初始化（建表 + 示例数据）。文档细节见 `数据库设计文档.md`。

### 2) 启动后端

- 配置文件：`src/main/resources/application.properties`
  - **数据库**：`spring.datasource.*`
  - **JWT 密钥**：通过环境变量 `JWT_SECRET` 提供（必填，至少 32 字节）
  - **环境切换**：`SPRING_PROFILES_ACTIVE=dev|prod`
  - **WebSocket 来源白名单**：`WS_ALLOWED_ORIGINS`（逗号分隔）
  - **Redis（可选）**：`REDIS_HOST/REDIS_PORT/REDIS_PASSWORD`

启动前建议先设置环境变量（Windows 示例）：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="请替换为至少32字节的高强度密钥"
$env:WS_ALLOWED_ORIGINS="http://localhost:8080,http://127.0.0.1:8080"
```

启动命令：

```bash
mvn spring-boot:run
```

- **API Base URL**：`http://localhost:9090`
- **Swagger UI**：`http://localhost:9090/swagger-ui.html`

### 3) 启动 Web 前端（Vue）

```bash
cd vue
npm install
npm run serve
```

开发环境下 `vue.config.js` 会将：

- `/api` 代理到 `http://localhost:9090`
- `/ws` 以 WebSocket 方式代理到后端

### 4) 启动移动端（UniApp / 微信小程序）

```bash
cd BMP-uniapp
npm install
npm run dev:mp-weixin
```

## 默认账号（示例数据）

初始化脚本通常会内置示例账号；如需对外部署，请务必自行修改/重置默认账号与密码。

## 文档索引

- `项目开发文档.md`
- `API文档.md`
- `数据库设计文档.md`
- `模块化测试文档.md`
- `Dashboard虚拟数据对接清单.md`

---

**最后更新**：2026-02-27
