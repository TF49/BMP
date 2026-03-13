# BMP 羽毛球管理系统 - Uniapp 开发进度文档

> 文档生成时间：2025-02-25  
> 项目路径：`BMP/BMP-uniapp`  
> 技术栈：Vue 3 + TypeScript + Pinia + uni-app（微信小程序为主）

---

## 一、项目概览

| 项目 | 说明 |
|------|------|
| **应用名称** | 羽毛球管理系统（微信小程序） |
| **包名** | bmp-miniprogram |
| **版本** | 1.0.0 (versionCode: 100) |
| **微信 AppID** | wx861e82a9f2470598 |
| **后端对接** | Spring Boot API（开发：`http://127.0.0.1:9090/api`） |

---

## 二、技术架构

### 2.1 核心依赖

- **Vue** 3.3.4  
- **uni-app** 3.x（@dcloudio/uni-app、vite-plugin-uni）  
- **Pinia** 2.1.7（状态管理）  
- **TypeScript** 5.2.2  
- **Vite** 5.2.8  
- **@dcloudio/uni-ui** 1.4.24（UI 组件）  
- **dayjs**、**lodash-es**（工具库）  
- **Vitest**（单元测试）

### 2.2 目录结构（主要）

```
BMP-uniapp/
├── api/                    # 接口封装
│   ├── auth.ts             # 登录/注册/用户信息
│   ├── venue.ts            # 场馆
│   ├── court.ts            # 场地
│   ├── booking.ts          # 预约
│   ├── course.ts           # 课程
│   ├── equipment.ts        # 器材
│   ├── tournament.ts       # 赛事
│   ├── recharge.ts         # 充值
│   ├── member.ts           # 会员
│   ├── search.ts           # 搜索
│   ├── stringing.ts        # 穿线服务
│   └── president/          # 会长端接口
│       ├── dashboard.ts
│       ├── user.ts / venue.ts / booking.ts / finance.ts / member.ts
│       └── index.ts
├── config/
│   ├── api.ts              # API 路径、分页、上传、缓存配置
│   └── env.ts              # 开发/生产环境 BASE_URL、IMAGE_URL
├── store/
│   ├── index.ts            # Pinia 实例
│   └── modules/
│       ├── user.ts         # 用户登录态、角色
│       ├── president.ts    # 会长端状态
│       └── theme.ts        # 主题（亮/暗）
├── utils/
│   ├── request.ts          # 统一请求（Token、403/401 处理）
│   ├── auth.ts / cache.ts / loading.ts / errorHandler.ts
│   ├── validate.ts / format.ts / debounce.ts / parsePagedList.ts
│   ├── constant.ts         # 业务常量（角色、预约/场地/支付/穿线状态等）
│   ├── roleCheck.ts        # 角色校验（USER/PRESIDENT 可用小程序，VENUE_MANAGER 引导网页端）
│   ├── navigation.ts / presidentRouter.ts / presidentTabBar.ts
│   └── animations.ts
├── components/
│   ├── common/             # NavBar、AnimatedButton、AnimatedInput、AnimatedBackground、Card、Empty、Loading、Skeleton
│   ├── president/          # PresidentLayout、PresidentNavBar、PresidentTabBar、GlassCard
│   ├── MobileLayout.vue
│   ├── TabBar.vue
│   ├── LoadingAnimation.vue / Skeleton.vue / VirtualList.vue
│   └── ...
├── pages/                  # 页面（见下表）
├── styles/                 # common.scss、theme.scss、animations.scss
├── static/                 # tabbar 图标等静态资源
├── pages.json              # 路由与 tabBar 配置
├── manifest.json           # 应用配置、微信 appid
├── App.vue / main.ts
└── package.json
```

---

## 三、页面与功能进度

### 3.1 路由与 TabBar（pages.json）

- **TabBar 共 5 项**：首页、场馆、课程、赛事、我的。  
- **全局**：`navigationStyle: "custom"`，标题栏自定义。  
- **已配置页面**：约 **55 个**（含子页面），见下表。

### 3.2 用户端页面（C 端）

| 模块 | 页面路径 | 说明 | 状态 |
|------|----------|------|------|
| **入口/认证** | `pages/login/login` | 登录 | ✅ 已实现 |
| | `pages/register/register` | 注册 | ✅ 已实现 |
| **首页** | `pages/index/index` | 首页（位置、搜索、Banner、常用功能、热门场馆、推荐课程/赛事） | ✅ 已实现 |
| **场馆** | `pages/venue/list` | 场馆列表 | ✅ 已实现 |
| | `pages/venue/detail` | 场馆详情 | ✅ 已实现 |
| | `pages/venue/list-optimized` | 场馆列表（优化版） | ✅ 已实现 |
| **预约** | `pages/booking/list` | 我的预约 | ✅ 已实现 |
| | `pages/booking/create` | 创建预约 | ✅ 已实现 |
| | `pages/booking/detail` | 预约详情 | ✅ 已实现 |
| **课程** | `pages/course/list` | 课程列表 | ✅ 已实现 |
| | `pages/course/detail` | 课程详情 | ✅ 已实现 |
| | `pages/course/booking` | 课程预约 | ✅ 已实现 |
| **器材** | `pages/equipment/list` | 器材列表 | ✅ 已实现 |
| | `pages/equipment/detail` | 器材详情 | ✅ 已实现 |
| | `pages/equipment/rental` | 器材租借 | ✅ 已实现 |
| **赛事** | `pages/tournament/list` | 赛事列表 | ✅ 已实现 |
| | `pages/tournament/detail` | 赛事详情 | ✅ 已实现 |
| | `pages/tournament/register` | 赛事报名 | ✅ 已实现 |
| **充值** | `pages/recharge/index` | 充值中心 | ✅ 已实现 |
| | `pages/recharge/records` | 充值记录 | ✅ 已实现 |
| **个人** | `pages/profile/index` | 个人中心 | ✅ 已实现 |
| | `pages/profile/info` | 个人信息 | ✅ 已实现 |
| | `pages/profile/records` | 消费记录 | ✅ 已实现 |
| | `pages/profile/member` | 会员信息 | ✅ 已实现 |
| **搜索** | `pages/search/index` | 搜索入口 | ✅ 已实现 |
| | `pages/search/result` | 搜索结果 | ✅ 已实现 |
| **穿线服务** | `pages/stringing/list` | 穿线服务列表（支持下拉刷新） | ✅ 已实现 |
| | `pages/stringing/detail` | 服务详情 | ✅ 已实现 |
| | `pages/stringing/create` | 新增服务 | ✅ 已实现 |
| | `pages/stringing/query` | 服务查询 | ✅ 已实现 |
| **设置** | `pages/settings/index` | 设置首页 | ✅ 已实现 |
| | `pages/settings/account` | 账户设置 | ✅ 已实现 |
| | `pages/settings/notification` | 通知设置 | ✅ 已实现 |
| | `pages/settings/privacy` | 隐私设置 | ✅ 已实现 |
| | `pages/settings/security` | 安全设置 | ✅ 已实现 |
| | `pages/settings/about` | 关于我们 | ✅ 已实现 |
| | `pages/settings/help` | 帮助与反馈 | ✅ 已实现 |
| | `pages/settings/change-password` | 修改密码 | ✅ 已实现 |
| | `pages/settings/change-phone` | 更换手机号 | ✅ 已实现 |
| | `pages/settings/feedback` | 用户反馈 | ✅ 已实现 |
| | `pages/settings/faq` | 常见问题 | ✅ 已实现 |

### 3.3 会长端页面（President）

| 模块 | 页面路径 | 说明 | 状态 |
|------|----------|------|------|
| **看板** | `pages/president/dashboard/index` | 会长看板（支持下拉刷新） | ✅ 已实现 |
| **用户** | `pages/president/user/list` | 用户管理 | ✅ 已实现 |
| | `pages/president/user/detail` | 用户详情 | ✅ 已实现 |
| | `pages/president/user/form` | 用户编辑 | ✅ 已实现 |
| **场馆** | `pages/president/venue/list` | 场馆管理 | ✅ 已实现 |
| | `pages/president/venue/detail` | 场馆详情 | ✅ 已实现 |
| | `pages/president/venue/form` | 场馆编辑 | ✅ 已实现 |
| **预约** | `pages/president/booking/list` | 预约管理 | ✅ 已实现 |
| | `pages/president/booking/detail` | 预约详情 | ✅ 已实现 |
| **财务** | `pages/president/finance/list` | 财务管理 | ✅ 已实现 |
| | `pages/president/finance/detail` | 财务详情 | ✅ 已实现 |
| | `pages/president/finance/reconciliation` | 全面对账 | ✅ 已实现 |
| **会员** | `pages/president/member/list` | 会员管理 | ✅ 已实现 |
| **个人** | `pages/president/profile/index` | 会长「我的」 | ✅ 已实现 |
| | `pages/president/profile/change-password` | 修改密码 | ✅ 已实现 |

---

## 四、API 与后端对接

### 4.1 已封装 API 模块

- **auth**：登录、注册、当前用户、刷新 Token、登出、更新信息/密码、发送验证码、换手机号、反馈。  
- **venue / court**：场馆列表/详情/图片/排期，场地列表。  
- **booking**：预约列表/详情/新增/更新/删除/状态/支付/退款、可用场地/会员/场馆。  
- **course**：课程列表/详情、课程预约、教练/场地。  
- **equipment**：器材列表/详情、租借列表/新增/详情。  
- **tournament**：赛事列表/详情、报名列表/新增/详情。  
- **recharge**：用户充值、管理端、充值记录。  
- **member**：会员信息、消费记录。  
- **search**：场馆/课程/赛事/器材搜索。  
- **stringing**：穿线列表/详情/按单号查询/新增/状态/球线/计价。  
- **president**：dashboard、user、venue、booking、finance、member 等会长端接口。

### 4.2 请求与鉴权（utils/request.ts）

- 基础 URL 来自 `config/env.ts`（开发 127.0.0.1:9090，生产可配）。  
- 除登录/注册/刷新等白名单外，自动携带 `Authorization: Bearer <token>`。  
- 403/401 统一处理：清除登录态、Toast、跳转登录页。  
- 业务错误通过 `result.code`（如 200/401）与 `result.message` 处理。

### 4.3 角色与端区分（utils/roleCheck.ts）

- **USER / PRESIDENT**：允许使用小程序（用户端 + 会长端）。  
- **VENUE_MANAGER**：仅提示使用网页端，不进入小程序业务。

---

## 五、状态与主题

- **store/modules/user**：token、refreshToken、userInfo、登录/登出、checkLogin、refreshToken、角色相关。  
- **store/modules/president**：会长端状态。  
- **store/modules/theme**：主题（亮/暗），在 `App.vue` 的 `onLaunch` 中 `themeStore.initTheme()`。

---

## 六、构建与运行

| 命令 | 说明 |
|------|------|
| `npm run dev:mp-weixin` | 微信小程序开发模式 |
| `npm run build:mp-weixin` | 微信小程序生产构建 |
| `npm run type-check` | Vue/TS 类型检查 |
| `npm run gen-tab-icons` | 生成 tabBar 图标（scripts/gen-tab-icons.mjs） |
| `npm run test` / `npm run test:ui` | Vitest 单测 |

---

## 七、进度小结

| 维度 | 完成情况 |
|------|----------|
| **页面数量** | 约 55 个页面均已配置并存在对应 .vue 文件 |
| **用户端** | 登录/注册、首页、场馆/预约/课程/器材/赛事/充值/个人/搜索/穿线/设置 全链路已覆盖 |
| **会长端** | 看板、用户/场馆/预约/财务/会员管理及会长个人与修改密码 已覆盖 |
| **API 封装** | 与后端 API 路径一致，按模块划分完整 |
| **鉴权与角色** | 统一请求、Token、403/401、角色校验（USER/PRESIDENT 可用，VENUE_MANAGER 引导网页端） |
| **状态与主题** | Pinia（user/president/theme）、主题初始化已接入 |
| **工程化** | TypeScript、Vite、Vitest、uni-app 微信编译与脚本就绪 |

**整体结论**：Uniapp 端（以微信小程序为主）功能与页面已基本完整，用户端与会长端双角色流程、API 对接、鉴权与角色控制均已实现，可进入联调、真机测试与体验优化阶段。

---

## 八、后续建议

1. **环境**：生产环境在 `config/env.ts` 中将 `PROD.BASE_URL` / `IMAGE_URL` 改为正式域名。  
2. **真机**：开发时若用真机，将 `env.ts` 中开发环境改为局域网 IP 或内网穿透地址。  
3. **静态资源**：确认 `static/tabbar/` 下 tab 图标齐全（或通过 `npm run gen-tab-icons` 生成）。  
4. **测试**：对关键流程（登录、预约、支付、会长看板）做端到端或手工回归。  
5. **文档**：若后端 API 有变更，同步更新 `config/api.ts` 与对应 `api/*.ts` 注释或类型。
