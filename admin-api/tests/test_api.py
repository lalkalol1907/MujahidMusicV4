from mujahid_admin.services.metrics_parser import build_metrics_summary, parse_prometheus


def test_status_unauthorized(client):
    response = client.get("/api/admin/status")
    assert response.status_code == 401


def test_status_authorized(client, auth_headers, monkeypatch):
    async def fake_health():
        return True

    async def fake_ping():
        return True

    monkeypatch.setattr("mujahid_admin.services.status.bot_client.health_check", fake_health)
    monkeypatch.setattr("mujahid_admin.services.status.ping_mongo", fake_ping)

    response = client.get("/api/admin/status", headers=auth_headers)
    assert response.status_code == 200
    body = response.json()
    assert body["botReachable"] is True
    assert body["mongoReachable"] is True


def test_metrics_parser():
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


def test_sessions_skip(client, auth_headers, monkeypatch):
    async def fake_moderate(guild_id, action, path, ip):
        from mujahid_admin.schemas.session import ActionResponse

        assert action == "SKIP"
        return ActionResponse(status="ok")

    monkeypatch.setattr(
        "mujahid_admin.api.routes.sessions.sessions_service.moderate_session",
        fake_moderate,
    )

    response = client.post("/api/admin/sessions/123/skip", headers=auth_headers)
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_audit_list(client, auth_headers, monkeypatch):
    from mujahid_admin.schemas.audit import AuditEntry, PaginatedAudit

    async def fake_list(page, page_size):
        return PaginatedAudit(
            items=[
                AuditEntry(
                    action="SKIP",
                    target="123",
                    at="2026-01-01T00:00:00",
                    ip="127.0.0.1",
                )
            ],
            page=1,
            page_size=50,
            total=1,
            total_pages=1,
        )

    monkeypatch.setattr("mujahid_admin.api.routes.audit.audit_service.list_audit", fake_list)

    response = client.get("/api/admin/audit", headers=auth_headers)
    assert response.status_code == 200
    body = response.json()
    assert body["total"] == 1
    assert body["items"][0]["action"] == "SKIP"
