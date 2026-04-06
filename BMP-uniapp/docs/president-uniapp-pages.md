# 协会会长端 · UniApp 页面清单与补充路线

> 用途：对照 Web 管理端能力，记录会长（`PRESIDENT`）在 UniApp 中**已落地页面**与**待补充页面**，便于迭代时勾选与排期。  
> 说明：场馆管理者（`VENUE_MANAGER`）可与会长**共用同一套页面**，仅在入口与接口数据范围上区分；**系统用户管理**仅会长可见（Web 路由亦如此）。

---

## 1. 已存在的页面

以下路径均在根目录 `pages.json` 中注册，且在 `pages/president/` 下存在对应 `.vue`（截至文档编写时的工程状态）。

### 1.1 工作台入口


| 路径                  | 说明                                      |
| ------------------- | --------------------------------------- |
| `pages/index/index` | 首页；会长登录后为会长态工作台（如 `PresidentIndex` 等组件） |


### 1.2 用户管理（系统用户）


| 路径                            | 说明      |
| ----------------------------- | ------- |
| `pages/president/user/list`   | 用户列表    |
| `pages/president/user/detail` | 用户详情    |
| `pages/president/user/form`   | 用户新增/编辑 |


### 1.3 场馆管理


| 路径                             | 说明      |
| ------------------------------ | ------- |
| `pages/president/venue/list`   | 场馆列表    |
| `pages/president/venue/detail` | 场馆详情（**当前无场地列表**；已约定在本页**内嵌「本馆场地」区块** + 接场地接口，见 §2.1） |
| `pages/president/venue/form`   | 场馆新增/编辑 |


### 1.4 预约流程


| 路径                                | 说明        |
| --------------------------------- | --------- |
| `pages/venue/booking`             | 会长与用户共用的预约提交流程页 |
| `pages/booking/confirm`           | 会长与用户共用的确认订单页，可按来源返回不同链路 |


### 1.5 会员管理


| 路径                            | 说明   |
| ----------------------------- | ---- |
| `pages/president/member/list` | 会员列表 |


> 当前无独立的 `member/detail`、`member/form`；若需与用户管理同级体验，见下文「待补充」。

### 1.6 财务管理


| 路径                                       | 说明        |
| ---------------------------------------- | --------- |
| `pages/president/finance/list`           | 财务列表/汇总入口 |
| `pages/president/finance/detail`         | 财务详情      |
| `pages/president/finance/reconciliation` | 对账/汇总     |


### 1.7 个人与账号


| 路径                                        | 说明     |
| ----------------------------------------- | ------ |
| `pages/president/profile/index`           | 会长个人中心 |
| `pages/president/profile/change-password` | 修改密码   |


### 1.8 全站能力（非 `president/` 目录，会长常用）


| 路径                   | 说明                         |
| -------------------- | -------------------------- |
| `pages/notice/index` | 通知中心（读公告列表）；发布通知仍在 Web 管理端 |


---

## 2. 待补充的页面（相对 Web 管理端）

下列模块在 Web 端（`AdminLayout` + 对应 `views/*.vue`）已存在，UniApp 中 `pages.json` 未注册或 `pages/president/` 下无实现（`utils/presidentRouter.ts` 中部分常量已预留路径，需与真实页面同步）。

勾选列供你后期自行打勾：`- [ ]` → `- [x]`。

### 2.1 场地管理（与场馆详情合并入口）

**已确认方案（产品）**：不强制单独做「全站场地列表页」；以 **`pages/president/venue/detail` 内嵌「本馆场地」列表**为主入口，对齐 Web `CourtManagement` 中「按场馆看场地」的心智。场地多时可在该区块内做折叠、筛选或「查看全部」仍停留本页锚点。

- [ ] **`pages/president/venue/detail` 增强** — 在现有场馆信息下方增加 **本馆场地列表**（卡片/行：名称、状态、类型等），数据来自按 `venueId` 查询的场地接口；支持点击进入单场地详情或编辑
- [ ] `pages/president/court/detail` — 单块场地详情（可选；也可先用弹层/半屏替代）
- [ ] `pages/president/court/form` — 单块场地新建/编辑（可选；可从场馆详情「添加场地」进入）
- [ ] ~~`pages/president/court/list`~~ — **非默认**：仅当未来需要「跨馆批量管场地」时再增加独立列表页

### 2.2 器材与租借（后台）

- `pages/president/equipment/list` — 器材库存列表（对齐 `EquipmentManagement`）
- `pages/president/equipment/detail` — 器材详情（可选）
- `pages/president/equipment/form` — 器材编辑（可选）
- `pages/president/equipment-rental/list` — 租借订单列表（对齐 `EquipmentRentalManagement`）
- `pages/president/equipment-rental/detail` — 租借订单详情（可选）

### 2.3 穿线服务（后台）

- `pages/president/stringing/list` — 穿线工单列表（对齐 `StringingServiceManagement`）
- `pages/president/stringing/detail` — 工单详情/改状态（可选）

### 2.4 教练与课程（后台）

- `pages/president/coach/list` — 教练列表（对齐 `CoachManagement`）
- `pages/president/coach/detail` — 教练详情（可选）
- `pages/president/coach/form` — 教练编辑（可选）
- `pages/president/course/list` — 课程列表（对齐 `CourseManagement`）
- `pages/president/course/detail` — 课程详情（可选）
- `pages/president/course/form` — 课程编辑（可选）
- `pages/president/course-booking/list` — 课程预约管理列表（对齐 `CourseBookingManagement`）
- `pages/president/course-booking/detail` — 课程预约详情（可选）

### 2.5 赛事（后台）

- `pages/president/tournament/list` — 赛事列表（对齐 `TournamentManagement`）
- `pages/president/tournament/detail` — 赛事详情（可选）
- `pages/president/tournament/form` — 赛事编辑（可选）
- `pages/president/tournament-registration/list` — 报名记录列表（对齐 `TournamentRegistrationManagement`）
- `pages/president/tournament-registration/detail` — 报名详情（可选）

### 2.6 财务扩展

- `pages/president/finance/audit-log` — 财务审计日志列表（对齐 `FinanceAuditLog`，宜做移动端精简）

### 2.7 充值（管理端）

- `pages/president/recharge/index` — 管理端充值相关（对齐 Web 管理端 `Recharge`；或与财务列表合并入口）

### 2.8 会员管理深化

- `pages/president/member/detail` — 会员详情（可选）
- `pages/president/member/form` — 会员信息调整（可选，视后端能力）

### 2.9 可选增强

- `pages/president/dashboard/index` — 独立会长工作台路由（若不想全部堆在 `pages/index/index`）
- `pages/president/notification/list` — 已发布公告列表（会长侧，可选）
- `pages/president/notification/form` — 发布/编辑通知（可选；需对接后端发布接口与权限）

---

## 3. 实施时注意

1. **pages.json**：每新增页面需在 `pages` 数组中注册，并视需要配置 `navigationStyle` 等。
2. **presidentRouter.ts**：若使用常量路由，新增页面后更新 `PRESIDENT_ROUTES` 与 `PRESIDENT_TAB_ROOTS`（如有），避免跳转死链。
3. **角色**：`VENUE_MANAGER` 使用同一套页面时，隐藏「用户管理」相关 Tab/入口；数据范围由接口按 `venueId` 控制。
4. **src/pages.json**：若工程存在双份 `pages.json`，新增页面后记得与根目录 `pages.json` 保持一致，避免构建使用错误配置。

---

## 4. Web 端对照索引（便于查接口与交互）


| Web 视图（`vue/src/views`）                | UniApp 会长端对应关系                            |
| -------------------------------------- | ----------------------------------------- |
| `CourtManagement.vue`                  | **主路径**：增强 `president/venue/detail` 内嵌本馆场地列表；**可选** `president/court/detail`、`form` |
| `EquipmentManagement.vue`              | 待补充 `president/equipment/*`               |
| `EquipmentRentalManagement.vue`        | 待补充 `president/equipment-rental/*`        |
| `StringingServiceManagement.vue`       | 待补充 `president/stringing/*`               |
| `CoachManagement.vue`                  | 待补充 `president/coach/*`                   |
| `CourseManagement.vue`                 | 待补充 `president/course/*`                  |
| `CourseBookingManagement.vue`          | 待补充 `president/course-booking/*`          |
| `TournamentManagement.vue`             | 待补充 `president/tournament/*`              |
| `TournamentRegistrationManagement.vue` | 待补充 `president/tournament-registration/*` |
| `FinanceAuditLog.vue`                  | 待补充 `president/finance/audit-log`         |
| `Recharge.vue`（管理端）                    | 待补充 `president/recharge/*` 或合并财务入口        |
| `UserManagement.vue`                   | 已有 `president/user/*`                     |
| `VenueManagement.vue`                  | 已有 `president/venue/*`                    |
| `BookingManagement.vue`                | 已并入 `pages/venue/booking` 与 `pages/booking/confirm` 链路 |
| `MemberManagement.vue`                 | 已有 `member/list`，详情/表单见 §2.8              |
| `FinanceManagement.vue`                | 已有 `president/finance/*`（审计见 §2.6）        |


---

*文档随项目迭代手动更新即可。*
