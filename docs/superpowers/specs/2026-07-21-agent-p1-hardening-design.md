# Agent P1 Hardening Design

## Goal

Close the eight P1 review findings without changing student-management behavior: fail closed on service credentials, reject replay, return real HTTP error statuses, enforce layered limits, honor venue schedules, serialize half-open probes, wire Java tools into the running Python service, and restore agent state from PostgreSQL-backed checkpoints.

## Architecture

The Java service remains the public JWT gateway and the only owner of MySQL business data. Agent Tool requests continue to use a service token plus a short-lived signed context, but authentication adds a replay guard keyed by context nonce and request fingerprint. Java configuration has no repository-known credential fallback.

The Python service keeps SQLAlchemy/asyncpg rather than adding a second PostgreSQL driver. `MockAgent` receives optional Tool dependencies and an optional checkpoint store. In PostgreSQL mode it restores the latest graph state before invocation and persists the resulting state afterward; in memory mode it retains the existing LangGraph `InMemorySaver` behavior.

Rate limiting uses a shared key-based interface. Development and tests use bounded in-memory windows; configured Redis deployments use atomic `SET NX`/counter operations. Limits cover caller IP, user, route, and read/write Tool category. A missing or unavailable production security dependency fails closed.

## Components

- Java `AgentProperties`: require externally supplied secrets and expose rate/replay configuration.
- Java `AgentReplayGuard`: atomically claim a nonce plus request fingerprint until token expiry.
- Java request limiting filter/service: enforce IP, user, endpoint, and Tool read/write limits.
- Java exception handlers: emit matching HTTP status and `Retry-After` for rate limiting.
- Java court Tool: resolve the applicable workday/weekend/holiday schedule and reject out-of-hours queries.
- Java and Python circuit breakers: permit one in-flight half-open probe.
- Python replay guard and limiter backends: memory by default, Redis when configured.
- Python application runtime: own one shared `JavaToolClient`, inject `VenueTool`/`CourtTool`, and close it during shutdown.
- Python `MockAgent`: deterministic Tool commands for integration testing and persisted turn state for PostgreSQL mode.
- Python checkpoint store: retain the current schema while exposing state-level load/save/delete operations used by the agent.

## Data Flow

1. A JWT-authenticated user calls the Java Agent gateway.
2. Java applies IP/user/endpoint limits, issues a fresh signed context, and calls FastAPI.
3. FastAPI validates the signature, claims the nonce once for the process boundary, and applies IP/user/route limits.
4. The session store verifies conversation ownership and acquires the conversation lock.
5. The agent restores persisted state when configured, optionally calls an injected Java Tool, invokes LangGraph, and saves the new state.
6. Java Tool authentication validates service token, signature, role/scope, and exact-request replay fingerprint before business validation.

## Error Handling

- Missing Java secrets prevent application startup instead of selecting known defaults.
- Duplicate nonce/request fingerprints return 401 without exposing token material.
- Limit rejection returns HTTP 429, `Retry-After`, and the existing response envelope.
- Downstream/open-circuit failures return HTTP 503.
- Venue schedule violations return HTTP/body code 400.
- Redis security failures fail closed in production and are explicit in logs without secrets.

## Testing

- Add Java regression tests for configuration, replay, HTTP status, layered limiting, business schedules, and concurrent half-open probes.
- Add Python regression tests for replay, Redis/memory limiter behavior, one half-open probe, runtime Tool calls, and state recovery across agent instances.
- Run the existing 55 Java Agent tests plus new tests.
- Run all Python tests, Ruff, mypy, and `git diff --check`.
- PostgreSQL integration tests remain environment-gated, but the same checkpoint behavior is exercised with the SQLAlchemy SQLite test backend.

## Scope

Student-management controllers, mappers, services, frontend views, SQL, and tests are excluded. No worktree, subagent, unrelated refactor, or dependency-family replacement is included.
