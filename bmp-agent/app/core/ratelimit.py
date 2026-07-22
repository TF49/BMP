"""进程内与 Redis 分布式固定窗口限流器（第五防线：限流）。

面向开发与生产环境的实现：按调用方标识（如 IP、用户 ID、路由、操作名）
在固定时间窗口内限制请求数。与 Java 网关侧的 ``AgentConversationRateLimiter`` 保持对齐。
"""

import threading
import time
from collections.abc import Callable
from dataclasses import dataclass
from typing import Any, Protocol


@dataclass(frozen=True)
class RateLimitDecision:
    """一次限流判定结果。"""

    allowed: bool
    retry_after_seconds: int


class RateLimiter(Protocol):
    def try_acquire(self, key: str) -> RateLimitDecision: ...


class InMemoryRateLimiter:
    """基于固定窗口计数的进程内限流器（线程安全）。"""

    def __init__(
        self,
        *,
        max_requests: int = 20,
        window_seconds: int = 60,
        max_keys: int = 10_000,
        clock: Callable[[], float] | None = None,
    ) -> None:
        if max_requests <= 0:
            raise ValueError("max_requests must be positive")
        if window_seconds <= 0:
            raise ValueError("window_seconds must be positive")
        if max_keys <= 0:
            raise ValueError("max_keys must be positive")
        self._max_requests = max_requests
        self._window_seconds = window_seconds
        self._max_keys = max_keys
        self._clock = clock or time.time
        self._lock = threading.Lock()
        self._windows: dict[str, tuple[int, int]] = {}

    def try_acquire(self, key: str) -> RateLimitDecision:
        if not key:
            return RateLimitDecision(allowed=False, retry_after_seconds=self._window_seconds)

        now = int(self._clock())
        window_start = now - (now % self._window_seconds)
        with self._lock:
            stale_keys = [k for k, (ws, _) in self._windows.items() if ws != window_start]
            for k in stale_keys:
                del self._windows[k]

            existing = self._windows.get(key)
            if existing is None:
                if len(self._windows) >= self._max_keys:
                    return RateLimitDecision(
                        allowed=False, retry_after_seconds=self._window_seconds
                    )
                self._windows[key] = (window_start, 1)
                return RateLimitDecision(allowed=True, retry_after_seconds=0)

            start, count = existing
            if start != window_start:
                self._windows[key] = (window_start, 1)
                return RateLimitDecision(allowed=True, retry_after_seconds=0)

            if count >= self._max_requests:
                retry_after = max(window_start + self._window_seconds - now, 1)
                return RateLimitDecision(allowed=False, retry_after_seconds=retry_after)

            self._windows[key] = (start, count + 1)
            return RateLimitDecision(allowed=True, retry_after_seconds=0)


class RedisRateLimiter:
    """基于 Redis INCR + EXPIRE 的分布式固定窗口限流器。"""

    def __init__(
        self,
        redis_client: Any,
        *,
        max_requests: int = 20,
        window_seconds: int = 60,
    ) -> None:
        self._redis = redis_client
        self._max_requests = max_requests
        self._window_seconds = window_seconds

    async def atry_acquire(self, key: str) -> RateLimitDecision:
        if not key:
            return RateLimitDecision(allowed=False, retry_after_seconds=self._window_seconds)
        now = int(time.time())
        window_start = now - (now % self._window_seconds)
        redis_key = f"agent:ratelimit:{key}:{window_start}"
        try:
            current = await self._redis.incr(redis_key)
            if current == 1:
                await self._redis.expire(redis_key, self._window_seconds)
            if current > self._max_requests:
                retry_after = max(window_start + self._window_seconds - now, 1)
                return RateLimitDecision(allowed=False, retry_after_seconds=retry_after)
            return RateLimitDecision(allowed=True, retry_after_seconds=0)
        except Exception:
            return RateLimitDecision(allowed=True, retry_after_seconds=0)

    def try_acquire(self, key: str) -> RateLimitDecision:
        return RateLimitDecision(allowed=True, retry_after_seconds=0)
