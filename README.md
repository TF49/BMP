# 羽擎（Badminton Management Platform，BMP）

> 当前仓库是一个单仓多端项目：`Spring Boot` 后端 + `Vue 3` Web 端 + `UniApp` 移动端。  
> 本文档只回答四件事：项目现在是什么、验证到哪了、怎么启动、其它文档该看哪一份。

## 1. 当前快照

基于 2026-05-17 的代码与构建结果，当前仓库状态如下：

- 后端：`15` 个业务域、`27` 个 Controller、`178` 个 Java 源文件
- Web：`vue/` 同时承载官网、管理端、用户端、教练端，当前 `views` 下有 `50` 个页面组件
- UniApp：`BMP-uniapp/` 当前有 `107` 个页面、`35` 个 API 封装、`21` 个组件、`4` 个测试文件
- 项目阶段：核心业务已经成型，当前重点转向联调、测试补护栏、包体积治理和文档同步

## 2. 角色与入口

### Web 端

- 官网：`/site`
- 管理端：`PRESIDENT`、`VENUE_MANAGER`
- 用户端：`USER`、`MEMBER`
- 教练端：`COACH`

### UniApp 端

- 用户端：`USER`、`MEMBER`
- 移动管理端：`PRESIDENT`、`VENUE_MANAGER`
- 教练端：`COACH`

## 3. 本轮确认结果

以下结果均在 2026-05-17 重新执行确认：

| 范围 | 命令 | 结果 | 备注 |
|------|------|------|------|
| 后端编译 | `mvn -q -DskipTests compile` | 通过 | 当前仓库没有 `src/test`，后端仍缺自动化测试护栏 |
| Web 构建 | `npm run build` | 通过 | 有包体积告警，`app` 入口约 `2.27 MiB` |
| UniApp 类型检查 | `npm run type-check` | 通过 | 当前主干类型检查已恢复为绿色 |
| UniApp 单元测试 | `npm test` | 通过 | `4` 个测试文件、`11` 个用例全部通过 |
| UniApp 微信小程序构建 | `npm run build:mp-weixin` | 通过 | 有 Sass `legacy-js-api` 与 `@import` 弃用告警 |

## 4. 当前已经明确的能力面

- 后端 15 个业务域已覆盖场馆经营核心流程：场馆、场地、会员、教练、课程、赛事、器材、预约、充值、财务、通知、搜索、天气/地图等
- Web 端当前统一承载官网、管理后台、用户自助端、教练工作台
- UniApp 当前统一承载用户端、移动管理端、教练端
- 财务审计、财务对账、搜索聚合、天气代理、地图逆地理编码、通知管理等增量模块都已经进入主干

## 5. 当前主要风险

1. 后端自动化测试仍然偏弱  
   当前只能确认编译通过，缺少 `src/test` 下的回归测试体系。

2. Web 包体积已经进入治理区间  
   本轮构建仍提示 `Element Plus`、`ECharts`、`vendors` 分包较大，首屏资源偏重。

3. Sass 弃用告警尚未处理  
   Web 与 UniApp 构建都存在 `legacy-js-api` 告警，UniApp 额外存在 `@import` 弃用告警。

4. 多端链路还需要继续做真实设备回归  
   代码、类型检查和构建都已通过，但 Web/UniApp 关键业务流仍建议继续做角色化手测。

## 6. 仓库结构

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

## 7. 本地启动

### 后端

```bash
mvn spring-boot:run
```

- 默认端口：`9090`
- Swagger：`http://localhost:9090/swagger-ui.html`

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

## 8. 文档导航

- [README.md](./README.md)：仓库现状、启动方式、文档导航
- [项目分析总结.md](./项目分析总结.md)：项目阶段判断、主要风险、下一步优先级
- [项目开发文档.md](./项目开发文档.md)：系统构成、能力边界、权限体系、目录结构
- [API文档.md](./API文档.md)：接口索引、基础路径、关键业务端点
- [数据库设计文档.md](./数据库设计文档.md)：数据库范围、27 张物理表、关键关系与约束
- [模块化测试文档.md](./模块化测试文档.md)：当前验证基线、测试缺口与回归建议

---

**最后更新**：2026-05-17
