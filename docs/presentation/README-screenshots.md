# BMP 答辩 HTML · 截图与录屏说明

---

## 项目三（当前答辩稿）

答辩文件：[`project3-defense.html`](./project3-defense.html) — **纯静态 HTML**，双击或用浏览器打开即可。  
蓝图配置：[`project3-blueprint.json`](./project3-blueprint.json)

**翻页**：空格 / ↑↓ / 滚轮 / **左键**（左侧 38% 上一章，其余下一章）/ 右侧进度点 —— **16 章**，约 **10 分钟**。

**讲解人**（右上角随章节切换）：

| 姓名 | 模块 | 章节 | 建议时长 |
|------|------|------|----------|
| 尹晴 | 总览 · 架构 · 收束 | 01–05、16 | ~3 min |
| 阿英莫 | 会员充值 · 课程预约 | 06–10 | ~3.5 min |
| 涂家乐 | Dashboard · 财务 · 通知搜索 | 11–15 | ~3 min |

**章节结构**：总览 5 章 → 核心业务 5 章（含 1 段完整链路 demo）→ 运营治理 5 章 → 收束 1 章。  
**范围说明**：全系统交付视角，**不展开**项目二场地预约与支付（收束页有一句口头边界说明）。

### 改稿（项目三）

```bash
# 在 BMP 仓库根目录执行
python "C:\Users\Administrator\.claude\skills\网页pptskill\html-defense-builder\scripts\apply_blueprint.py" --blueprint "docs/presentation/project3-blueprint.json" --template "docs/presentation/project3-defense.html" --output "docs/presentation/project3-defense.html"
```

**必须加 `--template project3-defense.html`**（从 project2 复制的那份，含 demo 录屏与截图渲染逻辑）。

### 项目三 · 截图清单（待补充）

将截图放入 `assets/screenshots/`：

| 文件名 | 章节 id | 内容 | 对应页面 |
|--------|---------|------|----------|
| `web-recharge.png` | `demo_member_recharge` | 会员充值页 | `vue/src/views/user/Recharge.vue` 或 `BMP-uniapp/pages/recharge/index.vue` |
| `web-dashboard.png` | `dashboard_overview` | Dashboard KPI + 图表 | `vue/src/views/Dashboard.vue` |
| `web-finance-audit.png` | `demo_finance_audit` | 财务 / 审计列表 | `vue/src/views/FinanceManagement.vue` |

### 项目三 · 演示录屏（待录制）

将 MP4 放入 `assets/videos/`，文件名与蓝图 `support.video` 一致：

| 文件 | 章节 id | 录制要点 |
|------|---------|----------|
| `毕设视频.mp4` | `demo_member_recharge` | user001 充值 → 课程预约 → 余额扣款成功 |
| `财务审计演示.mp4` | `demo_finance_audit` | admin 筛选流水 / 审计日志 / 对账或导出 |

录屏与截图就绪后：只改 `project3-blueprint.json`（路径已写好），再执行上面的 `apply_blueprint.py`，浏览器 **Ctrl+F5** 强刷。

### 10 分钟排练顺序

1. 启动 9090 API、8080 Web、微信开发者工具  
2. 尹晴：01–05 架构总览（~3 min）  
3. 阿英莫：06 过渡 → 07 充值到预约整链路 demo → 08–09 讲解（~3.5 min）  
4. 涂家乐：11 过渡 → 12 Dashboard → 13 财务 demo → 14 通知搜索 → 15 测试质量（~3 min）  
5. 尹晴：16 收束 + Q&A 缓冲（~0.5 min）

---

## 项目二（历史答辩稿）

答辩文件：[`project2-defense.html`](./project2-defense.html) — **纯静态 HTML**，双击或用浏览器打开即可，无需 Node / 构建。  
蓝图配置：[`project2-blueprint.json`](./project2-blueprint.json)

可选参考（非答辩主文件）：

- [`project2-defense.react.html`](./project2-defense.react.html) — 曾用 React 打包的单文件版（内联 JS，也能直接打开，但改稿需 `pnpm bundle`）
- [`project2-defense-artifact/`](./project2-defense-artifact/) — React 源码工程

**翻页**：空格 / ↑↓ / 滚轮 / 右侧进度点 —— 每章镜头推进并吸附到视口正中（当前 **22 章**，三人分模块讲解）。

**讲解人**：右上角随章节切换显示「讲解人：XXX」—— 尹晴（开场·简介）、阿英莫（预约模块）、涂家乐（支付模块）。在蓝图各章 `presenter` / `module` 字段配置。

**章节拆分**：简介 6 章 → 预约模块 7 章 → 支付模块 6 章 → 收束 1 章；`layout: "module"` 为模块过渡页，`layout: "visual"` 为图表独占页。

**流程图 / 架构图**：漏斗、分支树、flow-rail、pipeline、状态机、layer-stack、combo；在 `diagram` 字段配置。

## 改稿（推荐流程）

1. 编辑 [`project2-blueprint.json`](./project2-blueprint.json) 中的章节文案、`support`、`metrics` 等。
2. 重新生成 HTML：

```bash
python "C:\Users\Administrator\.claude\skills\网页pptskill\html-defense-builder\scripts\apply_blueprint.py" --blueprint "docs/presentation/project2-blueprint.json" --template "docs/presentation/project2-defense.html" --output "docs/presentation/project2-defense.html"
```

（在 BMP 仓库根目录执行。**必须加 `--template project2-defense.html`**：技能默认模板不含演示录屏 `layout: demo` 与截图渲染，仅用默认模板会覆盖掉视频播放器逻辑。）

3. 用浏览器打开 `project2-defense.html` 验收（有缓存时 **Ctrl+F5** 强刷）。

## 替换截图

1. 将截图放入 `assets/screenshots/` 目录（见下方文件名）。
2. 编辑蓝图中对应章节的 `support.items`；若需嵌入图片，可在生成后的 HTML 的 support 区加入 `<img src="assets/screenshots/xxx.png" alt="...">`，或先在蓝图的 `support.copy` 中注明路径后再生成。
3. 重新执行上面的 `apply_blueprint.py` 命令。

## 截图清单（已就位）

| 占位 id | 文件名 | 章节 | 内容 | 对应页面 |
|---------|--------|------|------|----------|
| web-booking-user | `web-booking-user.png` | `booking_web_user` | 用户选场（步骤 2） | `vue/src/views/user/Booking.vue` |
| web-booking-admin | `web-booking-admin.png` | `booking_web_admin` | 管理端预约列表 | `vue/src/views/BookingManagement.vue` |
| uniapp-booking-confirm | `uniapp-booking-confirm.png` | `payment_uniapp_ui` | 订单确认页 | `BMP-uniapp/pages/booking/confirm.vue` |
| uniapp-countdown | `uniapp-countdown.png` | `payment_uniapp_ui` | 待支付倒计时 | `booking/detail.vue` 等 |

蓝图 `support.image` / `items[].image` 已指向上述路径；`project2-defense.html` 内嵌了截图渲染逻辑。若仅改文案可只动蓝图后 `apply_blueprint.py`；若改 `supportHtml` 截图样式需同步改 HTML 中的 JS/CSS。

## 打开答辩稿

在资源管理器中双击 `project2-defense.html`，或拖入 Chrome / Edge：

- 滚轮 / 右侧进度点切换章节
- 键盘：空格、`↑` `↓` / `PageUp` `PageDown` / `Home` / `End`


## 演示录屏（本地）

| 文件 | 章节 id | 位置 |
|------|---------|------|
| `assets/videos/支付测试.mp4` | `demo_pay_success` | 紧接「07 预约模块」 |
| `assets/videos/支付倒计时测试.mp4` | `demo_auto_cancel` | 紧接「14 支付模块」 |
