import pytest
from fastapi.testclient import TestClient

from mujahid_admin.core.config import settings
from mujahid_admin.main import create_app


@pytest.fixture
def admin_key(monkeypatch):
    monkeypatch.setattr(settings, "admin_api_key", "test-admin-key")
    return "test-admin-key"


@pytest.fixture
def client(admin_key):
    with TestClient(create_app()) as test_client:
        yield test_client


@pytest.fixture
def auth_headers(admin_key):
    return {"Authorization": f"Bearer {admin_key}"}
