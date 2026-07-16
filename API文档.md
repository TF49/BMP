# 羽擎（BMP）API 文档

> **文档版本**：v2.4.0
> **项目版本**：1.0.0（功能开发已完成）  
> **最后更新**：2026-07-16
> **文档定位**：作为 BMP 接口的离线索引与联调速查手册。  
> **在线调试入口**：启动后端后访问 [Swagger UI](http://localhost:9090/swagger-ui.html)。

## 1. 使用说明

### 1.1 当前事实来源

离线文档用于帮助快速定位接口，但更细的请求字段、响应字段与注解说明，仍以以下两处为准：

1. Swagger UI
2. `src/main/java/com/badminton/bmp/modules/**/controller/*.java`

### 1.2 基础地址

- 基础 URL：`http://localhost:9090`
- API 前缀：`/api`
- Swagger UI：`/swagger-ui.html`

### 1.3 通用返回结构

所有接口统一使用 `Result<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1.4 认证方式

系统采用 JWT 双 Token，请求头格式：

```text
Authorization: Bearer {accessToken}
```

当前角色包括：

- `PRESIDENT`
- `VENUE_MANAGER`
- `USER`
- `MEMBER`
- `COACH`

### 1.5 分页约定

多数列表接口统一支持：

- `page`：页码，默认 `1`
- `size`：每页条数，默认 `10`

分页结构通常为：

```json
{
  "data": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "pages": 0
}
```

个别接口使用 `pageNum` / `pageSize`，如财务审计分页。

## 2. 当前接口地图

基于 2026-07-16 对 `modules/**/controller` 的复核，当前共有 **30** 个模块 API Controller、**243** 个方法级 HTTP 映射。`BaseController` 与 `GlobalExceptionHandler` 属于基础返回/异常组件，不计入业务接口 Controller。

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth` | 登录、注册、Token、当前用户、个人设置 |
| 用户管理 | `/api/user` | 用户 CRUD、角色查询、个人资料 |
| 用户设置 | `/api/settings` | 键值型设置 |
| 联系留言 | `/api/contact` | 前台匿名留言 |
| 用户反馈 | `/api/feedback` | 提交反馈；Controller 基路径实际为 `/api` |
| 天气服务 | `/api/weather` | IP 定位、天气代理 |
| 地图服务 | `/api/map` | 逆地理编码 |
| Dashboard | `/api/dashboard` | 汇总统计 |
| 官网公开概览 | `/api/site` | 匿名运营展示汇总 |
| 近期动态 | `/api/activity` | 最近会员/预约/财务动态 |
| 综合搜索 | `/api/search` | 场馆/课程/赛事/器材搜索 |
| 通知 | `/api/notifications` | 列表、发布、编辑、删除 |
| 支付自动取消配置 | `/api/payment/auto-cancel` | 倒计时与自动取消只读配置 |
| 场馆 | `/api/venue` | 场馆、图片、营业时间、状态日志 |
| 场地 | `/api/court` | 场地 CRUD、占用、利用率 |
| 会员 | `/api/member` | 会员 CRUD、统计、消费记录 |
| 充值 | `/api/recharge` | 自助充值、管理员代充、充值记录 |
| 教练 | `/api/coach` | 教练 CRUD、本人档案、未绑定账号 |
| 教练学员 | `/api/coach/students` | 本人学员、训练、课表、出勤、课程消费 |
| 课程 | `/api/course` | 课程 CRUD、教练我的课程 |
| 课程预约 | `/api/course/booking` | 课程预约、教练端预约处理 |
| 场地预约 | `/api/booking` | 拼场/包场、占用、支付退款 |
| 器材 | `/api/equipment` | 器材 CRUD、图片、库存预警 |
| 器材租借 | `/api/equipment/rental` | 租借、支付、退款 |
| 赛事 | `/api/tournament` | 赛事 CRUD |
| 赛事报名 | `/api/tournament/registration` | 报名、搭档、支付退款 |
| 穿线服务 | `/api/stringing` | 工单、价格计算、支付退款 |
| 财务 | `/api/finance` | 财务流水、趋势、占比、场馆趋势 |
| 财务审计 | `/api/finance/audit` | 审计日志分页与导出 |
| 财务对账 | `/api/finance/reconciliation` | 全面对账与分业务对账 |

## 3. 认证与系统类接口

### 3.1 认证：`/api/auth`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/login` | `POST` | 登录，返回双 Token |
| `/current` | `GET` | 当前登录用户 |
| `/update` | `POST` | 更新当前用户资料 |
| `/change-password` | `POST` | 修改密码 |
| `/upload-avatar` | `POST` | 上传头像 |
| `/settings` | `GET` | 获取当前用户设置 |
| `/settings` | `PUT` | 更新当前用户设置 |
| `/register` | `POST` | 注册 |
| `/logout` | `POST` | 退出登录 |
| `/refresh` | `POST` | 刷新 Token |
| `/check-lock/{username}` | `GET` | 查询锁定状态 |
| `/unlock/{username}` | `POST` | 管理解锁 |
| `/forgot-password` | `POST` | 找回密码 |
| `/delete-account` | `POST` | 注销账号 |

### 3.2 用户管理：`/api/user`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 用户列表 |
| `/info/{id}` | `GET` | 用户详情 |
| `/findByRole` | `GET` | 按角色查询用户 |
| `/member-candidates` | `GET` | 会员候选用户 |
| `/add` | `POST` | 新增用户 |
| `/update` | `PUT` | 更新用户 |
| `/{id}` | `DELETE` | 删除用户 |
| `/profile` | `GET` | 当前用户资料 |
| `/profile` | `PUT` | 更新当前用户资料 |

### 3.3 用户设置：`/api/settings`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | `GET` | 查询当前用户全部设置 |
| `/{key}` | `GET` | 查询单个设置项 |
| `/{key}` | `PUT` | 保存单个设置项 |
| `/{key}` | `DELETE` | 删除单个设置项 |
| `/` | `DELETE` | 删除当前用户全部设置 |
| `/batch` | `POST` | 批量保存设置 |

说明：

- `/api/settings` 是通用的当前用户 `key-value` 设置接口
- `/api/auth/settings` 是面向前端“安全/通知设置面板”的聚合读写接口，底层仍然落到 `UserSetting` 数据

### 3.4 联系、反馈、天气、地图

#### 联系留言：`/api/contact`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/submit` | `POST` | 前台匿名留言 |

#### 用户反馈：`/api/feedback`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | `POST` | 反馈提交，可匿名，可带登录态 |

说明：

- 当前完整接口为 `POST /api/feedback`
- Controller 的 `@RequestMapping` 为 `/api`，方法级映射为 `/feedback`

#### 天气服务：`/api/weather`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/ip-location` | `GET` | IP 定位 |
| `/wttr` | `GET` | `wttr.in` 天气代理 |
| `/qweather` | `GET` | 和风天气代理 |

#### 地图服务：`/api/map`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/reverse-geocode` | `GET` | 经纬度逆地理编码 |

### 3.5 搜索、动态、Dashboard、通知

#### 搜索：`/api/search`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/venues` | `GET` | 搜索场馆 |
| `/courses` | `GET` | 搜索课程 |
| `/tournaments` | `GET` | 搜索赛事 |
| `/equipment` | `GET` | 搜索器材 |
| `/suggestions` | `GET` | 搜索建议 |
| `/all` | `GET` | 聚合搜索 |

#### 近期动态：`/api/activity`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/recent` | `GET` | 最近会员、预约、财务动态 |

#### Dashboard：`/api/dashboard`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/summary` | `GET` | 汇总统计 |

#### 官网公开概览：`/api/site`

| 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/overview` | `GET` | 匿名 | 返回场地利用率、预约成功率/环比、器材可用数、当日收入/环比 |

#### 通知：`/api/notifications`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | `GET` | 通知列表 |
| `/` | `POST` | 发布通知 |
| `/{id}` | `GET` | 通知详情 |
| `/{id}` | `PUT` | 编辑通知 |
| `/{id}` | `DELETE` | 删除通知 |

### 3.6 支付自动取消配置：`/api/payment/auto-cancel`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/config` | `GET` | 获取支付超时自动取消配置，用于前端倒计时与服务端时间对齐 |

响应字段说明：

- `enabled`：是否启用自动取消
- `timeoutSeconds`：主配置字段，前端倒计时应优先使用该值
- `timeoutMinutes`：展示兼容字段，由后端根据秒值换算得到
- `serverTime`：服务端当前时间，格式为 `yyyy-MM-dd HH:mm:ss`

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "enabled": true,
    "timeoutSeconds": 5,
    "timeoutMinutes": 0.08333333333333333,
    "serverTime": "2026-05-17 14:30:05"
  }
}
```

配置口径说明：

- 运行时主配置项为 `bmp.payment.auto-cancel.timeout-seconds`
- 扫描频率配置项为 `bmp.payment.auto-cancel.scan-interval-ms`
- `timeout-minutes` 仅作为旧配置兼容输入保留，不作为新配置主写法
- `dev` 当前默认值为：`5 秒超时 + 1000 毫秒扫描`
- `prod` 当前默认值为：`900 秒超时 + 60000 毫秒扫描`，仍可通过环境变量覆盖

## 4. 场馆经营资源类接口

### 4.1 场馆：`/api/venue`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 场馆列表 |
| `/info/{id}` | `GET` | 场馆详情 |
| `/add` | `POST` | 新增场馆 |
| `/update` | `PUT` | 更新场馆 |
| `/{id}` | `DELETE` | 删除场馆 |
| `/upload-image` | `POST` | 上传单图 |
| `/upload-images` | `POST` | 上传多图 |
| `/{venueId}/images` | `GET` | 场馆图片列表 |
| `/{venueId}/images/bind` | `POST` | 绑定已有图片 |
| `/image/{id}` | `DELETE` | 删除图片 |
| `/image/{id}/sort` | `PUT` | 更新图片排序 |
| `/{venueId}/schedules` | `GET` | 营业时间列表 |
| `/{venueId}/schedules` | `POST` | 保存营业时间 |
| `/schedule/{id}` | `DELETE` | 删除营业时间 |
| `/{venueId}/status-logs` | `GET` | 状态日志 |
| `/statistics` | `GET` | 场馆统计 |

### 4.2 场地：`/api/court`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 场地列表 |
| `/info/{id}` | `GET` | 场地详情 |
| `/add` | `POST` | 新增场地 |
| `/update` | `PUT` | 更新场地 |
| `/{id}` | `DELETE` | 删除场地 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 场地统计 |
| `/venues` | `GET` | 场馆下拉 |
| `/{id}/bookings/today` | `GET` | 当天预约详情 |
| `/bookings/today/counts` | `GET` | 当天预约人数统计 |
| `/utilization/daily` | `GET` | 日利用率 |
| `/timeline/today` | `GET` | 当日时间轴 |

### 4.3 会员：`/api/member`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 会员列表 |
| `/info/{id}` | `GET` | 会员详情 |
| `/current` | `GET` | 当前登录用户的会员信息 |
| `/add` | `POST` | 新增会员 |
| `/update` | `PUT` | 更新会员 |
| `/{id}` | `DELETE` | 删除会员 |
| `/statistics` | `GET` | 会员统计 |
| `/distribution` | `GET` | 会员分布 |
| `/funnel` | `GET` | 会员漏斗 |
| `/expiring` | `GET` | 即将到期会员 |
| `/source` | `GET` | 会员来源分布 |
| `/{id}/consume-records` | `GET` | 消费记录 |

### 4.4 充值：`/api/recharge`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/user` | `POST` | 用户自助充值 |
| `/admin` | `POST` | 管理员代充 |
| `/records` | `GET` | 充值记录列表 |
| `/records/{memberId}` | `GET` | 某会员充值记录 |

### 4.5 教练：`/api/coach`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 教练列表 |
| `/me` | `GET` | 当前教练档案 |
| `/me` | `PUT` | 当前教练更新档案 |
| `/unbound-users` | `GET` | 未绑定教练档案的账号 |
| `/info/{id}` | `GET` | 教练详情 |
| `/add` | `POST` | 新增教练 |
| `/update` | `PUT` | 更新教练 |
| `/{id}` | `DELETE` | 删除教练 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 教练统计 |
| `/venues` | `GET` | 场馆下拉 |

### 4.6 教练学员：`/api/coach/students`

整组接口要求 `COACH` 角色，并且只能查看与当前教练课程存在有效关联的会员。学员关系由课程预约动态推导，保留窗口为课程结束后的 180 天。

| 接口 | 方法 | 说明 |
|------|------|------|
| `/` | `GET` | 本人学员列表与总数、风险数、今日数、平均出勤率汇总 |
| `/{id}` | `GET` | 学员详情 |
| `/{id}/progress` | `GET` | 按课程汇总训练进度 |
| `/{id}/schedule` | `GET` | 分页查询课程安排 |
| `/{id}/attendance` | `GET` | 分页查询出勤记录 |
| `/{id}/consume-records` | `GET` | 分页查询该教练课程产生的会员消费记录 |

列表参数：`page=1`、`size=20`、`keyword`、`memberType`、`riskOnly`、`todayOnly`、`orderBy`、`orderDirection`。`orderBy` 仅接受 `lastLessonTime`、`attendanceRate`、`totalPaidAmount`、`totalHours`。

课程安排/出勤参数：`startDate`、`endDate`、`status`、`attendanceStatus`、`page=1`、`size=20`。详情类查询按教练每秒最多 10 次，列表每秒最多 5 次。

### 4.7 课程：`/api/course`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/my` | `GET` | 当前教练的课程列表 |
| `/list` | `GET` | 课程列表 |
| `/info/{id}` | `GET` | 课程详情 |
| `/add` | `POST` | 新增课程 |
| `/update` | `PUT` | 更新课程 |
| `/{id}` | `DELETE` | 删除课程 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 课程统计 |
| `/coaches` | `GET` | 教练下拉 |
| `/courts` | `GET` | 场地下拉 |

## 5. 交易与服务类接口

### 5.1 场地预约：`/api/booking`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 预约列表 |
| `/info/{id}` | `GET` | 预约详情 |
| `/add` | `POST` | 新增预约 |
| `/update` | `PUT` | 更新预约 |
| `/{id}` | `DELETE` | 删除预约 |
| `/status` | `PUT` | 更新状态 |
| `/payment` | `POST` | 管理端支付 |
| `/member/payment` | `POST` | 用户支付本人预约 |
| `/refund` | `POST` | 退款 |
| `/statistics` | `GET` | 预约统计 |
| `/operation-todo` | `GET` | 运营待办统计 |
| `/trend` | `GET` | 预约趋势 |
| `/heatmap` | `GET` | 预约热力图 |
| `/venue-trend` | `GET` | 各场馆预约趋势 |
| `/occupancy/current` | `GET` | 当前占用率 |
| `/occupancy/range` | `GET` | 指定时段占用详情 |
| `/count` | `GET` | 指定时段拼场人数 |
| `/venues` | `GET` | 场馆下拉 |
| `/courts` | `GET` | 场地下拉 |
| `/members` | `GET` | 会员下拉 |

关键说明：

- 当前支持 `SHARED` 与 `PACKAGE` 两种预约模式
- 当前支持 `PACKAGE_HOUR`、`SHARED_HOUR`、`SHARED_TIME` 三种计费方式
- 新增预约请求支持 `courtIds` 多场地字段
- `/operation-todo`、`/trend`、`/heatmap`、`/venue-trend` 当前主要服务 Dashboard / 运营看板场景

### 5.2 器材：`/api/equipment`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 器材列表 |
| `/info/{id}` | `GET` | 器材详情 |
| `/add` | `POST` | 新增器材 |
| `/update` | `PUT` | 更新器材 |
| `/{id}` | `DELETE` | 删除器材 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 器材统计 |
| `/types` | `GET` | 器材类型选项 |
| `/low-stock` | `GET` | 低库存预警 |
| `/upload-image` | `POST` | 上传单图 |
| `/upload-images` | `POST` | 上传多图 |
| `/{equipmentId}/images` | `GET` | 图片列表 |
| `/image/{id}` | `DELETE` | 删除图片 |
| `/image/{id}/sort` | `PUT` | 更新排序 |
| `/venues` | `GET` | 场馆下拉 |

### 5.3 器材租借：`/api/equipment/rental`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 租借列表 |
| `/info/{id}` | `GET` | 租借详情 |
| `/add` | `POST` | 新增租借 |
| `/update` | `PUT` | 更新租借 |
| `/{id}` | `DELETE` | 删除/取消租借 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 租借统计 |
| `/payment` | `POST` | 支付 |
| `/refund` | `POST` | 退款 |
| `/equipments` | `GET` | 器材下拉 |
| `/members` | `GET` | 会员下拉 |

### 5.4 课程预约：`/api/course/booking`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/for-coach` | `GET` | 教练端预约列表 |
| `/for-coach/{id}` | `GET` | 教练端预约详情 |
| `/for-coach/status` | `PUT` | 教练端更新预约状态 |
| `/for-coach/attendance` | `PUT` | 教练端登记签到、完成或缺席 |
| `/list` | `GET` | 课程预约列表 |
| `/info/{id}` | `GET` | 课程预约详情 |
| `/add` | `POST` | 新增课程预约 |
| `/update` | `PUT` | 更新课程预约 |
| `/{id}` | `DELETE` | 删除课程预约 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 统计 |
| `/payment` | `POST` | 管理端支付 |
| `/member/payment` | `POST` | 用户支付本人预约 |
| `/refund` | `POST` | 退款 |
| `/courses` | `GET` | 课程下拉 |
| `/members` | `GET` | 会员下拉 |

考勤请求：

```json
{
  "id": 123,
  "action": "CHECK_IN",
  "remark": "按时到场"
}
```

- `action`：`CHECK_IN`、`COMPLETE`、`ABSENT`
- `attendanceStatus`：`0` 未登记、`1` 已签到、`2` 已完成、`3` 缺席
- 响应包含 `id`、`bookingStatus`、`attendanceStatus`、`actualCheckinTime`、`actualFinishTime`
- 同一动作重复提交按幂等成功处理；每个教练每秒最多 5 条考勤命令

### 5.5 赛事：`/api/tournament`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 赛事列表 |
| `/info/{id}` | `GET` | 赛事详情 |
| `/add` | `POST` | 新增赛事 |
| `/update` | `PUT` | 更新赛事 |
| `/{id}` | `DELETE` | 删除赛事 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 统计 |
| `/venues` | `GET` | 场馆下拉 |

### 5.6 赛事报名：`/api/tournament/registration`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 报名列表 |
| `/info/{id}` | `GET` | 报名详情 |
| `/add` | `POST` | 新增报名 |
| `/update` | `PUT` | 更新报名 |
| `/{id}` | `DELETE` | 删除报名 |
| `/status` | `PUT` | 更新状态 |
| `/statistics` | `GET` | 统计 |
| `/payment` | `POST` | 管理端支付 |
| `/member/payment` | `POST` | 用户支付本人报名 |
| `/refund` | `POST` | 退款 |
| `/tournaments` | `GET` | 赛事下拉 |
| `/members` | `GET` | 会员下拉 |

### 5.7 穿线服务：`/api/stringing`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 穿线列表 |
| `/info/{id}` | `GET` | 按 ID 查询 |
| `/info/no/{serviceNo}` | `GET` | 按单号查询 |
| `/add` | `POST` | 新增工单 |
| `/update` | `PUT` | 更新工单 |
| `/{id}` | `DELETE` | 删除工单 |
| `/status` | `PUT` | 更新状态 |
| `/cancel` | `POST` | 用户取消本人工单 |
| `/statistics` | `GET` | 统计 |
| `/strings` | `GET` | 线材下拉 |
| `/calculate-price` | `GET` | 价格计算 |
| `/payment` | `POST` | 管理端支付 |
| `/member/payment` | `POST` | 用户支付本人工单 |
| `/refund` | `POST` | 退款 |

## 6. 财务与统计类接口

### 6.1 财务：`/api/finance`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/list` | `GET` | 财务记录列表 |
| `/info/{id}` | `GET` | 财务记录详情 |
| `/add` | `POST` | 新增财务记录 |
| `/update` | `PUT` | 更新财务记录 |
| `/{id}` | `DELETE` | 删除财务记录 |
| `/statistics` | `GET` | 财务统计概览 |
| `/trend` | `GET` | 趋势数据 |
| `/venue-trend` | `GET` | 各场馆收入趋势 |
| `/ratio` | `GET` | 业务占比 |
| `/venues` | `GET` | 场馆下拉 |
| `/businessTypes` | `GET` | 业务类型选项 |

### 6.2 财务审计：`/api/finance/audit`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/page` | `GET` | 分页查询审计日志 |
| `/finance/{financeId}` | `GET` | 按财务记录查询 |
| `/operator/{operator}` | `GET` | 按操作人查询 |
| `/type/{operationType}` | `GET` | 按操作类型查询 |
| `/{id}` | `GET` | 审计日志详情 |
| `/export/create` | `POST` | 创建导出任务 |
| `/export/status/{taskId}` | `GET` | 查询导出状态 |
| `/export/download/{taskId}` | `GET` | 下载导出文件 |

注意：这个模块的分页参数使用 `pageNum` / `pageSize`。

### 6.3 财务对账：`/api/finance/reconciliation`

| 接口 | 方法 | 说明 |
|------|------|------|
| `/full` | `GET` | 全面对账，仅会长 |
| `/booking` | `GET` | 场地预约对账 |
| `/course` | `GET` | 课程预约对账 |
| `/equipment` | `GET` | 器材租借对账 |
| `/tournament` | `GET` | 赛事报名对账 |
| `/stringing` | `GET` | 穿线服务对账 |
| `/recharge` | `GET` | 会员充值对账 |

## 7. 关键枚举与约定

### 7.1 业务类型

| 值 | 说明 |
|---|------|
| `BOOKING` | 场地预约 |
| `COURSE` | 课程预约 |
| `EQUIPMENT` | 器材租借 |
| `TOURNAMENT` | 赛事报名 |
| `STRINGING` | 穿线服务 |
| `RECHARGE` | 会员充值 |
| `OTHER` | 其他收支 |

### 7.2 支付方式

| 值 | 说明 |
|---|------|
| `CASH` | 现金 |
| `ALIPAY` | 支付宝 |
| `WECHAT` | 微信 |
| `BALANCE` | 余额 |
| `BANK` | 银行转账 |

### 7.3 场地预约相关

| 类型 | 值 | 说明 |
|------|------|------|
| 预约模式 | `SHARED` | 拼场 |
| 预约模式 | `PACKAGE` | 包场 |
| 计费方式 | `PACKAGE_HOUR` | 包场按小时 |
| 计费方式 | `SHARED_HOUR` | 拼场按小时 |
| 计费方式 | `SHARED_TIME` | 拼场按次 |

### 7.4 常见状态示例

| 类型 | 主要取值 |
|------|------|
| 预约状态 | `0` 已取消 / `1` 待支付 / `2` 已支付 / `3` 进行中 / `4` 已完成 |
| 场馆状态 | `0` 关闭 / `1` 营业中 / `2` 暂停 |
| 场地状态 | `0` 维护中 / `1` 空闲 / `2` 预约中 / `3` 使用中 |

### 7.5 WebSocket / STOMP 约定

| 项 | 当前实现 |
|----|----------|
| 浏览器握手 | `/ws`（SockJS） |
| UniApp 握手 | `/ws-native`（原生 WebSocket） |
| 应用前缀 | `/app` |
| Broker 前缀 | `/topic`、`/queue` |
| 用户订阅 | `/user/queue/notifications` |
| 管理广播 | `/topic/admin/todo`、`/topic/admin/dashboard` |
| CONNECT 鉴权 | STOMP 头 `token` 或 `Authorization: Bearer {token}` |

教练学员事件类型：`COACH_STUDENT_NEW_BOOKING`、`COACH_STUDENT_CANCELLED`、`COACH_STUDENT_ATTENDANCE_CHANGED`。事件在业务事务成功提交后异步推送。

> 当前安全配置只显式匿名放行 `/ws` 与 `/ws/**`，未放行 `/ws-native`。UniApp 真机使用前需要同步修正 `SecurityConfig`，否则仅在 STOMP CONNECT 头携带 token 可能无法通过 HTTP WebSocket 握手。

### 7.6 查询降级与错误

- 教练学员查询遇到非业务型运行时异常时，会返回 15 分钟内的最后成功结果
- 无可用缓存时返回 `code=503`，`data` 包含 `traceId` 与 `retryable=true`
- 触发接口限流时抛出 `RateLimitException`，由统一异常处理返回 `429`

## 8. 维护说明

- 新增接口时，优先补充 Swagger 注解
- 本文档至少需要同步更新基础路径、模块索引与关键业务端点
- 如果只是接口字段微调，以 Swagger 为准；如果新增了新的 Controller 或新的基础路径，必须同步这份文档

## 9. 变更记录

| 日期 | 版本 | 变更内容 |
|------|------|----------|
| 2026-07-16 | v2.4.0 | 按 30 个模块 Controller / 243 个 HTTP 映射复核；新增官网概览、教练学员、考勤状态机与 WebSocket 约定 |
| 2026-05-23 | v2.3.0 | 标记 v1.0.0 交付完成；复核 27 个 Controller 与 `/api/payment/auto-cancel` 配置接口 |
| 2026-05-17 | v2.2.0 | 同步新增与遗漏的系统类、财务审计、财务对账、天气、地图、搜索、通知编辑等接口，并校正当前基础路径清单 |
| 2026-05-05 | v2.1.0 | 同步预约模式、三种计费方式、占用查询与拼场人数接口 |
| 2026-05-01 | v2.0.0 | 重构为手册型 API 文档 |

---

**维护者**：BMP 开发团队  
**仓库**：羽擎 BMP 单仓（根目录 `pom.xml` + `vue/` + `BMP-uniapp/`）
