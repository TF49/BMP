# 教练学员 WebSocket P6 设计

## 1. 目标与范围

在不影响现有 P1—P5 查询与手动刷新能力的前提下，为 BMP-uniapp 教练端补充实时通知和页面刷新能力。

本次只实现三类事件：

- `COACH_STUDENT_NEW_BOOKING`：课程产生新的有效预约。
- `COACH_STUDENT_CANCELLED`：学员主动取消自己的课程预约。
- `COACH_STUDENT_ATTENDANCE_CHANGED`：教练完成签到、完成或缺席登记。

不实现可选的 `COACH_MEMBER_STATUS_CHANGED`，不改 Vue Web 管理端页面，不删除现有 `onShow` 和下拉刷新兜底。

## 2. 现状与约束

- Java 后端已使用 Spring WebSocket、STOMP 和 `SimpMessagingTemplate`。
- 现有 `/ws` 端点启用了 SockJS，继续供 Vue Web 端使用。
- WebSocket CONNECT 阶段已通过 JWT 校验，并将 `sys_user.id` 绑定为 Principal。
- 教练档案通过 `sys_coach.user_id` 关联登录账号，点对点消息发送到 `/user/queue/notifications`。
- BMP-uniapp 目前没有 STOMP 客户端，只有微信 WebSocket 错误与连接数补丁。
- 微信小程序生产环境必须使用已备案并配置到小程序后台的 `wss` 合法域名。

## 3. 方案选择

采用“原生 STOMP 端点 + `@stomp/stompjs` + `uni.connectSocket` 适配器”。

不直接复用 SockJS 客户端，因为 SockJS 的浏览器传输与运行时假设在微信小程序中存在兼容风险；不另建自定义 WebSocket 协议，因为那会重复实现鉴权、订阅、心跳和重连。

## 4. 后端设计

### 4.1 连接端点

- 保留 `/ws` SockJS 端点，避免影响 Vue Web 管理端。
- 新增 `/ws-native` 原生 STOMP 端点，复用相同的允许来源配置和 `WebSocketAuthInterceptor`。
- 客户端在 STOMP CONNECT headers 中同时携带 `token` 和 `Authorization: Bearer <token>`。

### 4.2 事件模型

新增明确类型的事件载荷，公共字段包括：

- `type`
- `priority`
- `coachId`
- `memberId`
- `bookingId`
- `courseId`
- `courseName`
- `memberName`
- `occurredAt`

考勤事件额外包含 `bookingStatus` 和 `attendanceStatus`。事件不得包含手机号、身份证、余额、充值或无关消费信息。

### 4.3 目标用户解析

事件发布时通过 `booking.courseId → course.coachId → coach.userId` 定位教练登录账号。任何一级不存在时记录警告并跳过推送，不影响主业务事务。

### 4.4 事务一致性

业务方法只在数据库写入成功后发布领域事件；WebSocket 发送在事务成功提交后执行。事务回滚时不得推送。

WebSocket 发送异常只记录警告，不回滚预约或考勤业务。

### 4.5 触发点

- 新报名：`CourseBookingServiceImpl#add` 插入成功后触发，不区分学员本人或管理员代报。
- 学员取消：普通用户或会员把有效预约变为取消状态、或删除本人预约成功后触发。
- 教练取消：不发送 `COACH_STUDENT_CANCELLED`，避免向教练本人显示“学员取消”的错误语义。
- 系统超时取消、管理员退款：不冒充学员主动取消；本次不发送该事件。
- 考勤变化：`updateAttendanceForCoach` 实际更新成功后触发；幂等重复命令不重复推送。

### 4.6 合并与限流

- 后端只做事务后异步发送，不合并 HIGH 级取消事件，避免丢失具体取消信息。
- 前端对页面刷新统一做 200ms 防抖合并。
- HIGH 级弹窗按教练账号限制为一分钟最多一次；期间到达的事件仍触发数据刷新。

## 5. 小程序设计

### 5.1 STOMP 适配

- 增加 `@stomp/stompjs` 依赖。
- 实现最小 `uni.SocketTask` WebSocket 适配器，向 STOMP Client 提供 `send`、`close`、`onopen`、`onmessage`、`onerror`、`onclose` 和 `readyState`。
- 从 `API_BASE_URL` 去掉 `/api`，并把 `http/https` 转换为 `ws/wss`，拼接 `/ws-native`。
- 自动重连间隔为 3 秒，并启用 STOMP 心跳。

### 5.2 生命周期

- 仅当 token 存在且角色为 `COACH` 时连接。
- 登录、恢复登录和刷新 token 后同步连接状态。
- 退出登录、切换为非教练账号或 App 进入后台时断开。
- App 再次进入前台时按当前 token 和角色恢复连接。

### 5.3 事件响应

- `COACH_STUDENT_CANCELLED`：短震动、提示弹窗，并刷新当前相关页面；弹窗一分钟最多一次。
- `COACH_STUDENT_NEW_BOOKING`：增加“我的学员”入口角标；若当前位于学员列表或详情页则刷新。
- `COACH_STUDENT_ATTENDANCE_CHANGED`：不打断操作，只刷新当前学员相关页面。
- 页面刷新通过统一事件总线触发，并做 200ms 防抖；页面卸载时必须解除监听。
- 保留 `onShow` 和 refresher，WebSocket 断线不影响基本功能。

## 6. 测试设计

### 6.1 后端自动化测试

- 三类事件载荷和目标用户正确。
- JWT STOMP 鉴权继续生效，原生端点存在且 SockJS 端点未被移除。
- 新报名成功后发布；插入失败或事务回滚不发送。
- 学员主动取消发送；教练取消、系统取消和管理员退款不冒充学员取消。
- 考勤实际更新发送；幂等重复命令不重复发送。
- 推送异常不影响主业务结果。
- 事件载荷不包含敏感字段。

### 6.2 前端自动化测试

- WebSocket URL 的开发与生产协议转换正确。
- 只有教练角色连接，logout/onHide 正确断开。
- STOMP 消息解析失败时不导致应用崩溃。
- 三类事件的优先级、弹窗节流、角标累计和 200ms 刷新合并正确。
- 相关页面注册和注销刷新监听。

### 6.3 完整验证

- 后端 `mvn test`。
- BMP-uniapp `npm test`。
- BMP-uniapp `npm run type-check`。
- BMP-uniapp `npm run build:mp-weixin`。
- 双账号人工验证：学员报名、学员取消、教练签到/完成/缺席。

## 7. 代码审查与文档替换

实现和自动化验证完成后，安排独立代码审查。Critical 和 Important 问题必须修复并重新验证。

审查通过后删除根目录的 `教练学员查看功能-实施计划.md`，生成新的 `教练学员查看功能-测试计划.md`。测试计划应包含环境、账号与数据准备、自动化命令、三类 WebSocket 双账号用例、P1—P5 回归、权限和隐私检查、真机检查及验收记录模板。

## 8. 完成标准

- 三类 WebSocket 事件均有测试覆盖并通过。
- 小程序断线重连、角色隔离、退出断开和刷新兜底成立。
- Vue Web 现有 SockJS 功能不被破坏。
- 全量测试、类型检查和小程序构建成功。
- 独立代码审查无未解决的 Critical 或 Important 问题。
- 原实施计划已删除，新测试计划已生成。
