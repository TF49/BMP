# 羽毛球管理系统（BMP）微信小程序

## 项目简介

基于 UniApp + Vue 3 + TypeScript 开发的羽毛球管理系统微信小程序，与 Web 端共享同一套后端 API。

## 技术栈

- **框架**: UniApp
- **开发语言**: Vue 3 + TypeScript
- **状态管理**: Pinia
- **UI组件**: uni-ui + uview-plus（uView 2.x Vue3 兼容方案）
- **构建工具**: Vite

## 项目结构

```
uniapp/
├── api/              # API接口封装
├── components/        # 组件
├── config/            # 配置文件
├── pages/             # 页面
├── store/             # 状态管理
├── styles/            # 样式文件
├── types/             # 类型定义
├── utils/             # 工具函数
├── App.vue            # 应用入口
├── main.ts            # 主入口文件
├── manifest.json       # 应用配置
└── pages.json         # 页面配置
```

## 开发指南

### 安装依赖

```bash
npm install
```

### 开发运行

```bash
# 微信小程序
npm run dev:mp-weixin
```

### 构建打包

```bash
# 微信小程序
npm run build:mp-weixin
```

## uView 2.x（Vue3 兼容）接入说明

本项目已按 `uview-plus` 方案接入 uView 生态组件（`u-` 前缀）。

### 已完成配置

1. 安装依赖：`uview-plus`
2. 入口注册（`main.ts`）：
   - `import uviewPlus from 'uview-plus'`
   - `import 'uview-plus/index.scss'`
   - `app.use(uviewPlus)`
3. easycom 自动按需引入（`pages.json` 与 `src/pages.json`）：
   - `"^u-(.*)": "uview-plus/components/u-$1/u-$1.vue"`
4. 主题变量入口：新增 `uni.scss` 并引入 `uview-plus/theme.scss`

### 使用方式

配置完成后可直接在页面使用，无需手动导入组件：

```vue
<u-button type="primary">按钮</u-button>
```

### 当前已知问题

- `npm run build:mp-weixin` 目前受 `@dcloudio/uni-app` 与 `vue` 依赖矩阵影响，存在版本冲突（非 uView 组件语法问题）。
- 需要统一 `@dcloudio/*` 与 `vue/@vue/*` 版本后再进行最终构建验证。

## 开发规范

详见 `uniapp开发文档.md`

## 相关文档

- [项目开发文档](../项目开发文档.md)
- [API文档](../API文档.md)
- [UniApp开发文档](./uniapp开发文档.md)
