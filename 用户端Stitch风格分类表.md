# 用户端 Stitch 风格分类表

## 文档目的

这份文档用于将当前 `BMP-uniapp` 用户端页面按视觉来源和完成度分成 3 类：

1. 已是 Stitch 风格
2. 半 Stitch 风格
3. 非 Stitch 风格

这里的 “Stitch 风格” 不是单指页面是否由工具生成，而是指页面是否已经呈现出一套明显的 Google Stitch 式产品化移动端视觉语言，包括但不限于：

- `KINETIC LOGIC` 品牌字标
- 橙色品牌主色 `#ea580c / #ff6600 / #a33e00`
- 大圆角白卡片
- 英文标签、副标题、营销化标题区
- 更强的产品展示感，而不是传统业务表单页或列表页

---

## 一、分类结论

当前用户端已经存在一批明显的 Stitch 风格页面，并且核心业务链路已经逐步形成闭环。

整体上可以这样理解：

- 列表页、首页、部分详情页和流程页，已经明显进入 Stitch 风格
- 一些页面已经使用橙色品牌体系，但结构仍带有旧页面痕迹，属于半 Stitch
- 目前主要剩余的旧风格页面集中在设置子页和少量功能页

最大的结构性问题不是单页不好看，而是：

- 多条核心业务链路已经完成 Stitch 化
- 但设置子页、场馆细分流程仍存在旧版页面
- 风格断层已经从“主链路断层”收敛为“外围功能断层”

---

## 二、已是 Stitch 风格

这一类页面可以认为已经具备较强的 Stitch 视觉特征，能够作为后续继续扩展用户端新版 UI 的直接参考。

| 页面 | 路径 | 分类判断 | 主要依据 |
| --- | --- | --- | --- |
| 首页内容区 | `pages/index/components/MemberIndex.vue` | 已是 Stitch | 品牌字标、橙色 CTA、产品化服务宫格、卡片化内容区 |
| 我的预约 | `pages/booking/list.vue` | 已是 Stitch | 品牌顶栏、重点 hero 卡、深色统计块、橙色交互强调 |
| 场馆列表 | `pages/venue/list.vue` | 已是 Stitch | 品牌标题、卡片式场馆封面、橙色细节、高产品感列表布局 |
| 课程列表 | `pages/course/list.vue` | 已是 Stitch | 英文标题、教练卡、日期胶囊、课程卡、橙黑 CTA、明显 Stitch 气质 |
| 课程详情 | `pages/course/detail.vue` | 已是 Stitch | 已按新版原型重构：沉浸式 hero、教练卡、信息双卡、课程大纲、地图卡、底部固定预订栏，和课程列表形成连续品牌链路 |
| 课程预约 | `pages/course/booking.vue` | 已是 Stitch | 已按提交订单原型重构：品牌顶栏、课程订单卡、学员信息区、支付明细区、底部固定支付按钮，与课程详情页形成完整下单链路 |
| 赛事列表 | `pages/tournament/list.vue` | 已是 Stitch | 大 hero、倒计时、英文章节标题、营销式赛事卡、品牌活动区 |
| 赛事详情 | `pages/tournament/detail.vue` | 已是 Stitch | 已按新版原型重构：沉浸式赛事 hero、四宫格关键信息、赛事日程时间轴、规则清单、报名费行动卡，与赛事列表品牌语言衔接 |
| 赛事报名 | `pages/tournament/register.vue` | 已是 Stitch | 已按赛事报名原型重构：赛事横幅、分区表单、搭档信息卡、免责勾选区、底部固定报名费用栏，与赛事详情页形成完整报名链路 |
| 器材列表 | `pages/equipment/list.vue` | 已是 Stitch | 品牌顶栏、胶囊筛选、商品大图卡、橙色浮动操作按钮 |
| 器材详情 | `pages/equipment/detail.vue` | 已是 Stitch | 基于器材管理端原型做用户端适配：品牌顶栏、大图画廊、租借统计卡、价格卡、库存状态卡与用户可执行的租借/收藏/分享操作，和器材列表、租借确认页形成连续链路 |
| 器材租借确认 | `pages/equipment/rental.vue` | 已是 Stitch | 大留白、确认页卡片分区、橙色主操作按钮、清晰价格汇总 |
| 穿线预约 | `pages/stringing/create.vue` | 已是 Stitch | 分步骤卡片、品牌化预约流、橙色大按钮、信息架构成熟 |
| 穿线列表 | `pages/stringing/list.vue` | 已是 Stitch | 复用会长端穿线列表骨架：搜索卡、摘要统计、状态胶囊和工单卡片结构已迁移为用户端可读版本 |
| 穿线详情 | `pages/stringing/detail.vue` | 已是 Stitch | 复用会长端穿线详情骨架：状态 hero、分区字段卡、底部动作区已改造成用户端查看/取消/再次预约流程 |
| 穿线查询 | `pages/stringing/query.vue` | 已是 Stitch | 已重构为品牌化查询页：品牌 hero、工单编号输入、扫码识别、结果摘要卡与详情分区已统一为 Stitch 风格 |
| 充值中心 | `pages/recharge/index.vue` | 已是 Stitch | 复用会长端充值页骨架：会员身份卡、金额网格、支付方式卡、底部确认栏已迁移为用户端自助充值流程 |
| 充值记录 | `pages/recharge/records.vue` | 已是 Stitch | 品牌色总览卡、强卡片分组、记录样式统一、视觉节奏现代 |
| 消费记录 | `pages/profile/records.vue` | 已是 Stitch | 已按账单原型重做：橙色顶栏、账单周期卡、三类消费统计、退款双金额样式、历史记录按钮，和钱包链路视觉统一 |
| 安全设置 | `pages/settings/security.vue` | 已是 Stitch | 复用会长端修改密码页与安全卡片语言：安全等级 hero、账户安全卡、高级安全开关与登录记录分组已统一为 Stitch 风格 |
| 设置中心 | `pages/settings/index.vue` | 已是 Stitch | 已按用户端设置中心重构：品牌顶栏、橙色身份 hero、分组设置卡、偏好区与退出登录区已统一为 Stitch 风格 |
| 账户设置 | `pages/settings/account.vue` | 已是 Stitch | 已按用户端独立逻辑重构：品牌顶栏、账户 hero、身份资料双列表单、手机号入口与安全入口区已统一为 Stitch 风格，并保留用户端资料保存逻辑 |
| 通知设置 | `pages/settings/notification.vue` | 已是 Stitch | 已重构为品牌化通知设置页：品牌 hero、通知分组卡、开关控制和免打扰时段卡片已统一为 Stitch 风格 |
| 隐私设置 | `pages/settings/privacy.vue` | 已是 Stitch | 已重构为品牌化隐私中心：品牌 hero、隐私权限分组、数据管理卡和隐私文件入口已统一为 Stitch 风格 |
| 关于我们 | `pages/settings/about.vue` | 已是 Stitch | 已重构为品牌化 About 页面：品牌 hero、能力卡片、品牌信息、联系方式、渠道入口与法律版本区已统一为 Stitch 风格 |
| 帮助与反馈（用户端独立页） | `pages/settings/help-user.vue` | 已是 Stitch | 已拆分为独立用户端帮助页：品牌 hero、快捷入口、FAQ 快照、问题分类与联系方式已统一为 Stitch 风格，并避免影响会长端旧帮助页 |
| 搜索首页 | `pages/search/index.vue` | 已是 Stitch | 已重构为品牌化搜索入口：`KINETIC LOGIC` 顶部品牌区、搜索 hero、热门搜索、搜索历史与建议流已统一为 Stitch 语言 |
| 搜索结果 | `pages/search/result.vue` | 已是 Stitch | 已重构为品牌化搜索结果页：品牌头部、关键词摘要、分类胶囊切换、场馆/课程/赛事卡片与加载状态已统一为 Stitch 风格 |
| 我的 | `pages/profile/index.vue` | 已是 Stitch | 已按用户端独立逻辑重构：品牌 hero、账户统计条、会员入口、常用功能宫格与账户记录分组列表已统一为 Stitch 风格 |
| 个人信息 | `pages/profile/info.vue` | 已是 Stitch | 已按用户资料编辑页重构：品牌头图、资料卡片网格、账号安全快捷入口与底部保存按钮已统一为 Stitch 风格 |
| 会员信息 | `pages/profile/member.vue` | 已是 Stitch | 已升级为品牌化会员中心：会员身份卡、权益分组、成长信息与操作区已统一到新版 Stitch 风格 |
| 通知中心 | `pages/notice/index.vue` | 已是 Stitch | 已升级为品牌化通知中心：摘要信息卡、通知列表卡和阅读型内容结构已统一到 Stitch 风格 |
| 用户端底部导航 | `components/CustomTabBar/CustomTabBar.vue` | 已是 Stitch 配套组件 | 玻璃态底栏、橙色激活态、整体形态与新版页面一致 |

### 这一类页面的共同特征

- 已经明显摆脱旧绿色业务风
- 页面更像“产品型移动端界面”
- 排版、卡片、按钮、配色有统一品牌感
- 能直接作为后续用户端新页面的参考底稿

---

## 三、半 Stitch 风格

这一类页面已经和 Stitch 方向接近，部分页面甚至已经使用了橙色品牌体系，但还没有完全进入 Stitch 的版式和组件语言。

| 页面 | 路径 | 分类判断 | 主要原因 |
| --- | --- | --- | --- |
| 登录 | `pages/login/login.vue` | 半 Stitch | 品牌包装很强，但属于入口页视觉体系，与 Stitch 列表页语言不同 |
| 注册 | `pages/register/register.vue` | 半 Stitch | 与登录页同系，品牌化明显，但不完全属于 Stitch 主内容结构 |
| 找回密码 | `pages/recover/recover.vue` | 半 Stitch | 与登录注册同系，品牌感强，但更偏入口页 |
| 场馆详情 | `pages/venue/detail.vue` | 半 Stitch | 已升级为品牌化详情页，但仍可继续补充更多与场馆列表完全一致的卡片节奏与联动内容 |
| 场地预订 | `pages/venue/booking.vue` | 半 Stitch | 已升级为品牌化预约流程页，但仍可继续压实与场馆详情、预约确认页之间的完整链路一致性 |

### 这一类页面的共同特征

- 已经不再属于旧绿色业务风
- 部分模块已经换成橙色品牌色
- 但页面壳、章节结构、卡片语言还未完全统一
- 可以继续沿 Stitch 方向升级，而不是推倒重来

---

## 四、非 Stitch 风格

这一类页面仍以旧业务页结构为主，虽然个别地方已经换成了橙色或引入局部卡片样式，但整体并不属于 Stitch 风格。

### 1. 设置 / 资料模块

| 页面 | 路径 | 分类判断 | 原因 |
| --- | --- | --- | --- |
| 反馈 / FAQ 等设置子页 | `pages/settings/*` | 非 Stitch | 仍有部分旧式设置子页尚未 Stitch 化；用户端帮助页已拆分到 `pages/settings/help-user.vue`，原 `pages/settings/help.vue` 继续保留给会长端入口使用 |

---

## 五、最明显的风格断层

当前最需要注意的不是页面数量，而是链路断层。

### 1. 课程链路

- `Stitch`：`pages/course/list.vue`
- `Stitch`：`pages/course/detail.vue`
- `Stitch`：`pages/course/booking.vue`

结论：课程模块已补齐到“列表页 + 详情页 + 预约页”一致的新链路，当前这一条核心业务链路已完成 Stitch 化。

### 2. 赛事链路

- `Stitch`：`pages/tournament/list.vue`
- `Stitch`：`pages/tournament/detail.vue`
- `Stitch`：`pages/tournament/register.vue`

结论：赛事模块已补齐到“列表页 + 详情页 + 报名页”一致的新链路，当前这条核心业务链路已完成 Stitch 化。

### 3. 器材链路

- `Stitch`：`pages/equipment/list.vue`
- `Stitch`：`pages/equipment/detail.vue`
- `Stitch`：`pages/equipment/rental.vue`

结论：器材模块已补齐到“列表页 + 详情页 + 租借确认页”一致的新链路。

### 4. 充值链路

- `Stitch`：`pages/recharge/index.vue`
- `Stitch`：`pages/recharge/records.vue`

结论：充值模块已补齐到“充值中心 + 充值记录”一致的新链路。

### 5. 穿线链路

- `Stitch`：`pages/stringing/create.vue`
- `Stitch`：`pages/stringing/list.vue`
- `Stitch`：`pages/stringing/detail.vue`
- `Stitch`：`pages/stringing/query.vue`

结论：穿线模块已补齐到“预约页 + 查询页 + 列表页 + 详情页”一致的新体系，当前这条链路已完成 Stitch 化。

---

## 六、后续设计与改版建议

### 第一优先级：补齐场馆链路并继续消除高频断层

建议优先处理以下页面：

1. `pages/venue/detail.vue`
2. `pages/venue/booking.vue`
3. `pages/profile/member.vue`
4. `pages/notice/index.vue`
5. `pages/settings/feedback.vue`

原因：

- 这些页面承接高频入口或品牌化列表页
- 用户会在预订、资料、会员中心这些关键节点感受到是否还是同一套产品

### 第二优先级：统一用户中心相关页面

建议处理：

1. `pages/settings/feedback.vue`
2. `pages/settings/faq.vue`
3. `pages/venue/detail.vue`
4. `pages/venue/booking.vue`
5. `pages/profile/member.vue`

原因：

- 用户中心是高频使用区域
- 当前既有新版橙色风，也残留多张旧式设置子页，需要统一成同一套安全感、留白和卡片语言

### 第三优先级：补齐设置尾页与场馆细节页

建议处理：

1. `pages/settings/feedback.vue`
2. `pages/settings/faq.vue`
3. `pages/venue/detail.vue`
4. `pages/venue/booking.vue`
5. `pages/profile/member.vue`

原因：

- 这些页面仍不属于 Stitch 体系，和新版页面相比还缺少统一的产品化骨架
- 完成这一批后，用户端视觉体系会从“主链路完成”进一步升级为“日常高频区域基本统一”

---

## 七、可复用会长端页面清单

这部分的目的不是让用户端“照搬会长端”，而是明确哪些页面已经有可复用的 Stitch 化骨架，只需要去掉管理动作、替换为用户动作，就不必再重新让 Stitch 从零设计一版。

### 1. 可直接复用骨架并优先改造成用户端

| 用户端页面 | 当前状态 | 可复用的会长端页面 | 复用建议 |
| --- | --- | --- | --- |
| `pages/venue/detail.vue` | 半 Stitch | `pages/president/venue/detail.vue` | 已完成一轮用户端适配，当前仍可继续参考会长端详情页补强深层信息卡和联动模块，但不能直接共用会长端整页逻辑 |
| `pages/stringing/list.vue` | 已是 Stitch | `pages/president/stringing/list.vue` | 已完成复用改造，会长端搜索区、数据摘要卡、列表卡片与状态胶囊骨架已迁移为用户端可读版本 |
| `pages/stringing/detail.vue` | 已是 Stitch | `pages/president/stringing/detail.vue` | 已完成复用改造，会长端状态 hero、字段分区与底部动作区已改造成用户端查看流程 |
| `pages/recharge/index.vue` | 已是 Stitch | `pages/president/member/recharge.vue` | 已完成复用改造，会长端充值骨架已切换成用户端自助充值语义 |
| `pages/settings/security.vue` | 已是 Stitch | `pages/president/profile/change-password.vue` | 已完成复用改造，会长端密码页和安全卡片语言已迁移为用户端安全设置 |

### 2. 可部分复用结构，但需要较多用户化调整

| 用户端页面 | 当前状态 | 可复用的会长端页面 | 复用建议 |
| --- | --- | --- | --- |
| `pages/notice/index.vue` | 已是 Stitch | `pages/president/notification/list.vue` | 已完成用户端重写：借用了通知卡、时间区和摘要骨架，但已切回用户端阅读型通知中心逻辑 |
| `pages/profile/index.vue` | 已是 Stitch | `pages/president/profile/index.vue` | 已完成用户端重写：借用了顶部身份区和分组入口卡骨架，但菜单入口、账户数据和跳转均保留用户端实现 |
| `pages/profile/info.vue` | 已是 Stitch | `pages/president/profile/index.vue`、`pages/president/profile/change-password.vue` | 已完成用户端重写：借用了留白、卡片和安全区节奏，但字段、跳转、按钮行为均已切回用户端逻辑 |
| `pages/settings/index.vue` | 已是 Stitch | `pages/president/profile/index.vue` | 已完成用户端重写：借用了分组列表卡视觉骨架，但入口、跳转和偏好设置逻辑全部为用户端实现 |
| `pages/settings/account.vue` | 已是 Stitch | `pages/president/profile/index.vue`、`pages/president/profile/change-password.vue` | 已完成用户端重写：借用了资料卡与安全入口节奏，但资料保存、手机号入口和跳转均保留用户端逻辑 |

### 3. 不建议直接复用会长端的页面

这些页面即便会长端存在，也不建议拿来直接改用户端，因为它们本质上是后台管理逻辑，不是用户消费链路：

- `pages/search/index.vue`、`pages/search/result.vue`
  原因：当前没有对应合适的会长端搜索页可直接复用，强行复用管理列表会让用户端搜索结果显得像后台。
- `pages/settings/about.vue`、`pages/settings/privacy.vue`、`pages/settings/notification.vue`
  原因：这类页面信息密度低，更适合基于用户端已有页面统一做轻量 Stitch 化，而不是套用会长端工作台结构。
- `pages/settings/help.vue`
  原因：当前会长端 `pages/president/profile/index.vue` 直接跳转到该路由，因此保留为会长端入口使用；用户端已拆分到 `pages/settings/help-user.vue`，后续若要升级旧 `help.vue`，需要按双端兼容页面处理。
- 会长端各类 `form.vue`
  原因：大多是“新增 / 编辑”后台表单，字段密度、交互目标、权限语义都偏管理后台，只适合借局部控件风格，不适合整页复用。

### 4. 最值得优先复用的 5 个页面

如果目标是“尽量不再让 Stitch 重新出整套用户端稿”，我建议优先按下面顺序复用会长端：

1. `pages/venue/detail.vue`
   对应会长端：`pages/president/venue/detail.vue`
   原因：结构成熟，用户端只需做动作降权替换。
2. `pages/profile/member.vue`
   对应会长端：`pages/president/member/recharge.vue`
   原因：会员身份卡、权益卡、价格或等级区块的表达方式具备较高复用价值。
3. `pages/profile/index.vue`
   对应会长端：`pages/president/profile/index.vue`
   原因：顶部身份区和分组入口卡仍有较高复用价值，但必须保留用户端自己的菜单入口。
4. `pages/settings/account.vue`
   对应会长端：`pages/president/profile/change-password.vue`
   原因：资料表单、信息卡与操作节奏可以继续借鉴，但字段与绑定逻辑必须保留用户端实现。
5. `pages/settings/notification.vue`
   对应会长端：`pages/president/notification/list.vue`
   原因：通知列表与设置切换结构仍有较高复用价值，但要改成用户端偏好配置语义。

### 5. 一句话策略

后续用户端不必所有页面都重新让 Stitch 设计。更高效的办法是：

- 对“详情页 / 流程页 / 表单页”优先查找会长端是否已有 Stitch 化骨架
- 能复用的先做“去管理动作 + 换成用户动作”的适配
- 只有在会长端没有合适页面，或者信息架构完全不同的情况下，再单独让 Stitch 设计新的用户端页面

### 6. 复用边界说明

为了避免用户端改造反向影响会长端，后续文档里提到的“复用会长端页面”，统一按下面的边界理解：

#### 可以复用的内容

- 复用页面的视觉骨架：例如 hero 结构、卡片层级、列表节奏、分区标题、底部操作栏样式。
- 复用纯展示型样式：例如圆角、阴影、胶囊标签、摘要卡、无业务含义的展示组件。
- 复用布局思路：例如“顶部身份区 + 分组入口卡”、“搜索卡 + 列表卡”、“详情 hero + 信息卡 + 底部 CTA”。

#### 不应直接复用的内容

- 不直接复用会长端整页运行代码。
- 不直接 import 或共用 `PresidentLayout`。
- 不直接复用 `PRESIDENT_PAGES`、会长端专属路由常量、会长端入口菜单。
- 不直接调用 `@/api/president/*` 或其它会长端专属接口。
- 不直接保留发布、编辑、删除、审核、上下架、经营统计等会长端动作按钮。
- 不直接复用带角色判断、权限校验、会长端返回路径的脚本逻辑。

#### 推荐实施方式

最安全的做法不是“把会长端页面拿来共用”，而是：

1. 先把会长端页面拆解成“可借鉴的 UI 骨架”。
2. 用户端重新写自己的 `template`、跳转、接口和按钮行为。
3. 如果确实需要抽公共组件，只抽“纯展示组件”，并确保它只通过 `props` 驱动，不绑定角色、路由和会长端接口。
4. 任何涉及角色、权限、菜单入口、业务动作的逻辑，都必须保留在各自端内独立实现。

#### 一句话原则

可以借样式和结构，不要直接共用整页代码；否则最容易出现角色跳错、接口串用、返回路径错误、按钮语义不一致等问题，最终既影响用户端，也可能误伤会长端。

---

## 八、一句话结论

当前 `BMP-uniapp` 用户端已经完成多条核心业务链路的 Stitch 化，课程、赛事、器材、充值、穿线主链路都已有新版页面承接，搜索首页、搜索结果页与穿线查询页也已进入新版体系；接下来最值得继续推进的是设置子页与场馆链路补强，把 Stitch 风格从核心交易流程继续扩展到用户端日常高频区域。
