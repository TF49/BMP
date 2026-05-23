# 羽擎（Badminton Management Platform，BMP）

> 面向羽毛球场馆经营场景的单仓多端管理系统：**Spring Boot 后端** + **Vue 3 Web** + **UniApp 微信小程序**。  
> 项目功能开发已完成（v1.0.0），适合作为毕业设计、课程设计或开源展示仓库发布到 GitHub。

## 功能概览

| 类别 | 能力 |
|------|------|
| 场馆经营 | 场馆、场地、营业时间、状态日志、三套定价、占用与时间轴 |
| 会员与资金 | 会员档案、充值、消费记录、财务流水、审计日志、对账 |
| 预约与服务 | 场地预约（拼场/包场）、课程与课程预约、器材租借、赛事报名、穿线工单 |
| 人员与权限 | 五角色 RBAC（会长 / 场馆管理者 / 用户 / 会员 / 教练）、JWT 双 Token |
| 运营辅助 | Dashboard 统计、综合搜索、通知、天气代理、地图逆地理、WebSocket 推送 |
| 支付治理 | 待支付订单超时自动取消（可配置秒级超时，前后端倒计时一致） |

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | Java 17、Spring Boot 3.2、Spring Security、MyBatis-Plus、MySQL 8、Redis（可选）、WebSocket |
| Web | Vue 3.5、Vue Router 4、Element Plus、ECharts、Axios、Vue CLI 5 |
| 移动端 | UniApp 3、Vue 3、TypeScript、Pinia、Vite、uview-plus |

## 项目规模（2026-05-23）

| 范围 | 数量 |
|------|------|
| 后端业务模块 | `16`（含 `payment`） |
| REST Controller | `27` |
| Java 源文件 | `181` |
| 数据库物理表 | `27` |
| Web 页面（`vue/src/views`） | `51` |
| UniApp 页面 | `107` |
| UniApp API 封装 | `36` |
| UniApp 组件 | `23` |
| UniApp 单元测试 | `4` 文件 / `11` 用例 |

## 多端角色与入口

### Web（`vue/`）

| 角色 | 代码 | 典型入口 |
|------|------|----------|
| 官网 | — | `/site` |
| 管理端 | `PRESIDENT`、`VENUE_MANAGER` | `/dashboard` 及业务管理路由 |
| 用户端 | `USER`、`MEMBER` | `/user/*` |
| 教练端 | `COACH` | `/coach/*` |

### UniApp（`BMP-uniapp/`）

| 场景 | 目录 | 角色 |
|------|------|------|
| 用户主链路 | `pages/index`、`booking`、`course` 等 | `USER`、`MEMBER` |
| 移动管理 | `pages/president/**` | `PRESIDENT`、`VENUE_MANAGER` |
| 教练工作台 | `pages/coach/**` | `COACH` |

## 构建验证（2026-05-23）

| 范围 | 命令 | 结果 |
|------|------|------|
| 后端编译 | `mvn -q -DskipTests compile` | 通过 |
| Web 生产构建 | `cd vue && npm run build` | 通过（有包体积与 Sass 弃用告警） |
| UniApp 类型检查 | `cd BMP-uniapp && npm run type-check` | 通过 |
| UniApp 单元测试 | `cd BMP-uniapp && npm test` | 通过（11/11） |

> 说明：仓库当前无 `src/test` 后端自动化测试；Web 入口合计约 `2.28 MiB`，上线前建议按需做分包与按需加载优化。

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+（Web / UniApp）
- MySQL 8.x
- Redis（可选；未部署时注释 `application.properties` 中 Redis 相关配置，使用内存缓存）

### 1. 初始化数据库

在 MySQL 中执行：

```text
src/main/resources/sql/badminton.sql
```

初始化后可使用演示账号（密码均为 **`123456`**，BCrypt 存储）：

| 用户名 | 角色 | 说明 |
|--------|------|------|
| `admin` | `PRESIDENT` | 系统管理员 / 会长 |
| `venue_manager_1` | `VENUE_MANAGER` | 场馆管理者（绑定场馆 1） |
| `user001` | `USER` | 普通用户 |

更多演示数据见 SQL 脚本中的 `sys_user` 插入语句。

### 2. 配置后端

编辑 `src/main/resources/application.properties`（或通过环境变量覆盖）：

```properties
spring.datasource.username=你的数据库用户
spring.datasource.password=你的数据库密码
# 本地无 Redis 时，注释 spring.data.redis.* 四行
```

启动：

```bash
mvn spring-boot:run
```

- 默认端口：`9090`
- Swagger UI：<http://localhost:9090/swagger-ui.html>

### 3. 启动 Web

```bash
cd vue
npm install
npm run serve
```

- 默认端口：`8080`（代理 API 至 `http://localhost:9090`）

### 4. 启动 UniApp（微信小程序）

```bash
cd BMP-uniapp
npm install
npm run dev:mp-weixin
```

使用微信开发者工具打开 `BMP-uniapp/dist/dev/mp-weixin` 目录预览。

## 支付超时自动取消

业务订单支持「待支付超时自动取消」，配置以秒为主：

| 配置项 | 说明 |
|--------|------|
| `bmp.payment.auto-cancel.enabled` | 是否启用 |
| `bmp.payment.auto-cancel.timeout-seconds` | 超时秒数（主配置） |
| `bmp.payment.auto-cancel.scan-interval-ms` | 扫描间隔毫秒 |

开发环境默认 `timeout-seconds=5` 便于联调；**生产/提测**建议通过环境变量设置，例如：

```properties
BMP_PAYMENT_AUTO_CANCEL_TIMEOUT_SECONDS=300
BMP_PAYMENT_AUTO_CANCEL_SCAN_INTERVAL_MS=60000
```

只读配置接口：`GET /api/payment/auto-cancel/config`（含 `timeoutSeconds`、`serverTime`）。

## 仓库结构

```text
BMP/
├── pom.xml                          # Maven 后端
├── src/main/java/                   # Spring Boot 源码
├── src/main/resources/sql/          # 数据库全量脚本 badminton.sql
├── vue/                             # Vue 3 Web（官网 + 管理 + 用户 + 教练）
├── BMP-uniapp/                      # UniApp 小程序
├── README.md                        # 本文件
├── 项目开发文档.md
├── 项目分析总结.md
├── API文档.md
└── 数据库设计文档.md
```

## 可选环境变量（生产）

| 变量 | 用途 |
|------|------|
| `SPRING_PROFILES_ACTIVE` | 切换 `dev` / `prod` |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 |
| `JWT_SECRET` | JWT 密钥（建议 ≥256 bit） |
| `QWEATHER_API_KEY` | 和风天气 |
| `TENCENT_MAP_KEY` | 腾讯地图逆地理编码 |
| `BMP_PAYMENT_AUTO_CANCEL_*` | 支付超时自动取消 |

## 文档导航

| 文档 | 说明 |
|------|------|
| [项目开发文档.md](./项目开发文档.md) | 系统架构、能力地图、权限、目录约定 |
| [项目分析总结.md](./项目分析总结.md) | 项目阶段、交付说明、后续可选优化 |
| [API文档.md](./API文档.md) | 接口索引与联调速查 |
| [数据库设计文档.md](./数据库设计文档.md) | 27 张表、关系与初始化 |
| [BMP-uniapp/README.md](./BMP-uniapp/README.md) | 移动端专项说明 |

## 提交到 GitHub 前检查

- [ ] 确认 `.gitignore` 已排除 `node_modules/`、`target/`、`uploads/`、`logs/`、`.env*`
- [ ] 勿提交真实数据库密码、JWT 密钥、第三方 API Key
- [ ] 按需删除或脱敏 SQL 中的个人演示账号
- [ ] 在仓库 Description 中注明：Java 17 + Vue 3 + UniApp 羽毛球场馆管理系统

## 许可证

本仓库暂未附带 LICENSE 文件。若公开发布，请自行添加合适的开源协议（如 MIT）。

---

**版本**：1.0.0  
**最后更新**：2026-05-23
