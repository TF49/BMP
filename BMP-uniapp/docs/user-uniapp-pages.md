# 用户端 · UniApp 页面清单与补充路线（普通用户 + 会员）

> 角色：`USER`（普通用户）、`MEMBER`（会员，充值/规则达标升级，**与用户共用同一套路由与页面**）  
> 用途：对照 Web `UserLayout` 下 `/user/*` 能力，记录 C 端已注册页面、体验缺口与可选增强。  
> Web 对照：`vue/src/router/index.js` 中 `meta.roles: ['USER', 'MEMBER']` 的子路由。

---

## 1. 角色说明

- **同一套页面**：`USER` / `MEMBER` 不拆两套 `pages.json`，差异放在 **首页权益区、会员标签、价格/折扣展示** 等（依赖 `member/info`、用户信息与后端规则）。
- **与 Web 对齐目标**：会员单独首页或同页 **SVIP/等级权益** 与 Web `user/Dashboard.vue` 的 `memberLevel`、VIP 文案一致。

---

## 2. 已存在的页面（`pages.json` 已注册 · C 端主路径）

### 2.1 账号与公共

| 路径 | 说明 |
|------|------|
| `pages/login/login` | 登录 |
| `pages/register/register` | 注册 |
| `pages/recover/recover` | 找回密码 |
| `pages/notice/index` | 通知中心 |

### 2.2 首页与 Tab 相关

| 路径 | 说明 |
|------|------|
| `pages/index/index` | 首页（普通用户态 `MemberIndex` 等；会员态宜增强权益展示） |
| `pages/venue/list` | 场馆预订列表（Tab「场馆」） |
| `pages/course/list` | 课程预约列表（Tab「课程」） |
| `pages/tournament/list` | 赛事报名列表（Tab「赛事」） |
| `pages/profile/index` | 个人中心（Tab「我的」） |

### 2.3 场馆 / 场地（消费侧）

| 路径 | 说明 |
|------|------|
| `pages/venue/detail` | 场馆详情 |
| `pages/venue/booking` | 场地预约 |

### 2.4 预约（我的订单）

| 路径 | 说明 |
|------|------|
| `pages/booking/list` | 我的预约 |
| `pages/booking/confirm` | 确认订单 |
| `pages/booking/fee-detail` | 费用明细 |
| `pages/booking/detail` | 预约详情 |

### 2.5 充值

| 路径 | 说明 |
|------|------|
| `pages/recharge/index` | 充值中心 |
| `pages/recharge/records` | 充值记录 |

### 2.6 课程（消费侧）

| 路径 | 说明 |
|------|------|
| `pages/course/detail` | 课程详情 |
| `pages/course/booking` | 课程预约 |

### 2.7 器材（消费侧）

| 路径 | 说明 |
|------|------|
| `pages/equipment/list` | 器材列表 |
| `pages/equipment/detail` | 器材详情 |
| `pages/equipment/rental` | 器材租借 |

### 2.8 赛事（消费侧）

| 路径 | 说明 |
|------|------|
| `pages/tournament/detail` | 赛事详情 |
| `pages/tournament/register` | 赛事报名 |

### 2.9 穿线（消费侧）

| 路径 | 说明 |
|------|------|
| `pages/stringing/list` | 穿线服务列表 |
| `pages/stringing/detail` | 服务详情 |
| `pages/stringing/create` | 新增/下单 |
| `pages/stringing/query` | 服务查询 |

### 2.10 搜索

| 路径 | 说明 |
|------|------|
| `pages/search/index` | 搜索 |
| `pages/search/result` | 搜索结果 |

### 2.11 个人中心子页

| 路径 | 说明 |
|------|------|
| `pages/profile/info` | 个人信息 |
| `pages/profile/records` | 消费记录 |
| `pages/profile/member` | 会员信息 |

### 2.12 设置与子页

| 路径 | 说明 |
|------|------|
| `pages/settings/index` | 设置首页 |
| `pages/settings/account` | 账户设置 |
| `pages/settings/notification` | 通知设置 |
| `pages/settings/privacy` | 隐私设置 |
| `pages/settings/security` | 安全设置 |
| `pages/settings/about` | 关于我们 |
| `pages/settings/help` | 帮助与反馈 |
| `pages/settings/change-password` | 修改密码 |
| `pages/settings/change-phone` | 更换手机号 |
| `pages/settings/feedback` | 用户反馈 |
| `pages/settings/faq` | 常见问题 |

---

## 3. 页面完成度统计

### 3.1 整体进度

| 类别 | 已完成 | 待补充 | 完成度 |
|------|--------|--------|--------|
| 账号与公共 | 4 | 0 | 100% |
| 首页与 Tab | 5 | 0 | 100% |
| 场馆/场地 | 2 | 0 | 100% |
| 预约订单 | 4 | 0 | 100% |
| 充值 | 2 | 0 | 100% |
| 课程 | 2 | 0 | 100% |
| 器材 | 3 | 0 | 100% |
| 赛事 | 2 | 0 | 100% |
| 穿线 | 4 | 0 | 100% |
| 搜索 | 2 | 0 | 100% |
| 个人中心 | 3 | 0 | 100% |
| 设置 | 11 | 0 | 100% |
| **总计** | **44** | **0** | **100%** |

> 说明：用户端（C 端）所有核心页面已完成，待补充项主要为数据真实化和体验优化。

### 3.2 核心模块完成情况

| 模块 | 状态 | 说明 |
|------|------|------|
| 账号体系 | ✅ 已完成 | 登录、注册、找回密码、通知中心 |
| 场馆预订 | ✅ 已完成 | 场馆列表、详情、预约流程 |
| 课程预约 | ✅ 已完成 | 课程列表、详情、预约 |
| 赛事报名 | ✅ 已完成 | 赛事列表、详情、报名 |
| 器材租借 | ✅ 已完成 | 器材列表、详情、租借 |
| 穿线服务 | ✅ 已完成 | 服务列表、详情、下单、查询 |
| 充值系统 | ✅ 已完成 | 充值中心、充值记录 |
| 个人中心 | ✅ 已完成 | 个人信息、消费记录、会员信息 |
| 设置中心 | ✅ 已完成 | 账户、通知、隐私、安全等 11 个子页面 |
| 搜索功能 | ✅ 已完成 | 搜索首页、搜索结果 |

---

## 4. 待补充 / 待对齐（相对 Web 用户端）

以下为 **体验或 Web 已有、小程序建议补齐** 项，用勾选跟踪。

### 4.1 数据与展示对齐 Web

- [ ] 首页账户余额、会员等级、VIP/SVIP 文案：**接真实接口**（对齐 Web `user/Dashboard.vue`：`getCurrentMember`、余额、预约数等）
- [ ] 首页「预约提醒」：**接最近一条真实预约**，替换写死演示数据
- [ ] `USER` 与 `MEMBER`：**条件渲染**权益入口（升级引导 / SVIP 专区）

### 4.2 路由与页面缺失

- [ ] `pages/index` 内若仍存在跳转 **`/pages/insights/detail`**，需在 `pages.json` **注册对应页**或改为真实活动/赛事详情链接（当前易为死链）
- [ ] Web 有独立 **`/user/about`**、**`/user/help`**；小程序已落在 `settings/about`、`settings/help`，若需与 Web 路径心智一致，可保持现状或增加别名入口

### 4.3 可选增强（非必须）

- [ ] 用户端 **独立会员权益页**（如 `pages/member/benefits`），专供会员运营文案与等级说明
- [ ] 与 Web 一致的 **订单统计卡片**（待支付/进行中/已完成）单独模块或并入首页
- [ ] `list-optimized.vue` 若启用：需注册进 `pages.json` 并替换入口；否则可删除避免混淆

---

## 5. Web 端对照索引（`/user/*` → UniApp）

| Web 路由 | Web 视图 | UniApp 路径（主对应） |
|-----------|-----------|------------------------|
| `/user/dashboard` | `user/Dashboard.vue` | `pages/index/index`（用户态） |
| `/user/booking` | `user/Booking.vue` | `pages/venue/*` + `pages/booking/*` |
| `/user/recharge` | `user/Recharge.vue` | `pages/recharge/*` |
| `/user/equipment` | `user/EquipmentRental.vue` | `pages/equipment/*` |
| `/user/stringing` | `user/StringingService.vue` | `pages/stringing/*` |
| `/user/course` | `user/CourseBooking.vue` | `pages/course/*` |
| `/user/tournament` | `user/TournamentRegistration.vue` | `pages/tournament/*` |
| `/user/search` | `user/Search.vue` | `pages/search/*` |
| `/user/settings` | `user/Settings.vue` | `pages/settings/*` |
| `/user/profile` | `user/Profile.vue` | `pages/profile/*` |
| `/user/about` | `About.vue` | `pages/settings/about` |
| `/user/help` | `Help.vue` | `pages/settings/help` |

---

## 6. 总结

### 6.1 当前状态
- ✅ 所有核心页面已完成（44 个页面，100%）
- ⚠️ 部分页面数据需真实化（首页余额、会员等级、预约提醒等）
- ⚠️ 部分路由可能存在死链（如 `/pages/insights/detail`）

### 6.2 优化优先级建议
1. **P0 数据真实化**：首页账户余额、会员等级、预约提醒接真实接口
2. **P1 路由修复**：检查并修复死链（如 insights/detail）
3. **P2 体验增强**：会员权益页、订单统计卡片等

### 6.3 与其他端对比
| 端 | 页面完成度 | 核心功能状态 |
|----|-----------|-------------|
| 用户端（C 端） | 100% | ✅ 已完成 |
| 会长端 | 99% | ✅ 已完成（核心 100%） |
| 教练端 | 17% | ❌ 待开发 |

---

*文档随项目迭代手动更新。最后更新：2026-04-12*
