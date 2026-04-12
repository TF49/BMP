# 教练端 · UniApp 页面清单与补充路线

> 角色：`COACH`（教练）  
> 用途：对照 Web `CoachLayout` 下 `/coach/*`，规划教练在 UniApp 中的**独立端**页面；当前工程**基本无**教练工作台体系，以下以「待建」为主。  
> Web 对照：`vue/src/router/index.js` 中 `path: '/coach'` 子路由。

---

## 1. 工程现状

### 1.1 已存在的页面

| 路径 | 说明 | 用途 |
|------|------|------|
| `pages/coach/detail` | 教练详情页 | **C 端用户查看教练信息**，非教练工作台 |

> ⚠️ 注意：`pages/coach/detail` 是面向普通用户的教练信息展示页，不是教练端工作台页面。

### 1.2 教练工作台现状

- **无独立教练工作台**：未发现教练端工作台相关页面（如 dashboard、我的课程、课表、预约管理等）。
- 教练信息仅作为 **C 端课程/搜索** 中的展示字段（如 `coachName`），非教练工作台。
- `types/user.ts` 等若未包含 `COACH` 角色完整支持，登录分流、TabBar、守卫需在接入教练端时一并扩展。

---

## 2. 页面完成度统计

### 2.1 整体进度

| 类别 | 已完成 | 待补充 | 完成度 |
|------|--------|--------|--------|
| C 端展示页 | 1 | 0 | 100% |
| 教练工作台 | 0 | 5 | 0% |
| **总计** | **1** | **5** | **17%** |

> 说明：
> - **C 端展示页**：面向普通用户查看教练信息的页面，已完成
> - **教练工作台**：教练登录后使用的管理页面（工作台、课程、课表、预约、档案），尚未开发

### 2.2 核心模块完成情况

| 模块 | 状态 | 说明 |
|------|------|------|
| 教练信息展示（C 端） | ✅ 已完成 | `pages/coach/detail` |
| 教练工作台 | ❌ 待开发 | 需要 dashboard、courses、schedule、bookings、profile 等页面 |

---

## 3. Web 端已有功能（教练专属）

| Web 路由 | Web 视图 | 功能摘要 |
|-----------|-----------|-----------|
| `/coach/dashboard` | `coach/Dashboard.vue` | 教练工作台 / 概览 |
| `/coach/courses` | `coach/MyCourses.vue` | 我的课程 |
| `/coach/schedule` | `coach/Schedule.vue` | 我的课表 |
| `/coach/bookings` | `coach/Bookings.vue` | 预约明细 |
| `/coach/profile` | `coach/Profile.vue` | 我的档案 |

---

## 4. 建议在 UniApp 中新增的页面（勾选跟踪）

建议统一前缀 **`pages/coach/`**（工作台页面），并在 `pages.json` 注册；布局可采用 **独立 TabBar** 或与现网 `CustomTabBar` 按角色切换。

> ⚠️ 注意：`pages/coach/detail` 已存在但用途不同，建议工作台页面使用 `pages/coach/dashboard`、`pages/coach/workspace` 等路径避免混淆。

### 4.1 工作台

- [ ] `pages/coach/dashboard/index` — 对齐 `CoachDashboard`，数据概览、快捷入口

### 4.2 课程

- [ ] `pages/coach/courses/list` — 对齐 `CoachMyCourses`（列表即可，详情按需）
- [ ] `pages/coach/courses/detail` — 课程详情（可选）

### 4.3 课表

- [ ] `pages/coach/schedule/index` — 对齐 `CoachSchedule`（日历/周视图可分期：先列表后日历）

### 4.4 预约

- [ ] `pages/coach/bookings/list` — 对齐 `CoachBookings`
- [ ] `pages/coach/bookings/detail` — 单条预约详情（可选）

### 4.5 个人档案

- [ ] `pages/coach/profile/index` — 对齐 `CoachProfile`
- [ ] `pages/coach/profile/edit` — 编辑资料（可选，也可合并 profile 页）
- [ ] `pages/coach/profile/change-password` — 若不与 C 端设置共用，可单独页或复用 `pages/settings/change-password`

### 4.6 账号与公共（可复用）

- [ ] 登录后 **按角色跳转** `COACH` → `pages/coach/dashboard/index`（在 `login` 或全局路由逻辑中）
- [ ] 复用 `pages/login/login`、`pages/recover/recover` 等公共页
- [ ] 通知：可复用 `pages/notice/index`（若业务上教练需看同一公告）

---

## 5. 实施时注意

1. **pages.json**：教练端首页建议设为 `pages/coach/dashboard/index` 或保留统一 `pages/index` 内分支（二选一，避免双首页混乱）。注意 `pages/coach/detail` 已被 C 端使用。
2. **角色守卫**：`COACH` 不可进入 `pages/president/*` 与 C 端 Tab 下单流程（与 Web 守卫策略一致）。
3. **API**：在 `api/` 下新增 `coach/*` 或与 Web 共用现有教练相关接口（按后端实际路径拆分）。
4. **类型**：`types/user.ts`、`store/modules/user.ts` 中 `role` 需支持 `'COACH'`，与 `utils/roleCheck.ts` 一致。
5. **路径规划**：由于 `pages/coach/detail` 已用于 C 端，建议工作台页面使用更明确的路径如 `pages/coach/workspace/*` 或 `pages/coach-workspace/*`。

---

## 6. Web → UniApp 路径映射（目标态）

| Web | 建议 UniApp 路径 |
|-----|------------------|
| `/coach/dashboard` | `pages/coach/dashboard/index` 或 `pages/coach/workspace/dashboard` |
| `/coach/courses` | `pages/coach/courses/list` 或 `pages/coach/workspace/courses` |
| `/coach/schedule` | `pages/coach/schedule/index` 或 `pages/coach/workspace/schedule` |
| `/coach/bookings` | `pages/coach/bookings/list` 或 `pages/coach/workspace/bookings` |
| `/coach/profile` | `pages/coach/profile/index` 或 `pages/coach/workspace/profile` |
| （C 端）教练详情 | `pages/coach/detail`（已存在） |

> 说明：由于 `pages/coach/detail` 已用于 C 端，建议工作台页面使用独立路径避免混淆。

---

## 7. 总结

### 7.1 当前状态
- ✅ C 端教练详情页已完成（`pages/coach/detail`）
- ❌ 教练工作台系统尚未开发（0%）

### 6.2 开发优先级建议

> 💡 **定位说明**：UniApp 端为移动端轻量化管理工具，复杂功能建议在 Web 管理端完成。

1. **P0 核心**：工作台（dashboard）、我的课程（courses）、课表（schedule）
2. **P1 重要**：预约管理（bookings）
3. **P2 可选**：个人档案编辑（profile/edit）、复杂数据分析等建议使用 Web 端

### 7.3 与其他端对比
| 端 | 页面完成度 | 工作台状态 |
|----|-----------|-----------|
| 会长端 | 99% | ✅ 已完成 |
| 教练端 | 17% | ❌ 待开发 |
| C 端 | 100% | N/A |

---

*文档随项目迭代手动更新。最后更新：2026-04-12*
