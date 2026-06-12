from __future__ import annotations

from datetime import UTC, datetime
from typing import Any

from motor.motor_asyncio import AsyncIOMotorClient, AsyncIOMotorDatabase

from mujahid_admin.config import settings

_client: AsyncIOMotorClient | None = None


def get_client() -> AsyncIOMotorClient:
    global _client
    if _client is None:
        _client = AsyncIOMotorClient(settings.mongo_uri)
    return _client


def get_db() -> AsyncIOMotorDatabase:
    return get_client()[settings.mongo_db]


async def ping_mongo() -> bool:
    try:
        await get_client().admin.command("ping")
        return True
    except Exception:
        return False


async def close_mongo() -> None:
    global _client
    if _client is not None:
        _client.close()
        _client = None


async def list_playlists(
    page: int,
    page_size: int,
    owner_id: str | None = None,
    name: str | None = None,
) -> dict[str, Any]:
    db = get_db()
    query: dict[str, Any] = {}
    if owner_id:
        query["ownerId"] = int(owner_id)
    if name:
        query["name"] = {"$regex": name, "$options": "i"}

    total = await db.playlists.count_documents(query)
    skip = max(page - 1, 0) * page_size
    cursor = db.playlists.find(query).sort([("ownerId", 1), ("name", 1)]).skip(skip).limit(page_size)

    items = []
    async for doc in cursor:
        tracks = doc.get("tracks") or []
        items.append(
            {
                "ownerId": str(doc.get("ownerId")),
                "name": doc.get("name"),
                "trackCount": len(tracks),
            }
        )

    return {
        "items": items,
        "page": page,
        "pageSize": page_size,
        "total": total,
        "totalPages": max((total + page_size - 1) // page_size, 1),
    }


async def get_playlist(owner_id: str, name: str) -> dict[str, Any] | None:
    db = get_db()
    doc = await db.playlists.find_one({"ownerId": int(owner_id), "name": name})
    if doc is None:
        return None
    return {
        "ownerId": str(doc.get("ownerId")),
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
                "playlistCount": {"$sum": 1},
                "trackCount": {"$sum": {"$size": {"$ifNull": ["$tracks", []]}}},
            }
        },
        {"$sort": {"playlistCount": -1}},
        {"$limit": 10},
    ]
    top_owners = []
    async for row in db.playlists.aggregate(pipeline):
        top_owners.append(
            {
                "ownerId": str(row["_id"]),
                "playlistCount": row["playlistCount"],
                "trackCount": row["trackCount"],
            }
        )

    total_playlists = await db.playlists.count_documents({})
    track_pipeline = [
        {"$project": {"trackCount": {"$size": {"$ifNull": ["$tracks", []]}}}},
        {"$group": {"_id": None, "totalTracks": {"$sum": "$trackCount"}}},
    ]
    total_tracks = 0
    async for row in db.playlists.aggregate(track_pipeline):
        total_tracks = row.get("totalTracks", 0)

    return {
        "totalPlaylists": total_playlists,
        "totalTracks": total_tracks,
        "topOwners": top_owners,
    }


async def write_audit(action: str, target: str, ip: str | None = None) -> None:
    db = get_db()
    await db.admin_audit.insert_one(
        {
            "action": action,
            "target": target,
            "at": datetime.now(UTC),
            "ip": ip,
        }
    )


async def list_audit(page: int, page_size: int) -> dict[str, Any]:
    db = get_db()
    total = await db.admin_audit.count_documents({})
    skip = max(page - 1, 0) * page_size
    cursor = db.admin_audit.find({}).sort("at", -1).skip(skip).limit(page_size)
    items = []
    async for doc in cursor:
        items.append(
            {
                "action": doc.get("action"),
                "target": doc.get("target"),
                "at": doc.get("at").isoformat() if doc.get("at") else None,
                "ip": doc.get("ip"),
            }
        )
    return {
        "items": items,
        "page": page,
        "pageSize": page_size,
        "total": total,
        "totalPages": max((total + page_size - 1) // page_size, 1),
    }
