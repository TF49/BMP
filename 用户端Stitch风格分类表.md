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

当前用户端已经存在一批明显的 Stitch 风格页面，但还没有形成完整闭环。

整体上可以这样理解：

- 列表页、首页、部分流程页，已经明显进入 Stitch 风格
- 一些页面已经使用橙色品牌体系，但结构仍带有旧页面痕迹，属于半 Stitch
- 大量详情页、设置页、资料页仍保留旧版业务风，属于非 Stitch

最大的结构性问题不是单页不好看，而是：

- 新的 Stitch 风格列表页已经出现
- 但点进去后的详情、表单、确认页很多还是旧页面
- 导致同一业务链路内出现明显风格断层

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
| 赛事列表 | `pages/tournament/list.vue` | 已是 Stitch | 大 hero、倒计时、英文章节标题、营销式赛事卡、品牌活动区 |
| 器材列表 | `pages/equipment/list.vue` | 已是 Stitch | 品牌顶栏、胶囊筛选、商品大图卡、橙色浮动操作按钮 |
| 器材租借确认 | `pages/equipment/rental.vue` | 已是 Stitch | 大留白、确认页卡片分区、橙色主操作按钮、清晰价格汇总 |
| 穿线预约 | `pages/stringing/create.vue` | 已是 Stitch | 分步骤卡片、品牌化预约流、橙色大按钮、信息架构成熟 |
| 充值记录 | `pages/recharge/records.vue` | 已是 Stitch | 品牌色总览卡、强卡片分组、记录样式统一、视觉节奏现代 |
| 消费记录 | `pages/profile/records.vue` | 已是 Stitch | 已按账单原型重做：橙色顶栏、账单周期卡、三类消费统计、退款双金额样式、历史记录按钮，和钱包链路视觉统一 |
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
| 我的 | `pages/profile/index.vue` | 半 Stitch | 品牌色明显，但更像独立个人中心视觉稿，不完全是 Stitch 页面结构 |
| 通知中心 | `pages/notice/index.vue` | 半 Stitch | 已使用橙色品牌体系，但更偏内容页整理，不是典型 Stitch hero/card 流程页 |
| 登录 | `pages/login/login.vue` | 半 Stitch | 品牌包装很强，但属于入口页视觉体系，与 Stitch 列表页语言不同 |
| 注册 | `pages/register/register.vue` | 半 Stitch | 与登录页同系，品牌化明显，但不完全属于 Stitch 主内容结构 |
| 找回密码 | `pages/recover/recover.vue` | 半 Stitch | 与登录注册同系，品牌感强，但更偏入口页 |
| 场馆详情 | `pages/venue/detail.vue` | 半 Stitch | 品牌感较强、局部现代，但详情布局仍有传统业务页痕迹 |
| 场地预订 | `pages/venue/booking.vue` | 半 Stitch | 操作流程已较新，但整体样式还未完全达到 Stitch 统一程度 |

### 这一类页面的共同特征

- 已经不再属于旧绿色业务风
- 部分模块已经换成橙色品牌色
- 但页面壳、章节结构、卡片语言还未完全统一
- 可以继续沿 Stitch 方向升级，而不是推倒重来

---

## 四、非 Stitch 风格

这一类页面仍以旧业务页结构为主，虽然个别地方已经换成了橙色或引入局部卡片样式，但整体并不属于 Stitch 风格。

### 1. 课程模块

| 页面 | 路径 | 分类判断 | 原因 |
| --- | --- | --- | --- |
| 课程预约 | `pages/course/booking.vue` | 非 Stitch | 仍是旧流程页风格，没有 Stitch 的卡片化产品表达 |

### 2. 器材模块

| 页面 | 路径 | 分类判断 | 原因 |
| --- | --- | --- | --- |
| 器材详情 | `pages/equipment/detail.vue` | 非 Stitch | 旧版详情页结构明显，样式与器材列表风格断层 |

### 3. 赛事模块

| 页面 | 路径 | 分类判断 | 原因 |
| --- | --- | --- | --- |
| 赛事详情 | `pages/tournament/detail.vue` | 非 Stitch | 传统信息详情页结构，状态色和交互风格仍偏旧页 |
| 赛事报名 | `pages/tournament/register.vue` | 非 Stitch | 典型旧表单页，绿色交互痕迹明显，和赛事列表断层大 |

### 4. 设置 / 搜索 / 资料模块

| 页面 | 路径 | 分类判断 | 原因 |
| --- | --- | --- | --- |
| 设置首页 | `pages/settings/index.vue` | 非 Stitch | 旧设置列表页结构，绿色主色明显 |
| 账户设置 | `pages/settings/account.vue` | 非 Stitch | 旧资料编辑页 |
| 通知设置 | `pages/settings/notification.vue` | 非 Stitch | 旧开关列表页结构 |
| 隐私设置 | `pages/settings/privacy.vue` | 非 Stitch | 旧设置页样式 |
| 安全设置 | `pages/settings/security.vue` | 非 Stitch | 旧设置页样式 |
| 关于我们 | `pages/settings/about.vue` | 非 Stitch | 旧信息展示页样式 |
| 帮助 / FAQ / 反馈等设置子页 | `pages/settings/*` | 非 Stitch | 大部分仍属旧业务页体系 |
| 搜索首页 | `pages/search/index.vue` | 非 Stitch | 搜索结构传统，绿色图标与旧业务页气质明显 |
| 搜索结果 | `pages/search/result.vue` | 非 Stitch | 结果列表仍未纳入 Stitch 体系 |
| 会员信息 | `pages/profile/member.vue` | 非 Stitch | 绿色会员卡 + 旧版 section 结构，非 Stitch |
| 个人信息 | `pages/profile/info.vue` | 非 Stitch | 旧资料页样式 |

### 5. 充值模块

| 页面 | 路径 | 分类判断 | 原因 |
| --- | --- | --- | --- |
| 充值中心 | `pages/recharge/index.vue` | 非 Stitch | 绿色支付主色、传统 section 结构，和充值记录页不属于同一体系 |

### 6. 穿线模块

| 页面 | 路径 | 分类判断 | 原因 |
| --- | --- | --- | --- |
| 穿线列表 | `pages/stringing/list.vue` | 非 Stitch | 旧 header + tab + glass-card 混搭，不是 Stitch 版式 |
| 穿线详情 | `pages/stringing/detail.vue` | 非 Stitch | 旧详情页结构明显 |
| 穿线查询 | `pages/stringing/query.vue` | 非 Stitch | 偏旧式功能页 |

---

## 五、最明显的风格断层

当前最需要注意的不是页面数量，而是链路断层。

### 1. 课程链路

- `Stitch`：`pages/course/list.vue`
- `Stitch`：`pages/course/detail.vue`
- `非 Stitch`：`pages/course/booking.vue`

结论：课程模块已补齐到“列表页 + 详情页”一致的新链路，当前主要缺口转为预约页。

### 2. 赛事链路

- `Stitch`：`pages/tournament/list.vue`
- `非 Stitch`：`pages/tournament/detail.vue`
- `非 Stitch`：`pages/tournament/register.vue`

结论：赛事列表已经是新版产品页，但报名流程仍是旧表单页。

### 3. 器材链路

- `Stitch`：`pages/equipment/list.vue`
- `非 Stitch`：`pages/equipment/detail.vue`
- `Stitch`：`pages/equipment/rental.vue`

结论：器材模块出现“列表页新、确认页新、详情页旧”的中间状态。

### 4. 充值链路

- `非 Stitch`：`pages/recharge/index.vue`
- `Stitch`：`pages/recharge/records.vue`

结论：同一功能模块内部风格割裂非常明显。

### 5. 穿线链路

- `Stitch`：`pages/stringing/create.vue`
- `非 Stitch`：`pages/stringing/list.vue`
- `非 Stitch`：`pages/stringing/detail.vue`

结论：穿线预约页已升级，但列表和详情页仍在旧体系。

---

## 六、后续设计与改版建议

### 第一优先级：沿 Stitch 风格补齐详情和流程页

建议优先处理以下页面：

1. `pages/course/detail.vue`
2. `pages/course/booking.vue`
3. `pages/tournament/detail.vue`
4. `pages/tournament/register.vue`
5. `pages/equipment/detail.vue`

原因：

- 这些页面直接承接 Stitch 风格列表页
- 用户最容易在这些跳转节点上感受到“像换了一个产品”

### 第二优先级：统一用户中心相关页面

建议处理：

1. `pages/recharge/index.vue`
2. `pages/profile/member.vue`
3. `pages/profile/info.vue`
4. `pages/settings/index.vue`
5. `pages/settings/*`

原因：

- 用户中心是高频使用区域
- 当前既有旧绿色业务风，也有新版橙色风，虽然 `pages/profile/records.vue` 已完成升级，但资料页、会员页、充值页仍存在明显割裂

### 第三优先级：补齐搜索和穿线模块

建议处理：

1. `pages/search/index.vue`
2. `pages/search/result.vue`
3. `pages/stringing/list.vue`
4. `pages/stringing/detail.vue`
5. `pages/stringing/query.vue`

原因：

- 这些页面当前不属于 Stitch 体系
- 但功能上又与新版用户端其它业务紧密相关

---

## 七、一句话结论

当前 `BMP-uniapp` 用户端已经有一批明显的 Stitch 风格页面，且课程详情页、消费记录页已补齐到新版原型体系；但设置页、资料页、搜索页和若干流程页仍大量保留旧版业务风，因此下一步最重要的不是继续零散新增新页面，而是按模块把 Stitch 风格从入口页持续补齐到完整链路。
