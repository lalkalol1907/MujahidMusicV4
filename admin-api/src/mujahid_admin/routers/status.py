from __future__ import annotations

import time

from fastapi import APIRouter, Depends

from mujahid_admin.client.bot import bot_client
from mujahid_admin.config import settings
from mujahid_admin.db.mongo import ping_mongo
from mujahid_admin.dependencies import verify_admin

router = APIRouter(prefix="/api/admin", tags=["status"])

_started_at = time.time()


@router.get("/status")
async def status(_: None = Depends(verify_admin)) -> dict:
    bot_ok = await bot_client.health_check()
    mongo_ok = await ping_mongo()
    return {
        "status": "UP" if bot_ok and mongo_ok else "DEGRADED",
        "uptimeSeconds": int(time.time() - _started_at),
        "botReachable": bot_ok,
        "mongoReachable": mongo_ok,
        "environment": settings.environment,
    }
