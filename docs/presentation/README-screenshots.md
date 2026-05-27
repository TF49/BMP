# 项目二答辩 HTML · 截图占位说明

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
python "C:\Users\Administrator\.claude\skills\网页pptskill\html-defense-builder\scripts\apply_blueprint.py" --blueprint "docs/presentation/project2-blueprint.json" --output "docs/presentation/project2-defense.html"
```

（在 BMP 仓库根目录执行时，路径按上式；或在 `docs/presentation` 下把路径写成相对路径。）

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
