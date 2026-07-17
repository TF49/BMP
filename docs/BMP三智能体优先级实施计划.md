# BMP 三智能体优先级实施计划

> 文档版本：v1.0
> 创建日期：2026-07-17
> 文档状态：待实施
> 适用项目：羽擎（Badminton Management Platform，BMP）
> 使用方式：严格按照 `P0 -> P1 -> P2 -> P3` 顺序实施；前一优先级验收未通过，不进入下一优先级。

## 1. 计划定位

本文档是项目根目录 `BMP三智能体开发计划.md` 的执行清单，负责回答“先做什么、后做什么、做到什么程度才算完成”。总体架构、业务边界和验收指标仍以现有设计文档为依据：

- `BMP三智能体开发计划.md`：总体目标、范围和里程碑。
- `docs/技术架构文档.md`：系统架构、数据流和三智能体边界。
- `docs/API接口规范文档.md`：Java Agent 网关、Tool API 和 FastAPI 内部接口。
- `docs/数据库架构文档.md`：PostgreSQL、Redis 和 Checkpoint 设计。
- `docs/安全防护文档.md`：API 六道防线和审计要求。

本文档不替代上述设计文档。实施过程中如果发现冲突，先完成 `P0-01 契约统一`，再继续编码。

## 2. 优先级定义

| 优先级 | 含义 | 进入条件 | 退出条件 |
| --- | --- | --- | --- |
| P0 | 阻断项与工程底座 | 当前即可开始 | FastAPI + Mock Agent 最小闭环可运行 |
| P1 | Java/Python 安全集成 | P0 全部通过 | 鉴权后的只读 Tool 调用可端到端运行 |
| P2 | 三智能体业务 MVP | P1 全部通过 | 三只 Agent 和对应前端主流程可验收 |
| P3 | 生产化与 LLMOps | P2 全部通过 | 监控、安全、评估、灰度和回滚达标 |

推荐单人连续开发周期为 8 至 11 周。每个任务完成后，在复选框后补充完成日期和对应 Git Commit，例如：`[x] 2026-07-20 / abc1234`。

## 3. 全局约束

- Python 统一使用 3.12，Java 保持项目现有的 Java 17 和 Spring Boot 3.2。
- Python Agent 不直接连接 BMP MySQL，只能通过 Java Tool API 读取或修改业务数据。
- Java 继续负责用户认证、资源权限、事务、价格、库存、预约冲突和资金规则。
- Agent 类型由客户端显式指定为 `booking`、`analytics` 或 `support`，第一版不让模型自动猜测。
- Java 对前端提供 `/api/agent/**`；FastAPI 仅提供 `/api/v1/agent/process` 和 `/health` 等内部接口。
- 所有写操作必须同时具备权限校验、业务校验、明确确认、幂等控制和审计日志。
- 开发顺序采用测试先行：先写失败测试，再写最小实现，最后运行完整回归。
- 每完成一个独立任务单独提交，禁止把多个优先级混在同一个提交中。
- `.env`、虚拟环境、缓存、日志、覆盖率报告和生成的项目目录清单不得提交。

## 4. 当前基线

截至 2026-07-17，项目状态如下：

- [x] 已完成总体开发计划和 4 份专项设计文档。
- [x] 已创建 `bmp-agent` 基础目录、依赖清单和环境变量模板。
- [x] 已配置根目录与 Python 子项目的 `.gitignore`。
- [ ] Python 服务入口、核心模块、Agent、API 和有效测试尚未实现。
- [ ] Java Agent 网关和 `/api/agent-tools/**` 尚未实现。
- [ ] 三只 Agent、RAG、前端入口和生产监控尚未实现。

当前存在的阻断问题：

1. `bmp-agent/README.md` 和 `pyproject.toml` 使用 Python 3.11，设计文档要求 Python 3.12。
2. README 和 API 规范中的接口路径不一致。
3. `docs/数据库架构文档.md` 在 PostgreSQL 中声明了指向 MySQL `users` 表的外键，该约束无法成立。
4. README 提供了 `python -m app.main`，但当前不存在 `app/main.py`。

---

## 5. P0：契约统一与 Agent 工程底座

目标：建立一个可启动、可测试、具备统一错误和追踪能力的 FastAPI 服务，并跑通不调用 Java Tool 的 Mock Agent。

### P0-01 统一文档与代码契约

**涉及文件**

- 修改：`bmp-agent/README.md`
- 修改：`bmp-agent/pyproject.toml`
- 修改：`bmp-agent/requirements.txt`
- 修改：`docs/技术架构文档.md`
- 修改：`docs/API接口规范文档.md`
- 修改：`docs/数据库架构文档.md`

**任务清单**

- [ ] 将 Python 最低版本统一为 3.12，将 Ruff 和 mypy 目标版本统一为 `py312`。
- [ ] 明确 Java 公共接口为 `/api/agent/**`，FastAPI 内部处理接口为 `/api/v1/agent/process`，健康检查为 `/health`。
- [ ] 删除 PostgreSQL `agent_conversations.user_id -> users.id` 外键，只保留普通 `user_id` 字段和索引。
- [ ] 明确会话归属和用户有效性由 Java 网关与 Tool 层校验，PostgreSQL 不复制 MySQL 用户表。
- [ ] 将 README 标记为“工程骨架”，在入口实现前不宣称服务已经可运行。
- [ ] 确定依赖管理规则：`pyproject.toml` 保存工具配置，`requirements.txt` 保存经过兼容性验证的固定版本。

**验收**

```powershell
rg -n "3\.11|/api/v1/health|REFERENCES users" bmp-agent docs -g '*.md' -g '*.toml'
```

预期：没有未解释的旧版本、旧健康检查路径或跨数据库外键。

### P0-02 固化 Python 开发环境

**涉及文件**

- 修改：`bmp-agent/requirements.txt`
- 修改：`bmp-agent/.env.example`
- 修改：`bmp-agent/README.md`

**任务清单**

- [ ] 安装 Python 3.12，并使用 `py -3.12 -m venv venv` 重建本地虚拟环境。
- [ ] 在干净虚拟环境中安装依赖，解决 FastAPI、Pydantic、LangGraph 和 LangChain 的版本兼容问题。
- [ ] 固定所有直接依赖版本，记录 Python 和核心依赖版本。
- [ ] 检查 `.env.example` 只包含占位符，不包含真实密钥、内网地址或生产数据库凭证。
- [ ] 在 README 中记录 Windows PowerShell 下的安装、启动、测试和质量检查命令。

**验收**

```powershell
cd bmp-agent
python --version
python -m pip check
```

预期：Python 为 3.12.x，`pip check` 返回 `No broken requirements found`。

### P0-03 实现 FastAPI 基础设施

**创建文件**

- `bmp-agent/app/main.py`
- `bmp-agent/app/core/config.py`
- `bmp-agent/app/core/exceptions.py`
- `bmp-agent/app/observability/logging.py`
- `bmp-agent/app/observability/tracing.py`
- `bmp-agent/app/api/schemas.py`
- `bmp-agent/app/api/routes.py`
- `bmp-agent/tests/conftest.py`
- `bmp-agent/tests/test_config.py`
- `bmp-agent/tests/test_api.py`

**任务清单**

- [ ] 先编写配置缺失、健康检查和统一异常的失败测试。
- [ ] 使用 Pydantic Settings 加载应用、模型、数据库、Redis、日志和服务认证配置。
- [ ] 区分开发与生产校验：生产环境禁止占位密钥，开发环境允许关闭非必要外部依赖。
- [ ] 建立统一异常类型和 `{code, message, data, trace_id}` 响应结构。
- [ ] 使用 `ContextVar` 管理 `trace_id`，优先复用请求头中的合法 TraceId，否则生成新值。
- [ ] 实现结构化日志，禁止记录 JWT、API Key、签名、完整 Prompt 和未脱敏用户信息。
- [ ] 实现 `GET /health`，分别报告应用、模型、数据库和 Redis 状态；非必要依赖未配置时标记 `disabled`。
- [ ] 实现 FastAPI 生命周期管理，避免使用已弃用的启动事件写法。

**验收**

```powershell
cd bmp-agent
python -m app.main
pytest tests/test_config.py tests/test_api.py -v
```

预期：服务可启动；健康检查与异常测试全部通过；响应包含 `trace_id`。

### P0-04 建立 LLM 抽象与结构化输出

**创建文件**

- `bmp-agent/app/llm/base.py`
- `bmp-agent/app/llm/openai_adapter.py`
- `bmp-agent/app/llm/schemas.py`
- `bmp-agent/tests/test_llm.py`

**任务清单**

- [ ] 先编写模型配置、超时、无效 JSON 和供应商异常的失败测试。
- [ ] 定义与供应商无关的聊天模型接口，业务 Agent 不直接导入 OpenAI 客户端。
- [ ] 使用 OpenAI-compatible 配置实现适配器，统一超时和错误转换。
- [ ] 所有需要进入状态图的模型结果使用 Pydantic 结构化模型校验。
- [ ] 测试默认使用 Fake/Stub 模型，不消耗真实 Token；真实模型连接测试使用显式标记单独运行。

**验收**

```powershell
cd bmp-agent
pytest tests/test_llm.py -v
```

预期：离线测试全部通过；未配置真实 API Key 时不会误发外部请求。

### P0-05 实现会话、Checkpoint 和 Mock Agent

**创建文件**

- `bmp-agent/app/memory/session.py`
- `bmp-agent/app/memory/checkpoint.py`
- `bmp-agent/app/agents/base.py`
- `bmp-agent/app/agents/mock_agent.py`
- `bmp-agent/tests/test_session.py`
- `bmp-agent/tests/test_agents.py`

**修改文件**

- `bmp-agent/app/api/schemas.py`
- `bmp-agent/app/api/routes.py`

**任务清单**

- [ ] 定义包含 `conversation_id`、`user_id`、`agent_type`、`thread_id`、创建时间和过期时间的会话模型。
- [ ] 先使用内存实现会话与 Checkpoint，并通过接口隔离存储实现，便于后续替换 PostgreSQL。
- [ ] 编写 LangGraph Mock 状态图，实现接收消息、更新状态和返回固定结构响应。
- [ ] 实现 `POST /api/v1/agent/process`，要求 Java 签名上下文中的用户与会话归属一致。
- [ ] 验证不同用户、不同会话和不同 Agent 类型之间状态互不串用。
- [ ] 验证过期会话、未知 Agent 类型、空消息和超长消息返回明确错误。

**验收**

```powershell
cd bmp-agent
pytest tests/test_session.py tests/test_agents.py tests/test_api.py -v
```

预期：Mock Agent 最小闭环通过；会话隔离测试通过；尚未调用任何 Java 业务 Tool。

### P0-06 通过 M1 质量门槛

- [ ] `pytest` 全部通过，并生成覆盖率报告。
- [ ] `ruff check app tests` 无错误。
- [ ] `ruff format --check app tests` 无格式差异。
- [ ] `mypy app` 无阻断错误。
- [ ] README 中的安装、启动、健康检查和测试命令均经过实际验证。
- [ ] 提交中不包含 `.env`、`venv/`、`__pycache__/`、`htmlcov/` 或 `project_structure.txt`。

**完整验收命令**

```powershell
cd bmp-agent
pytest
ruff check app tests
ruff format --check app tests
mypy app
```

**M1 完成定义**：FastAPI + LangGraph Mock Agent 可以从干净环境启动和测试，失败时返回统一错误并可通过 `trace_id` 定位。

---

## 6. P1：Java 网关、Tool 契约与安全集成

目标：让前端通过现有 JWT 访问 Java Agent 网关，并让 Python 使用短期签名上下文调用一个只读 Java Tool。

### P1-01 建立 Java Agent 模块和公共 DTO

**创建目录与文件**

- `src/main/java/com/badminton/bmp/modules/agent/controller/AgentController.java`
- `src/main/java/com/badminton/bmp/modules/agent/service/AgentGatewayService.java`
- `src/main/java/com/badminton/bmp/modules/agent/service/impl/AgentGatewayServiceImpl.java`
- `src/main/java/com/badminton/bmp/modules/agent/client/AgentServiceClient.java`
- `src/main/java/com/badminton/bmp/modules/agent/dto/AgentConversationRequest.java`
- `src/main/java/com/badminton/bmp/modules/agent/dto/AgentMessageRequest.java`
- `src/main/java/com/badminton/bmp/modules/agent/dto/AgentActionRequest.java`
- `src/main/java/com/badminton/bmp/modules/agent/vo/AgentResponseVO.java`
- `src/test/java/com/badminton/bmp/modules/agent/AgentControllerTest.java`

**任务清单**

- [ ] 先编写创建会话、发送消息、确认动作和拒绝动作的 MockMvc 失败测试。
- [ ] 复用现有 `Result<T>` 和 `GlobalExceptionHandler`，避免创建第二套全局响应体系。
- [ ] 从 Spring Security Context 获取用户身份，禁止信任请求体中的 `user_id`、角色或场馆范围。
- [ ] 实现 `/api/agent/conversations`、消息、确认和拒绝接口的 DTO 校验。
- [ ] 使用配置化 FastAPI 地址、连接超时和读取超时，不在代码中写死主机名。

### P1-02 实现短期上下文签名和 Tool 认证

**创建文件**

- `src/main/java/com/badminton/bmp/modules/agent/security/AgentContext.java`
- `src/main/java/com/badminton/bmp/modules/agent/security/AgentContextSigner.java`
- `src/main/java/com/badminton/bmp/modules/agent/security/AgentToolAuthenticationFilter.java`
- `src/main/java/com/badminton/bmp/modules/agent/config/AgentProperties.java`
- `src/test/java/com/badminton/bmp/modules/agent/AgentContextSignerTest.java`
- `src/test/java/com/badminton/bmp/modules/agent/AgentToolAuthenticationFilterTest.java`

**任务清单**

- [ ] 上下文至少包含用户 ID、角色、场馆范围、签发时间、过期时间、随机数和 TraceId。
- [ ] 使用 HMAC-SHA256 签名，服务密钥仅通过环境变量注入。
- [ ] 签名有效期控制在 5 分钟以内，并拒绝过期、篡改、重放和缺失字段的请求。
- [ ] Tool 接口同时校验服务身份、上下文签名、角色和资源归属。
- [ ] 安全日志只记录摘要、失败类型和 TraceId，不记录原始签名或密钥。

### P1-03 实现第一组只读 Tool

**创建文件**

- `src/main/java/com/badminton/bmp/modules/agent/controller/AgentVenueToolController.java`
- `src/main/java/com/badminton/bmp/modules/agent/controller/AgentCourtToolController.java`
- `src/main/java/com/badminton/bmp/modules/agent/service/AgentVenueToolService.java`
- `src/main/java/com/badminton/bmp/modules/agent/service/AgentCourtToolService.java`
- `src/test/java/com/badminton/bmp/modules/agent/AgentVenueToolControllerTest.java`
- `src/test/java/com/badminton/bmp/modules/agent/AgentCourtToolControllerTest.java`

**任务清单**

- [ ] 复用现有 `VenueService`、`CourtService` 和预约查询能力，不复制业务逻辑。
- [ ] 实现 `GET /api/agent-tools/venues` 和 `GET /api/agent-tools/courts/availability`。
- [ ] 对日期、时间范围、场馆 ID、预约模式和结果数量进行白名单及边界校验。
- [ ] 验证场馆营业状态、场地状态和用户可访问范围。
- [ ] 返回专用稳定 DTO，不直接暴露数据库实体或 MyBatis 对象。

### P1-04 实现 Python Tool 客户端

**创建文件**

- `bmp-agent/app/tools/base.py`
- `bmp-agent/app/tools/client.py`
- `bmp-agent/app/tools/venue.py`
- `bmp-agent/app/tools/court.py`
- `bmp-agent/tests/test_tools.py`

**任务清单**

- [ ] 使用共享 HTTPX AsyncClient、连接池和统一超时。
- [ ] 自动透传服务密钥、签名上下文、TraceId 和幂等键。
- [ ] 将 Java 错误码转换为 Python 领域异常，不向模型暴露内部堆栈。
- [ ] 仅对明确的网络瞬时错误重试；写操作默认不自动重试。
- [ ] 使用 MockTransport 完成超时、401、403、404、429 和 503 测试。

### P1-05 落实 API 六道防线

- [ ] 第一防线：Java Validation 与 Python Pydantic 双层参数校验。
- [ ] 第二防线：Java Service 校验营业状态、时间冲突、报价有效期和业务状态。
- [ ] 第三防线：JWT、角色、场馆范围、资源归属和 Tool 白名单校验。
- [ ] 第四防线：所有写 Tool 使用 `Idempotency-Key`、Redis 原子状态和数据库唯一约束。
- [ ] 第五防线：按 IP、用户、接口和写操作设置分层限流，返回 429 和可重试时间。
- [ ] 第六防线：为 FastAPI 与 Java Tool 调用配置超时、熔断、有限重试和静态降级。
- [ ] 统一异常兜底，响应不得包含堆栈、SQL、Prompt、供应商错误原文和敏感字段。
- [ ] 为六道防线分别建立正向与拒绝路径测试。

### P1-06 持久化会话和 Checkpoint

**创建文件**

- `bmp-agent/alembic.ini`
- `bmp-agent/alembic/env.py`
- `bmp-agent/alembic/versions/0001_agent_core_tables.py`
- `bmp-agent/app/memory/postgres.py`
- `bmp-agent/tests/integration/test_postgres_checkpoint.py`

**任务清单**

- [ ] 创建会话、消息、动作、Tool 日志和 LangGraph Checkpoint 表。
- [ ] `user_id` 和 `venue_id` 只保存外部标识，不建立跨 MySQL 外键。
- [ ] 配置连接池、事务边界、会话过期和清理任务。
- [ ] 开发环境保留内存实现，集成环境通过配置切换 PostgreSQL。
- [ ] 验证服务重启后可以按用户和会话恢复状态。

### P1-07 通过 M2 集成门槛

```powershell
# Java
mvn test

# Python
cd bmp-agent
pytest
ruff check app tests
mypy app
```

- [ ] 前端 JWT 能通过 Java 网关转换为短期 Agent 上下文。
- [ ] Python 能调用场馆和空场查询 Tool。
- [ ] 伪造签名、过期签名、跨用户、跨场馆和无权限请求全部被拒绝。
- [ ] Java、FastAPI 和 Tool 日志可以使用同一 TraceId 关联。

**M2 完成定义**：安全的只读 Tool 调用端到端通过，Agent 仍不能直接访问 MySQL。

---

## 7. P2：三智能体业务 MVP 与前端接入

P2 内部仍按以下顺序实施：智能预订 Agent -> 经营分析 Agent -> 场馆客服 Agent -> 多端统一体验。

### P2-01 智能预订 Agent

**主要文件**

- Python：`bmp-agent/app/agents/booking/`
- Java：`src/main/java/com/badminton/bmp/modules/agent/controller/AgentBookingToolController.java`
- Java 测试：`src/test/java/com/badminton/bmp/modules/agent/AgentBookingToolControllerTest.java`
- 评估集：`bmp-agent/evals/booking/booking_cases.jsonl`

**任务清单**

- [ ] 实现日期、时间、场馆、模式和偏好的结构化提取。
- [ ] 缺少信息时每次只追问一个关键字段。
- [ ] 实现空场查询、相邻时间搜索和替代方案排序。
- [ ] 由 Java 生成报价，模型不得计算最终价格、优惠或余额。
- [ ] 使用 LangGraph interrupt/resume 实现明确确认。
- [ ] 创建预约必须携带幂等键，只生成待支付订单，不自动支付。
- [ ] 建立不少于 50 条评估样本，覆盖模糊时间、无空场、报价过期、重复确认和越权。

**验收门槛**

- 未确认写入数量为 0。
- 重复确认只能创建一个预约。
- 报价、订单金额和 Java 业务结果完全一致。
- 预订主流程端到端测试通过。

### P2-02 经营分析 Agent

**主要文件**

- Python：`bmp-agent/app/agents/analytics/`
- Java：`src/main/java/com/badminton/bmp/modules/agent/controller/AgentAnalyticsToolController.java`
- 复用服务：`DashboardService`、`FinanceService` 和预约聚合能力。
- 评估集：`bmp-agent/evals/analytics/analytics_cases.jsonl`

**任务清单**

- [ ] 固化预约量、利用率、收入和业务构成的指标字典与计算口径。
- [ ] 实现经营总览、预约趋势、场地热力图、财务趋势、收入构成和场馆对比 Tool。
- [ ] Tool 只返回聚合结果，不允许模型生成或执行任意 SQL。
- [ ] 服务端根据用户角色确定场馆范围，忽略客户端伪造范围。
- [ ] 输出必须包含统计周期、数据范围、指标口径和受控 ECharts 数据。
- [ ] 建立不少于 30 条固定数据评估样本，校验数值、权限和空数据表达。

### P2-03 场馆客服 Agent

**主要文件**

- Python：`bmp-agent/app/agents/support/`
- 知识模块：`bmp-agent/app/knowledge/`
- Java：`src/main/java/com/badminton/bmp/modules/agent/controller/AgentSupportToolController.java`
- 评估集：`bmp-agent/evals/support/support_cases.jsonl`

**任务清单**

- [ ] 建立经过运营确认的场馆介绍、营业规则、价格、预约、退款、课程和器材知识源。
- [ ] 实现文档加载、清洗、切片、Embedding、pgvector 索引和可选 Rerank。
- [ ] 每个回答返回来源标题、文档版本和更新时间。
- [ ] 实时价格和营业状态必须调用 Java Tool，不使用知识库旧值。
- [ ] 证据不足、证据冲突、资金争议和 Prompt 注入时拒答或转人工。
- [ ] 实现知识版本、重新索引、回滚和人工咨询单。
- [ ] 建立不少于 80 条评估样本，覆盖正确回答、拒答、引用和敏感信息保护。

### P2-04 Web 与 UniApp 接入

**Web 文件**

- `vue/src/api/agent.js`
- `vue/src/views/AgentWorkspace.vue`
- `vue/src/components/agent/AgentMessageList.vue`
- `vue/src/components/agent/AgentComposer.vue`
- `vue/src/components/agent/AgentActionConfirm.vue`
- `vue/src/router/index.js`

**UniApp 文件**

- `BMP-uniapp/api/agent.ts`
- `BMP-uniapp/pages/agent/index.vue`
- `BMP-uniapp/components/agent/AgentMessageList.vue`
- `BMP-uniapp/components/agent/AgentActionConfirm.vue`
- `BMP-uniapp/pages.json`

**任务清单**

- [ ] Web 优先接入经营分析，UniApp 优先接入预订和客服。
- [ ] 实现会话列表、消息状态、结构化卡片、引用、确认/拒绝和转人工入口。
- [ ] 写操作使用明确确认控件，禁用重复点击并展示处理状态。
- [ ] 先稳定 REST，之后再评估 Web SSE 和 UniApp WebSocket。
- [ ] 验证弱网、超时、重试、会话恢复、长文本和移动端内容溢出。

**前端验收**

```powershell
cd vue
npm run lint
npm run build

cd ..\BMP-uniapp
npm run type-check
npm run test
npm run build:mp-weixin
```

### P2-05 通过 M3-M6 业务门槛

- [ ] 三只 Agent 各自评估集达到总体计划规定的指标。
- [ ] 所有金额、数量、比例和实时状态都能追溯到 Java Tool 结果。
- [ ] 三只 Agent 的状态、Prompt、Tool 白名单和评估集相互隔离。
- [ ] Web 与 UniApp 的主流程均完成角色、弱网和重复操作测试。
- [ ] Agent 不可用时，用户仍能回到原有 BMP 页面完成业务。

---

## 8. P3：生产化、评估与 LLMOps

### P3-01 可观测性与成本控制

- [ ] 接入 OpenTelemetry 与 Langfuse，贯通 Java、FastAPI、LangGraph、Tool 和模型 Span。
- [ ] 监控请求量、P50/P95 延迟、错误率、Tool 成功率、Token、成本和业务转化率。
- [ ] 为模型、Prompt、Tool、知识库和评估集记录版本。
- [ ] 设置用户、Agent 和全局 Token 预算，超限时降级为普通业务入口或人工服务。

### P3-02 隐私、审计与数据生命周期

- [ ] 对日志、追踪和模型输入执行敏感字段脱敏。
- [ ] 配置会话、消息、Checkpoint、审计日志和知识版本的留存周期。
- [ ] 实现用户会话删除、过期数据清理和审计查询。
- [ ] 验证管理人员不能绕过审计查看完整私密对话。

### P3-03 部署、压测与故障演练

**计划创建文件**

- `bmp-agent/Dockerfile`
- `docker-compose.agent.yml`
- `.github/workflows/agent-ci.yml`
- `docs/Agent部署与回滚手册.md`

**任务清单**

- [ ] 容器化 FastAPI，使用非 root 用户并配置健康检查。
- [ ] 在集成环境部署 PostgreSQL/pgvector、Redis 和 Agent 服务。
- [ ] 完成模型超时、Tool 503、Redis 故障、PostgreSQL 故障和网络抖动演练。
- [ ] 验证熔断后不会拖垮 Java 主服务，原业务页面仍可使用。
- [ ] 完成并发、长会话和大知识库检索压测，记录容量基线。

### P3-04 灰度上线与回滚

- [ ] 为三只 Agent 分别配置功能开关和用户白名单。
- [ ] 按内部账号、测试场馆、小比例用户、全量用户顺序灰度。
- [ ] 每个灰度阶段观察错误率、延迟、成本、拒答率和业务转化率。
- [ ] 演练关闭 Agent、回滚模型、回滚 Prompt、回滚知识索引和回滚服务版本。
- [ ] 所有上线检查项通过后再标记 M7 完成。

**M7 完成定义**：Agent 故障不会影响 BMP 核心业务；关键链路可追踪；安全、评估、灰度和回滚均有验证记录。

---

## 9. 推荐开发节奏

| 周次 | 主要目标 | 对应任务 |
| --- | --- | --- |
| 第 1 周 | 统一契约并完成 FastAPI 底座 | P0-01 至 P0-04 |
| 第 2 周 | Mock Agent、会话隔离和 M1 | P0-05 至 P0-06 |
| 第 3 周 | Java 网关、签名上下文和只读 Tool | P1-01 至 P1-04 |
| 第 4 周 | 六道防线、PostgreSQL Checkpoint 和 M2 | P1-05 至 P1-07 |
| 第 5-6 周 | 智能预订 Agent MVP | P2-01 |
| 第 7 周 | 经营分析 Agent MVP | P2-02 |
| 第 8 周 | 场馆客服 Agent MVP | P2-03 |
| 第 9 周 | Web 与 UniApp 接入 | P2-04 至 P2-05 |
| 第 10-11 周 | 生产化、灰度和回滚 | P3-01 至 P3-04 |

时间不足时，优先保证 P0、P1 和智能预订 Agent。经营分析、客服 RAG、流式输出和高级 LLMOps 不得挤占安全闭环与测试时间。

## 10. 每个任务的完成检查模板

完成任何任务前，逐项确认：

- [ ] 需求和接口契约已明确，没有使用旧路径或旧字段。
- [ ] 失败测试已先编写，并验证在实现前确实失败。
- [ ] 最小实现已完成，没有加入当前优先级之外的功能。
- [ ] 单元测试、相关集成测试和回归测试全部通过。
- [ ] 日志不包含密钥、JWT、签名、SQL、完整 Prompt 或未脱敏个人信息。
- [ ] 文档、环境变量模板和实际实现保持一致。
- [ ] `git diff --check` 无错误，提交内容不包含本地生成物。
- [ ] 已生成单一职责的 Git Commit，并在本计划对应任务后记录 Commit ID。

## 11. 当前立即执行项

下一次开始开发时，只执行以下顺序，不跳到三只业务 Agent：

1. `P0-01`：统一 Python 版本、API 路径和数据库外键设计。
2. `P0-02`：重建 Python 3.12 环境并锁定兼容依赖。
3. `P0-03`：用测试驱动实现 FastAPI 配置、异常、TraceId 和健康检查。
4. `P0-04`：实现可替换、可离线测试的 LLM 适配层。
5. `P0-05`：跑通内存会话和 Mock Agent 状态图。
6. `P0-06`：通过 M1 完整质量门槛后，再开始 Java Agent 网关。
