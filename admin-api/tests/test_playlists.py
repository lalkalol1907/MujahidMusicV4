from __future__ import annotations

import json
from unittest.mock import MagicMock

import pytest
from fastapi.testclient import TestClient

from mujahid_admin.config import settings
from mujahid_admin.main import create_app


@pytest.fixture
def auth_headers():
    return {"Authorization": f"Bearer {settings.admin_api_key}"}


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(settings, "admin_api_key", "test-admin-key")
    app = create_app()
    return TestClient(app)


def test_playlists_requires_auth(client):
    assert client.get("/api/admin/playlists").status_code == 401


def test_playlists_list(client, monkeypatch, auth_headers):
    async def fake_list(page, page_size, owner_id, name):
        return {"items": [], "page": 1, "pageSize": 50, "total": 0, "totalPages": 1}

    monkeypatch.setattr("mujahid_admin.routers.playlists.list_playlists", fake_list)
    response = client.get("/api/admin/playlists", headers=auth_headers)
    assert response.status_code == 200
    assert response.json()["total"] == 0
