from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, Query, Request, Response

from mujahid_admin.db.mongo import delete_playlist, get_playlist, list_playlists, playlist_stats, write_audit
from mujahid_admin.dependencies import check_rate_limit, verify_admin

router = APIRouter(prefix="/api/admin", tags=["playlists"])


@router.get("/playlists/stats")
async def stats(_: None = Depends(verify_admin)) -> dict:
    return await playlist_stats()


@router.get("/playlists")
async def playlists(
    page: int = Query(default=1, ge=1),
    page_size: int = Query(default=50, ge=1, le=100, alias="pageSize"),
    owner_id: str | None = Query(default=None, alias="ownerId"),
    name: str | None = Query(default=None),
    _: None = Depends(verify_admin),
) -> dict:
    return await list_playlists(page, page_size, owner_id, name)


@router.get("/playlists/{owner_id}/{name}")
async def playlist_detail(
    owner_id: str,
    name: str,
    _: None = Depends(verify_admin),
) -> dict:
    doc = await get_playlist(owner_id, name)
    if doc is None:
        raise HTTPException(status_code=404, detail="Playlist not found")
    return doc


@router.delete("/playlists/{owner_id}/{name}", status_code=204)
async def playlist_delete(
    owner_id: str,
    name: str,
    request: Request,
    _: None = Depends(verify_admin),
) -> Response:
    check_rate_limit(request)
    deleted = await delete_playlist(owner_id, name)
    if not deleted:
        raise HTTPException(status_code=404, detail="Playlist not found")
    await write_audit(
        "DELETE_PLAYLIST",
        f"{owner_id}/{name}",
        request.client.host if request.client else None,
    )
    return Response(status_code=204)
