# 羽擎（Badminton Management Platform，BMP）

> 当前仓库是一个单仓多端项目：`Spring Boot` 后端 + `Vue 3` Web 端 + `UniApp` 移动端。  
> 本文档只负责说明当前现状、目录入口、启动方式和文档分工，不承担深度分析与详细能力说明。

## 当前现状

- 后端仍是项目核心，业务主流程已经成型。
- `vue/` 同时承载官网、管理端、用户端、教练端四类 Web 场景。
- `BMP-uniapp/` 已包含用户端、移动管理端、教练端三类入口。
- 项目当前阶段不是“继续补骨架”，而是“在现有功能面上做联调、治理和文档收口”。

## 仓库结构

```text
BMP/
├─ pom.xml
├─ src/              # Spring Boot 后端
├─ vue/              # Vue 3 Web 端
├─ BMP-uniapp/       # UniApp 移动端
├─ uploads/          # 上传资源
├─ logs/             # 运行日志
└─ 根目录文档
```

## 角色与端

### Web 端

- 官网：`/site`
- 管理端：`PRESIDENT`、`VENUE_MANAGER`
- 用户端：`USER`、`MEMBER`
- 教练端：`COACH`

### UniApp 端

- 用户端：`USER`、`MEMBER`
- 移动管理端：`PRESIDENT`、`VENUE_MANAGER`
- 教练端：`COACH`

## 当前已确认情况

### 已落地

- 后端 15 个业务域已覆盖场馆经营核心流程
- Web 多角色入口已成型
- UniApp 用户端、移动管理端、教练端入口均已落地
- WebSocket、定时任务、JWT、数据权限等基础能力已接入

### 仍需持续推进

- UniApp 多角色链路联调与体验收口
- Web 包体积治理
- 后端自动化测试补齐
- 根目录文档继续收敛

## 已验证结果

2026-04-25 已完成以下验证：

- 后端：`mvn -q -DskipTests compile` 通过
- Web：`npm run build` 通过
- UniApp：`npm run type-check` 通过
- UniApp：`npm test` 通过，3 个测试文件、8 个测试全部通过

## 本地启动

### 后端

```bash
mvn spring-boot:run
```

默认端口：`9090`

### Web

```bash
cd vue
npm install
npm run serve
```

### UniApp

```bash
cd BMP-uniapp
npm install
npm run dev:mp-weixin
```

## 文档分工

- [README.md](/D:/Developer%20Tool/IDEA/JAVAEE/BMP/README.md)：当前现状、目录入口、启动方式、文档导航
- [项目分析总结.md](/D:/Developer%20Tool/IDEA/JAVAEE/BMP/项目分析总结.md)：阶段判断、主要风险、下一步优先级
- [项目开发文档.md](/D:/Developer%20Tool/IDEA/JAVAEE/BMP/项目开发文档.md)：详细能力、权限体系、模块说明、项目结构
- [API文档.md](/D:/Developer%20Tool/IDEA/JAVAEE/BMP/API文档.md)：接口说明
- [数据库设计文档.md](/D:/Developer%20Tool/IDEA/JAVAEE/BMP/数据库设计文档.md)：数据库设计
- [模块化测试文档.md](/D:/Developer%20Tool/IDEA/JAVAEE/BMP/模块化测试文档.md)：测试清单

---

**最后更新**：2026-05-01
