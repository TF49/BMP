# 协会会长端 · UniApp 页面清单与补充路线

> 更新日期：2026-04-12  
> 页面完成度：**100%** ✅（核心功能 100%）  
> 用途：对照 Web 管理端能力，记录会长（`PRESIDENT`）在 UniApp 中**已落地页面**与**待补充页面**，便于迭代时勾选与排期。  
> 说明：场馆管理者（`VENUE_MANAGER`）可与会长**共用同一套页面**，仅在入口与接口数据范围上区分；**系统用户管理**仅会长可见（Web 路由亦如此）。  
> 定位：UniApp 端为**移动端轻量化管理工具**，核心复杂功能在 Web 管理端完成。

---

## 1. 已存在的页面 ✅

以下路径均在根目录 `pages.json` 中注册，且在 `pages/president/` 下存在对应 `.vue` 文件。

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
| `pages/president/venue/detail` | 场馆详情    |
| `pages/president/venue/form`   | 场馆新增/编辑 |

### 1.4 场地管理 ✅

| 路径                            | 说明      |
| ----------------------------- | ------- |
| `pages/president/court/list`   | 场地列表    |
| `pages/president/court/detail` | 场地详情    |
| `pages/president/court/form`   | 场地新增/编辑 |

### 1.5 预约流程

| 路径                                | 说明        |
| --------------------------------- | --------- |
| `pages/venue/booking`             | 会长与用户共用的预约提交流程页 |
| `pages/booking/confirm`           | 会长与用户共用的确认订单页，可按来源返回不同链路 |

### 1.6 会员管理

| 路径                               | 说明   |
| -------------------------------- | ---- |
| `pages/president/member/list`    | 会员列表 |
| `pages/president/member/detail`  | 会员详情 |
| `pages/president/member/form`    | 会员编辑 |
| `pages/president/member/recharge`| 会员充值 |

### 1.7 器材管理 ✅

| 路径                                  | 说明      |
| ----------------------------------- | ------- |
| `pages/president/equipment/list`    | 器材库存列表  |
| `pages/president/equipment/detail`  | 器材详情    |
| `pages/president/equipment/form`    | 器材新增/编辑 |
| `pages/president/equipment/stock`   | 库存管理    |

### 1.8 器材租借管理 ✅

| 路径                                        | 说明      |
| ----------------------------------------- | ------- |
| `pages/president/equipment-rental/list`   | 租借订单列表  |
| `pages/president/equipment-rental/detail` | 租借订单详情  |
| `pages/president/equipment-rental/form`   | 租借订单创建  |

### 1.9 穿线服务管理 ✅

| 路径                                 | 说明      |
| ---------------------------------- | ------- |
| `pages/president/stringing/list`   | 穿线工单列表  |
| `pages/president/stringing/detail` | 穿线工单详情  |
| `pages/president/stringing/form`   | 穿线工单创建  |

### 1.10 教练管理 ✅

| 路径                             | 说明      |
| ------------------------------ | ------- |
| `pages/president/coach/list`   | 教练列表    |
| `pages/president/coach/detail` | 教练详情    |
| `pages/president/coach/form`   | 教练新增/编辑 |

### 1.11 课程管理 ✅

| 路径                             | 说明      |
| ------------------------------ | ------- |
| `pages/president/course/list`   | 课程列表    |
| `pages/president/course/detail` | 课程详情    |
| `pages/president/course/form`   | 课程新增/编辑 |

### 1.12 课程预约管理 ✅

| 路径                                      | 说明      |
| --------------------------------------- | ------- |
| `pages/president/course-booking/list`   | 课程预约列表  |
| `pages/president/course-booking/detail` | 课程预约详情  |

### 1.13 赛事管理 ✅

| 路径                                  | 说明      |
| ----------------------------------- | ------- |
| `pages/president/tournament/list`   | 赛事列表    |
| `pages/president/tournament/detail` | 赛事详情    |
| `pages/president/tournament/form`   | 赛事新增/编辑 |

### 1.14 赛事报名管理 ✅

| 路径                                                | 说明    |
| ------------------------------------------------- | ----- |
| `pages/president/tournament-registration/list`    | 报名列表  |
| `pages/president/tournament-registration/detail`  | 报名详情  |

### 1.15 财务管理

| 路径                                       | 说明        |
| ---------------------------------------- | --------- |
| `pages/president/finance/list`           | 财务列表/汇总入口 |
| `pages/president/finance/detail`         | 财务详情      |
| `pages/president/finance/reconciliation` | 对账/汇总     |
| `pages/president/finance/audit-log`      | 财务审计日志    |

### 1.16 通知管理 ✅

| 路径                                   | 说明      |
| ------------------------------------ | ------- |
| `pages/president/notification/list`  | 通知列表    |
| `pages/president/notification/detail`| 通知详情    |
| `pages/president/notification/form`  | 通知新增/编辑 |

### 1.17 个人与账号

| 路径                                        | 说明     |
| ----------------------------------------- | ------ |
| `pages/president/profile/index`           | 会长个人中心 |
| `pages/president/profile/change-password` | 修改密码   |

### 1.18 全站能力（非 `president/` 目录，会长常用）

| 路径                   | 说明                         |
| -------------------- | -------------------------- |
| `pages/notice/index` | 通知中心（读公告列表） |

---

## 2. 待补充的页面（可选增强）

> ✅ **说明**：所有核心页面已完成。会员充值功能已通过 `pages/president/member/recharge.vue` 实现，无需单独开发充值管理页面。

> 💡 **重要**：UniApp 端定位为移动端轻量化管理工具，核心复杂功能在 Web 管理端完成。以下功能非必需。

**当前无待补充页面** - 所有核心业务功能页面已完成 ✅

---

## 3. 页面完成度统计

### 核心业务模块（14个）

| 模块 | 列表页 | 详情页 | 表单页 | 完成度 |
|------|--------|--------|--------|--------|
| 用户管理 | ✅ | ✅ | ✅ | 100% |
| 场馆管理 | ✅ | ✅ | ✅ | 100% |
| 场地管理 | ✅ | ✅ | ✅ | 100% |
| 会员管理 | ✅ | ✅ | ✅ | 100% |
| 器材管理 | ✅ | ✅ | ✅ | 100% |
| 器材租借 | ✅ | ✅ | ✅ | 100% |
| 穿线服务 | ✅ | ✅ | ✅ | 100% |
| 教练管理 | ✅ | ✅ | ✅ | 100% |
| 课程管理 | ✅ | ✅ | ✅ | 100% |
| 课程预约 | ✅ | ✅ | - | 100% |
| 赛事管理 | ✅ | ✅ | ✅ | 100% |
| 赛事报名 | ✅ | ✅ | - | 100% |
| 财务管理 | ✅ | ✅ | - | 100% |
| 通知管理 | ✅ | ✅ | ✅ | 100% |

**总计：14/14 核心模块完成 = 100%**

### 可选增强模块

**无待补充模块** - 所有功能已完成 ✅

> 💡 **说明**：会员充值功能已通过 `pages/president/member/recharge.vue` 实现，支持管理员为会员代充值（现金/支付宝/微信），无需单独开发充值管理页面。

> 💡 **定位说明**：UniApp 端作为移动端轻量化管理工具，主要用于日常查看和简单操作。复杂的数据分析、批量操作、系统配置等功能建议在 Web 管理端完成。

### 综合完成度

**页面铺设：100%** ✅（14个核心模块全部完成，会员充值已实现）

> 核心业务功能已 100% 完成，UniApp 端可正常使用。会员充值功能已通过 `member/recharge.vue` 实现。

---

## 4. 实施时注意

1. **pages.json**：每新增页面需在 `pages` 数组中注册，并视需要配置 `navigationStyle` 等。
2. **presidentRouter.ts**：若使用常量路由，新增页面后更新 `PRESIDENT_ROUTES` 与 `PRESIDENT_TAB_ROOTS`（如有），避免跳转死链。
3. **角色**：`VENUE_MANAGER` 使用同一套页面时，隐藏「用户管理」相关 Tab/入口；数据范围由接口按 `venueId` 控制。
4. **src/pages.json**：若工程存在双份 `pages.json`，新增页面后记得与根目录 `pages.json` 保持一致，避免构建使用错误配置。

---

## 5. Web 端对照索引（便于查接口与交互）

| Web 视图（`vue/src/views`）                | UniApp 会长端对应关系                            | 状态 |
| -------------------------------------- | ----------------------------------------- | ---- |
| `UserManagement.vue`                   | ✅ `president/user/*`                     | 已完成 |
| `VenueManagement.vue`                  | ✅ `president/venue/*`                    | 已完成 |
| `CourtManagement.vue`                  | ✅ `president/court/*`                    | 已完成 |
| `MemberManagement.vue`                 | ✅ `president/member/*`                   | 已完成 |
| `EquipmentManagement.vue`              | ✅ `president/equipment/*`                | 已完成 |
| `EquipmentRentalManagement.vue`        | ✅ `president/equipment-rental/*`         | 已完成 |
| `StringingServiceManagement.vue`       | ✅ `president/stringing/*`                | 已完成 |
| `CoachManagement.vue`                  | ✅ `president/coach/*`                    | 已完成 |
| `CourseManagement.vue`                 | ✅ `president/course/*`                   | 已完成 |
| `CourseBookingManagement.vue`          | ✅ `president/course-booking/*`           | 已完成 |
| `TournamentManagement.vue`             | ✅ `president/tournament/*`               | 已完成 |
| `TournamentRegistrationManagement.vue` | ✅ `president/tournament-registration/*`  | 已完成 |
| `FinanceManagement.vue`                | ✅ `president/finance/*`                  | 已完成 |
| `FinanceAuditLog.vue`                  | ✅ `president/finance/audit-log`          | 已完成 |
| `BookingManagement.vue`                | ✅ `pages/venue/booking` + `pages/booking/confirm` | 已完成 |
| `Recharge.vue`（管理端）                    | ⚠️ 待补充 `president/recharge/*` 或使用 `member/recharge` | 待定 |

---

## 6. 更新日志

### 2026-04-12
- ✅ 更新文档，反映实际页面完成情况
- ✅ 将所有已存在的页面从"待补充"移到"已存在"
- ✅ 页面完成度从 90% 更新为 **100%**（所有核心页面已完成）
- ✅ 新增页面完成度统计表
- ✅ 确认会员充值功能已实现（`member/recharge.vue`），无需单独开发充值管理页面
- ✅ 明确 UniApp 端定位：移动端轻量化管理工具，复杂功能在 Web 端

---

*文档随项目迭代手动更新。*
