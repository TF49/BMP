# BMP Agent Service

BMP 三智能体服务 - 基于 Python + LangGraph 的智能 Agent 服务。

> 当前状态：P0 工程底座已完成。FastAPI、LLM 适配层、内存会话和 LangGraph Mock Agent 可运行。

## 📋 项目简介

BMP Agent 是羽擎（Badminton Management Platform）项目的智能体服务，采用 FastAPI + LangGraph 架构，提供三个专用智能体：

- **智能预订 Agent** - 自然语言完成场地预订
- **经营分析 Agent** - 数据分析和业务洞察
- **场馆客服 Agent** - 基于知识库的智能客服

## 🚀 快速开始

### 环境要求

- Python 3.12.13
- P0 默认不需要 PostgreSQL 或 Redis
- 后续集成环境使用 PostgreSQL 15+ 和 Redis 7.0+

### 安装依赖

```powershell
# 标准 Windows Python Launcher
py -3.12 -m venv venv

# 如果 Python 由 uv 管理，使用下面这条命令代替上一条
uv venv --python 3.12.13 --seed venv

.\venv\Scripts\Activate.ps1
python -m pip install -r requirements.txt
python -m pip check
```

### 配置环境变量

```powershell
Copy-Item .env.example .env

# 编辑 .env 文件，填入实际配置
# 特别注意：OPENAI_API_KEY 需要配置为实际的 API Key
```

### 运行服务

```powershell
# 开发模式
python -m app.main

# 支持热重载
uvicorn app.main:app --reload --port 8000
```

服务启动后访问：
- API 文档：http://localhost:8000/docs
- 健康检查：http://localhost:8000/health

### P0 Mock Agent 验证

```powershell
$headers = @{
    "X-Agent-Context-Token" = "dev-only-context-token"
    "X-Agent-Trace-Id" = "local-smoke-test"
}
$body = @{
    conversation_id = "conv-local-1"
    agent_type = "booking"
    message = @{ content = "hello"; message_id = "msg-local-1" }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod http://localhost:8000/health
Invoke-RestMethod http://localhost:8000/api/v1/agent/process `
    -Method Post -Headers $headers -ContentType "application/json" -Body $body
```

同一 `conversation_id` 再请求一次时，响应 `metadata.turn` 从 `1` 增加到 `2`。

## 📁 项目结构

```
bmp-agent/
├── app/                        # 应用主目录
│   ├── api/                    # API 路由与 DTO
│   ├── agents/                 # LangGraph 子图
│   │   ├── booking/           # 智能预订 Agent
│   │   ├── analytics/         # 经营分析 Agent
│   │   └── support/           # 场馆客服 Agent
│   ├── tools/                 # Java Tool 客户端
│   ├── llm/                   # LLM 模型适配
│   ├── memory/                # 会话与 Checkpoint
│   ├── observability/         # 日志与追踪
│   └── core/                  # 核心配置
├── tests/                     # 测试目录
├── alembic/                   # 数据库迁移
├── requirements.txt           # 依赖清单
├── .env.example              # 环境变量模板
└── README.md                 # 项目说明
```

## 🧪 测试

```powershell
# 运行所有测试
python -m pytest

# 运行特定测试文件
python -m pytest tests/test_api.py

# pytest 默认生成覆盖率报告并强制总覆盖率不低于 80%
python -m pytest
```

## 🔍 代码质量检查

```powershell
# Ruff 代码检查
ruff check app tests

# Ruff 格式检查
ruff format --check app tests

# MyPy 类型检查
mypy app
```

## 📚 API 文档

启动服务后，访问 http://localhost:8000/docs 查看完整的 API 文档。

### 接口边界

- Java 面向前端：`/api/agent/**`
- FastAPI 内部处理：`POST /api/v1/agent/process`
- FastAPI 健康检查：`GET /health`

## 🔐 安全配置

开发环境使用的密钥配置在 `.env` 文件中，**请勿提交到版本控制**。

生产环境应使用专用的密钥管理服务。

## 📖 开发指南

详细的开发指南请参考：
- [技术架构文档](../docs/技术架构文档.md)
- [优先级实施计划](../docs/BMP三智能体优先级实施计划.md)

## 🤝 贡献

本项目正在开发中，欢迎贡献代码和反馈问题。

## 📄 许可证

[待定]
