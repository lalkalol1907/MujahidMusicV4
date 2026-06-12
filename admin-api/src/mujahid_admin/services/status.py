from __future__ import annotations

import time

from mujahid_admin.clients.bot import bot_client
from mujahid_admin.core.config import settings
from mujahid_admin.db.connection import ping_mongo
from mujahid_admin.schemas.status import StatusResponse

_started_at = time.time()


async def get_status() -> StatusResponse:
    bot_ok = await bot_client.health_check()
    mongo_ok = await ping_mongo()
    return StatusResponse(
        status="UP" if bot_ok and mongo_ok else "DEGRADED",
        uptime_seconds=int(time.time() - _started_at),
        bot_reachable=bot_ok,
        mongo_reachable=mongo_ok,
        environment=settings.environment,
    )
