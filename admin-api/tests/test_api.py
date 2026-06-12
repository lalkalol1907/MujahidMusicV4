import pytest
from fastapi.testclient import TestClient

from mujahid_admin.config import settings
from mujahid_admin.main import create_app


@pytest.fixture
def client(monkeypatch):
    monkeypatch.setattr(settings, "admin_api_key", "test-admin-key")
    return TestClient(create_app())


def test_status_unauthorized(client):
    response = client.get("/api/admin/status")
    assert response.status_code == 401


def test_status_authorized(client, monkeypatch):
    async def fake_health():
        return True

    async def fake_ping():
        return True

    monkeypatch.setattr("mujahid_admin.routers.status.bot_client.health_check", fake_health)
    monkeypatch.setattr("mujahid_admin.routers.status.ping_mongo", fake_ping)

    response = client.get(
        "/api/admin/status",
        headers={"Authorization": "Bearer test-admin-key"},
    )
    assert response.status_code == 200
    body = response.json()
    assert body["botReachable"] is True
    assert body["mongoReachable"] is True


def test_metrics_parser():
    from mujahid_admin.services.metrics_parser import build_metrics_summary, parse_prometheus

    text = """
# HELP mujahid_guilds_total guilds
mujahid_guilds_total 3
mujahid_active_players 1
mujahid_commands_total{command="play"} 10
"""
    parsed = parse_prometheus(text)
    summary = build_metrics_summary(parsed)
    assert summary["guilds_total"] == 3
    assert summary["active_players"] == 1
    assert summary["commands"][0]["command"] == "play"
