# BMP 三智能体 API 规范文档

> 文档版本：v1.4
> 创建日期：2026-07-17
> 更新日期：2026-07-23
> 适用项目：羽擎（Badminton Management Platform，BMP）
> 文档状态：P1/P2-01~P2-05 已完成（P2-05 框架与测试优化已完成，P3 准备中）

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
- **全链路追踪**：HTTP 请求头 `X-Agent-Trace-Id` 用于在 Web / UniApp / Java API 网关 / FastAPI Agent / Tool 间透传统一 Trace ID

### 1.3 通用响应格式

所有 API 响应遵循统一格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "trace_id": "abc123def456"
}
```

#### 错误码规范

| 错误码 | 说明 | HTTP 状态码 |
| --- | --- | --- |
| 200 | 成功 | 200 |
| 400 | 参数校验或业务规则违反 | 400 |
| 401 | 未授权或会话过期 | 401 |
| 403 | 权限不足 | 403 |
| 404 | 资源不存在 | 404 |
| 429 | 请求过于频繁 | 429 |
| 500 | 内部服务错误 | 500 |
| 502 | 第三方服务错误 | 502 |
| 503 | 服务不可用 | 503 |
| 504 | 第三方服务超时 | 504 |

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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
  "message": "success",
  "data": null,
  "trace_id": "trace_012"
}
```

## 3. Tool API

### 3.1 Tool API 认证

Tool API 使用双重认证机制：

1. **服务认证**：通过 `X-Agent-Service-Token` 头部验证服务身份
2. **上下文签名**：通过 `X-Agent-Context-Token` 头部验证短期签名用户上下文

#### 请求头示例

```http
X-Agent-Service-Token: service_token_abc123
X-Agent-Context-Token: {base64url_payload}.{base64url_hmac}
X-Agent-Trace-Id: trace_123
```

用户 ID、角色、场馆范围、过期时间和 nonce 只能来自签名上下文，禁止使用独立请求头覆盖。

### 3.2 智能预订 Tool

#### 3.2.1 查询营业场馆

查询用户可访问的营业场馆列表。

##### 请求

```http
GET /api/agent-tools/venues?status=1&limit=20
X-Agent-Service-Token: {service_token}
X-Agent-Context-Token: {context_token}
X-Agent-Trace-Id: {trace_id}
```

##### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "venueId": 123,
      "venueName": "羽球中心",
      "address": "北京市朝阳区",
      "businessHours": "09:00-22:00",
      "status": 1,
      "statusText": "营业中"
    }
  ]
}
```

#### 3.2.2 查询可用场地

查询指定时间段的可用场地。

##### 请求

```http
GET /api/agent-tools/courts/availability?venueId=123&date=2026-07-22&startTime=15:00&endTime=16:00&limit=20
X-Agent-Service-Token: {service_token}
X-Agent-Context-Token: {context_token}
X-Agent-Trace-Id: {trace_id}
```

##### 查询参数

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| venueId | integer | 是 | 场馆 ID，必须大于 0 |
| date | string | 是 | 日期，格式：YYYY-MM-DD |
| startTime | string | 是 | 开始时间，格式：HH:mm |
| endTime | string | 是 | 结束时间，格式：HH:mm |
| limit | integer | 否 | 返回数量，1 至 50，默认 20 |

##### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "courtId": 1,
      "courtCode": "C-01",
      "courtName": "1号场地",
      "venueId": 123,
      "billingType": "HOUR",
      "pricePerHour": 50.00,
      "available": true,
      "date": "2026-07-22",
      "startTime": "15:00",
      "endTime": "16:00"
    }
  ]
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
  "code": 200,
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
X-Agent-Trace-Id: {trace_id}
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
  }
}
```

用户 ID、角色和场馆范围只能来自 `X-Agent-Context-Token` 的验证结果。请求体不得携带或覆盖用户身份。

##### 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "response": "Mock Agent 已接收消息。",
    "type": "text",
    "requires_action": false,
    "actions": [],
    "references": [],
    "metadata": {
      "model": "mock",
      "tokens_used": 0,
      "duration_ms": 1,
      "tool_calls": [],
      "turn": 1
    }
  },
  "trace_id": "trace_123"
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
  "code": 200,
  "message": "success",
  "data": {
    "status": "healthy",
    "timestamp": "2026-07-17T10:30:00Z",
    "services": {
      "app": "healthy",
      "llm": "disabled",
      "database": "disabled",
      "redis": "disabled"
    }
  },
  "trace_id": "trace_123"
}
```

### 4.3 大模型 Responses API

FastAPI Agent 服务通过 OpenAI Python SDK 调用 OpenAI-compatible Responses API。业务 Agent 只依赖项目内的模型抽象，不直接拼接供应商请求。

#### 4.3.1 环境变量

| 环境变量 | 当前配置 | 必填 | 说明 |
| --- | --- | --- | --- |
| `OPENAI_BASE_URL` | `https://bobdong.cn/v1` | 是 | SDK 基础地址，不包含末尾的 `/responses` |
| `OPENAI_API_KEY` | 不在文档中保存 | 是 | 仅写入本地或部署环境的密钥管理系统，不得提交到 Git、日志或接口响应 |
| `OPENAI_MODEL` | `gpt-5.6-luna` | 是 | Responses API 使用的模型标识 |
| `LLM_TIMEOUT_SECONDS` | `30` | 是 | 单次模型请求超时时间，单位为秒 |

SDK 根据 `OPENAI_BASE_URL` 自动生成最终请求地址：

```text
https://bobdong.cn/v1/responses
```

#### 4.3.2 文本生成请求

```http
POST /v1/responses HTTP/1.1
Host: bobdong.cn
Authorization: Bearer {OPENAI_API_KEY}
Content-Type: application/json
```

```json
{
  "model": "gpt-5.6-luna",
  "input": [
    {
      "role": "user",
      "content": "请回复 OK"
    }
  ]
}
```

#### 4.3.3 结构化输出请求

需要 Pydantic 模型校验的调用增加 `text.format` 参数：

```json
{
  "model": "gpt-5.6-luna",
  "input": [
    {
      "role": "user",
      "content": "返回 JSON，status 必须为 OK"
    }
  ],
  "text": {
    "format": {
      "type": "json_object"
    }
  }
}
```

#### 4.3.4 响应与项目内映射

```json
{
  "id": "resp_123",
  "object": "response",
  "model": "gpt-5.6-luna",
  "output": [
    {
      "type": "message",
      "role": "assistant",
      "content": [
        {
          "type": "output_text",
          "text": "OK"
        }
      ]
    }
  ],
  "usage": {
    "input_tokens": 8,
    "output_tokens": 2,
    "total_tokens": 10
  }
}
```

OpenAI SDK 将 `output` 中的文本聚合为 `response.output_text`。模型适配器将其转换为项目内的 `ChatResult`：

| Responses API 字段 | `ChatResult` 字段 |
| --- | --- |
| `response.output_text` | `content` |
| `response.model` | `model` |
| `response.usage.input_tokens` | `input_tokens` |
| `response.usage.output_tokens` | `output_tokens` |

模型服务超时映射为 `ModelTimeoutError`，其他供应商异常映射为不包含密钥和上游响应正文的 `ModelProviderError`，空响应或无效结构化输出映射为 `ModelResponseError`。

## 5. 错误处理

### 5.1 参数校验错误

```json
{
  "code": 400,
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
  "code": 403,
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
  "code": 400,
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
  "code": 503,
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
  "code": 429,
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
curl -X GET "http://localhost:9090/api/agent-tools/courts/availability?venueId=123&date=2026-07-22&startTime=15:00&endTime=16:00" \
  -H "X-Agent-Service-Token: $SERVICE_TOKEN" \
  -H "X-Agent-Context-Token: $CONTEXT_TOKEN" \
  -H "X-Agent-Trace-Id: trace_123"

# 缺少服务认证
curl -X GET "http://localhost:9090/api/agent-tools/courts/availability?venueId=123&date=2026-07-22&startTime=15:00&endTime=16:00" \
  -H "X-Agent-Context-Token: $CONTEXT_TOKEN" \
  -H "X-Agent-Trace-Id: trace_123"

# 无效的签名
curl -X GET "http://localhost:9090/api/agent-tools/courts/availability?venueId=123&date=2026-07-22&startTime=15:00&endTime=16:00" \
  -H "X-Agent-Service-Token: $SERVICE_TOKEN" \
  -H "X-Agent-Context-Token: invalid-token" \
  -H "X-Agent-Trace-Id: trace_123"
```

## 9. 附录

### 9.1 HTTP 状态码映射

| HTTP 状态码 | 错误码 | 说明 |
| --- | --- | --- |
| 200 | 200 | 成功 |
| 400 | 400 | 参数或业务错误 |
| 401 | 401 | 认证失败或会话过期 |
| 403 | 403 | 权限不足 |
| 404 | 404 | 资源不存在 |
| 429 | 429 | 请求过于频繁 |
| 500 | 500 | 内部服务错误 |
| 502 | 502 | 第三方服务错误 |
| 503 | 503 | 服务不可用 |
| 504 | 504 | 第三方服务超时 |

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
