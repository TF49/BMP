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

2026-05-05 已完成以下新增验证与收口：

- 后端预约模块已完成“拼场 / 包场 + 主单 / 场地明细”重构，并已同步数据库结构
- Web 端预约页、预约管理页、场地管理页已接入预约模式、多场地与三套价格展示
- UniApp 用户预约页、移动管理端建单页、预约列表/详情/确认页已完成增量改造
- UniApp：`npm run build:mp-weixin` 通过

## 今日完成摘要

2026-05-05 本轮主要完成了预约模块的一次前后端联动收口：

- 后端：
  - 场地预约由“单 `courtId` 独占预约”升级为“`biz_booking` 主单 + `biz_booking_court` 场地明细”
  - 支持 `SHARED` 拼场与 `PACKAGE` 包场两种模式
  - 支持三种计费方式：`PACKAGE_HOUR`、`SHARED_HOUR`、`SHARED_TIME`
  - 冲突规则升级为：
    - 拼场可叠加拼场
    - 包场只能选择空闲场地
    - 若同场地同时段已有包场，则不能再拼场
    - 包场不能包下整个场馆全部场地，且需至少提前 2 小时提交
- Web 端：
  - 用户预约链路接入模式切换、多场地包场、拼场人数展示、中文计费方式映射
  - 管理端预约管理与场地价格配置页接入三套价格字段
- UniApp 端：
  - 在保留原页面结构与样式骨架的前提下，增量接入拼场/包场、多场地、三种价格
  - 修复了预约链路中计费方式英文枚举直接展示的问题

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

**最后更新**：2026-05-05
