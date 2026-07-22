# BMP Agent Service

BMP 三智能体服务 - 基于 Python + LangGraph 的智能 Agent 服务。

> 当前状态：P1 Java/Python 安全集成与 Tool 契约已于 2026-07-21 完成并通过 M2 验收。
>
> P2-01 智能预订 Agent MVP 已于 2026-07-22 完整完成：端到端跑通「结构化提取 (含相对时间转换) → CourtTool 空场与相邻替代推荐 → Java 报价 → 用户明确确认（LangGraph interrupt/resume）→ Java 创建待支付预约」。金额一律由 Java 计算（模型不算价/优惠/余额），创建仅生成待支付订单（status=1 / paymentStatus=0），并通过 `Idempotency-Key` 保证重复确认只创建一个预约。建立了 50 条覆盖模糊时间、满场、报价过期、重复确认和越权的评估集（`evals/booking/booking_cases.jsonl`）。
>
> P2-02 经营分析 Agent 核心功能已于 2026-07-22 完整完成：实现经营总览、预约趋势、场地利用率热力图、财务趋势、业务收入构成及场馆横向对比 6 个只读分析 Tool。固化指标字典与计算口径，由 Java 服务端强约束角色与场馆范围（绝对禁止模型生成/执行任意 SQL），输出统一包含统计周期、数据范围、指标口径及受控 ECharts 图表数据。建立了 32 条覆盖数值、权限与空数据的固化评估样本（`evals/analytics/analytics_cases.jsonl`）。Python 测试套件全部通过 (73/73 PASSED)。
>
> P2-03 场馆客服 Agent MVP 已于 2026-07-22 完整完成：基于知识库（RAG 检索）与经过运营确认的场馆介绍、规则、价格退款政策、课程器材及 FAQ，回答中强约束返回来源标题、文档版本和更新时间。实时价格与营业状态强约束调用 Java 网关只读 Tool。实现了证据不足/冲突/资金争议转人工（创建转接单）及提示词注入安全拦截防护。完成了知识版本控制与回滚，并建立了 85 条覆盖引用、实时 Tool、争议转人工与安全拦截的评估集（`evals/support/support_cases.jsonl`）。Python 全量测试套件全部通过 (81/81 PASSED)。

## 📋 项目简介

BMP Agent 是羽擎（Badminton Management Platform）项目的智能体服务，采用 FastAPI + LangGraph 架构，提供三个专用智能体：

- **智能预订 Agent** - 自然语言完成场地预订（第一阶段已打通报价 → 确认 → 创建待支付闭环）
- **经营分析 Agent** - 数据分析和业务洞察（已完成核心功能与受控 ECharts 输出）
- **场馆客服 Agent** - 基于 RAG 知识库与实时 Tool 的智能客服（带来源引用、版本回滚与争议转人工）


## 🚀 快速开始

### 环境要求

- Python 3.12.13（精确版本）
- 本地开发可使用内存 Checkpoint / 限流器，无需外部服务依赖；集成环境使用 PostgreSQL 15+ 和 Redis 7.0+

### 确认 Python 版本

```powershell
# 检查已安装的 Python 版本
py -0

# 验证 3.12.13 是否可用
py -3.12 --version
```

**预期输出**：`Python 3.12.13`

如果系统中没有 Python 3.12.13，请按以下方式安装：

**Windows 用户**：
1. 访问 [Python 官网](https://www.python.org/downloads/)
2. 下载 Python 3.12.13 Windows 安装程序
3. 安装时勾选 "Add Python to PATH"
4. 安装完成后重新打开 PowerShell 验证版本

**使用 uv 管理的用户**：
```powershell
# uv 会自动下载指定版本的 Python
uv venv --python 3.12.13 --seed venv
```

### 安装依赖

```powershell
# 标准 Windows Python Launcher
py -3.12 -m venv venv

# 如果 Python 由 uv 管理，使用下面这条命令代替上一条
uv venv --python 3.12.13 --seed venv

.\venv\Scripts\Activate.ps1
python --version  # 必须输出 Python 3.12.13
python -m pip install -r requirements.txt
python -m pip check
```

**重要**：虚拟环境激活后，`python --version` 必须输出 `Python 3.12.13`；其他补丁版本不符合当前锁定环境。

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

默认健康检查不连接外部服务，未检查项显示为 `disabled`。仅在需要验证已配置依赖时设置 `EXTERNAL_HEALTH_CHECKS=true`；单项失败会显示为 `unhealthy`，整体状态显示为 `degraded`。

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
│   ├── agents/                 # Agent 契约、Mock Agent 与预订 Agent（booking_agent）
│   ├── tools/                 # Java Tool 客户端（venue/court/booking）
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
# 运行所有测试 (标准虚拟环境 或 uv 环境)
.\venv\Scripts\python -m pytest
# 或使用 uv：
uv run pytest

# 运行特定测试文件
.\venv\Scripts\python -m pytest tests/test_api.py
```

默认测试只允许本机回环连接，任何外部网络连接都会被阻断；`live_llm` 测试必须显式选择后才会开放网络。

## 🔍 代码质量检查

```powershell
# Ruff 代码检查
.\venv\Scripts\ruff check app tests
# 或使用 uv：uv run ruff check app tests

# Ruff 格式检查
.\venv\Scripts\ruff format --check app tests
# 或使用 uv：uv run ruff format --check app tests

# MyPy 类型检查
.\venv\Scripts\mypy app
# 或使用 uv：uv run mypy app
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
