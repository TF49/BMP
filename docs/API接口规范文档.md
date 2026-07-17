# BMP 三智能体 API 规范文档

> 文档版本：v1.0
> 创建日期：2026-07-17
> 适用项目：羽擎（Badminton Management Platform，BMP）
> 文档状态：开发中

## 1. API 概览

### 1.1 API 分类

BMP 三智能体系统包含两类 API：

1. **Agent 网关 API**：面向前端用户，提供对话和会话管理
2. **Tool API**：面向 Agent 服务，提供业务工具调用

### 1.2 通信协议

- **协议**：HTTP/1.1, HTTP/2
- **数据格式**：JSON
- **字符编码**：UTF-8
- **时区**：UTC，所有时间字段使用 ISO 8601 格式

### 1.3 通用响应格式

所有 API 响应遵循统一格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "trace_id": "abc123def456"
}
```

#### 错误码规范

| 错误码 | 说明 | HTTP 状态码 |
| --- | --- | --- |
| 0 | 成功 | 200 |
| 1001 | 参数校验失败 | 400 |
| 1002 | 业务规则违反 | 400 |
| 1003 | 资源不存在 | 404 |
| 2001 | 未授权 | 401 |
| 2002 | 权限不足 | 403 |
| 2003 | 会话过期 | 401 |
| 3001 | 请求过于频繁 | 429 |
| 3002 | 服务不可用 | 503 |
| 5001 | 内部服务错误 | 500 |
| 5002 | 第三方服务错误 | 502 |

## 2. Agent 网关 API

### 2.1 创建会话

创建一个新的 Agent 会话。

#### 请求

```http
POST /api/agent/conversations
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

#### 请求体

```json
{
  "agent_type": "booking",
  "venue_id": "venue_123"
}
```

#### 参数说明

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| agent_type | string | 是 | Agent 类型：booking, analytics, support |
| venue_id | string | 否 | 场馆 ID，某些 Agent 需要 |

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversation_id": "conv_abc123",
    "agent_type": "booking",
    "thread_id": "thread_xyz789",
    "created_at": "2026-07-17T10:30:00Z",
    "expires_at": "2026-07-17T11:30:00Z"
  },
  "trace_id": "trace_123"
}
```

### 2.2 发送消息

向会话发送用户消息。

#### 请求

```http
POST /api/agent/conversations/{conversation_id}/messages
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

#### 请求体

```json
{
  "content": "我想明天下午3点预订一个场地",
  "message_id": "msg_123"
}
```

#### 参数说明

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| content | string | 是 | 用户消息内容 |
| message_id | string | 否 | 消息唯一标识，用于去重 |

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "message_id": "msg_123",
    "response": "好的，我来帮您查询明天下午3点的可用场地。请问您希望在哪个场馆预订？",
    "type": "text",
    "requires_action": false,
    "actions": [],
    "references": [],
    "metadata": {
      "model": "gpt-4",
      "tokens_used": 150,
      "duration_ms": 1200
    }
  },
  "trace_id": "trace_456"
}
```

#### 响应类型说明

| type | 说明 |
| --- | --- |
| text | 纯文本响应 |
| card | 结构化卡片数据 |
| confirmation | 需要用户确认的动作 |
| error | 错误信息 |

### 2.3 确认动作

确认一个待执行的动作。

#### 请求

```http
POST /api/agent/conversations/{conversation_id}/actions/{action_id}/confirm
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

#### 请求体

```json
{
  "idempotency_key": "idemp_abc123"
}
```

#### 参数说明

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| idempotency_key | string | 是 | 幂等键，防止重复操作 |

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "action_id": "action_123",
    "status": "confirmed",
    "result": {
      "booking_id": "booking_456",
      "order_id": "order_789",
      "amount": 50.00,
      "expires_at": "2026-07-17T11:00:00Z"
    }
  },
  "trace_id": "trace_789"
}
```

### 2.4 拒绝动作

拒绝一个待执行的动作。

#### 请求

```http
POST /api/agent/conversations/{conversation_id}/actions/{action_id}/reject
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "action_id": "action_123",
    "status": "rejected"
  },
  "trace_id": "trace_890"
}
```

### 2.5 查询会话

查询会话摘要信息。

#### 请求

```http
GET /api/agent/conversations/{conversation_id}
Authorization: Bearer {jwt_token}
```

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "conversation_id": "conv_abc123",
    "agent_type": "booking",
    "created_at": "2026-07-17T10:30:00Z",
    "last_activity_at": "2026-07-17T10:35:00Z",
    "message_count": 5,
    "status": "active"
  },
  "trace_id": "trace_901"
}
```

### 2.6 删除会话

删除会话及其相关数据。

#### 请求

```http
DELETE /api/agent/conversations/{conversation_id}
Authorization: Bearer {jwt_token}
```

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": null,
  "trace_id": "trace_012"
}
```

## 3. Tool API

### 3.1 Tool API 认证

Tool API 使用双重认证机制：

1. **服务认证**：通过 `X-Agent-Service-Key` 头部验证服务身份
2. **上下文签名**：通过 `X-Agent-Context-Signature` 头部验证用户上下文

#### 请求头示例

```http
X-Agent-Service-Key: service_key_abc123
X-Agent-Context-Signature: signature_xyz789
X-Agent-Trace-Id: trace_123
X-Agent-User-Id: user_456
X-Agent-User-Role: MEMBER
X-Agent-Venue-Id: venue_789
```

### 3.2 智能预订 Tool

#### 3.2.1 查询营业场馆

查询用户可访问的营业场馆列表。

##### 请求

```http
GET /api/agent-tools/venues
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "venues": [
      {
        "id": "venue_123",
        "name": "羽球中心",
        "address": "北京市朝阳区",
        "business_hours": {
          "open": "09:00",
          "close": "22:00"
        },
        "status": "open"
      }
    ]
  },
  "trace_id": "trace_123"
}
```

#### 3.2.2 查询可用场地

查询指定时间段的可用场地。

##### 请求

```http
GET /api/agent-tools/courts/availability?venue_id=venue_123&date=2026-07-18&start_time=15:00&end_time=16:00&mode=single
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 查询参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| venue_id | string | 是 | 场馆 ID |
| date | string | 是 | 日期，格式：YYYY-MM-DD |
| start_time | string | 是 | 开始时间，格式：HH:mm |
| end_time | string | 是 | 结束时间，格式：HH:mm |
| mode | string | 否 | 预约模式：single, double, training |

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "available_courts": [
      {
        "court_id": "court_1",
        "court_name": "1号场地",
        "court_type": "塑胶",
        "price": 50.00
      }
    ],
    "alternatives": [
      {
        "suggestion": "15:00-16:00 已满，建议 14:00-15:00",
        "date": "2026-07-18",
        "start_time": "14:00",
        "end_time": "15:00"
      }
    ]
  },
  "trace_id": "trace_456"
}
```

#### 3.2.3 生成预约报价

生成预约报价，不创建实际订单。

##### 请求

```http
POST /api/agent-tools/bookings/quote
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
Content-Type: application/json
```

##### 请求体

```json
{
  "venue_id": "venue_123",
  "court_id": "court_1",
  "date": "2026-07-18",
  "start_time": "15:00",
  "end_time": "16:00",
  "mode": "single"
}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "quote_id": "quote_123",
    "venue": {
      "id": "venue_123",
      "name": "羽球中心"
    },
    "court": {
      "id": "court_1",
      "name": "1号场地"
    },
    "time_slot": {
      "date": "2026-07-18",
      "start_time": "15:00",
      "end_time": "16:00"
    },
    "pricing": {
      "base_price": 50.00,
      "discount": 0.00,
      "final_price": 50.00,
      "currency": "CNY"
    },
    "expires_at": "2026-07-17T11:00:00Z"
  },
  "trace_id": "trace_789"
}
```

#### 3.2.4 创建预约

创建预约订单。

##### 请求

```http
POST /api/agent-tools/bookings
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
X-Agent-Idempotency-Key: {idempotency_key}
Content-Type: application/json
```

##### 请求体

```json
{
  "quote_id": "quote_123",
  "action_id": "action_456"
}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "booking_id": "booking_789",
    "order_id": "order_012",
    "status": "pending_payment",
    "amount": 50.00,
    "expires_at": "2026-07-17T11:30:00Z"
  },
  "trace_id": "trace_012"
}
```

#### 3.2.5 查询预约结果

查询预约结果。

##### 请求

```http
GET /api/agent-tools/bookings/{booking_id}
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "booking_id": "booking_789",
    "venue": {
      "id": "venue_123",
      "name": "羽球中心"
    },
    "court": {
      "id": "court_1",
      "name": "1号场地"
    },
    "time_slot": {
      "date": "2026-07-18",
      "start_time": "15:00",
      "end_time": "16:00"
    },
    "status": "confirmed",
    "amount": 50.00
  },
  "trace_id": "trace_345"
}
```

### 3.3 经营分析 Tool

#### 3.3.1 经营总览

获取经营总览数据。

##### 请求

```http
GET /api/agent-tools/analytics/summary?venue_id=venue_123&start_date=2026-07-01&end_date=2026-07-17
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 查询参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| venue_id | string | 否 | 场馆 ID，不填则返回所有场馆 |
| start_date | string | 是 | 开始日期，格式：YYYY-MM-DD |
| end_date | string | 是 | 结束日期，格式：YYYY-MM-DD |

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "period": {
      "start_date": "2026-07-01",
      "end_date": "2026-07-17"
    },
    "metrics": {
      "total_bookings": 1500,
      "total_revenue": 75000.00,
      "occupancy_rate": 0.75,
      "average_duration": 1.5
    },
    "venues": [
      {
        "venue_id": "venue_123",
        "venue_name": "羽球中心",
        "bookings": 800,
        "revenue": 40000.00,
        "occupancy_rate": 0.80
      }
    ]
  },
  "trace_id": "trace_678"
}
```

#### 3.3.2 预约趋势

获取预约趋势数据。

##### 请求

```http
GET /api/agent-tools/analytics/booking-trend?venue_id=venue_123&start_date=2026-07-01&end_date=2026-07-17&granularity=daily
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 查询参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| venue_id | string | 否 | 场馆 ID |
| start_date | string | 是 | 开始日期 |
| end_date | string | 是 | 结束日期 |
| granularity | string | 否 | 粒度：daily, weekly, monthly |

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "trend": [
      {
        "date": "2026-07-01",
        "bookings": 85,
        "revenue": 4250.00
      },
      {
        "date": "2026-07-02",
        "bookings": 92,
        "revenue": 4600.00
      }
    ]
  },
  "trace_id": "trace_901"
}
```

#### 3.3.3 场地热力图

获取场地使用热力图数据。

##### 请求

```http
GET /api/agent-tools/analytics/occupancy-heatmap?venue_id=venue_123&date=2026-07-17
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "date": "2026-07-17",
    "heatmap": [
      {
        "court_id": "court_1",
        "court_name": "1号场地",
        "hourly_occupancy": [
          {"hour": 9, "occupancy": 0.2},
          {"hour": 10, "occupancy": 0.8},
          {"hour": 11, "occupancy": 1.0}
        ]
      }
    ]
  },
  "trace_id": "trace_234"
}
```

#### 3.3.4 财务趋势

获取财务趋势数据。

##### 请求

```http
GET /api/agent-tools/analytics/finance-trend?venue_id=venue_123&start_date=2026-07-01&end_date=2026-07-17
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "trend": [
      {
        "date": "2026-07-01",
        "revenue": 4250.00,
        "cost": 850.00,
        "profit": 3400.00
      }
    ]
  },
  "trace_id": "trace_567"
}
```

#### 3.3.5 业务收入构成

获取业务收入构成数据。

##### 请求

```http
GET /api/agent-tools/analytics/business-ratio?venue_id=venue_123&start_date=2026-07-01&end_date=2026-07-17
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "composition": [
      {
        "category": "场地预订",
        "amount": 60000.00,
        "ratio": 0.80
      },
      {
        "category": "器材租借",
        "amount": 10000.00,
        "ratio": 0.13
      },
      {
        "category": "培训课程",
        "amount": 5000.00,
        "ratio": 0.07
      }
    ]
  },
  "trace_id": "trace_890"
}
```

#### 3.3.6 场馆对比

获取场馆对比数据（仅会长）。

##### 请求

```http
GET /api/agent-tools/analytics/venue-comparison?start_date=2026-07-01&end_date=2026-07-17
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: PRESIDENT
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "comparison": [
      {
        "venue_id": "venue_123",
        "venue_name": "羽球中心",
        "revenue": 40000.00,
        "occupancy_rate": 0.80,
        "rank": 1
      },
      {
        "venue_id": "venue_456",
        "venue_name": "羽毛球馆",
        "revenue": 35000.00,
        "occupancy_rate": 0.75,
        "rank": 2
      }
    ]
  },
  "trace_id": "trace_012"
}
```

### 3.4 场馆客服 Tool

#### 3.4.1 查询场馆实时信息

查询场馆实时营业信息。

##### 请求

```http
GET /api/agent-tools/support/venues/{venue_id}
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "venue_id": "venue_123",
    "venue_name": "羽球中心",
    "status": "open",
    "business_hours": {
      "open": "09:00",
      "close": "22:00"
    },
    "current_time": "2026-07-17T10:30:00Z",
    "available_courts": 8,
    "total_courts": 10
  },
  "trace_id": "trace_345"
}
```

#### 3.4.2 查询当前服务价格

查询场馆当前价格信息。

##### 请求

```http
GET /api/agent-tools/support/venues/{venue_id}/prices
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "venue_id": "venue_123",
    "price_list": [
      {
        "court_type": "塑胶",
        "time_slot": "工作日 09:00-18:00",
        "price": 40.00,
        "unit": "小时"
      },
      {
        "court_type": "塑胶",
        "time_slot": "工作日 18:00-22:00",
        "price": 60.00,
        "unit": "小时"
      },
      {
        "court_type": "塑胶",
        "time_slot": "周末 09:00-22:00",
        "price": 80.00,
        "unit": "小时"
      }
    ],
    "updated_at": "2026-07-01T00:00:00Z"
  },
  "trace_id": "trace_678"
}
```

#### 3.4.3 创建人工咨询单

创建人工客服转接单。

##### 请求

```http
POST /api/agent-tools/support/handoffs
X-Agent-Service-Key: {service_key}
X-Agent-Context-Signature: {signature}
X-Agent-Trace-Id: {trace_id}
X-Agent-User-Id: {user_id}
X-Agent-User-Role: {role}
X-Agent-Idempotency-Key: {idempotency_key}
Content-Type: application/json
```

##### 请求体

```json
{
  "venue_id": "venue_123",
  "topic": "退款问题",
  "description": "用户对退款政策有疑问",
  "conversation_id": "conv_abc123"
}
```

##### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "handoff_id": "handoff_123",
    "status": "pending",
    "estimated_wait_time": 5,
    "created_at": "2026-07-17T10:30:00Z"
  },
  "trace_id": "trace_901"
}
```

## 4. FastAPI 内部 API

### 4.1 Agent 调用接口

Spring Boot Agent 网关调用 FastAPI Agent 服务的内部接口。

#### 4.1.1 处理消息

##### 请求

```http
POST /api/v1/agent/process
X-Agent-Context-Token: {context_token}
Content-Type: application/json
```

##### 请求体

```json
{
  "conversation_id": "conv_abc123",
  "agent_type": "booking",
  "message": {
    "content": "我想明天下午3点预订一个场地",
    "message_id": "msg_123"
  },
  "user_context": {
    "user_id": "user_456",
    "role": "MEMBER",
    "venue_id": "venue_123"
  }
}
```

##### 响应

```json
{
  "response": "好的，我来帮您查询明天下午3点的可用场地。请问您希望在哪个场馆预订？",
  "type": "text",
  "requires_action": false,
  "actions": [],
  "references": [],
  "metadata": {
    "model": "gpt-4",
    "tokens_used": 150,
    "duration_ms": 1200,
    "tool_calls": []
  }
}
```

### 4.2 健康检查

#### 请求

```http
GET /health
```

#### 响应

```json
{
  "status": "healthy",
  "timestamp": "2026-07-17T10:30:00Z",
  "services": {
    "llm": "healthy",
    "database": "healthy",
    "redis": "healthy",
    "java_tools": "healthy"
  }
}
```

## 5. 错误处理

### 5.1 参数校验错误

```json
{
  "code": 1001,
  "message": "参数校验失败",
  "data": {
    "errors": [
      {
        "field": "date",
        "message": "日期格式不正确，应为 YYYY-MM-DD"
      },
      {
        "field": "start_time",
        "message": "开始时间不能晚于结束时间"
      }
    ]
  },
  "trace_id": "trace_123"
}
```

### 5.2 权限错误

```json
{
  "code": 2002,
  "message": "权限不足",
  "data": {
    "required_role": "VENUE_MANAGER",
    "current_role": "USER"
  },
  "trace_id": "trace_456"
}
```

### 5.3 业务规则错误

```json
{
  "code": 1002,
  "message": "业务规则违反",
  "data": {
    "rule": "场地已被预订",
    "suggestion": "建议选择其他时间段或场地"
  },
  "trace_id": "trace_789"
}
```

### 5.4 服务不可用

```json
{
  "code": 3002,
  "message": "服务暂时不可用",
  "data": {
    "retry_after": 60,
    "fallback_url": "/booking"
  },
  "trace_id": "trace_012"
}
```

## 6. 限流规范

### 6.1 限流维度

| 维度 | 限制 |
| --- | --- |
| 用户对话频率 | 10 次/分钟 |
| 用户并发会话数 | 5 个 |
| 每日模型预算 | 100,000 tokens/用户 |
| IP 请求频率 | 100 次/分钟 |
| Tool 写操作 | 5 次/分钟 |
| Tool 读操作 | 50 次/分钟 |

### 6.2 限流响应

```json
{
  "code": 3001,
  "message": "请求过于频繁",
  "data": {
    "limit": 10,
    "remaining": 0,
    "reset": "2026-07-17T10:31:00Z"
  },
  "trace_id": "trace_345"
}
```

## 7. 版本管理

### 7.1 API 版本

当前版本：v1

未来版本将通过 URL 路径进行区分：
- v1: `/api/v1/...`
- v2: `/api/v2/...`

### 7.2 兼容性策略

- **向后兼容**：新增字段不影响现有客户端
- **弃用通知**：通过响应头 `X-API-Deprecation` 通知
- **弃用期限**：至少保留 6 个月
- **破坏性变更**：通过主版本号升级

## 8. 测试用例

### 8.1 Agent 网关 API 测试

#### 创建会话测试

```bash
# 正常创建会话
curl -X POST http://localhost:8080/api/agent/conversations \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"agent_type": "booking", "venue_id": "venue_123"}'

# 缺少必填参数
curl -X POST http://localhost:8080/api/agent/conversations \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"venue_id": "venue_123"}'

# 无效的 agent_type
curl -X POST http://localhost:8080/api/agent/conversations \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"agent_type": "invalid", "venue_id": "venue_123"}'
```

### 8.2 Tool API 测试

#### 查询可用场地测试

```bash
# 正常查询
curl -X GET "http://localhost:8080/api/agent-tools/courts/availability?venue_id=venue_123&date=2026-07-18&start_time=15:00&end_time=16:00" \
  -H "X-Agent-Service-Key: $SERVICE_KEY" \
  -H "X-Agent-Context-Signature: $SIGNATURE" \
  -H "X-Agent-Trace-Id: trace_123" \
  -H "X-Agent-User-Id: user_456" \
  -H "X-Agent-User-Role: MEMBER"

# 缺少服务认证
curl -X GET "http://localhost:8080/api/agent-tools/courts/availability?venue_id=venue_123&date=2026-07-18&start_time=15:00&end_time=16:00" \
  -H "X-Agent-Trace-Id: trace_123" \
  -H "X-Agent-User-Id: user_456" \
  -H "X-Agent-User-Role: MEMBER"

# 无效的签名
curl -X GET "http://localhost:8080/api/agent-tools/courts/availability?venue_id=venue_123&date=2026-07-18&start_time=15:00&end_time=16:00" \
  -H "X-Agent-Service-Key: $SERVICE_KEY" \
  -H "X-Agent-Context-Signature: invalid_signature" \
  -H "X-Agent-Trace-Id: trace_123" \
  -H "X-Agent-User-Id: user_456" \
  -H "X-Agent-User-Role: MEMBER"
```

## 9. 附录

### 9.1 HTTP 状态码映射

| HTTP 状态码 | 错误码 | 说明 |
| --- | --- | --- |
| 200 | 0 | 成功 |
| 400 | 1001, 1002 | 参数或业务错误 |
| 401 | 2001, 2003 | 认证失败 |
| 403 | 2002 | 权限不足 |
| 404 | 1003 | 资源不存在 |
| 429 | 3001 | 请求过于频繁 |
| 500 | 5001 | 内部服务错误 |
| 502 | 5002 | 第三方服务错误 |
| 503 | 3002 | 服务不可用 |

### 9.2 数据类型规范

| 类型 | 格式 | 示例 |
| --- | --- | --- |
| string | UTF-8 字符串 | "venue_123" |
| integer | 64位整数 | 123456 |
| number | 浮点数，保留2位小数 | 50.00 |
| boolean | true/false | true |
| datetime | ISO 8601 | "2026-07-17T10:30:00Z" |
| date | YYYY-MM-DD | "2026-07-17" |
| time | HH:mm | "15:00" |
| array | JSON 数组 | [1, 2, 3] |
| object | JSON 对象 | {"key": "value"} |

### 9.3 ID 生成规范

| ID 类型 | 前缀 | 示例 |
| --- | --- | --- |
| 会话 ID | conv_ | conv_abc123 |
| 消息 ID | msg_ | msg_123 |
| 动作 ID | action_ | action_456 |
| 报价 ID | quote_ | quote_789 |
| 预约 ID | booking_ | booking_012 |
| 订单 ID | order_ | order_345 |
| 转接 ID | handoff_ | handoff_678 |
| 追踪 ID | trace_ | trace_901 |
