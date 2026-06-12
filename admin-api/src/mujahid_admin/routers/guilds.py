from __future__ import annotations

from fastapi import APIRouter, Depends

from mujahid_admin.client.bot import bot_client
from mujahid_admin.dependencies import verify_admin

router = APIRouter(prefix="/api/admin", tags=["guilds"])


@router.get("/guilds")
async def list_guilds(_: None = Depends(verify_admin)) -> list:
    return await bot_client.get("/internal/guilds")
