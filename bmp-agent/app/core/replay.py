import asyncio
import time
from typing import Any, Protocol


class ReplayGuard(Protocol):
    async def claim(self, *, nonce: str, expires_at_ms: int, scope: str) -> bool: ...


class InMemoryReplayGuard:
    def __init__(self, *, max_claims: int = 100_000) -> None:
        if max_claims <= 0:
            raise ValueError("max_claims must be positive")
        self._max_claims = max_claims
        self._claims: dict[str, int] = {}
        self._lock = asyncio.Lock()

    async def claim(self, *, nonce: str, expires_at_ms: int, scope: str) -> bool:
        if not nonce or expires_at_ms == 0:
            return True
        now_ms = int(time.time() * 1000)
        if expires_at_ms <= now_ms or not scope:
            return False
        key = f"{scope}:{nonce}"
        async with self._lock:
            stale = [claim for claim, expiry in self._claims.items() if expiry <= now_ms]
            for claim in stale:
                del self._claims[claim]
            if key in self._claims:
                return False
            if len(self._claims) >= self._max_claims:
                return False
            self._claims[key] = expires_at_ms
            return True


class RedisReplayGuard:
    def __init__(self, redis_client: Any) -> None:
        self._redis = redis_client

    async def claim(self, *, nonce: str, expires_at_ms: int, scope: str) -> bool:
        if not nonce or expires_at_ms == 0:
            return True
        ttl_ms = expires_at_ms - int(time.time() * 1000)
        if ttl_ms <= 0 or not scope:
            return False
        result = await self._redis.set(f"agent:replay:{scope}:{nonce}", "1", px=ttl_ms, nx=True)
        return bool(result)
