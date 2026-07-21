"""调用 Java Tool 的轻量熔断器（第六防线：熔断降级）。

三态机：``CLOSED``（正常放行）→ 连续失败达到阈值转 ``OPEN``（快速失败）→ 冷却期结束后
转 ``HALF_OPEN``（放行一次试探）；试探成功回到 ``CLOSED``，失败重新打开。与 Java 网关侧的
``AgentServiceCircuitBreaker`` 行为一致，避免下游故障时持续发起无谓调用。
"""

import threading
import time
from collections.abc import Callable
from enum import Enum


class CircuitState(Enum):
    CLOSED = "closed"
    OPEN = "open"
    HALF_OPEN = "half_open"


class CircuitBreaker:
    """线程安全的三态熔断器。"""

    def __init__(
        self,
        *,
        failure_threshold: int = 5,
        reset_timeout_seconds: float = 30.0,
        clock: Callable[[], float] | None = None,
    ) -> None:
        if failure_threshold <= 0:
            raise ValueError("failure_threshold must be positive")
        if reset_timeout_seconds <= 0:
            raise ValueError("reset_timeout_seconds must be positive")
        self._failure_threshold = failure_threshold
        self._reset_timeout = reset_timeout_seconds
        self._clock = clock or time.monotonic
        self._lock = threading.Lock()
        self._state = CircuitState.CLOSED
        self._failures = 0
        self._opened_at = 0.0

    def allow_request(self) -> bool:
        """判断当前是否允许发起下游调用；冷却期结束时放行一次试探。"""
        with self._lock:
            if self._state is CircuitState.OPEN:
                if self._clock() - self._opened_at >= self._reset_timeout:
                    self._state = CircuitState.HALF_OPEN
                    return True
                return False
            if self._state is CircuitState.HALF_OPEN:
                return False
            return True

    def record_success(self) -> None:
        """记录一次成功调用：清零失败计数并关闭熔断。"""
        with self._lock:
            self._failures = 0
            self._state = CircuitState.CLOSED

    def record_failure(self) -> None:
        """记录一次失败调用：达到阈值或试探失败时打开熔断。"""
        with self._lock:
            self._failures += 1
            if self._state is CircuitState.HALF_OPEN or self._failures >= self._failure_threshold:
                self._state = CircuitState.OPEN
                self._opened_at = self._clock()

    @property
    def state(self) -> CircuitState:
        """供测试与监控读取当前状态。"""
        with self._lock:
            return self._state
