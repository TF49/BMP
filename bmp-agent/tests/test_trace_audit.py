"""
P2-05 验收测试 - 维度 2 数据溯源与 Trace ID 贯穿审计自动化测试

测试目标：
1. 验证 HTTP 响应包包体与 Header 规范包含非空 Trace ID。
2. 验证 Request Trace ID 在全链路（Gateway -> Agent -> Tool）的透传与保持（请求头 `X-Agent-Trace-Id`）。
3. 验证 HMAC 上下文验签中 Trace 关联字段完整性。
"""

import base64
import hashlib
import hmac
import json
import time
import uuid
from fastapi.testclient import TestClient
import pytest

from app.main import app
from app.core.config import AppSettings
from app.core.security import HmacAgentContextVerifier, VerifiedAgentContext


@pytest.fixture
def api_client() -> TestClient:
    """FastAPI TestClient"""
    return TestClient(app)


def test_trace_id_generation_and_propagation(api_client: TestClient) -> None:
    """
    验证 Trace ID 生成与透传
    - 当客户端提供合法的 X-Agent-Trace-Id 时，网关必须透传保持
    - 当客户端未提供 X-Agent-Trace-Id 时，网关必须自动生成 Trace ID
    """
    # 1. 自动生成场景
    resp = api_client.get("/health")
    assert resp.status_code == 200
    data = resp.json()
    assert "trace_id" in data and data["trace_id"], "响应包体内必须包含非空 trace_id"
    header_trace = resp.headers.get("X-Agent-Trace-Id")
    assert header_trace == data["trace_id"], "Header 中的 X-Agent-Trace-Id 必须与响应体 trace_id 一致"

    # 2. 客户端指定 Trace ID 保持场景
    custom_trace_id = f"trace_audit_{uuid.uuid4().hex[:12]}"
    resp2 = api_client.get("/health", headers={"X-Agent-Trace-Id": custom_trace_id})
    assert resp2.status_code == 200
    data2 = resp2.json()
    assert data2["trace_id"] == custom_trace_id, f"客户端传参 {custom_trace_id} 必须全程保持"
    assert resp2.headers.get("X-Agent-Trace-Id") == custom_trace_id


def test_hmac_agent_context_verifier_trace_preservation() -> None:
    """
    验证 Agent 上下文 HMAC 签名传输中 Trace ID 与审计信息的完整性
    """
    test_trace_id = f"trace_audit_{uuid.uuid4().hex[:8]}"
    secret_str = "test-secret-key-32bytes-length!!"
    
    settings = AppSettings(
        agent_context_mode="signed",
        agent_context_secret=secret_str
    )
    verifier = HmacAgentContextVerifier(settings)
    
    # 构造合法的 Java 对齐 HMAC Token
    now_ms = int(time.time() * 1000)
    payload_dict = {
        "user_id": "1001",
        "role": "VENUE_MANAGER",
        "venue_id": "1",
        "trace_id": test_trace_id,
        "nonce": "test-nonce-123456",
        "issued_at": now_ms,
        "expires_at": now_ms + 60000
    }
    
    payload_json = json.dumps(payload_dict, separators=(",", ":"))
    payload_bytes = payload_json.encode("utf-8")
    
    sig_bytes = hmac.new(secret_str.encode("utf-8"), payload_bytes, hashlib.sha256).digest()
    
    token = (
        base64.urlsafe_b64encode(payload_bytes).decode("utf-8").rstrip("=")
        + "."
        + base64.urlsafe_b64encode(sig_bytes).decode("utf-8").rstrip("=")
    )
    
    # 执行验签
    verified_context: VerifiedAgentContext = verifier.verify(token, test_trace_id)
    
    assert verified_context.trace_id == test_trace_id, "上下文解密后 Trace ID 必须匹配"
    assert verified_context.user_id == "1001"
    assert verified_context.role == "VENUE_MANAGER"
    assert verified_context.venue_id == "1"


def test_error_response_envelope_trace_audit(api_client: TestClient) -> None:
    """
    验证异常路径下的 404 / 422 响应同样携带统一 Trace ID
    """
    test_trace_id = f"trace_err_{uuid.uuid4().hex[:8]}"
    resp = api_client.get("/api/agent/conversations/non-exist-id-12345", headers={"X-Agent-Trace-Id": test_trace_id})
    data = resp.json()
    assert data.get("trace_id") == test_trace_id, "404 错误响应中 Trace ID 丢失"
    assert resp.headers.get("X-Agent-Trace-Id") == test_trace_id
