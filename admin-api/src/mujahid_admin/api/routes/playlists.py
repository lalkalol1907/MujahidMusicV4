from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Query, Request, Response

from mujahid_admin.core.dependencies import check_rate_limit
from mujahid_admin.schemas.playlist import PaginatedPlaylists, PlaylistDetail, PlaylistStats
from mujahid_admin.services import playlists as playlists_service

router = APIRouter()


@router.get("/playlists/stats", response_model=PlaylistStats)
async def stats() -> PlaylistStats:
    return await playlists_service.get_playlist_stats()


@router.get("/playlists", response_model=PaginatedPlaylists)
async def playlists(
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=50, ge=1, le=100, alias="pageSize"),
    owner_id: str | None = Query(default=None, alias="ownerId"),
    name: str | None = Query(default=None),
) -> PaginatedPlaylists:
    return await playlists_service.list_playlists(page, page_size, owner_id, name)


@router.get("/playlists/{owner_id}/{name}", response_model=PlaylistDetail)
async def playlist_detail(owner_id: str, name: str) -> PlaylistDetail:
    doc = await playlists_service.get_playlist(owner_id, name)
    if doc is None:
        raise HTTPException(status_code=404, detail="Playlist not found")
    return doc


@router.delete("/playlists/{owner_id}/{name}", status_code=204)
async def playlist_delete(
    owner_id: str,
    name: str,
    request: Request,
    _: None = Depends(check_rate_limit),
) -> Response:
    deleted = await playlists_service.delete_playlist(
        owner_id,
        name,
        request.client.host if request.client else None,
    )
    if not deleted:
        raise HTTPException(status_code=404, detail="Playlist not found")
    return Response(status_code=204)
