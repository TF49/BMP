# 会长端 UI 扫描整改清单

## 1. 扫描范围

- 扫描目录：`BMP-uniapp/pages/president/**`
- 扫描对象：会长端 `uniapp` 前端页面、会长端专属布局与主题配置
- 重点检查：
- 页面是否全部落地
- 页面 UI 是否达到正式完成状态
- 整体设计风格是否统一
- 是否统一采用橙色主题

## 2. 总体结论

- 会长端页面覆盖度较高，当前共扫描到 `45` 个页面，`pages.json` 中均已注册，对应文件也都存在。
- 但“页面存在”不等于“UI 已全部完成”。目前只能判断为：页面基本铺开完成，但不少页面仍处于半成品、原型态或待接口联调状态。
- 整体设计语言有统一主干，但没有完全收口，当前至少并存多种子风格。
- 会长端主视觉偏橙色，但并没有做到彻底统一的橙色主题，绿色全局 token 和绿色主按钮仍有明显残留。

## 3. 核心判断

### 3.1 页面是否全部完成

- 从“页面数量与路由注册”看：基本完成
- 从“UI 成熟度与交互完整性”看：未全部完成

当前扫描结果：

- 会长端页面总数：`45`
- 使用 `PresidentLayout` 的页面数：`43`
- 直接引入 `@/api/president/*` 的页面数：`11`
- 存在 `mock` 痕迹的页面：至少 `10`
- 存在 `TODO` 痕迹的页面：至少 `10`

结论：

- 会长端不能定义为“没做完”
- 但也不能定义为“全部正式完成”
- 更准确的状态是：`页面已铺齐，正式完成度中等`

### 3.2 整体风格是否是一家人

- 是“一部分像一家人”，不是“全部完全统一”

统一点：

- 多数页面使用统一会长端壳层 `PresidentLayout`
- 大量页面共享浅灰背景、白色卡片、大圆角、橙色强调色、品牌字样 `Kinetic Logic`
- 列表页普遍有统一结构：顶部栏、搜索区、统计卡、列表卡片、悬浮新增按钮

不统一点：

- 有的页面走标准管理后台风
- 有的页面走玻璃拟态风
- 有的页面走运营工作台风
- 有的页面仍保留原型稿式底部 Tab Mockup

结论：

- 当前属于“有统一基因，但未完全收口”

### 3.3 是否全部采用橙色主题

- 不是

当前状态更接近：

- 会长端局部主视觉：橙色
- 全局主题基线：仍有绿色残留
- 功能状态色：绿、蓝、红、灰混用

因此不能定义为“全部采用橙色主题”，只能定义为：

- `会长端以橙色为主，但未完成全局橙色主题统一`

## 4. 页面分级清单

## 4.1 已完成度较高

这些页面具备较完整的结构、较稳定的视觉表现，部分已接入真实接口，可作为会长端标准页参考。

| 页面 | 完成度 | 风格一致性 | 橙色主题 | 备注 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| `pages/president/user/list.vue` | 高 | 高 | 较高 | 列表页结构成熟，可作为管理列表基准页 | P2 |
| `pages/president/user/detail.vue` | 中高 | 中 | 低 | 已接接口，但主按钮仍为绿色渐变 | P1 |
| `pages/president/user/form.vue` | 中高 | 中 | 低 | 已接接口，但主按钮仍为绿色渐变 | P1 |
| `pages/president/venue/list.vue` | 高 | 高 | 高 | 会长端列表页完成度较高 | P2 |
| `pages/president/venue/form.vue` | 中高 | 中高 | 高 | 接口较完整，仍需统一表单视觉细节 | P2 |
| `pages/president/member/form.vue` | 中高 | 中 | 高 | 可用，但表达偏“产品原型风” | P2 |
| `pages/president/member/recharge.vue` | 中高 | 中 | 中 | 功能可用，颜色混用较多 | P2 |
| `pages/president/finance/detail.vue` | 中高 | 中 | 中 | 接口完成度较好，但玻璃态与常规页不完全一致 | P2 |
| `pages/president/profile/change-password.vue` | 中高 | 中 | 中 | 可用，但与其他表单页可继续统一 | P3 |

## 4.2 半完成 / 原型态页面

这些页面虽然存在并可进入，但仍有 `mock`、`TODO`、`开发中`、`示例`、静态数据回退等信号，不建议视为正式完成页。

| 页面 | 完成度 | 风格一致性 | 橙色主题 | 主要问题 | 优先级 |
| --- | --- | --- | --- | --- | --- |
| `pages/president/course-booking/detail.vue` | 低 | 中 | 中 | 使用 `mockDetail()`，详情/取消/核销待接入 | P1 |
| `pages/president/court/detail.vue` | 低 | 中 | 中 | 详情依赖 mock，动作按钮未完成 | P1 |
| `pages/president/court/list.vue` | 低 | 低 | 中 | 静态数组、原型式 tabbar、未走统一壳层 | P0 |
| `pages/president/equipment-rental/detail.vue` | 低 | 中 | 中 | 详情 mock，归还流程待接入 | P1 |
| `pages/president/equipment-rental/form.vue` | 低 | 中 | 中 | 手动录入仍是 TODO | P1 |
| `pages/president/finance/audit-log.vue` | 低 | 中 | 中 | 分页筛选接口仍待接入 | P1 |
| `pages/president/member/detail.vue` | 低 | 中 | 中 | 仍有 mock 指标与“开发中”模块 | P1 |
| `pages/president/member/list.vue` | 中 | 中 | 中 | 保留 mock 回退逻辑，完成度不稳定 | P2 |
| `pages/president/notification/detail.vue` | 低 | 中 | 中 | 详情 mock，撤回接口待接入 | P1 |
| `pages/president/notification/form.vue` | 低 | 中 | 中 | 草稿、发布接口未完成 | P1 |
| `pages/president/notification/list.vue` | 低中 | 低中 | 中高 | 列表为静态 notices，操作按钮为“开发中” | P2 |
| `pages/president/stringing/detail.vue` | 低 | 中 | 中 | 详情 mock，打印/进度接口未接入 | P1 |
| `pages/president/stringing/form.vue` | 低 | 中 | 中 | 创建工单接口未接入 | P1 |
| `pages/president/stringing/list.vue` | 低中 | 低中 | 中高 | 多处操作仍为示例/开发中 | P2 |
| `pages/president/tournament/detail.vue` | 低中 | 中 | 中 | 筛选、编排、导出仍未完成 | P2 |
| `pages/president/tournament/list.vue` | 低中 | 中 | 中 | 搜索、设置仍为开发中 | P2 |
| `pages/president/tournament-registration/detail.vue` | 低 | 中 | 中 | 详情 mock，通过/拒绝接口待接入 | P1 |
| `pages/president/tournament-registration/list.vue` | 低中 | 中 | 中 | 静态列表+开发中筛选 | P2 |

## 4.3 风格不统一页面

这些页面最容易破坏“会长端像一家人”的整体观感，应优先收口。

| 页面 | 问题类型 | 具体表现 | 优先级 |
| --- | --- | --- | --- |
| `pages/president/court/list.vue` | 原型页残留 | 未使用 `PresidentLayout`，自带 `Bottom Tab Bar Mockup`，整体不像正式会长端页面 | P0 |
| `pages/president/venue/detail.vue` | 壳层不统一 | 未使用 `PresidentLayout`，底部自建玻璃操作栏，细节系统与其他详情页分离 | P0 |
| `pages/president/profile/index.vue` | 视觉浓度不一致 | 玻璃拟态感较强，与大多数管理页差异明显 | P1 |
| `pages/president/notification/list.vue` | 运营工作台风 | 抽屉导航、信息台布局，和标准 CRUD 页差异较大 | P2 |
| `pages/president/stringing/list.vue` | 运营工作台风 | 信息组织方式与普通管理页不同，易形成独立子风格 | P2 |
| `pages/president/equipment-rental/list.vue` | 组件化独立风格 | 结构更像独立产品模块，不像统一会长端列表页 | P2 |

建议收口方向：

- 列表页统一模板：
- 顶部栏
- Hero 区
- 搜索筛选
- 数据卡片
- 列表卡片
- FAB

- 详情/表单页统一模板：
- 顶部栏
- 主信息卡
- 分区内容卡
- 底部主操作区

## 4.4 非橙色主题残留

这部分说明会长端还没有真正完成橙色主题统一。

### 4.4.1 主题源头残留

| 文件 | 问题 | 优先级 |
| --- | --- | --- |
| `uni.scss` | 全局主色仍是绿色系，`$u-primary`、`$bmp-brand-primary` 未切换为橙色 | P0 |
| `styles/theme.scss` | `--color-primary` 仍是绿色，H5 变量基线不统一 | P0 |
| `utils/presidentTabBar.ts` | tab 选中色仍为绿色 `#3cc51f` | P0 |

### 4.4.2 页面级绿色主色残留

| 页面 | 问题 | 优先级 |
| --- | --- | --- |
| `pages/president/finance/reconciliation.vue` | 主按钮使用绿色渐变 | P0 |
| `pages/president/user/detail.vue` | 主视觉 CTA 使用绿色渐变 | P0 |
| `pages/president/user/form.vue` | 主按钮使用绿色渐变 | P0 |

### 4.4.3 功能色混用较多页面

| 页面 | 问题 | 优先级 |
| --- | --- | --- |
| `pages/president/member/recharge.vue` | 支付方式色彩较多，品牌橙色不够聚焦 | P2 |
| `pages/president/tournament-registration/list.vue` | 成功/拒绝/信息色对比强，主品牌色存在感偏弱 | P2 |
| `pages/president/coach/list.vue` | 状态色较多，整体橙色主导感被削弱 | P2 |

## 5. 按任务级别拆分

下面按“任务级别”来拆，方便你直接做排期、指派和验收。

### 5.1 一级任务：主题与框架统一

任务目标：

- 把会长端从“局部橙色”改成“系统级橙色主题”
- 把会长端页面壳层统一成一套
- 把最明显的原型残留去掉

涉及文件：

- `components/president/PresidentLayout.vue`
- `utils/presidentTabBar.ts`
- `uni.scss`
- `styles/theme.scss`
- `pages/president/court/list.vue`
- `pages/president/venue/detail.vue`
- `pages/president/finance/reconciliation.vue`
- `pages/president/user/detail.vue`
- `pages/president/user/form.vue`

主要任务：

- 统一主色 token 为橙色
- 将 tabBar 激活态统一改为橙色
- 清理绿色主按钮、绿色渐变、绿色主视觉 CTA
- 让 `court/list` 接入统一 `PresidentLayout`
- 让 `venue/detail` 回归统一壳层，不再独立维护底部玻璃操作栏
- 统一详情页底部操作区表现

验收标准：

- 会长端所有主按钮、主图标、激活态、进度条、tab 选中态为同一套橙色规范
- 会长端不再出现“页面壳层长得不像一个产品”的情况

### 5.2 二级任务：半成品页面转正式页

任务目标：

- 清理 `mock`、`TODO`、`开发中`
- 把关键详情页和表单页从原型态提升到正式可用态

涉及页面：

- `pages/president/course-booking/detail.vue`
- `pages/president/court/detail.vue`
- `pages/president/equipment-rental/detail.vue`
- `pages/president/equipment-rental/form.vue`
- `pages/president/finance/audit-log.vue`
- `pages/president/member/detail.vue`
- `pages/president/notification/detail.vue`
- `pages/president/notification/form.vue`
- `pages/president/stringing/detail.vue`
- `pages/president/stringing/form.vue`
- `pages/president/tournament-registration/detail.vue`

主要任务：

- 去掉 `mockDetail()` 和静态详情数据
- 去掉“开发中”“示例”“占位”交互
- 接入真实详情接口
- 接入真实主操作接口
- 没有后端能力前，先隐藏未完成按钮，不要保留假功能入口

验收标准：

- 页面不再依赖 mock 数据展示核心信息
- 页面主要功能按钮可真实执行
- 页面不再出现明显的待开发文案

### 5.3 三级任务：列表页风格收口

任务目标：

- 让会长端所有列表页看起来属于同一套后台
- 减少“这个页面像运营台，那个页面像原型稿”的割裂感

涉及页面：

- `pages/president/member/list.vue`
- `pages/president/notification/list.vue`
- `pages/president/stringing/list.vue`
- `pages/president/equipment-rental/list.vue`
- `pages/president/tournament/list.vue`
- `pages/president/tournament-registration/list.vue`
- `pages/president/member/recharge.vue`

主要任务：

- 统一列表页顶部栏结构
- 统一搜索栏样式
- 统一统计卡样式
- 统一卡片圆角、阴影、边框、留白
- 统一 FAB 与筛选区表现
- 收敛抽屉式、运营台式、独立组件式的视觉差异

验收标准：

- 不同模块列表页切换时，用户能明显感受到是同一个会长端产品
- 不再存在过于突兀的独立子风格

### 5.4 四级任务：视觉精修与体验打磨

任务目标：

- 在统一完成后做细节提升
- 提高高级感，而不是继续扩大风格差异

涉及页面：

- `pages/president/profile/index.vue`
- `pages/president/profile/change-password.vue`
- `pages/president/member/form.vue`
- `pages/president/venue/form.vue`
- `pages/president/finance/detail.vue`

主要任务：

- 统一玻璃态页面和普通卡片页的层级感
- 统一表单页标题、说明文案、按钮、间距
- 统一安全区与底部按钮区处理
- 修正细节配色、辅助色、留白和字号层级

验收标准：

- 页面细节更稳，不再出现“有些页很精致，有些页很朴素”的落差
- 会长端在视觉上达到正式上线产品的统一感

## 6. 按任务级别对应的优先级映射

| 任务级别 | 对应优先级 | 说明 |
| --- | --- | --- |
| 一级任务 | P0 | 必须先做，决定会长端是否统一 |
| 二级任务 | P1 | 必须尽快做，决定页面是否真完成 |
| 三级任务 | P2 | 决定整体观感是否像一家人 |
| 四级任务 | P3 | 精修阶段，提升上线品质 |

## 7. 推荐最终状态

建议会长端最终统一为以下设计规范：

- 品牌主色：橙色
- 成功态：绿色，仅用于状态反馈
- 错误态：红色，仅用于警示
- 信息态：蓝色，仅用于辅助信息，不承担品牌角色
- 列表页头部：统一
- 详情页头部：统一
- 表单页底部主按钮：统一
- tabBar 激活态：统一橙色
- 渐变背景：统一使用会长端暖橙浅色系

## 8. 最终结论

当前会长端 UI 的结论可以明确写成一句话：

> 会长端 `uniapp` 前端页面已经基本铺齐，但仍未达到“全部正式完成”的状态；整体设计风格已有统一基础，但尚未完全收口成一套；主视觉偏橙色，但还没有真正完成全局橙色主题统一。
