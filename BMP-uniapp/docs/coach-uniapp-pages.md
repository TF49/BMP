# 教练端 · UniApp 页面清单与补充路线

> 角色：`COACH`（教练）  
> 用途：对照 Web `CoachLayout` 下 `/coach/*`，规划教练在 UniApp 中的**独立端**页面；当前工程**尚无** `pages/coach/*` 体系，以下以「待建」为主。  
> Web 对照：`vue/src/router/index.js` 中 `path: '/coach'` 子路由。

---

## 1. 工程现状

- **无独立教练端目录**：未发现 `pages/coach/**/*.vue` 在 `pages.json` 中注册。
- 教练信息仅作为 **C 端课程/搜索** 中的展示字段（如 `coachName`），非教练工作台。
- `types/user.ts` 等若未包含 `COACH`，登录分流、TabBar、守卫需在接入教练端时一并扩展。

---

## 2. Web 端已有功能（教练专属）

| Web 路由 | Web 视图 | 功能摘要 |
|-----------|-----------|-----------|
| `/coach/dashboard` | `coach/Dashboard.vue` | 教练工作台 / 概览 |
| `/coach/courses` | `coach/MyCourses.vue` | 我的课程 |
| `/coach/schedule` | `coach/Schedule.vue` | 我的课表 |
| `/coach/bookings` | `coach/Bookings.vue` | 预约明细 |
| `/coach/profile` | `coach/Profile.vue` | 我的档案 |

---

## 3. 建议在 UniApp 中新增的页面（勾选跟踪）

建议统一前缀 **`pages/coach/`**，并在 `pages.json` 注册；布局可采用 **独立 TabBar** 或与现网 `CustomTabBar` 按角色切换。

### 3.1 工作台

- [ ] `pages/coach/dashboard/index` — 对齐 `CoachDashboard`，数据概览、快捷入口

### 3.2 课程

- [ ] `pages/coach/courses/list` — 对齐 `CoachMyCourses`（列表即可，详情按需）
- [ ] `pages/coach/courses/detail` — 课程详情（可选）

### 3.3 课表

- [ ] `pages/coach/schedule/index` — 对齐 `CoachSchedule`（日历/周视图可分期：先列表后日历）

### 3.4 预约

- [ ] `pages/coach/bookings/list` — 对齐 `CoachBookings`
- [ ] `pages/coach/bookings/detail` — 单条预约详情（可选）

### 3.5 个人档案

- [ ] `pages/coach/profile/index` — 对齐 `CoachProfile`
- [ ] `pages/coach/profile/edit` — 编辑资料（可选，也可合并 profile 页）
- [ ] `pages/coach/profile/change-password` — 若不与 C 端设置共用，可单独页或复用 `pages/settings/change-password`

### 3.6 账号与公共（可复用）

- [ ] 登录后 **按角色跳转** `COACH` → `pages/coach/dashboard/index`（在 `login` 或全局路由逻辑中）
- [ ] 复用 `pages/login/login`、`pages/recover/recover` 等公共页
- [ ] 通知：可复用 `pages/notice/index`（若业务上教练需看同一公告）

---

## 4. 实施时注意

1. **pages.json**：教练端首页建议设为 `pages/coach/dashboard/index` 或保留统一 `pages/index` 内分支（二选一，避免双首页混乱）。
2. **角色守卫**：`COACH` 不可进入 `pages/president/*` 与 C 端 Tab 下单流程（与 Web 守卫策略一致）。
3. **API**：在 `api/` 下新增 `coach/*` 或与 Web 共用现有教练相关接口（按后端实际路径拆分）。
4. **类型**：`types/user.ts`、`store/modules/user.ts` 中 `role` 需支持 `'COACH'`，与 `utils/roleCheck.ts` 一致。

---

## 5. Web → UniApp 路径映射（目标态）

| Web | 建议 UniApp 路径 |
|-----|------------------|
| `/coach/dashboard` | `pages/coach/dashboard/index` |
| `/coach/courses` | `pages/coach/courses/list` |
| `/coach/schedule` | `pages/coach/schedule/index` |
| `/coach/bookings` | `pages/coach/bookings/list` |
| `/coach/profile` | `pages/coach/profile/index` |

---

*文档随项目迭代手动更新。*
