# 羽毛球管理系统（BMP）API接口文档

> **文档版本**: v1.6.0
> **最后更新**: 2026-02-23
> **基础URL**: `http://localhost:9090`
> **API前缀**: `/api`
> **项目状态**: ✅ Web端100%完成 | ✅ 所有API接口已实现 | 🚧 UniApp移动端开发中

### 在线文档与调试（Swagger UI）

**在线文档与接口调试请使用 Swagger UI**：启动后端服务后访问 [http://localhost:9090/swagger-ui.html](http://localhost:9090/swagger-ui.html)。  
在 Swagger UI 中可查看所有接口说明、请求/响应结构，并可直接发起请求调试。需认证的接口请先调用登录接口获取 Token，再点击页面顶部 **Authorize** 填写 `Bearer {AccessToken}`。  
本文档（API文档.md）保留为离线速查与目录索引，后续新增或变更接口时请同步更新本文档中的目录与关键示例。

---

### 2.12 检查账户锁定状态

**接口**: `GET /api/auth/check-lock/{username}`

**权限**: 无需认证

**路径参数**:
- `username`: 用户名

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "locked": false,
    "message": "账户正常"
  }
}
```

---

### 2.13 管理员解锁账户

**接口**: `POST /api/auth/unlock/{username}`

**权限**: 需要管理员认证

**路径参数**:
- `username`: 用户名

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "账户解锁成功"
}
```

---

## 目录

1. [基础说明](#1-基础说明)
2. [认证模块](#2-认证模块)
3. [用户管理模块](#3-用户管理模块)
4. [场馆管理模块](#4-场馆管理模块)
5. [会员管理模块](#5-会员管理模块)
6. [场地管理模块](#6-场地管理模块)
7. [场地预约模块](#7-场地预约模块)
8. [器材管理模块](#8-器材管理模块)
9. [器材租借模块](#9-器材租借模块)
10. [教练管理模块](#10-教练管理模块)
11. [课程管理模块](#11-课程管理模块)
12. [课程预约模块](#12-课程预约模块)
13. [赛事管理模块](#13-赛事管理模块)
14. [赛事报名模块](#14-赛事报名模块)
15. [穿线服务模块](#15-穿线服务模块)
16. [通知管理模块](#16-通知管理模块)
17. [充值管理模块](#17-充值管理模块)
18. [财务管理模块](#18-财务管理模块)

---

## 1. 基础说明

### 1.1 统一返回格式

所有API接口统一使用 `Result<T>` 格式返回：

```json
{
  "code": 200,           // 状态码：200-成功，500-错误，403-无权限
  "message": "success",  // 提示信息
  "data": {}             // 返回数据（泛型）
}
```

**状态码说明**:
- `200`: 请求成功
- `500`: 服务器错误
- `403`: 无权限访问

### 1.2 认证方式

系统采用 **JWT Token** 认证机制，支持双Token（AccessToken + RefreshToken）。

**请求头格式**:
```
Authorization: Bearer {AccessToken}
```

**Token说明**:
- **AccessToken**: 有效期2小时，用于API请求认证
- **RefreshToken**: 有效期7天，用于刷新AccessToken

### 1.3 字段命名约定（重要）

为避免前后端命名风格冲突，文档约定：
- API 请求/响应使用 camelCase（示例：`venueId`、`rechargeAmount`）
- 数据库列使用 snake_case（示例：`venue_id`、`recharge_amount`）
- 后端实现包含字段映射层（camelCase <-> snake_case），开发者无需在请求中使用下划线字段

（测试与示例在本说明中均使用 camelCase）

### 1.3 权限角色

系统支持三种角色：
- `PRESIDENT`: 协会会长（最高权限）
- `VENUE_MANAGER`: 场馆管理者（管理自己场馆的数据）
- `USER`: 普通用户（管理自己的数据）

### 1.4 分页参数

所有列表接口支持分页，统一参数：
- `page`: 页码（默认1）
- `size`: 每页数量（默认10，最大100）

**分页返回格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "data": [],        // 数据列表
    "total": 100,      // 总记录数
    "page": 1,         // 当前页码
    "size": 10,        // 每页数量
    "pages": 10        // 总页数
  }
}
```

---

## 2. 认证模块

**基础路径**: `/api/auth`

### 2.1 用户登录

**接口**: `POST /api/auth/login`

**权限**: 无需认证

**请求体**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200,
    "message": "登录成功",
    "user": {
      "id": 1,
      "username": "admin",
      "role": "PRESIDENT",
      "status": 1
    }
  }
}
```

**说明**:
- 登录接口包含限流保护（IP限流 + 用户名限流）
- 连续失败5次后锁定账户15分钟
- 返回双Token（AccessToken + RefreshToken）
- 新增账户锁定检查和解锁功能

---

### 2.2 用户注册

**接口**: `POST /api/auth/register`

**权限**: 无需认证

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456",
  "confirmPassword": "123456",
  "idCard": "110101199001011234",
  "role": "USER"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "注册成功"
}
```

**说明**:
- 协会会长（PRESIDENT）只能存在一个
- 密码和确认密码必须一致

---

### 2.3 获取当前用户信息

**接口**: `GET /api/auth/current`

**权限**: 需要认证

**请求头**:
```
Authorization: Bearer {token}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "role": "PRESIDENT",
    "status": 1
  }
}
```

---

### 2.4 刷新Token

**接口**: `POST /api/auth/refresh`

**权限**: 无需认证

**请求体**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "新的AccessToken",
    "refreshToken": "新的RefreshToken",
    "expiresIn": 7200,
    "message": "Token刷新成功",
    "user": {}
  }
}
```

---

### 2.5 用户退出登录

**接口**: `POST /api/auth/logout`

**权限**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "退出登录成功"
}
```

---

### 2.6 当前用户信息更新

**接口**: `POST /api/auth/update`

**权限**: 需要认证

**请求体**（仅可编辑字段）:
```json
{
  "phone": "13800138000",
  "gender": "MALE",
  "avatar": "/api/uploads/avatars/xxx.jpg",
  "signature": "个性签名"
}
```

**响应**: 成功返回更新后的用户信息（data 为 User 对象）；失败返回 code 500 及 msg。

---

### 2.7 修改密码

**接口**: `POST /api/auth/change-password`

**权限**: 需要认证

**请求体**:
```json
{
  "oldPassword": "原密码",
  "newPassword": "新密码（至少6位）"
}
```

**响应**: 成功 code 200；失败返回 msg（如「原密码错误」）。

---

### 2.8 上传头像

**接口**: `POST /api/auth/upload-avatar`

**权限**: 需要认证

**请求**: `multipart/form-data`，字段名 `file`（图片文件，支持 jpg/jpeg/png/webp，最大 2MB）。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "/api/uploads/avatars/xxx.jpg"
  }
}
```

上传后前端需将 `url` 通过 `POST /api/auth/update` 写回用户 `avatar` 字段。

---

### 2.9 获取当前用户设置

**接口**: `GET /api/auth/settings`

**权限**: 需要认证

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "loginAlert": true,
    "strangeDevice": true,
    "siteMessage": true,
    "emailNotify": false,
    "smsNotify": false
  }
}
```

---

### 2.10 保存当前用户设置

**接口**: `PUT /api/auth/settings`

**权限**: 需要认证

**请求体**:
```json
{
  "loginAlert": true,
  "strangeDevice": true,
  "siteMessage": true,
  "emailNotify": false,
  "smsNotify": false
}
```

**响应**: 成功 code 200，data 为「保存成功」或 null。

---

### 2.11 提交反馈

**接口**: `POST /api/feedback`

**权限**: 可匿名（可选带 Token 关联用户）

**请求体**:
```json
{
  "content": "反馈内容（必填）",
  "contact": "联系方式（选填）"
}
```

**响应**: 成功 code 200；失败返回 msg。

---

## 3. 用户管理模块

**基础路径**: `/api/user`

**权限要求**: 仅协会会长（PRESIDENT）

### 3.1 用户列表

**接口**: `GET /api/user/list`

**查询参数**:
- `username` (可选): 用户名（模糊搜索）
- `role` (可选): 角色（PRESIDENT/VENUE_MANAGER/USER）
- `status` (可选): 状态（0-禁用，1-启用）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "data": [
      {
        "id": 1,
        "username": "admin",
        "role": "PRESIDENT",
        "status": 1,
        "venueId": null
      }
    ],
    "total": 10,
    "page": 1,
    "size": 10,
    "pages": 1
  }
}
```

---

### 3.2 用户详情

**接口**: `GET /api/user/info/{id}`

**路径参数**:
- `id`: 用户ID

---

### 3.3 按角色查询用户

**接口**: `GET /api/user/findByRole`

**查询参数**:
- `role`: 角色（PRESIDENT/VENUE_MANAGER/USER）

---

### 3.4 新增用户

**接口**: `POST /api/user/add`

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456",
  "idCard": "110101199001011234",
  "role": "VENUE_MANAGER",
  "venueId": 1,
  "status": 1
}
```

---

### 3.5 更新用户

**接口**: `PUT /api/user/update`

**请求体**:
```json
{
  "id": 1,
  "username": "admin",
  "role": "PRESIDENT",
  "status": 1
}
```

---

### 3.6 删除用户

**接口**: `DELETE /api/user/{id}`

**路径参数**:
- `id`: 用户ID

---

## 4. 场馆管理模块

**基础路径**: `/api/venue`

**权限要求**: 协会会长（PRESIDENT）完全管理，场馆管理者（VENUE_MANAGER）仅查看

### 4.1 场馆列表

**接口**: `GET /api/venue/list`

**查询参数**:
- `venueName` (可选): 场馆名称（模糊搜索）
- `status` (可选): 状态（0-关闭，1-营业中，2-暂停）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 4.2 场馆详情

**接口**: `GET /api/venue/info/{id}`

---

### 4.3 新增场馆

**接口**: `POST /api/venue/add`

**权限**: 仅协会会长

**请求体**:
```json
{
  "venueName": "羽毛球馆A",
  "address": "北京市朝阳区xxx",
  "contactPhone": "010-12345678",
  "contactPerson": "张三",
  "businessHours": "06:00-24:00",
  "description": "场馆描述",
  "status": 1
}
```

---

### 4.4 更新场馆

**接口**: `PUT /api/venue/update`

**权限**: 仅协会会长

---

### 4.5 删除场馆

**接口**: `DELETE /api/venue/{id}`

**权限**: 仅协会会长

---

### 4.6 上传场馆图片（单张）

**接口**: `POST /api/venue/upload-image`

**权限**: 仅协会会长

**请求类型**: `multipart/form-data`

**参数**:
- `venueId`: 场馆ID
- `file`: 图片文件

---

### 4.7 上传场馆图片（多张）

**接口**: `POST /api/venue/upload-images`

**权限**: 仅协会会长

**请求类型**: `multipart/form-data`

**参数**:
- `venueId`: 场馆ID
- `files`: 图片文件数组

---

### 4.8 获取场馆图片列表

**接口**: `GET /api/venue/{venueId}/images`

---

### 4.9 删除场馆图片

**接口**: `DELETE /api/venue/image/{id}`

**权限**: 仅协会会长

---

### 4.10 更新图片排序

**接口**: `PUT /api/venue/image/{id}/sort`

**权限**: 仅协会会长

**请求体**:
```json
{
  "sortOrder": 1
}
```

---

### 4.11 获取场馆营业时间

**接口**: `GET /api/venue/{venueId}/schedules`

---

### 4.12 设置场馆营业时间

**接口**: `POST /api/venue/{venueId}/schedules`

**权限**: 仅协会会长

**请求体**:
```json
[
  {
    "scheduleType": "WORKDAY",
    "startTime": "06:00",
    "endTime": "24:00",
    "isActive": 1
  },
  {
    "scheduleType": "WEEKEND",
    "startTime": "08:00",
    "endTime": "22:00",
    "isActive": 1
  }
]
```

**说明**:
- `scheduleType`: WORKDAY（工作日）、WEEKEND（周末）、HOLIDAY（节假日）

---

### 4.13 删除营业时间

**接口**: `DELETE /api/venue/schedule/{id}`

**权限**: 仅协会会长

---

### 4.14 获取场馆状态变更日志

**接口**: `GET /api/venue/{venueId}/status-logs`

---

### 4.15 场馆统计

**接口**: `GET /api/venue/statistics`

---

## 5. 会员管理模块

**基础路径**: `/api/member`

### 5.1 会员列表

**接口**: `GET /api/member/list`

**权限**: 需要认证

**查询参数**:
- `memberName` (可选): 会员姓名（模糊搜索）
- `phone` (可选): 手机号
- `memberId` (可选): 会员ID
- `memberType` (可选): 会员类型（NORMAL/MEMBER）
- `status` (可选): 状态（0-冻结，1-正常，2-到期）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

**权限说明**:
- 协会会长：查看所有会员
- 场馆管理者：查看自己场馆的会员
- 普通用户：仅查看自己的会员信息

---

### 5.2 会员详情

**接口**: `GET /api/member/info/{id}`

---

### 5.3 新增会员

**接口**: `POST /api/member/add`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "memberName": "张三",
  "gender": 1,
  "phone": "13800138000",
  "idCard": "110101199001011234",
  "memberType": "NORMAL",
  "memberLevel": 1,
  "status": 1
}
```

---

### 5.4 更新会员

**接口**: `PUT /api/member/update`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 5.5 删除会员

**接口**: `DELETE /api/member/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

**说明**: 逻辑删除

---

### 5.6 会员统计

**接口**: `GET /api/member/statistics`

---

### 5.7 会员消费记录

**接口**: `GET /api/member/{id}/consume-records`

**查询参数**:
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

## 6. 场地管理模块

**基础路径**: `/api/court`

### 6.1 场地列表

**接口**: `GET /api/court/list`

**查询参数**:
- `venueId` (可选): 场馆ID
- `courtCode` (可选): 场地编号
- `status` (可选): 状态（0-维护中，1-空闲，2-预约中，3-使用中）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 6.2 场地详情

**接口**: `GET /api/court/info/{id}`

---

### 6.3 新增场地

**接口**: `POST /api/court/add`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "courtCode": "A01",
  "venueId": 1,
  "courtName": "场地A01",
  "billingType": "HOUR",
  "pricePerHour": 100.00,
  "status": 1
}
```

**说明**:
- `billingType`: HOUR（按小时）、TIME（按次）
- 场地编号在同一场馆内唯一

---

### 6.4 更新场地

**接口**: `PUT /api/court/update`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 6.5 删除场地

**接口**: `DELETE /api/court/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 6.6 更新场地状态

**接口**: `PUT /api/court/status`

**权限**: PRESIDENT, VENUE_MANAGER

**查询参数**:
- `id`: 场地ID
- `status`: 状态值

---

### 6.7 场地统计

**接口**: `GET /api/court/statistics`

---

### 6.8 获取场馆下拉列表

**接口**: `GET /api/court/venues`

---

### 6.9 获取场地当天预约用户

**接口**: `GET /api/court/{id}/bookings/today`

**说明**: 返回当天预约该场地的用户信息（姓名脱敏）

---

### 6.10 获取所有场地当天预约统计

**接口**: `GET /api/court/bookings/today/counts`

---

## 7. 场地预约模块

**基础路径**: `/api/booking`

### 7.1 预约列表

**接口**: `GET /api/booking/list`

**权限**: 需要认证

**查询参数**:
- `memberId` (可选): 会员ID
- `courtId` (可选): 场地ID
- `status` (可选): 预约状态
- `keyword` (可选): 关键词搜索
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 7.2 预约详情

**接口**: `GET /api/booking/info/{id}`

---

### 7.3 新增预约

**接口**: `POST /api/booking/add`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**请求体**:
```json
{
  "memberId": 1,
  "courtId": 1,
  "bookingDate": "2026-01-25",
  "startTime": "14:00",
  "endTime": "16:00",
  "orderAmount": 200.00,
  "paymentMethod": "BALANCE",
  "status": 1
}
```

**说明**:
- 自动生成预约单号（格式：BK+日期+序号）
- 自动检测时间冲突
- 自动更新场地状态

---

### 7.4 更新预约

**接口**: `PUT /api/booking/update`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 7.5 删除预约

**接口**: `DELETE /api/booking/{id}`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 7.6 更新预约状态

**接口**: `PUT /api/booking/status`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**查询参数**:
- `id`: 预约ID
- `status`: 状态值（0-已取消，1-待支付，2-已支付，3-进行中，4-已完成）

---

### 7.7 处理支付

**接口**: `POST /api/booking/payment`

**权限**: PRESIDENT, VENUE_MANAGER

**查询参数**:
- `bookingId`: 预约ID
- `paymentMethod`: 支付方式（CASH/ALIPAY/WECHAT/BALANCE）

**说明**:
- 余额支付时自动扣减会员余额
- 自动记录消费记录

---

### 7.8 处理退款

**接口**: `POST /api/booking/refund`

**权限**: PRESIDENT, VENUE_MANAGER

**查询参数**:
- `bookingId`: 预约ID

**说明**:
- 余额支付时自动回滚会员余额
- 自动记录退款记录（负数冲正）

---

### 7.9 预约统计

**接口**: `GET /api/booking/statistics`

---

### 7.10 获取场地下拉列表

**接口**: `GET /api/booking/courts`

---

### 7.11 获取会员下拉列表

**接口**: `GET /api/booking/members`

---

## 8. 器材管理模块

**基础路径**: `/api/equipment`

### 8.1 器材列表

**接口**: `GET /api/equipment/list`

**查询参数**:
- `equipmentName` (可选): 器材名称
- `equipmentType` (可选): 器材类型（RACKET/SHUTTLE/STRING）
- `status` (可选): 状态（0-停用，1-正常）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 8.2 器材详情

**接口**: `GET /api/equipment/info/{id}`

---

### 8.3 新增器材

**接口**: `POST /api/equipment/add`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "equipmentCode": "EQ001",
  "equipmentName": "羽毛球拍",
  "equipmentType": "RACKET",
  "brand": "YONEX",
  "price": 500.00,
  "rentalPrice": 20.00,
  "totalQuantity": 10,
  "availableQuantity": 10,
  "status": 1
}
```

---

### 8.4 更新器材

**接口**: `PUT /api/equipment/update`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 8.5 删除器材

**接口**: `DELETE /api/equipment/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 8.6 更新器材状态

**接口**: `PUT /api/equipment/status`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 8.7 器材统计

**接口**: `GET /api/equipment/statistics`

---

### 8.8 上传器材图片（单张）

**接口**: `POST /api/equipment/upload-image`

**权限**: PRESIDENT, VENUE_MANAGER

**请求类型**: `multipart/form-data`

---

### 8.9 上传器材图片（多张）

**接口**: `POST /api/equipment/upload-images`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 8.10 获取器材图片列表

**接口**: `GET /api/equipment/{equipmentId}/images`

---

### 8.11 删除器材图片

**接口**: `DELETE /api/equipment/image/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 8.12 更新图片排序

**接口**: `PUT /api/equipment/image/{id}/sort`

**权限**: PRESIDENT, VENUE_MANAGER

---

## 9. 器材租借模块

**基础路径**: `/api/equipment/rental`

### 9.1 租借列表

**接口**: `GET /api/equipment/rental/list`

**查询参数**:
- `memberId` (可选): 会员ID
- `equipmentId` (可选): 器材ID
- `status` (可选): 租借状态
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 9.2 租借详情

**接口**: `GET /api/equipment/rental/info/{id}`

---

### 9.3 新增租借

**接口**: `POST /api/equipment/rental/add`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**请求体**:
```json
{
  "memberId": 1,
  "equipmentId": 1,
  "quantity": 2,
  "rentalDate": "2026-01-25",
  "rentalAmount": 40.00,
  "paymentMethod": "BALANCE",
  "status": 1
}
```

**说明**:
- 自动生成租借单号（格式：ER+日期+序号）
- 自动扣减器材可用数量

---

### 9.4 更新租借

**接口**: `PUT /api/equipment/rental/update`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 9.5 删除租借

**接口**: `DELETE /api/equipment/rental/{id}`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 9.6 更新租借状态

**接口**: `PUT /api/equipment/rental/status`

**权限**: PRESIDENT, VENUE_MANAGER

**查询参数**:
- `id`: 租借ID
- `status`: 状态值（0-已取消，1-租借中，2-已归还，3-逾期）

**说明**: 归还时自动恢复器材可用数量

---

### 9.7 租借统计

**接口**: `GET /api/equipment/rental/statistics`

---

### 9.8 处理支付

**接口**: `POST /api/equipment/rental/payment`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 9.9 处理退款

**接口**: `POST /api/equipment/rental/refund`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 9.10 获取器材下拉列表

**接口**: `GET /api/equipment/rental/equipments`

---

### 9.11 获取会员下拉列表

**接口**: `GET /api/equipment/rental/members`

---

## 10. 教练管理模块

**基础路径**: `/api/coach`

### 10.1 教练列表

**接口**: `GET /api/coach/list`

**查询参数**:
- `coachName` (可选): 教练姓名
- `venueId` (可选): 场馆ID
- `status` (可选): 状态（0-停用，1-正常）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 10.2 教练详情

**接口**: `GET /api/coach/info/{id}`

---

### 10.3 新增教练

**接口**: `POST /api/coach/add`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "coachName": "王教练",
  "gender": 1,
  "phone": "13800138000",
  "specialty": "专业羽毛球",
  "experience": "10年教学经验",
  "hourlyPrice": 200.00,
  "rating": 4.9,
  "status": 1
}
```

---

### 10.4 更新教练

**接口**: `PUT /api/coach/update`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 10.5 删除教练

**接口**: `DELETE /api/coach/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 10.6 更新教练状态

**接口**: `PUT /api/coach/status`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 10.7 教练统计

**接口**: `GET /api/coach/statistics`

---

### 10.8 当前教练信息（教练端）

**接口**: `GET /api/coach/me`

**权限**: COACH

**说明**: 返回当前登录教练的档案信息（含场馆名等）。若当前用户为 COACH 但未绑定教练档案，返回 404 或明确错误“未绑定教练档案”。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "coachName": "王教练",
    "venueId": 1,
    "venueName": "某某体育馆",
    "userId": 10,
    "totalStudents": 50,
    ...
  }
}
```

---

### 10.9 教练本人更新档案（教练端）

**接口**: `PUT /api/coach/me`

**权限**: COACH

**请求体**: 仅允许可编辑字段，如 `coachName`、`phone`、`specialty`、`experience`、`avatar`（不含 venueId、status、rating、totalStudents、hourlyPrice 等由管理员维护的字段）。

**说明**: 仅允许教练更新自己的可编辑字段；未绑定时返回错误。

---

### 10.10 未绑定教练档案的 COACH 用户列表（管理端关联账号下拉）

**接口**: `GET /api/coach/unbound-users`

**权限**: PRESIDENT, VENUE_MANAGER

**说明**: 返回 role=COACH 且尚未被任何 sys_coach.user_id 绑定的用户列表（id、username 等），供管理端教练表单“关联账号”下拉使用。

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    { "id": 10, "username": "coach01" },
    { "id": 11, "username": "coach02" }
  ]
}
```

---

## 11. 课程管理模块

**基础路径**: `/api/course`

### 11.1 课程列表

**接口**: `GET /api/course/list`

**查询参数**:
- `courseName` (可选): 课程名称
- `coachId` (可选): 教练ID
- `courtId` (可选): 场地ID
- `status` (可选): 课程状态
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 11.2 课程详情

**接口**: `GET /api/course/info/{id}`

---

### 11.3 新增课程

**接口**: `POST /api/course/add`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "courseName": "初级羽毛球课程",
  "coachId": 1,
  "courtId": 1,
  "coursePrice": 200.00,
  "courseDuration": 60,
  "courseDate": "2026-01-25",
  "startTime": "14:00",
  "endTime": "15:00",
  "maxStudents": 10,
  "currentStudents": 0,
  "status": 1
}
```

---

### 11.4 更新课程

**接口**: `PUT /api/course/update`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 11.5 删除课程

**接口**: `DELETE /api/course/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 11.6 更新课程状态

**接口**: `PUT /api/course/status`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 11.7 课程统计

**接口**: `GET /api/course/statistics`

---

### 11.8 获取教练下拉列表

**接口**: `GET /api/course/coaches`

---

### 11.9 获取场地下拉列表

**接口**: `GET /api/course/courts`

---

### 11.10 当前教练的课程列表（教练端）

**接口**: `GET /api/course/my`

**权限**: COACH

**查询参数**:
- `courtId` (可选): 场地ID
- `status` (可选): 课程状态
- `keyword` (可选): 关键词
- `startTime` (可选): 开始时间（课表时间范围）
- `endTime` (可选): 结束时间（课表时间范围）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

**说明**: 仅返回当前登录教练的课程；教练未绑定时返回空列表。前端课表可通过本接口带 startTime/endTime 获取日期范围内课程。

---

## 12. 课程预约模块

**基础路径**: `/api/course/booking`

### 12.1 课程预约列表

**接口**: `GET /api/course/booking/list`

**查询参数**:
- `memberId` (可选): 会员ID
- `courseId` (可选): 课程ID
- `status` (可选): 预约状态
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 12.2 课程预约详情

**接口**: `GET /api/course/booking/info/{id}`

---

### 12.3 新增课程预约

**接口**: `POST /api/course/booking/add`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**请求体**:
```json
{
  "memberId": 1,
  "courseId": 1,
  "orderAmount": 200.00,
  "paymentMethod": "BALANCE",
  "status": 1
}
```

**说明**:
- 自动生成预约单号（格式：CB+日期+序号）
- 自动检查课程名额
- 自动增加课程当前学员数

---

### 12.4 更新课程预约

**接口**: `PUT /api/course/booking/update`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 12.5 删除课程预约

**接口**: `DELETE /api/course/booking/{id}`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**说明**: 删除时自动减少课程当前学员数

---

### 12.6 当前教练所教课程的预约列表（教练端）

**接口**: `GET /api/course/booking/for-coach`

**权限**: COACH

**查询参数**:
- `status` (可选): 预约状态
- `keyword` (可选): 关键词（预约单号、课程名、会员名等）
- `page` (默认1): 页码
- `size` (默认10): 每页数量

**说明**: 仅返回当前教练所教课程的预约记录；教练未绑定时返回空列表。返回结构与预约列表一致（含会员名、课程名、时间、金额、状态等）。

---

### 12.6 更新预约状态

**接口**: `PUT /api/course/booking/status`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 12.7 课程预约统计

**接口**: `GET /api/course/booking/statistics`

---

### 12.8 处理支付

**接口**: `POST /api/course/booking/payment`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 12.9 处理退款

**接口**: `POST /api/course/booking/refund`

**权限**: PRESIDENT, VENUE_MANAGER

**说明**: 退款时自动减少课程当前学员数

---

### 12.10 获取课程下拉列表

**接口**: `GET /api/course/booking/courses`

---

### 12.11 获取会员下拉列表

**接口**: `GET /api/course/booking/members`

---

## 13. 赛事管理模块

**基础路径**: `/api/tournament`

### 13.1 赛事列表

**接口**: `GET /api/tournament/list`

**查询参数**:
- `tournamentName` (可选): 赛事名称
- `venueId` (可选): 场馆ID
- `tournamentType` (可选): 赛事类型（SINGLE/DOUBLE/MIXED/TEAM）
- `status` (可选): 赛事状态
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 13.2 赛事详情

**接口**: `GET /api/tournament/info/{id}`

---

### 13.3 新增赛事

**接口**: `POST /api/tournament/add`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "tournamentName": "春季羽毛球赛",
  "tournamentType": "DOUBLE",
  "venueId": 1,
  "maxParticipants": 32,
  "currentParticipants": 0,
  "registrationStart": "2026-01-20 00:00:00",
  "registrationEnd": "2026-02-10 23:59:59",
  "tournamentStart": "2026-02-15 09:00:00",
  "tournamentEnd": "2026-02-20 18:00:00",
  "entryFee": 50.00,
  "status": 1
}
```

---

### 13.4 更新赛事

**接口**: `PUT /api/tournament/update`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 13.5 删除赛事

**接口**: `DELETE /api/tournament/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 13.6 更新赛事状态

**接口**: `PUT /api/tournament/status`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 13.7 赛事统计

**接口**: `GET /api/tournament/statistics`

---

### 13.8 获取场馆下拉列表

**接口**: `GET /api/tournament/venues`

---

## 14. 赛事报名模块

**基础路径**: `/api/tournament/registration`

### 14.1 报名列表

**接口**: `GET /api/tournament/registration/list`

**查询参数**:
- `tournamentId` (可选): 赛事ID
- `memberId` (可选): 会员ID
- `status` (可选): 报名状态
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 14.2 报名详情

**接口**: `GET /api/tournament/registration/info/{id}`

---

### 14.3 新增报名

**接口**: `POST /api/tournament/registration/add`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**请求体**:
```json
{
  "tournamentId": 1,
  "memberId": 1,
  "partnerId": 2,
  "entryFee": 50.00,
  "paymentMethod": "BALANCE",
  "status": 1
}
```

**说明**:
- 自动生成报名单号（格式：TR+日期+序号）
- 自动检查赛事名额
- 自动增加赛事当前参赛人数
- 双打/混双支持搭档会员（partnerId）

---

### 14.4 更新报名

**接口**: `PUT /api/tournament/registration/update`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 14.5 删除报名

**接口**: `DELETE /api/tournament/registration/{id}`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**说明**: 删除时自动减少赛事当前参赛人数

---

### 14.6 更新报名状态

**接口**: `PUT /api/tournament/registration/status`

**权限**: PRESIDENT, VENUE_MANAGER, USER

---

### 14.7 报名统计

**接口**: `GET /api/tournament/registration/statistics`

---

### 14.8 处理支付

**接口**: `POST /api/tournament/registration/payment`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 14.9 处理退款

**接口**: `POST /api/tournament/registration/refund`

**权限**: PRESIDENT, VENUE_MANAGER

**说明**: 退款时自动减少赛事当前参赛人数

---

### 14.10 获取赛事下拉列表

**接口**: `GET /api/tournament/registration/tournaments`

---

### 14.11 获取会员下拉列表

**接口**: `GET /api/tournament/registration/members`

---

## 15. 穿线服务模块

**基础路径**: `/api/stringing`

### 15.1 穿线服务列表

**接口**: `GET /api/stringing/list`

**查询参数**:
- `memberId` (可选): 会员ID
- `status` (可选): 服务状态（0-已取消，1-等待穿线，2-正在穿线，3-已完成）
- `serviceNo` (可选): 服务编号
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 15.2 穿线服务详情

**接口**: `GET /api/stringing/info/{id}`

**路径参数**:
- `id`: 穿线服务ID

---

### 15.3 按服务编号查询

**接口**: `GET /api/stringing/info/no/{serviceNo}`

**路径参数**:
- `serviceNo`: 服务编号

**说明**: 用于快速查询服务状态

---

### 15.4 新增穿线服务

**接口**: `POST /api/stringing/add`

**权限**: PRESIDENT, VENUE_MANAGER, USER

**请求体**:
```json
{
  "memberId": 1,
  "racketBrand": "YONEX",
  "racketModel": "ARC-11",
  "stringId": 1,
  "tension": 26,
  "isOwnString": false,
  "remark": "主线26磅，副线24磅",
  "servicePrice": 50.00,
  "paymentMethod": "ALIPAY",
  "status": 1
}
```

**说明**:
- 自动生成服务编号（格式：ST+日期+序号）
- 支持自带线材选项

---

### 15.5 更新穿线服务状态

**接口**: `PUT /api/stringing/status`

**权限**: PRESIDENT, VENUE_MANAGER

**查询参数**:
- `id`: 穿线服务ID
- `status`: 状态值（0-已取消，1-等待穿线，2-正在穿线，3-已完成）

**说明**: 状态变更时自动记录操作日志

---

### 15.6 更新穿线服务（完整信息）

**接口**: `PUT /api/stringing/update`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "id": 1,
  "remark": "已更换线材",
  "status": 3
}
```

---

### 15.7 删除穿线服务记录

**接口**: `DELETE /api/stringing/{id}`

**权限**: PRESIDENT, VENUE_MANAGER

**路径参数**:
- `id`: 穿线服务ID

---

### 15.8 获取线材列表

**接口**: `GET /api/stringing/strings`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "stringName": "BG80",
      "brand": "YONEX",
      "thickness": 0.80,
      "price": 25.00
    }
  ]
}
```

---

### 15.9 计算穿线价格

**接口**: `GET /api/stringing/calculate-price`

**查询参数**:
- `stringId`: 线材ID
- `isOwnString`: 是否自带线材（false/true）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stringPrice": 25.00,
    "servicePrice": 25.00,
    "totalPrice": 50.00
  }
}
```

---

### 15.10 穿线服务统计

**接口**: `GET /api/stringing/statistics`

---

## 16. 通知管理模块

**基础路径**: `/api/notifications`

### 16.1 通知列表

**接口**: `GET /api/notifications`

**权限**: 需要认证

**查询参数**:
- `page` (默认1): 页码
- `size` (默认20): 每页数量

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "data": [
      {
        "id": 1,
        "title": "系统通知",
        "content": "通知内容",
        "type": "SYSTEM",
        "createTime": "2026-02-08T10:00:00"
      }
    ],
    "total": 10,
    "page": 1,
    "size": 20,
    "pages": 1
  }
}
```

---

### 16.2 通知详情

**接口**: `GET /api/notifications/{id}`

**权限**: 需要认证

**路径参数**:
- `id`: 通知ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "系统通知",
    "content": "通知内容",
    "type": "SYSTEM",
    "createTime": "2026-02-08T10:00:00"
  }
}
```

---

### 16.3 发布通知

**接口**: `POST /api/notifications`

**权限**: 需要会长或场馆管理员权限

**请求体**:
```json
{
  "title": "系统通知",
  "content": "通知内容",
  "type": "SYSTEM"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "通知发布成功",
  "data": {
    "id": 1,
    "title": "系统通知",
    "content": "通知内容",
    "type": "SYSTEM",
    "createTime": "2026-02-08T10:00:00"
  }
}
```

---

### 16.4 删除通知

**接口**: `DELETE /api/notifications/{id}`

**权限**: 需要会长或场馆管理员权限

**路径参数**:
- `id`: 通知ID

**响应示例**:
```json
{
  "code": 200,
  "message": "通知删除成功"
}
```

---

## 17. 充值管理模块

**基础路径**: `/api/recharge`

### 15.1 用户自助充值

**接口**: `POST /api/recharge/user`

**权限**: 需要认证

**请求体**:
```json
{
  "memberId": 1,
  "rechargeAmount": 300.00,
  "rechargeMethod": "ALIPAY",
  "remark": "用户自助充值"
}
```

**说明**:
- 用户只能为自己充值
- 单次充值金额≥200元时自动升级为会员

---

### 15.2 管理员代充

**接口**: `POST /api/recharge/admin`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "memberId": 1,
  "rechargeAmount": 500.00,
  "rechargeMethod": "CASH",
  "remark": "管理员代充"
}
```

---

### 15.3 充值记录列表

**接口**: `GET /api/recharge/records`

**查询参数**:
- `memberId` (可选): 会员ID
- `startDate` (可选): 开始日期
- `endDate` (可选): 结束日期
- `page` (默认1): 页码
- `size` (默认10): 每页数量

---

### 15.4 会员充值记录

**接口**: `GET /api/recharge/records/{memberId}`

**路径参数**:
- `memberId`: 会员ID

---

## 18. 财务管理模块

**基础路径**: `/api/finance`

**权限要求**: 仅协会会长（PRESIDENT）和场馆管理者（VENUE_MANAGER）

### 16.1 财务记录列表

**接口**: `GET /api/finance/list`

**查询参数**:
- `venueId` (可选): 场馆ID
- `businessType` (可选): 业务类型（BOOKING/COURSE/EQUIPMENT/TOURNAMENT/RECHARGE/OTHER）
- `incomeExpenseType` (可选): 收支类型（0-支出，1-收入）
- `startDate` (可选): 开始日期
- `endDate` (可选): 结束日期
- `keyword` (可选): 关键词搜索
- `page` (默认1): 页码
- `size` (默认10): 每页数量

**权限说明**:
- 协会会长：查看所有场馆的财务记录
- 场馆管理者：仅查看自己场馆的财务记录

---

### 16.2 财务记录详情

**接口**: `GET /api/finance/info/{id}`

---

### 16.3 新增财务记录

**接口**: `POST /api/finance/add`

**权限**: PRESIDENT, VENUE_MANAGER

**请求体**:
```json
{
  "businessType": "OTHER",
  "incomeExpenseType": 1,
  "amount": 1000.00,
  "paymentMethod": "CASH",
  "venueId": 1,
  "remark": "其他收入"
}
```

**说明**:
- 自动生成财务单号（格式：FN+日期+序号）

---

### 16.4 更新财务记录

**接口**: `PUT /api/finance/update`

**权限**: PRESIDENT, VENUE_MANAGER

---

### 16.5 删除财务记录

**接口**: `DELETE /api/finance/{id}`

**权限**: 仅协会会长（PRESIDENT）

---

### 16.6 财务统计概览

**接口**: `GET /api/finance/statistics`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalIncome": 50000.00,
    "totalExpense": 20000.00,
    "netIncome": 30000.00,
    "monthIncome": 15000.00,
    "monthExpense": 5000.00,
    "incomeGrowthRate": 15.5,
    "expenseGrowthRate": 8.2
  }
}
```

---

### 16.7 财务趋势数据

**接口**: `GET /api/finance/trend`

**查询参数**:
- `period` (可选): 时间周期（day/week/month，默认day）
- `startDate` (可选): 开始日期
- `endDate` (可选): 结束日期
- `venueId` (可选): 场馆ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categories": ["2026-01-20", "2026-01-21", "2026-01-22"],
    "income": [1000, 1500, 2000],
    "expense": [500, 600, 700]
  }
}
```

---

### 16.8 业务类型占比

**接口**: `GET /api/finance/ratio`

**查询参数**:
- `startDate` (可选): 开始日期
- `endDate` (可选): 结束日期
- `venueId` (可选): 场馆ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "name": "场地预约",
      "value": 30000,
      "percentage": 60.0
    },
    {
      "name": "课程预约",
      "value": 15000,
      "percentage": 30.0
    }
  ]
}
```

---

### 16.9 获取场馆下拉列表

**接口**: `GET /api/finance/venues`

**权限**: 仅协会会长（PRESIDENT）

**说明**: 返回所有场馆列表，供财务筛选使用

---

### 16.10 获取业务类型选项

**接口**: `GET /api/finance/businessTypes`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {"value": "BOOKING", "label": "场地预约"},
    {"value": "COURSE", "label": "课程预约"},
    {"value": "EQUIPMENT", "label": "器材租借"},
    {"value": "TOURNAMENT", "label": "赛事报名"},
    {"value": "RECHARGE", "label": "会员充值"},
    {"value": "OTHER", "label": "其他收支"}
  ]
}
```

---

## 附录

### A. 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 403 | 无权限访问 |
| 500 | 服务器错误 |

### B. 业务类型枚举

| 值 | 说明 |
|---|------|
| BOOKING | 场地预约 |
| COURSE | 课程预约 |
| EQUIPMENT | 器材租借 |
| TOURNAMENT | 赛事报名 |
| RECHARGE | 会员充值 |
| OTHER | 其他收支 |

### C. 支付方式枚举

| 值 | 说明 |
|---|------|
| CASH | 现金 |
| ALIPAY | 支付宝 |
| WECHAT | 微信 |
| BALANCE | 余额 |

### D. 预约状态枚举

| 值 | 说明 |
|---|------|
| 0 | 已取消 |
| 1 | 待支付 |
| 2 | 已支付 |
| 3 | 进行中 |
| 4 | 已完成 |

### E. 场地状态枚举

| 值 | 说明 |
|---|------|
| 0 | 维护中 |
| 1 | 空闲 |
| 2 | 预约中 |
| 3 | 使用中 |

### F. 场馆状态枚举

| 值 | 说明 |
|---|------|
| 0 | 关闭 |
| 1 | 营业中 |
| 2 | 暂停 |

### G. 会员类型枚举

| 值 | 说明 |
|---|------|
| NORMAL | 普通用户 |
| MEMBER | 会员 |

### H. 会员状态枚举

| 值 | 说明 |
|---|------|
| 0 | 冻结 |
| 1 | 正常 |
| 2 | 到期 |

---

**文档维护者**: BMP开发团队  
**最后更新**: 2026-02-09  
**版本**: v1.3.0
