from __future__ import annotations

from mujahid_admin.repositories import audit as audit_repo
from mujahid_admin.repositories import playlists as playlist_repo
from mujahid_admin.schemas.playlist import (
    PaginatedPlaylists,
    PlaylistDetail,
    PlaylistStats,
    PlaylistSummary,
    PlaylistTrack,
    TopOwner,
)


def _paginate(page: int, page_size: int, total: int) -> int:
    return max((total + page_size - 1) // page_size, 1)


async def list_playlists(
    page: int,
    page_size: int,
    owner_id: str | None = None,
    name: str | None = None,
) -> PaginatedPlaylists:
    items, total = await playlist_repo.list_playlists(page, page_size, owner_id, name)
    return PaginatedPlaylists(
        items=[
            PlaylistSummary(
                owner_id=str(item["owner_id"]),
                name=item["name"],
                track_count=item["track_count"],
            )
            for item in items
        ],
        page=page,
        page_size=page_size,
        total=total,
        total_pages=_paginate(page, page_size, total),
    )


async def get_playlist(owner_id: str, name: str) -> PlaylistDetail | None:
    doc = await playlist_repo.get_playlist(owner_id, name)
    if doc is None:
        return None
    return PlaylistDetail(
        owner_id=str(doc["owner_id"]),
        name=doc["name"],
        tracks=[PlaylistTrack.model_validate(track) for track in doc["tracks"]],
    )


async def delete_playlist(owner_id: str, name: str, ip: str | None) -> bool:
    deleted = await playlist_repo.delete_playlist(owner_id, name)
    if deleted:
        await audit_repo.write_audit("DELETE_PLAYLIST", f"{owner_id}/{name}", ip)
    return deleted


async def get_playlist_stats() -> PlaylistStats:
    stats = await playlist_repo.playlist_stats()
    return PlaylistStats(
        total_playlists=stats["total_playlists"],
        total_tracks=stats["total_tracks"],
        top_owners=[
            TopOwner(
                owner_id=str(owner["owner_id"]),
                playlist_count=owner["playlist_count"],
                track_count=owner["track_count"],
            )
            for owner in stats["top_owners"]
        ],
    )
