# 羽毛球管理系统（BMP）微信小程序

## 项目简介

基于 UniApp + Vue 3 + TypeScript 开发的羽毛球管理系统微信小程序，与 Web 端共享同一套后端 API。

## 技术栈

- **框架**: UniApp
- **开发语言**: Vue 3 + TypeScript
- **状态管理**: Pinia
- **UI组件**: uni-ui
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

## 开发规范

详见 `uniapp开发文档.md`

## 相关文档

- [项目开发文档](../项目开发文档.md)
- [API文档](../API文档.md)
- [UniApp开发文档](./uniapp开发文档.md)
