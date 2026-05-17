# 羽擎（BMP）UniApp 端

## 项目定位

`BMP-uniapp/` 不再只是单一用户端工程，当前已经同时承载：

- 用户端：`USER`、`MEMBER`
- 移动管理端：`PRESIDENT`、`VENUE_MANAGER`
- 教练端：`COACH`

它与 Web 端共享同一套后端 API，负责移动场景下的预约、课程、器材、赛事、充值、通知、个人中心，以及移动管理和教练工作流。

## 当前快照（2026-05-17）

基于目录与 `pages.json` 复核，当前 UniApp 端规模如下：

| 范围 | 数量 |
|------|------|
| 页面 | `107` |
| API 封装 | `35` |
| 组件 | `21` |
| 测试文件 | `4` |

当前顶层页面目录共 17 组：

- `booking`
- `coach`
- `course`
- `equipment`
- `index`
- `login`
- `notice`
- `president`
- `profile`
- `recharge`
- `recover`
- `register`
- `search`
- `settings`
- `stringing`
- `tournament`
- `venue`

## 技术栈

- **框架**：UniApp 3 + Vue 3
- **语言**：TypeScript
- **状态管理**：Pinia
- **UI 组件**：`uni-ui` + `uview-plus`
- **构建工具**：Vite
- **测试**：Vitest

## 目录结构

```text
BMP-uniapp/
├── api/              # API 接口封装
├── components/       # 组件
├── composables/      # 组合式逻辑
├── config/           # 配置文件
├── pages/            # 用户端、移动管理端、教练端页面
├── store/            # 状态管理
├── styles/           # 全局样式
├── tests/            # 单元测试
├── types/            # 类型定义
├── utils/            # 工具函数
├── App.vue           # 应用入口
├── main.ts           # 主入口文件
├── manifest.json     # 应用配置
└── pages.json        # 页面注册配置
```

## 开发与构建

### 安装依赖

```bash
npm install
```

### 开发运行

```bash
npm run dev:mp-weixin
```

### 构建打包

```bash
npm run build:mp-weixin
```

### 质量检查

```bash
npm run type-check
npm test
```

## 当前验证结果

以下结果均在 2026-05-17 重新确认：

| 命令 | 结果 | 备注 |
|------|------|------|
| `npm run type-check` | 通过 | 当前主干类型检查已恢复为绿色 |
| `npm test` | 通过 | `4` 个测试文件、`11` 个用例全部通过 |
| `npm run build:mp-weixin` | 通过 | 存在 Sass `legacy-js-api` 与 `@import` 弃用告警 |

## uView Plus 接入说明

本项目当前使用 `uview-plus`（Vue 3 兼容方案），`u-` 前缀组件已经接入完成。

### 已完成配置

1. 安装依赖：`uview-plus`
2. 在 `main.ts` 中注册：
   - `import uviewPlus from 'uview-plus'`
   - `import 'uview-plus/index.scss'`
   - `app.use(uviewPlus)`
3. 在 `pages.json` 中配置 easycom：
   - `"^u-(.*)": "uview-plus/components/u-$1/u-$1.vue"`
4. 在样式入口中引入 `uview-plus/theme.scss`

### 当前注意事项

- 当前构建仍会提示 Sass `legacy-js-api` 弃用
- `uview-plus/index.scss` 与 `App.vue` 中的 `@import` 也会触发弃用警告
- 这些问题暂时不阻塞构建，但已经是需要后续治理的技术债

## 当前边界

- 代码、类型检查、单元测试、小程序构建都已通过，但真实设备回归仍建议继续做
- `pages/president/**` 和 `pages/coach/**` 页面已经成组落地，后续重点在联调与体验收口，而不是再补页面骨架

## 相关文档

- [根目录 README](../README.md)
- [项目开发文档](../项目开发文档.md)
- [API 文档](../API文档.md)
- [模块化测试文档](../模块化测试文档.md)

---

**最后更新**：2026-05-17
