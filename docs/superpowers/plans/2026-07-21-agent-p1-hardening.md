# Agent P1 Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every reviewed P1 security, integration, persistence, resilience, and quality claim true in the running application.

**Architecture:** Keep the current Spring Boot and FastAPI boundaries. Add small injectable guards and stores, wire existing components into application startup, and preserve the existing response envelopes while correcting transport semantics.

**Tech Stack:** Java 17, Spring Boot, Spring Security, Redis, Python 3.12, FastAPI, SQLAlchemy async, LangGraph, HTTPX, pytest, JUnit 5.

## Global Constraints

- Do not modify student-management code.
- Do not create a worktree or use subagents.
- Write and run a failing regression test before every production behavior change.
- Preserve existing public paths and response envelope field names.
- Use UTF-8 explicitly for every PowerShell `Get-Content` call.

---

### Task 1: Java fail-closed security and transport semantics

**Files:**
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/java/com/badminton/bmp/common/exception/GlobalExceptionHandler.java`
- Create: `src/test/java/com/badminton/bmp/modules/agent/AgentHttpErrorContractTest.java`

**Interfaces:**
- Produces real HTTP 429/503 responses and no default Agent credential values.

- [x] Write MockMvc tests whose controllers throw `AgentRateLimitException` and `ServiceUnavailableException`, asserting HTTP 429/503 and `Retry-After`.
- [x] Run the new test and verify it fails with HTTP 200.
- [x] Return `ResponseEntity<Result<Object>>` from the two handlers and attach `Retry-After` for 429.
- [x] Remove credential fallback literals from `application.properties`.
- [x] Run the new test and `Agent*Test` suite.

### Task 2: Java replay, layered limits, schedules, and circuit breaker

**Files:**
- Create: `src/main/java/com/badminton/bmp/modules/agent/security/AgentReplayGuard.java`
- Modify: `src/main/java/com/badminton/bmp/modules/agent/security/AgentToolAuthenticationFilter.java`
- Modify: `src/main/java/com/badminton/bmp/modules/agent/support/AgentConversationRateLimiter.java`
- Modify: `src/main/java/com/badminton/bmp/modules/agent/support/AgentServiceCircuitBreaker.java`
- Modify: `src/main/java/com/badminton/bmp/modules/agent/service/impl/AgentCourtToolServiceImpl.java`
- Test: `src/test/java/com/badminton/bmp/modules/agent/*Test.java`

**Interfaces:**
- `AgentReplayGuard.tryClaim(AgentContext, HttpServletRequest): boolean`
- `AgentConversationRateLimiter.tryAcquire(String dimension, String key, int limit, long windowSeconds): Decision`

- [x] Add tests proving an identical signed Tool request is rejected while a different query is allowed.
- [x] Add tests for independent IP/user/endpoint/read-write limit keys.
- [x] Add a test proving only one half-open request is allowed before success/failure feedback.
- [x] Add schedule tests for before-open, after-close, weekend, and valid time ranges.
- [x] Run each focused test and verify the expected failure.
- [x] Implement bounded replay claims, keyed limiter dimensions, half-open probe serialization, and schedule validation.
- [x] Run all Java Agent tests.

### Task 3: Python replay and layered limiting

**Files:**
- Create: `bmp-agent/app/core/replay.py`
- Modify: `bmp-agent/app/core/ratelimit.py`
- Modify: `bmp-agent/app/api/routes.py`
- Modify: `bmp-agent/app/main.py`
- Test: `bmp-agent/tests/test_security.py`
- Test: `bmp-agent/tests/test_ratelimit.py`
- Test: `bmp-agent/tests/test_api.py`

**Interfaces:**
- `ReplayGuard.claim(nonce: str, expires_at_ms: int, scope: str) -> Awaitable[bool]`
- `RateLimiter.try_acquire(key: str, max_requests: int | None = None, window_seconds: int | None = None) -> RateLimitDecision`

- [x] Add API tests proving the same signed process token is rejected on reuse.
- [x] Add tests for distinct IP, user, route, and operation limit keys.
- [x] Run focused tests and verify failures.
- [x] Implement injectable memory guards and Redis-backed atomic variants, then apply them in the process route.
- [x] Run focused tests to green.

### Task 4: Python Tool runtime and persisted agent state

**Files:**
- Modify: `bmp-agent/app/agents/mock_agent.py`
- Modify: `bmp-agent/app/memory/postgres.py`
- Modify: `bmp-agent/app/main.py`
- Modify: `bmp-agent/app/tools/client.py`
- Test: `bmp-agent/tests/test_agents.py`
- Test: `bmp-agent/tests/test_postgres_store.py`
- Test: `bmp-agent/tests/test_api.py`

**Interfaces:**
- `MockAgent(..., checkpoint_store: AgentCheckpointStore | None, venue_tool: VenueTool | None, court_tool: CourtTool | None)`
- `PostgresCheckpointStore.load_state(thread_id: str) -> dict[str, Any] | None`
- `PostgresCheckpointStore.save_state(thread_id: str, state: dict[str, Any]) -> None`

- [x] Add a test recreating the agent/store and asserting the next turn continues after restart.
- [x] Add a process-route test proving a deterministic Tool command reaches HTTPX and returns Tool references.
- [x] Run focused tests and verify failures.
- [x] Wire the checkpoint store and one shared Tool client into app startup and shutdown.
- [x] Persist/restore MockAgent graph state and implement deterministic read-only Tool commands.
- [x] Run focused and full Python tests.

### Task 5: Python breaker and quality gate

**Files:**
- Modify: `bmp-agent/app/tools/circuit_breaker.py`
- Modify: `bmp-agent/app/core/health.py`
- Modify: `bmp-agent/app/core/security.py`
- Modify: `bmp-agent/tests/test_circuit_breaker.py`
- Modify: `bmp-agent/tests/test_security.py`
- Modify: `bmp-agent/tests/test_api.py`

**Interfaces:**
- Half-open state permits exactly one in-flight request until `record_success` or `record_failure`.

- [x] Add a failing second-half-open-probe test.
- [x] Implement a half-open probe flag under the existing lock.
- [x] Fix Ruff import and exception chaining errors and the trailing blank line.
- [x] Run pytest, Ruff, and mypy.

### Task 6: Documentation and final verification

**Files:**
- Modify: `docs/BMP三智能体优先级实施计划.md`
- Modify: `docs/API接口规范文档.md`

**Interfaces:**
- Documents match the implemented headers, parameter names, statuses, test counts, and integration-test conditions.

- [x] Update stale Tool header/query/response examples to the implemented contract.
- [x] Record actual test counts and distinguish environment-gated PostgreSQL verification.
- [x] Run `mvn test -Dtest=Agent*Test`.
- [x] Run Python pytest, Ruff, and mypy.
- [x] Run `git diff --check` and inspect the final scoped diff.
