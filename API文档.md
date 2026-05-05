# 羽擎（BMP）API 文档

> **文档版本**：v2.1.0  
> **最后更新**：2026-05-05  
> **文档定位**：本手册用于说明 BMP 的接口约定、模块边界、主要接口清单与关键业务端点。  
> **调试入口**：启动后端后访问 [Swagger UI](http://localhost:9090/swagger-ui.html) 进行在线查看与调试。

## 1. 使用说明

### 1.1 文档职责

本文件用于做两件事：

1. 作为离线接口速查手册  
2. 作为模块级接口索引，帮助开发、联调与排障

更细的字段定义、请求响应结构与在线调试，以 Swagger UI 为准。

### 1.2 基础地址

- 基础 URL：`http://localhost:9090`
- API 前缀：`/api`
- Swagger UI：`/swagger-ui.html`

## 2. 通用约定

### 2.1 统一返回格式

所有接口统一使用 `Result<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 2.2 状态码

| 状态码 | 说明 |
|------|------|
| `200` | 请求成功 |
| `403` | 无权限访问 |
| `500` | 业务失败或服务器错误 |

### 2.3 认证方式

系统采用 JWT 双 Token：

- `AccessToken`：有效期 2 小时
- `RefreshToken`：有效期 7 天

请求头格式：

```text
Authorization: Bearer {AccessToken}
```

### 2.4 字段命名

- API 请求与响应：`camelCase`
- 数据库字段：`snake_case`
- 映射由后端负责处理

### 2.5 分页约定

列表接口统一支持：

- `page`：页码，默认 `1`
- `size`：每页条数，默认 `10`，最大 `100`

分页返回结构统一为：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "data": [],
    "total": 0,
    "page": 1,
    "size": 10,
    "pages": 0
  }
}
```

### 2.6 角色口径

系统当前角色包括：

- `PRESIDENT`
- `VENUE_MANAGER`
- `USER`
- `MEMBER`
- `COACH`

## 3. 接口地图

### 3.1 模块索引

| 模块 | 基础路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth` | 登录、注册、当前用户、Token、设置、头像 |
| 反馈 | `/api/feedback` | 反馈提交 |
| 用户管理 | `/api/user` | 用户 CRUD 与角色查询 |
| 场馆管理 | `/api/venue` | 场馆、图片、营业时间、状态日志 |
| 会员管理 | `/api/member` | 会员信息、统计、消费记录 |
| 场地管理 | `/api/court` | 场地 CRUD、状态、统计 |
| 场地预约 | `/api/booking` | 场地预约、支付、退款、统计 |
| 器材管理 | `/api/equipment` | 器材 CRUD、图片、统计 |
| 器材租借 | `/api/equipment/rental` | 租借、支付、退款、统计 |
| 教练管理 | `/api/coach` | 教练 CRUD、教练本人档案、绑定账号 |
| 课程管理 | `/api/course` | 课程 CRUD、教练课程视图 |
| 课程预约 | `/api/course/booking` | 课程预约、支付退款、教练端预约视图 |
| 赛事管理 | `/api/tournament` | 赛事 CRUD、统计 |
| 赛事报名 | `/api/tournament/registration` | 报名、支付退款、统计 |
| 穿线服务 | `/api/stringing` | 穿线工单、价格计算、统计 |
| 通知管理 | `/api/notifications` | 通知发布、列表、详情 |
| 充值管理 | `/api/recharge` | 用户自助充值、管理员代充、充值记录 |
| 财务管理 | `/api/finance` | 财务流水、趋势、占比、场馆筛选 |

### 3.2 典型业务流涉及接口

| 业务流 | 主要接口 |
|------|------|
| 登录与鉴权 | `/api/auth/login`、`/api/auth/current`、`/api/auth/refresh` |
| 场地预约 | `/api/booking/add`、`/api/booking/payment`、`/api/booking/refund` |
| 课程预约 | `/api/course/booking/add`、`/api/course/booking/payment`、`/api/course/booking/refund` |
| 器材租借 | `/api/equipment/rental/add`、`/api/equipment/rental/payment`、`/api/equipment/rental/refund` |
| 赛事报名 | `/api/tournament/registration/add`、`/api/tournament/registration/payment`、`/api/tournament/registration/refund` |
| 教练端 | `/api/coach/me`、`/api/course/my`、`/api/course/booking/for-coach` |
| 财务统计 | `/api/finance/statistics`、`/api/finance/trend`、`/api/finance/ratio` |

## 4. 认证与用户域

### 4.1 认证模块

基础路径：`/api/auth`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/login` | `POST` | 用户登录，返回双 Token |
| `/register` | `POST` | 用户注册 |
| `/current` | `GET` | 获取当前登录用户 |
| `/refresh` | `POST` | 刷新 Token |
| `/logout` | `POST` | 用户退出登录 |
| `/update` | `POST` | 更新当前用户资料 |
| `/change-password` | `POST` | 修改密码 |
| `/upload-avatar` | `POST` | 上传头像 |
| `/settings` | `GET` | 获取当前用户设置 |
| `/settings` | `PUT` | 保存当前用户设置 |
| `/check-lock/{username}` | `GET` | 检查账户锁定状态 |
| `/unlock/{username}` | `POST` | 管理解锁账户 |

关键说明：

- 登录接口包含 IP 限流与用户名限流
- 连续失败达到阈值会触发账户锁定
- 头像上传完成后需通过 `/api/auth/update` 写回用户资料

### 4.2 反馈模块

基础路径：`/api/feedback`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | `POST` | 提交反馈，可匿名 |

### 4.3 用户管理模块

基础路径：`/api/user`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 用户列表 |
| `/info/{id}` | `GET` | 用户详情 |
| `/findByRole` | `GET` | 按角色查询用户 |
| `/add` | `POST` | 新增用户 |
| `/update` | `PUT` | 更新用户 |
| `/{id}` | `DELETE` | 删除用户 |

权限要点：

- 用户管理仅会长可操作
- 用户角色已包含 `COACH`、`MEMBER`

## 5. 经营资源域

### 5.1 场馆管理

基础路径：`/api/venue`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 场馆列表 |
| `/info/{id}` | `GET` | 场馆详情 |
| `/add` | `POST` | 新增场馆 |
| `/update` | `PUT` | 更新场馆 |
| `/{id}` | `DELETE` | 删除场馆 |
| `/upload-image` | `POST` | 上传单张场馆图片 |
| `/upload-images` | `POST` | 上传多张场馆图片 |
| `/{venueId}/images` | `GET` | 场馆图片列表 |
| `/image/{id}` | `DELETE` | 删除场馆图片 |
| `/image/{id}/sort` | `PUT` | 更新图片排序 |
| `/{venueId}/schedules` | `GET` | 获取营业时间 |
| `/{venueId}/schedules` | `POST` | 设置营业时间 |
| `/schedule/{id}` | `DELETE` | 删除营业时间 |
| `/{venueId}/status-logs` | `GET` | 状态变更日志 |
| `/statistics` | `GET` | 场馆统计 |

### 5.2 会员管理

基础路径：`/api/member`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 会员列表 |
| `/info/{id}` | `GET` | 会员详情 |
| `/add` | `POST` | 新增会员 |
| `/update` | `PUT` | 更新会员 |
| `/{id}` | `DELETE` | 删除会员 |
| `/statistics` | `GET` | 会员统计 |
| `/{id}/consume-records` | `GET` | 会员消费记录 |

### 5.3 场地管理

基础路径：`/api/court`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 场地列表 |
| `/info/{id}` | `GET` | 场地详情 |
| `/add` | `POST` | 新增场地 |
| `/update` | `PUT` | 更新场地 |
| `/{id}` | `DELETE` | 删除场地 |
| `/status` | `PUT` | 更新场地状态 |
| `/statistics` | `GET` | 场地统计 |
| `/venues` | `GET` | 场馆下拉列表 |
| `/{id}/bookings/today` | `GET` | 获取场地当天预约用户 |
| `/bookings/today/counts` | `GET` | 获取所有场地当天预约统计 |

### 5.4 器材管理

基础路径：`/api/equipment`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 器材列表 |
| `/info/{id}` | `GET` | 器材详情 |
| `/add` | `POST` | 新增器材 |
| `/update` | `PUT` | 更新器材 |
| `/{id}` | `DELETE` | 删除器材 |
| `/status` | `PUT` | 更新器材状态 |
| `/statistics` | `GET` | 器材统计 |
| `/upload-image` | `POST` | 上传单张器材图片 |
| `/upload-images` | `POST` | 上传多张器材图片 |
| `/{equipmentId}/images` | `GET` | 器材图片列表 |
| `/image/{id}` | `DELETE` | 删除器材图片 |
| `/image/{id}/sort` | `PUT` | 更新器材图片排序 |

### 5.5 教练管理

基础路径：`/api/coach`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 教练列表 |
| `/info/{id}` | `GET` | 教练详情 |
| `/add` | `POST` | 新增教练 |
| `/update` | `PUT` | 更新教练 |
| `/{id}` | `DELETE` | 删除教练 |
| `/status` | `PUT` | 更新教练状态 |
| `/statistics` | `GET` | 教练统计 |
| `/me` | `GET` | 获取当前教练档案 |
| `/me` | `PUT` | 教练本人更新档案 |
| `/unbound-users` | `GET` | 获取未绑定教练档案的 COACH 用户列表 |

关键说明：

- `/me` 与 `/unbound-users` 是当前教练端与管理端关联账号的关键接口
- 教练未绑定档案时，接口会返回明确错误

## 6. 交易与经营流程域

### 6.1 场地预约

基础路径：`/api/booking`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 预约列表 |
| `/info/{id}` | `GET` | 预约详情 |
| `/add` | `POST` | 新增预约 |
| `/update` | `PUT` | 更新预约 |
| `/{id}` | `DELETE` | 删除预约 |
| `/status` | `PUT` | 更新预约状态 |
| `/payment` | `POST` | 处理支付 |
| `/member/payment` | `POST` | 普通用户支付本人预约 |
| `/refund` | `POST` | 处理退款 |
| `/statistics` | `GET` | 预约统计 |
| `/courts` | `GET` | 场地下拉列表 |
| `/members` | `GET` | 会员下拉列表 |
| `/occupancy/range` | `GET` | 查询某场地某时段占用明细 |
| `/count` | `GET` | 查询某场地某时段拼场人数 |

关键说明：

- 预约单号格式：`BK + 日期 + 序号`
- 当前预约模式：
  - `bookingMode=SHARED`
  - `bookingMode=PACKAGE`
- 当前预约计费方式：
  - `pricingMode=PACKAGE_HOUR`
  - `pricingMode=SHARED_HOUR`
  - `pricingMode=SHARED_TIME`
- 新增/更新预约时支持：
  - `courtId`：兼容旧单场地请求
  - `courtIds`：新多场地请求字段
  - `bookingMode`
  - `pricingMode`
- 当前冲突规则：
  - `SHARED` 与 `SHARED` 可叠加
  - `PACKAGE` 与任意模式在同场地同时间段互斥
  - `PACKAGE` 只能选择空闲场地，且不能包下整个场馆全部场地
  - `PACKAGE` 需至少提前 2 小时提交

### 6.2 器材租借

基础路径：`/api/equipment/rental`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 租借列表 |
| `/info/{id}` | `GET` | 租借详情 |
| `/add` | `POST` | 新增租借 |
| `/update` | `PUT` | 更新租借 |
| `/{id}` | `DELETE` | 删除租借 |
| `/status` | `PUT` | 更新租借状态 |
| `/statistics` | `GET` | 租借统计 |
| `/payment` | `POST` | 处理支付 |
| `/refund` | `POST` | 处理退款 |
| `/equipments` | `GET` | 器材下拉列表 |
| `/members` | `GET` | 会员下拉列表 |

### 6.3 课程管理

基础路径：`/api/course`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 课程列表 |
| `/info/{id}` | `GET` | 课程详情 |
| `/add` | `POST` | 新增课程 |
| `/update` | `PUT` | 更新课程 |
| `/{id}` | `DELETE` | 删除课程 |
| `/status` | `PUT` | 更新课程状态 |
| `/statistics` | `GET` | 课程统计 |
| `/coaches` | `GET` | 教练下拉列表 |
| `/courts` | `GET` | 场地下拉列表 |
| `/my` | `GET` | 当前教练的课程列表 |

### 6.4 课程预约

基础路径：`/api/course/booking`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 课程预约列表 |
| `/info/{id}` | `GET` | 课程预约详情 |
| `/add` | `POST` | 新增课程预约 |
| `/update` | `PUT` | 更新课程预约 |
| `/{id}` | `DELETE` | 删除课程预约 |
| `/status` | `PUT` | 更新预约状态 |
| `/statistics` | `GET` | 课程预约统计 |
| `/payment` | `POST` | 处理支付 |
| `/refund` | `POST` | 处理退款 |
| `/courses` | `GET` | 课程下拉列表 |
| `/members` | `GET` | 会员下拉列表 |
| `/for-coach` | `GET` | 当前教练所教课程的预约列表 |

关键说明：

- 课程预约单号格式：`CB + 日期 + 序号`
- `/for-coach` 是教练端预约明细页的核心接口

### 6.5 赛事管理

基础路径：`/api/tournament`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 赛事列表 |
| `/info/{id}` | `GET` | 赛事详情 |
| `/add` | `POST` | 新增赛事 |
| `/update` | `PUT` | 更新赛事 |
| `/{id}` | `DELETE` | 删除赛事 |
| `/status` | `PUT` | 更新赛事状态 |
| `/statistics` | `GET` | 赛事统计 |
| `/venues` | `GET` | 场馆下拉列表 |

### 6.6 赛事报名

基础路径：`/api/tournament/registration`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 报名列表 |
| `/info/{id}` | `GET` | 报名详情 |
| `/add` | `POST` | 新增报名 |
| `/update` | `PUT` | 更新报名 |
| `/{id}` | `DELETE` | 删除报名 |
| `/status` | `PUT` | 更新报名状态 |
| `/statistics` | `GET` | 报名统计 |
| `/payment` | `POST` | 处理支付 |
| `/refund` | `POST` | 处理退款 |
| `/tournaments` | `GET` | 赛事下拉列表 |
| `/members` | `GET` | 会员下拉列表 |

关键说明：

- 报名单号格式：`TR + 日期 + 序号`
- 支持 `partnerId` 承接双打 / 混双搭档

### 6.7 穿线服务

基础路径：`/api/stringing`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 穿线服务列表 |
| `/info/{id}` | `GET` | 穿线服务详情 |
| `/info/no/{serviceNo}` | `GET` | 按服务编号查询 |
| `/add` | `POST` | 新增穿线服务 |
| `/status` | `PUT` | 更新穿线状态 |
| `/update` | `PUT` | 更新穿线服务完整信息 |
| `/{id}` | `DELETE` | 删除穿线服务记录 |
| `/strings` | `GET` | 获取线材列表 |
| `/calculate-price` | `GET` | 计算穿线价格 |
| `/statistics` | `GET` | 穿线统计 |

关键说明：

- 服务编号格式：`ST + 日期 + 序号`
- 支持自带线材

## 7. 通知、充值与财务域

### 7.1 通知管理

基础路径：`/api/notifications`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | `GET` | 通知列表 |
| `/{id}` | `GET` | 通知详情 |
| `/` | `POST` | 发布通知 |
| `/{id}` | `DELETE` | 删除通知 |

### 7.2 充值管理

基础路径：`/api/recharge`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/user` | `POST` | 用户自助充值 |
| `/admin` | `POST` | 管理员代充 |
| `/records` | `GET` | 充值记录列表 |
| `/records/{memberId}` | `GET` | 指定会员充值记录 |

关键说明：

- 单次充值金额达到阈值时可触发会员升级

### 7.3 财务管理

基础路径：`/api/finance`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 财务记录列表 |
| `/info/{id}` | `GET` | 财务记录详情 |
| `/add` | `POST` | 新增财务记录 |
| `/update` | `PUT` | 更新财务记录 |
| `/{id}` | `DELETE` | 删除财务记录 |
| `/statistics` | `GET` | 财务统计概览 |
| `/trend` | `GET` | 财务趋势数据 |
| `/ratio` | `GET` | 业务类型占比 |
| `/venues` | `GET` | 获取场馆下拉列表 |
| `/businessTypes` | `GET` | 获取业务类型选项 |

关键说明：

- 财务单号格式：`FN + 日期 + 序号`
- 财务数据覆盖预约、课程、租借、赛事、充值、穿线等业务来源

## 8. 关键枚举

### 8.1 业务类型

| 值 | 说明 |
|---|------|
| `BOOKING` | 场地预约 |
| `COURSE` | 课程预约 |
| `EQUIPMENT` | 器材租借 |
| `TOURNAMENT` | 赛事报名 |
| `RECHARGE` | 会员充值 |
| `STRINGING` | 穿线服务 |
| `OTHER` | 其他收支 |

### 8.2 支付方式

| 值 | 说明 |
|---|------|
| `CASH` | 现金 |
| `ALIPAY` | 支付宝 |
| `WECHAT` | 微信 |
| `BALANCE` | 余额 |
| `BANK` | 银行转账 |

### 8.3 预约状态

| 值 | 说明 |
|---|------|
| `0` | 已取消 |
| `1` | 待支付 |
| `2` | 已支付 |
| `3` | 进行中 |
| `4` | 已完成 |

### 8.4 资源状态示例

| 类型 | 主要取值 |
|------|------|
| 场馆状态 | `0` 关闭 / `1` 营业中 / `2` 暂停 |
| 场地状态 | `0` 维护中 / `1` 空闲 / `2` 预约中 / `3` 使用中 |
| 会员状态 | `0` 冻结 / `1` 正常 / `2` 到期 |

### 8.5 预约模式与计费方式

| 类型 | 值 | 说明 |
|------|---|------|
| 预约模式 | `SHARED` | 拼场 |
| 预约模式 | `PACKAGE` | 包场 |
| 计费方式 | `PACKAGE_HOUR` | 包场按小时 |
| 计费方式 | `SHARED_HOUR` | 拼场按小时 |
| 计费方式 | `SHARED_TIME` | 拼场按次 |

## 9. 维护说明

- 新增接口时，优先补 Swagger 注解与在线文档
- 本文件至少应同步更新模块索引、基础路径和关键业务端点
- 如果接口大范围调整，应优先更新“接口地图”和“关键业务流”

## 10. 变更记录

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-05-05 | v2.1.0 | 同步预约模块重构：补充拼场/包场、三种计费方式、占用查询与拼场人数接口说明 |
| 2026-05-01 | v2.0.0 | 重构为手册型 API 文档，统一接口约定、模块索引与关键业务流说明，去除旧版散列示例与错位编号 |

---

**维护者**：BMP开发团队  
**项目路径**：D:\Developer Tool\IDEA\JAVAEE\BMP
