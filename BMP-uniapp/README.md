# 羽擎（BMP）UniApp 端

> 与根目录 [README](../README.md) 配套使用。项目版本 **1.0.0**，功能开发已完成。

## 项目定位

`BMP-uniapp/` 同时承载：

| 场景 | 角色 |
|------|------|
| 用户端 | `USER`、`MEMBER` |
| 移动管理端 | `PRESIDENT`、`VENUE_MANAGER` |
| 教练端 | `COACH` |

与 Web 端共用同一套 Spring Boot API，覆盖预约、课程、器材、赛事、充值、穿线、通知、财务（移动管理）等完整移动链路。

## 规模快照（2026-05-23）

| 项 | 数量 |
|----|------|
| 页面 | 107 |
| API 封装（`api/**/*.ts`） | 36 |
| 组件 | 23 |
| 单元测试 | 4 文件 / 11 用例 |

顶层页面目录：`booking`、`coach`、`course`、`equipment`、`index`、`login`、`notice`、`president`、`profile`、`recharge`、`recover`、`register`、`search`、`settings`、`stringing`、`tournament`、`venue`。

## 技术栈

- UniApp 3 + Vue 3 + TypeScript
- Pinia、Vite、Vitest
- `uni-ui`、`uview-plus`

## 目录结构

```text
BMP-uniapp/
├── api/              # 用户端 + president + internal 接口封装
├── components/       # 通用、支付、会长端、教练端组件
├── pages/            # 页面（用户 / president / coach）
├── store/            # Pinia 状态
├── tests/            # Vitest 单测
├── utils/            # 工具与常量
├── pages.json        # 页面注册
└── manifest.json     # 应用配置
```

## 开发与构建

```bash
npm install
npm run dev:mp-weixin      # 开发
npm run build:mp-weixin    # 生产构建
npm run type-check         # 类型检查
npm test                   # 单元测试
```

构建产物目录：`dist/dev/mp-weixin`（开发）或 `dist/build/mp-weixin`（生产），用微信开发者工具打开。

## 后端联调

1. 先启动根目录 Spring Boot（默认 `http://localhost:9090`）。
2. 在 `config/` 中确认 API 基址与 Web 一致。
3. 小程序真机调试需在公众平台配置 request 合法域名。

## 验证结果（2026-05-23）

| 命令 | 结果 |
|------|------|
| `npm run type-check` | 通过 |
| `npm test` | 11/11 通过 |
| `npm run build:mp-weixin` | 通过（Sass 弃用告警，不阻塞） |

## uView Plus

已在 `main.ts` 注册，并在 `pages.json` 配置 easycom（`^u-(.*)`）。构建时可能出现 Sass `legacy-js-api` 与 `@import` 弃用提示，属已知技术债，不影响当前交付。

## 相关文档

- [项目开发文档](../项目开发文档.md)
- [API 文档](../API文档.md)
- [数据库设计文档](../数据库设计文档.md)

---

**最后更新**：2026-05-23
