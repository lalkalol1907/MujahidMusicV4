from __future__ import annotations

from datetime import UTC, datetime
from typing import Any

from mujahid_admin.db.connection import get_db


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


async def list_audit(page: int, page_size: int) -> tuple[list[dict[str, Any]], int]:
    db = get_db()
    total = await db.admin_audit.count_documents({})
    skip = max(page - 1, 0) * page_size
    cursor = db.admin_audit.find({}).sort("at", -1).skip(skip).limit(page_size)
    items: list[dict[str, Any]] = []
    async for doc in cursor:
        at = doc.get("at")
        items.append(
            {
                "action": doc.get("action"),
                "target": doc.get("target"),
                "at": at,
                "ip": doc.get("ip"),
            }
        )
    return items, total
