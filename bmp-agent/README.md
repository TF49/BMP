# BMP Agent Service

BMP 三智能体服务 - 基于 Python + LangGraph 的智能 Agent 服务

## 📋 项目简介

BMP Agent 是羽擎（Badminton Management Platform）项目的智能体服务，采用 FastAPI + LangGraph 架构，提供三个专用智能体：

- **智能预订 Agent** - 自然语言完成场地预订
- **经营分析 Agent** - 数据分析和业务洞察
- **场馆客服 Agent** - 基于知识库的智能客服

## 🚀 快速开始

### 环境要求

- Python 3.11 或更高版本
- PostgreSQL 15+ （生产环境）或 SQLite（开发环境）
- Redis 7.0+（可选，开发环境可使用内存缓存）

### 安装依赖

```bash
# 激活虚拟环境
# Windows
venv\Scripts\activate
# macOS/Linux
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

### 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入实际配置
# 特别注意：OPENAI_API_KEY 需要配置为实际的 API Key
```

### 运行服务

```bash
# 开发模式（支持热重载）
python -m app.main

# 或使用 uvicorn 直接运行
uvicorn app.main:app --reload --port 8000
```

服务启动后访问：
- API 文档：http://localhost:8000/docs
- 健康检查：http://localhost:8000/api/v1/health

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

```bash
# 运行所有测试
pytest

# 运行特定测试文件
pytest tests/test_api.py

# 生成覆盖率报告
pytest --cov=app --cov-report=html
```

## 🔍 代码质量检查

```bash
# Ruff 代码检查
ruff check app/

# Ruff 代码格式化
ruff format app/

# MyPy 类型检查
mypy app/
```

## 📚 API 文档

启动服务后，访问 http://localhost:8000/docs 查看完整的 API 文档。

### 主要接口

- `POST /api/v1/conversations` - 创建会话
- `POST /api/v1/conversations/{conversation_id}/messages` - 发送消息
- `GET /api/v1/health` - 健康检查

## 🔐 安全配置

开发环境使用的密钥配置在 `.env` 文件中，**请勿提交到版本控制**。

生产环境应使用专用的密钥管理服务。

## 📖 开发指南

详细的开发指南请参考：
- [技术架构文档](../docs/技术架构文档.md)
- [阶段1实施指南](../docs/阶段1实施指南.md)

## 🤝 贡献

本项目正在开发中，欢迎贡献代码和反馈问题。

## 📄 许可证

[待定]
