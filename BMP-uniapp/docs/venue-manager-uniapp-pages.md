# 场馆管理者端 · UniApp 页面清单与补充路线

> 角色：`VENUE_MANAGER`（场馆管理者）  
> 用途：与 Web 管理端对齐，记录 UniApp 中**建议具备**的管理页、**工程现状**与**待补充**项，便于与会长端分工、共用代码时查阅。  
> 对照文档：[协会会长端 · UniApp 页面清单](./president-uniapp-pages.md)

---

## 1. 与会长端的差异（产品 / 路由规则）

| 项目 | 协会会长 `PRESIDENT` | 场馆管理者 `VENUE_MANAGER` |
|------|----------------------|----------------------------|
| 系统用户管理 | Web：`/admin/user/management`，UniApp：`pages/president/user/*` | **无**（Web 路由上亦无此菜单） |
| 其余管理模块 | 全平台/全馆数据（由后端约束） | **通常为单馆**数据范围（`venueId`，由后端与登录身份决定） |
| UniApp 页面路径 | 建议使用 `pages/president/*` 或后续统一 `pages/admin/*` | **与会长共用同一套页面文件**，仅 **隐藏「用户管理」入口**、Tab 与路由守卫不同 |

---

## 2. 工程现状（重要）

当前 `utils/roleCheck.ts` 中说明：**场馆管理员请使用网页端**，登录校验侧可能仍 **不允许** `VENUE_MANAGER` 使用小程序。若产品改为「场馆管理者也可登录 UniApp」：

- [ ] 放开 `isAllowedRole` / 登录后分流逻辑，使 `VENUE_MANAGER` 进入管理端壳（与会长共用布局）
- [ ] 自定义 TabBar：不包含 `pages/president/user/list`
- [ ] 任意跳转 `president/user/*` 的路由需加角色守卫，**场馆管理者直接 403 或重定向**

---

## 3. 已存在、且场馆管理者应复用的页面

与会长共用 **`pages/president/`** 下已实现部分（**不含**用户管理三页）：

| 模块 | 路径 | 说明 |
|------|------|------|
| 工作台入口 | `pages/index/index` | 需做「场馆管理者态」首页（可与会长工作台分支展示） |
| 场馆管理 | `pages/president/venue/list`、`detail`、`form` | 列表多为单馆或本馆优先 |
| 预约管理 | `pages/venue/booking`、`pages/booking/confirm` | 本馆预约 |
| 会员管理 | `pages/president/member/list` | 本馆会员 |
| 财务管理 | `pages/president/finance/list`、`detail`、`reconciliation` | 视后端是否开放给场馆角色 |
| 个人与账号 | `pages/president/profile/index`、`change-password` | 与会长相同 |
| 通知 | `pages/notice/index` | 读公告 |

**不应给场馆管理者展示的页面（仅会长）：**

| 路径 | 说明 |
|------|------|
| `pages/president/user/list` | 系统用户列表 |
| `pages/president/user/detail` | 系统用户详情 |
| `pages/president/user/form` | 系统用户编辑 |

---

## 4. 待补充页面（相对 Web 管理端 · 与会长清单一致）

场馆管理者在 Web 端与会长**共用同一批管理菜单**（除用户管理外），故 **待补充模块与会长文档 §2 相同**，仅实现时：

- 列表/统计接口带 **本馆 `venueId`**
- UI 上可不展示「新建全平台场馆」等超出单馆权限的操作（由后端拒绝 + 前端隐藏）

请直接对照 **[president-uniapp-pages.md §2 待补充](./president-uniapp-pages.md#2-待补充页面相对-web-管理端)**，勾选下列大类即可（不在此重复展开子路径，避免双份维护）：

- [ ] 场地管理：与会长一致，**主路径**为 `venue/detail` 内嵌本馆场地；可选 `president/court/detail`、`form`
- [ ] 器材库存 `president/equipment/*`
- [ ] 器材租借后台 `president/equipment-rental/*`
- [ ] 穿线后台 `president/stringing/*`
- [ ] 教练管理 `president/coach/*`
- [ ] 课程管理 `president/course/*`
- [ ] 课程预约管理 `president/course-booking/*`
- [ ] 赛事管理 `president/tournament/*`
- [ ] 赛事报名管理 `president/tournament-registration/*`
- [ ] 财务审计 `president/finance/audit-log`
- [ ] 管理端充值 `president/recharge/*`（若后端对场馆角色开放）
- [ ] 会员详情/编辑 `president/member/detail`、`form`（可选）
- [ ] 独立工作台 `president/dashboard/index`（可选）
- [ ] 通知发布 `president/notification/*`（可选）

---

## 5. Web 端对照索引

与会长相同，参见 [president-uniapp-pages.md §4](./president-uniapp-pages.md#4-web-端对照索引便于查接口与交互)。  
**唯一差异**：场馆管理者 **无** `UserManagement.vue` 对应的小程序页面。

---

*文档随项目迭代手动更新；待补充明细以会长文档 §2 为准。*
