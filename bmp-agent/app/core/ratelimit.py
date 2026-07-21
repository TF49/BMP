"""进程内固定窗口限流器（第五防线：限流）。

面向开发环境的轻量实现：按调用方标识（通常为用户 ID）在固定时间窗口内限制请求数。
与 Java 网关侧的 ``AgentConversationRateLimiter`` 采用同样的固定窗口算法，作为 Python
服务的二次防线。分布式部署时应替换为 Redis + Lua 的实现。
"""

import threading
import time
from collections.abc import Callable
from dataclasses import dataclass


@dataclass(frozen=True)
class RateLimitDecision:
    """一次限流判定结果。"""

    allowed: bool
    retry_after_seconds: int


class InMemoryRateLimiter:
    """基于固定窗口计数的进程内限流器（线程安全）。

    内存管理：每次写入时清除所有不属于当前时间窗口的旧条目，防止
    _windows 字典随用户数线性增长。max_keys 作为兜底上限，超限时
    直接拒绝新 key 的请求（不影响已有 key）。
    """

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
        # key -> (window_start_epoch_second, count)
        self._windows: dict[str, tuple[int, int]] = {}

    def try_acquire(self, key: str) -> RateLimitDecision:
        """尝试为 ``key`` 占用一个配额；空标识直接拒绝。"""
        if not key:
            return RateLimitDecision(allowed=False, retry_after_seconds=self._window_seconds)

        now = int(self._clock())
        window_start = now - (now % self._window_seconds)
        with self._lock:
            # 清理属于旧窗口的所有 key，防止内存随用户数无界增长。
            # 因为固定窗口按时间对齐，所有 key 在同一个窗口内 window_start 相同，
            # 进入新窗口后旧条目可以安全删除。
            stale_keys = [k for k, (ws, _) in self._windows.items() if ws != window_start]
            for k in stale_keys:
                del self._windows[k]

            existing = self._windows.get(key)
            if existing is None:
                # 兜底保护：超过 max_keys 时拒绝新 key，防止极端情况下的内存爆炸。
                if len(self._windows) >= self._max_keys:
                    return RateLimitDecision(
                        allowed=False, retry_after_seconds=self._window_seconds
                    )
                self._windows[key] = (window_start, 1)
                return RateLimitDecision(allowed=True, retry_after_seconds=0)

            start, count = existing
            if start != window_start:
                # 该 key 的窗口已过期，重置计数（正常情况下已被上面的清理覆盖，
                # 此处作为防御性兜底）。
                self._windows[key] = (window_start, 1)
                return RateLimitDecision(allowed=True, retry_after_seconds=0)

            if count >= self._max_requests:
                retry_after = max(window_start + self._window_seconds - now, 1)
                return RateLimitDecision(allowed=False, retry_after_seconds=retry_after)

            self._windows[key] = (start, count + 1)
            return RateLimitDecision(allowed=True, retry_after_seconds=0)
