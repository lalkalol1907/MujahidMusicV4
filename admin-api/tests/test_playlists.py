from __future__ import annotations


def test_playlists_requires_auth(client):
    assert client.get("/api/admin/playlists").status_code == 401


def test_playlists_list(client, monkeypatch, auth_headers):
    from mujahid_admin.schemas.playlist import PaginatedPlaylists

    async def fake_list(page, page_size, owner_id, name):
        return PaginatedPlaylists(
            items=[],
            page=1,
            page_size=50,
            total=0,
            total_pages=1,
        )

    monkeypatch.setattr(
        "mujahid_admin.api.routes.playlists.playlists_service.list_playlists",
        fake_list,
    )
    response = client.get("/api/admin/playlists", headers=auth_headers)
    assert response.status_code == 200
    assert response.json()["total"] == 0
