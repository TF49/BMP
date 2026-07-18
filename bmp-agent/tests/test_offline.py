import socket

import pytest
from pytest_socket import SocketConnectBlockedError


def test_network_access_is_disabled_by_default() -> None:
    with pytest.raises(SocketConnectBlockedError):
        socket.create_connection(("192.0.2.1", 80), timeout=0.01)
