# 羽擎（Badminton Management Platform，BMP）

> 当前仓库是一个单仓多端项目：`Spring Boot` 后端 + `Vue 3` Web 端 + `UniApp` 移动端。  
> 本文档内容基于 2026-04-25 对代码、配置、构建结果和测试结果的复核整理，不以现有分析类 Markdown 作为依据。

## 当前项目结论

- 后端是项目核心，围绕场馆经营形成了较完整的业务中台。
- `vue/` 不只是后台管理端，还混合了承载官网、用户端、教练端三类入口。
- `BMP-uniapp/` 已不只是普通用户小程序，实际包含用户端和 `president` 移动管理端两套入口。
- 当前仓库里未看到 `BMPandroid/` 目录，旧文档里对 Android 端的描述已不再适用。

## 技术栈

### 后端

- Java 17
- Spring Boot 3.2.0
- Spring Security
- MyBatis-Plus 3.5.15
- MySQL 8.x
- Spring Cache + Redis
- Spring WebSocket + STOMP + SockJS
- SpringDoc OpenAPI / Swagger UI
- EasyExcel

### Web 端

- Vue 3.5
- Vue Router 4
- Element Plus 2.11
- Axios 1.13
- ECharts 6
- Vue CLI 5
- TypeScript + JavaScript 混合工程

### UniApp 端

- UniApp 3 + Vue 3
- Vite
- Pinia
- uview-plus
- Vitest
- TypeScript

## 当前结构

```text
BMP/
├─ pom.xml
├─ src/
│  └─ main/
│     ├─ java/com/badminton/bmp/
│     │  ├─ common/
│     │  ├─ config/
│     │  ├─ framework/
│     │  ├─ modules/
│     │  └─ websocket/
│     └─ resources/
│        ├─ application.properties
│        ├─ application-dev.properties
│        ├─ application-prod.properties
│        └─ sql/badminton.sql
├─ vue/
├─ BMP-uniapp/
├─ uploads/
├─ logs/
└─ 多份根目录文档
```

## 业务模块

后端 `modules/` 当前包含以下 15 个业务域：

- `activity`
- `booking`
- `coach`
- `course`
- `court`
- `dashboard`
- `equipment`
- `finance`
- `member`
- `notification`
- `search`
- `stringing`
- `system`
- `tournament`
- `venue`

这些模块已经覆盖了场馆、场地、预约、会员、充值、财务、教练课程、赛事、器材租借、通知、搜索等主流程。

## 多端职责

### 后端

- 统一提供 `/api/**` REST 接口
- 统一 JWT 认证和刷新
- 在服务层做数据权限兜底
- 提供 WebSocket 推送管理端待办和用户订单状态
- 通过定时任务推进预约、课程、赛事、租借等状态

### Web 端

- 官网入口：`/site`
- 管理端：`PRESIDENT`、`VENUE_MANAGER`
- 用户端：`USER`、`MEMBER`
- 教练端：`COACH`

### UniApp 端

- 用户端：首页、场馆预订、课程、器材、赛事、充值、个人中心
- 会长移动端：`pages/president/**`
- 当前角色策略并不完全统一，详见“已确认问题”

## 已确认问题

### 1. Web 端体积偏大

2026-04-25 执行 `vue` 构建时通过，但出现明显体积告警：

- `chunk-element-plus` 约 1.0 MiB
- `chunk-echarts` 约 896 KiB
- `chunk-vendors` 约 776 KiB
- 主入口合计约 2.25 MiB

这说明 Web 端功能已经很多，但按当前打包方式，首屏和弱网体验会受影响。

### 2. UniApp 角色策略存在不一致

`BMP-uniapp/utils/roleCheck.ts` 中：

- `isAllowedRole()` 只允许 `USER` 和 `PRESIDENT`
- `isUserRole()` 却又把 `MEMBER` 视为用户端角色

这意味着业务语义已经把 `MEMBER` 当普通用户体系的一部分，但登录放行逻辑并没有完全跟上。

### 3. 文档与仓库现状曾出现漂移

- 旧 README 提到的 `BMPandroid/` 当前仓库内不存在
- 根目录存在多份分析类文档，部分时间戳、规模数据、完成度描述已经落后于当前代码状态

### 4. 后端自动化测试明显偏弱

- `pom.xml` 中已没有测试依赖
- 本次仅验证了后端可编译，未发现成体系的后端单元测试/集成测试

这意味着后端的稳定性目前主要依赖人工联调和运行时验证。

## 已验证结果

2026-04-25 在当前仓库完成了以下验证：

- 后端：`mvn -q -DskipTests compile` 通过
- Web：`npm run build` 通过
- UniApp：`npm run type-check` 通过
- UniApp：`npm test` 通过，3 个测试文件、8 个测试全部通过

## 本地启动

### 后端

```bash
mvn spring-boot:run
```

默认端口：`9090`

### Web

```bash
cd vue
npm install
npm run serve
```

### UniApp

```bash
cd BMP-uniapp
npm install
npm run dev:mp-weixin
```

## 根目录文档说明

当前根目录保留了多份说明文档。若继续维护本仓库，建议逐步收敛为：

- `README.md`：只保留当前状态、启动方式、结构总览
- `项目分析总结.md`：保留深度分析、风险、演进建议
- `项目结构分析.md`：保留目录、职责、边界说明

---

**最后更新**：2026-04-25
