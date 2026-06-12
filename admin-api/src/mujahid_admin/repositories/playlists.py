from __future__ import annotations

from typing import Any

from mujahid_admin.db.connection import get_db


async def list_playlists(
    page: int,
    page_size: int,
    owner_id: str | None = None,
    name: str | None = None,
) -> tuple[list[dict[str, Any]], int]:
    db = get_db()
    query: dict[str, Any] = {}
    if owner_id:
        query["ownerId"] = int(owner_id)
    if name:
        query["name"] = {"$regex": name, "$options": "i"}

    total = await db.playlists.count_documents(query)
    skip = max(page - 1, 0) * page_size
    cursor = (
        db.playlists.find(query)
        .sort([("ownerId", 1), ("name", 1)])
        .skip(skip)
        .limit(page_size)
    )

    items: list[dict[str, Any]] = []
    async for doc in cursor:
        tracks = doc.get("tracks") or []
        items.append(
            {
                "owner_id": doc.get("ownerId"),
                "name": doc.get("name"),
                "track_count": len(tracks),
            }
        )
    return items, total


async def get_playlist(owner_id: str, name: str) -> dict[str, Any] | None:
    db = get_db()
    doc = await db.playlists.find_one({"ownerId": int(owner_id), "name": name})
    if doc is None:
        return None
    return {
        "owner_id": doc.get("ownerId"),
        "name": doc.get("name"),
        "tracks": doc.get("tracks") or [],
    }


async def delete_playlist(owner_id: str, name: str) -> bool:
    db = get_db()
    result = await db.playlists.delete_one({"ownerId": int(owner_id), "name": name})
    return result.deleted_count > 0


async def playlist_stats() -> dict[str, Any]:
    db = get_db()
    pipeline = [
        {
            "$group": {
                "_id": "$ownerId",
                "playlist_count": {"$sum": 1},
                "track_count": {"$sum": {"$size": {"$ifNull": ["$tracks", []]}}},
            }
        },
        {"$sort": {"playlist_count": -1}},
        {"$limit": 10},
    ]
    top_owners: list[dict[str, Any]] = []
    async for row in db.playlists.aggregate(pipeline):
        top_owners.append(
            {
                "owner_id": row["_id"],
                "playlist_count": row["playlist_count"],
                "track_count": row["track_count"],
            }
        )

    total_playlists = await db.playlists.count_documents({})
    track_pipeline = [
        {"$project": {"track_count": {"$size": {"$ifNull": ["$tracks", []]}}}},
        {"$group": {"_id": None, "total_tracks": {"$sum": "$track_count"}}},
    ]
    total_tracks = 0
    async for row in db.playlists.aggregate(track_pipeline):
        total_tracks = row.get("total_tracks", 0)

    return {
        "total_playlists": total_playlists,
        "total_tracks": total_tracks,
        "top_owners": top_owners,
    }
